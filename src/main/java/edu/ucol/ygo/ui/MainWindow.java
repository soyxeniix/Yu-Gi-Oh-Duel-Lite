package edu.ucol.ygo.ui;

import edu.ucol.ygo.logic.BattleListener;
import edu.ucol.ygo.logic.Duel;
import edu.ucol.ygo.model.Card;
import edu.ucol.ygo.net.YgoApiClient;

import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

public class MainWindow extends JFrame implements BattleListener {
    private final JTextArea log = new JTextArea();
    private final JButton btnInit = new JButton("Iniciar duelo");

    // Grids adaptables (columnas dinámicas)
    private final JPanel playerPanel = new JPanel(new GridLayout(1, 0, 16, 16));
    private final JPanel aiPanel     = new JPanel(new GridLayout(1, 0, 16, 16));

    private final Duel duel = new Duel();
    private final YgoApiClient api = new YgoApiClient();

    private final List<Card> currentPlayerHand = new ArrayList<>();
    private final List<Card> currentAiHand = new ArrayList<>();

    // Mapa para poder remover el panel asociado a cada Card
    private final Map<Card, JPanel> playerCardViews = new HashMap<>();
    private final Map<Card, JPanel> aiCardViews     = new HashMap<>();

    public MainWindow() {
        super("Yu-Gi-Oh! Duel Lite");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1280, 760);
        setLocationRelativeTo(null);

        log.setEditable(false);
        JScrollPane logScroll = new JScrollPane(log);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(btnInit);
        add(top, BorderLayout.NORTH);

        // Secciones con título + Scroll (para que siempre se vean completas)
        JScrollPane playerScroll = new JScrollPane(playerPanel);
        JScrollPane aiScroll     = new JScrollPane(aiPanel);
        JPanel playerSec = labeledSection("Tu mano", playerScroll);
        JPanel aiSec     = labeledSection("Mano de la máquina", aiScroll);

        // Split superior: jugador vs máquina (mitad y mitad)
        JSplitPane topBottom = new JSplitPane(JSplitPane.VERTICAL_SPLIT, playerSec, aiSec);
        topBottom.setResizeWeight(0.5);
        topBottom.setContinuousLayout(true);
        topBottom.setDividerLocation(0.48);

