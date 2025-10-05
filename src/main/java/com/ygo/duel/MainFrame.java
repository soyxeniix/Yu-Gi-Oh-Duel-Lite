package com.ygo.duel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.net.URL;
import java.util.List;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class MainFrame extends JFrame implements BattleListener {
    private final JTextArea log = new JTextArea(10, 60);
    private final JLabel scoreLbl = new JLabel("Jugador 0 - 0 IA");
    private final JButton btnIniciar = new JButton("Iniciar duelo");
    private final JButton btnElegir = new JButton("Elegir carta");
    private final JComboBox<Card> cmbPlayerCards = new JComboBox<>();
    private final JToggleButton tglDefense = new JToggleButton("Defender");
    private final JPanel playerCardsPanel = new JPanel(new GridLayout(1, 3, 10, 10));
    private final JPanel aiCardsPanel = new JPanel(new GridLayout(1, 3, 10, 10));

    private Duel duel;
    private List<Card> playerHand;
    private List<Card> aiHand;

    public MainFrame() {
        super("Yu-Gi-Oh! Duel Lite");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(980, 700);
        setLocationRelativeTo(null);

        log.setEditable(false);
        JScrollPane scroll = new JScrollPane(log);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        JPanel top = new JPanel(new BorderLayout());
        top.setBorder(new EmptyBorder(10, 10, 10, 10));
        top.add(scoreLbl, BorderLayout.WEST);
        top.add(btnIniciar, BorderLayout.EAST);

        JPanel mid = new JPanel(new GridLayout(2,1,10,10));
        JPanel playerPanel = new JPanel(new BorderLayout());
        playerPanel.setBorder(BorderFactory.createTitledBorder("Cartas del Jugador"));
        playerPanel.add(playerCardsPanel, BorderLayout.CENTER);

        JPanel aiPanel = new JPanel(new BorderLayout());
        aiPanel.setBorder(BorderFactory.createTitledBorder("Cartas de la IA (ocultas)"));
        aiPanel.add(aiCardsPanel, BorderLayout.CENTER);

        mid.add(playerPanel);
        mid.add(aiPanel);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottom.add(new JLabel("Tu carta:"));
        bottom.add(cmbPlayerCards);
        bottom.add(tglDefense);
        bottom.add(btnElegir);

        JPanel content = new JPanel(new BorderLayout(10,10));
        content.setBorder(new EmptyBorder(10,10,10,10));
        content.add(top, BorderLayout.NORTH);
        content.add(mid, BorderLayout.CENTER);
        content.add(scroll, BorderLayout.EAST);
        content.add(bottom, BorderLayout.SOUTH);

        setContentPane(content);

        // Eventos
        btnIniciar.addActionListener(e -> loadHandsAsync());
        btnElegir.addActionListener(e -> onPlayerChoose());

        // Estado inicial
        btnElegir.setEnabled(false);
        cmbPlayerCards.setEnabled(false);
        tglDefense.setEnabled(false);
    }

    private void onPlayerChoose() {
        if (duel == null) return;
        if (duel.isFinished()) return;
        Card selected = (Card) cmbPlayerCards.getSelectedItem();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Elige una carta primero.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Mode mode = tglDefense.isSelected() ? Mode.DEFENSE : Mode.ATTACK;

        duel.playTurn(selected, mode);
        cmbPlayerCards.removeItem(selected);
        if (cmbPlayerCards.getItemCount() == 0) {
            btnElegir.setEnabled(false);
            cmbPlayerCards.setEnabled(false);
            tglDefense.setEnabled(false);
        }
    }

    private void loadHandsAsync() {
        btnIniciar.setEnabled(false);
        appendLog("Cargando manos (3 cartas por jugador)...");
        // No bloquear UI: SwingWorker
        SwingWorker<List<List<Card>>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<List<Card>> doInBackground() throws Exception {
                YgoApiClient api = new YgoApiClient();
                List<Card> player = api.getInitialHand(3);
                List<Card> ai = api.getInitialHand(3);
                return Arrays.asList(player, ai);
            }
            @Override
            protected void done() {
                try {
                    List<List<Card>> res = get();
                    playerHand = res.get(0);
                    aiHand = res.get(1);
                    if (playerHand.size() < 3 || aiHand.size() < 3) {
                        throw new IllegalStateException("No se cargaron 3 cartas por jugador.");
                    }
                    setupHandsUI(playerHand, aiHand);
                    duel = new Duel(playerHand, aiHand, MainFrame.this);
                    appendLog("Turno inicial: " + (duel.isPlayerTurn() ? "Jugador" : "IA") + ".");
                    scoreLbl.setText("Jugador 0 - 0 IA");

                    btnElegir.setEnabled(true);
                    cmbPlayerCards.setEnabled(true);
                    tglDefense.setEnabled(true);
                } catch (InterruptedException | ExecutionException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(MainFrame.this,
                            "Error al cargar las cartas: " + ex.getCause().getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                    btnIniciar.setEnabled(true);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(MainFrame.this,
                            "Error: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                    btnIniciar.setEnabled(true);
                }
            }
        };
        worker.execute();
    }

    private void setupHandsUI(List<Card> player, List<Card> ai) {
        playerCardsPanel.removeAll();
        aiCardsPanel.removeAll();
        cmbPlayerCards.removeAllItems();

        for (Card c : player) {
            cmbPlayerCards.addItem(c);
            playerCardsPanel.add(cardPanel(c, false)); // visible
        }
        for (Card c : ai) {
            aiCardsPanel.add(cardPanel(c, true)); // ocultar nombre, mostrar dorso
        }

        playerCardsPanel.revalidate();
        playerCardsPanel.repaint();
        aiCardsPanel.revalidate();
        aiCardsPanel.repaint();
    }

    private JPanel cardPanel(Card c, boolean hide) {
        JPanel p = new JPanel(new BorderLayout(5,5));
        p.setBorder(new EmptyBorder(5,5,5,5));
        JLabel img = new JLabel();
        img.setHorizontalAlignment(SwingConstants.CENTER);

        if (hide) {
            img.setIcon(UIManager.getIcon("OptionPane.questionIcon"));
            p.add(img, BorderLayout.CENTER);
            JLabel name = new JLabel("???");
            name.setHorizontalAlignment(SwingConstants.CENTER);
            p.add(name, BorderLayout.SOUTH);
        } else {
            try {
                ImageIcon icon = new ImageIcon(new URL(c.getImageUrlSmall()));
                // Escala suave
                Image scaled = icon.getImage().getScaledInstance(180, 260, Image.SCALE_SMOOTH);
                img.setIcon(new ImageIcon(scaled));
            } catch (Exception e) {
                img.setIcon(UIManager.getIcon("OptionPane.warningIcon"));
            }
            p.add(img, BorderLayout.CENTER);
            JLabel name = new JLabel("<html><center>" + c.getName() + "<br/>ATK " + c.getAtk() + " / DEF " + c.getDef() + "</center></html>");
            name.setHorizontalAlignment(SwingConstants.CENTER);
            p.add(name, BorderLayout.SOUTH);
        }
        return p;
    }

    private void appendLog(String s) {
        log.append(s + "\n");
        log.setCaretPosition(log.getDocument().getLength());
    }

    // ====== BattleListener ======
    @Override
    public void onTurn(String playerCard, String aiCard, String winner) {
        appendLog("Turno -> Jugador: " + playerCard + "  |  IA: " + aiCard + "  => Gana: " + winner);
    }

    @Override
    public void onScoreChanged(int playerScore, int aiScore) {
        scoreLbl.setText("Jugador " + playerScore + " - " + aiScore + " IA");
    }

    @Override
    public void onDuelEnded(String winner) {
        appendLog("=== Ã‚Â¡Duelo terminado! Ganador: " + winner + " ===");
        JOptionPane.showMessageDialog(this, "Ganador final: " + winner, "DUELO", JOptionPane.INFORMATION_MESSAGE);
        // Deshabilitar controles tras terminar
        (new javax.swing.Timer(100, e -> {
            cmbPlayerCards.setEnabled(false);
            tglDefense.setEnabled(false);
            ((javax.swing.Timer)e.getSource()).stop();
        })).start();
    }
}