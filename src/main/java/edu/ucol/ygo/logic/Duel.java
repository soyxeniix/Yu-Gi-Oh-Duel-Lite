package edu.ucol.ygo.logic;

import edu.ucol.ygo.model.Card;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Duel {
    private final List<Card> playerHand = new ArrayList<>();
    private final List<Card> aiHand = new ArrayList<>();
    private int playerScore = 0;
    private int aiScore = 0;
    private final Random random = new Random();
    private BattleListener listener;
    private boolean playerTurn; // aleatorio al iniciar

    public void setListener(BattleListener listener) { this.listener = listener; }

    public void setHands(List<Card> player, List<Card> ai) {
        playerHand.clear(); aiHand.clear();
        playerHand.addAll(player); aiHand.addAll(ai);
        // resetear marcador y elegir turno inicial
        playerScore = 0; aiScore = 0;
        playerTurn = random.nextBoolean();
        if (listener != null) {
            listener.onScoreChanged(playerScore, aiScore);
        }
    }

    public boolean isFinished() { return playerScore == 2 || aiScore == 2; }
    public boolean isPlayerTurn() { return playerTurn; }

    /**
     * Jugar una ronda.
     * @param playerCard Carta elegida por el jugador
     * @param attackMode true = jugador la usa en ATK, false = DEF
     */
    public void playRound(Card playerCard, boolean attackMode) {
        if (isFinished()) return;
        if (!playerHand.contains(playerCard)) return;

        playerHand.remove(playerCard);
        Card aiCard = aiHand.remove(random.nextInt(aiHand.size()));
        boolean aiAttackMode = random.nextBoolean(); // la máquina elige aleatorio

        // avisa inmediatamente qué cartas se usaron (para que la UI las elimine)
        if (listener != null) {
            listener.onCardsUsed(playerCard, aiCard);
        }

        String winner;
        if (playerTurn) {
            winner = battle(playerCard, attackMode, aiCard, aiAttackMode, true);
        } else {
            winner = battle(aiCard, aiAttackMode, playerCard, attackMode, false);
        }

        if (listener != null) {
            listener.onTurn(playerCard.toString(), aiCard.toString(),
                    attackMode ? "ATK" : "DEF",
                    aiAttackMode ? "ATK" : "DEF",
                    winner);
            listener.onScoreChanged(playerScore, aiScore);
            if (isFinished()) {
                listener.onDuelEnded(playerScore == 2 ? "Jugador" : "Máquina");
                return;
            }
        }

        playerTurn = !playerTurn;

        // Cierre si no quedan cartas
        if (playerHand.isEmpty() && aiHand.isEmpty() && !isFinished()) {
            String matchWinner;
            if (playerScore > aiScore) matchWinner = "Jugador";
            else if (aiScore > playerScore) matchWinner = "Máquina";
            else matchWinner = "Empate";
            if (listener != null) listener.onDuelEnded(matchWinner);
        }
    }

    /**
     * Lógica de batalla simplificada:
     * - ATK vs ATK → mayor ATK gana.
     * - ATK vs DEF → ATK del atacante vs DEF del defensor.
     * - DEF vs ATK → DEF del atacante vs ATK del defensor.
     * - DEF vs DEF → Empate (sin puntos).
     */
    private String battle(Card atkCard, boolean atkMode, Card defCard, boolean defMode, boolean playerAttacks) {
        if (atkMode && defMode) { // ATK vs ATK
            if (atkCard.getAtk() > defCard.getAtk()) {
                if (playerAttacks) { playerScore++; return "Jugador"; }
                else { aiScore++; return "Máquina"; }
            } else if (atkCard.getAtk() < defCard.getAtk()) {
                if (playerAttacks) { aiScore++; return "Máquina"; }
                else { playerScore++; return "Jugador"; }
            } else {
                return "Empate";
            }
        } else if (atkMode && !defMode) { // ATK vs DEF
            if (atkCard.getAtk() > defCard.getDef()) {
                if (playerAttacks) { playerScore++; return "Jugador"; }
                else { aiScore++; return "Máquina"; }
            } else if (atkCard.getAtk() < defCard.getDef()) {
                if (playerAttacks) { aiScore++; return "Máquina"; }
                else { playerScore++; return "Jugador"; }
            } else {
                return "Empate";
            }
        } else if (!atkMode && defMode) { // DEF vs ATK
            if (defCard.getAtk() > atkCard.getDef()) {
                if (playerAttacks) { aiScore++; return "Máquina"; }
                else { playerScore++; return "Jugador"; }
            } else if (defCard.getAtk() < atkCard.getDef()) {
                if (playerAttacks) { playerScore++; return "Jugador"; }
                else { aiScore++; return "Máquina"; }
            } else {
                return "Empate";
            }
        } else { // DEF vs DEF
            return "Empate";
        }
    }
}