        // Split raíz: cartas vs log (80% / 20%)
        JSplitPane rootSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topBottom, logScroll);
        rootSplit.setResizeWeight(0.8);
        rootSplit.setContinuousLayout(true);
        rootSplit.setDividerLocation(0.78);

        add(rootSplit, BorderLayout.CENTER);

        duel.setListener(this);
        btnInit.addActionListener(e -> startMatch());
    }

    private JPanel labeledSection(String title, JComponent content) {
        JPanel wrapper = new JPanel(new BorderLayout());
        JLabel lbl = new JLabel(title);
        lbl.setBorder(BorderFactory.createEmptyBorder(6,12,6,12));
        wrapper.add(lbl, BorderLayout.NORTH);
        wrapper.add(content, BorderLayout.CENTER);
        return wrapper;
    }

    private void startMatch() {
        btnInit.setEnabled(false);
        log.setText("Cargando cartas...\n");
        playerPanel.removeAll();
        aiPanel.removeAll();
        currentPlayerHand.clear();
        currentAiHand.clear();
        playerCardViews.clear();
        aiCardViews.clear();

        CompletableFuture.runAsync(() -> {
            try {
                List<Card> player = new ArrayList<>();
                List<Card> ai     = new ArrayList<>();
                for (int i = 0; i < 3; i++) {
                    logAppendEDT("-> Pidiendo carta jugador " + (i+1) + "...");
                    player.add(api.fetchRandomMonster());
                    logAppendEDT("-> Pidiendo carta máquina " + (i+1) + "...");
                    ai.add(api.fetchRandomMonster());
                }
                SwingUtilities.invokeLater(() -> setupHands(player, ai));
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this,
                            "Error cargando cartas desde API: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                    btnInit.setEnabled(true);
                });
            }
        });
    }

    private void setupHands(List<Card> player, List<Card> ai) {
        duel.setHands(player, ai);
        currentPlayerHand.addAll(player);
        currentAiHand.addAll(ai);

        log.append("Cartas listas. Turno inicial: " + (duel.isPlayerTurn() ? "Jugador" : "Máquina") + "\n");

        playerPanel.removeAll();
        for (Card c : currentPlayerHand) {
            JPanel p = makePlayerCardPanel(c);
            playerCardViews.put(c, p);
            playerPanel.add(p);
        }

        aiPanel.removeAll();
        for (Card c : currentAiHand) {
            JPanel p2 = makeAiCardPanel(c);
            aiCardViews.put(c, p2);
            aiPanel.add(p2);
        }

        revalidate();
        repaint();
    }

    private JPanel makePlayerCardPanel(Card c) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.add(centered(new JLabel("<html><b>" + escape(c.getName()) + "</b><br/>ATK " + c.getAtk() + " / DEF " + c.getDef() + "</html>")));
        p.add(Box.createVerticalStrut(6));
        p.add(centered(loadImageLabel(c.getImageUrl(), 360, 250)));
        p.add(Box.createVerticalStrut(6));

        JPanel btns = new JPanel(new FlowLayout());
        JButton atk = new JButton("Atacar");
        JButton def = new JButton("Defender");
        btns.add(atk); btns.add(def);
        p.add(btns);

        // Puedes elegir ATK o DEF en cada ronda
        atk.addActionListener(e -> {
            if (!duel.isFinished()) {
                duel.playRound(c, true);
                atk.setEnabled(false); def.setEnabled(false);
            }
        });
        def.addActionListener(e -> {
            if (!duel.isFinished()) {
                duel.playRound(c, false);
                atk.setEnabled(false); def.setEnabled(false);
            }
        });

        return p;
    }

    private JPanel makeAiCardPanel(Card c) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.add(centered(new JLabel("<html><b>" + escape(c.getName()) + "</b><br/>ATK " + c.getAtk() + " / DEF " + c.getDef() + "</html>")));
        p.add(Box.createVerticalStrut(6));
        p.add(centered(loadImageLabel(c.getImageUrl(), 360, 250)));
        p.add(Box.createVerticalStrut(6));
        return p;
    }

    private static JComponent centered(JComponent c) {
        JPanel w = new JPanel(new FlowLayout(FlowLayout.CENTER));
        w.add(c);
        return w;
    }

    private JLabel loadImageLabel(String url, int w, int h) {
        try {
            if (url == null || url.isBlank()) throw new Exception("URL vacía");
            Image img = new ImageIcon(new URL(url)).getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
            return new JLabel(new ImageIcon(img));
        } catch (Exception ex) {
            JLabel fallback = new JLabel("(sin imagen)");
            fallback.setPreferredSize(new Dimension(w, h));
            fallback.setHorizontalAlignment(SwingConstants.CENTER);
            fallback.setVerticalAlignment(SwingConstants.CENTER);
            return fallback;
        }
    }

    private static String escape(String s) {
        return s == null ? "" : s.replace("<", "&lt;").replace(">", "&gt;");
    }

    private void logAppendEDT(String msg) {
        SwingUtilities.invokeLater(() -> log.append(msg + "\n"));
    }

    // ====== BattleListener ======

    // Nuevo callback: quitar visualmente las cartas usadas
    @Override
    public void onCardsUsed(Card playerCard, Card aiCard) {
        JPanel p = playerCardViews.remove(playerCard);
        if (p != null) playerPanel.remove(p);
        JPanel q = aiCardViews.remove(aiCard);
        if (q != null) aiPanel.remove(q);
        playerPanel.revalidate(); playerPanel.repaint();
        aiPanel.revalidate(); aiPanel.repaint();
    }

    // Firma con 5 parámetros (se mantiene)
    @Override
    public void onTurn(String playerCard, String aiCard, String modePlayer, String modeAI, String winner) {
        log.append("Jugador (" + modePlayer + "): " + playerCard +
                "  vs  Máquina (" + modeAI + "): " + aiCard + "  -> Gana: " + winner + "\n");
    }

    @Override
    public void onScoreChanged(int playerScore, int aiScore) {
        log.append("Marcador: Jugador " + playerScore + " - " + aiScore + " Máquina\n");
    }

    @Override
    public void onDuelEnded(String winner) {
        log.append("=== ¡Duelo terminado! Ganador: " + winner + " ===\n");
        JOptionPane.showMessageDialog(this, "Ganador: " + winner);
        btnInit.setEnabled(true);
    }
}