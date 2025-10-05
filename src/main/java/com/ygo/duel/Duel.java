package com.ygo.duel;

import java.util.*;

public class Duel {
    private final List<Card> playerHand;
    private final List<Card> aiHand;
    private final Random rnd = new Random();
    private int playerScore = 0;
    private int aiScore = 0;
    private boolean playerTurn;
    private final BattleListener listener;

    public Duel(List<Card> playerHand, List<Card> aiHand, BattleListener listener) {
        this.playerHand = new ArrayList<>(Objects.requireNonNull(playerHand));
        this.aiHand = new ArrayList<>(Objects.requireNonNull(aiHand));
        this.listener = listener;
        this.playerTurn = rnd.nextBoolean(); // turno inicial aleatorio
    }

    public int getPlayerScore() { return playerScore; }
    public int getAiScore() { return aiScore; }
    public boolean isPlayerTurn() { return playerTurn; }
    public List<Card> getPlayerHand() { return Collections.unmodifiableList(playerHand); }
    public List<Card> getAiHand() { return Collections.unmodifiableList(aiHand); }

    public boolean isFinished() {
        return playerScore == 2 || aiScore == 2 || (playerHand.isEmpty() && aiHand.isEmpty());
    }

    public void playTurn(Card playerCard, Mode playerMode) {
        if (isFinished()) return;

        // IA elige carta al azar y modo aleatorio
        Card aiCard = aiHand.remove(rnd.nextInt(aiHand.size()));
        Mode aiMode = rnd.nextBoolean() ? Mode.ATTACK : Mode.DEFENSE;

        // Jugador: remover su carta usada
        playerHand.remove(playerCard);

        // Reglas:
        String winner;
        if (playerMode == Mode.ATTACK && aiMode == Mode.ATTACK) {
            winner = (playerCard.getAtk() >= aiCard.getAtk()) ? "Jugador" : "IA";
        } else if (playerMode == Mode.ATTACK && aiMode == Mode.DEFENSE) {
            winner = (playerCard.getAtk() >= aiCard.getDef()) ? "Jugador" : "IA";
        } else if (playerMode == Mode.DEFENSE && aiMode == Mode.ATTACK) {
            winner = (aiCard.getAtk() > playerCard.getDef()) ? "IA" : "Jugador";
        } else {
            // ambos en defensa -> desempate por DEF, si empatan gana quien NO tenía el turno
            if (playerCard.getDef() == aiCard.getDef()) {
                winner = playerTurn ? "IA" : "Jugador";
            } else {
                winner = (playerCard.getDef() >= aiCard.getDef()) ? "Jugador" : "IA";
            }
        }

        if ("Jugador".equals(winner)) playerScore++; else aiScore++;

        if (listener != null) {
            listener.onTurn(
                playerCard + " (" + (playerMode==Mode.ATTACK?"ATK":"DEF") + ")",
                aiCard + " (" + (aiMode==Mode.ATTACK?"ATK":"DEF") + ")",
                winner
            );
            listener.onScoreChanged(playerScore, aiScore);
        }

        if (isFinished() && listener != null) {
            String finalWinner = (playerScore > aiScore) ? "Jugador" : "IA";
            listener.onDuelEnded(finalWinner);
        }

        // Alternar turno
        playerTurn = !playerTurn;
    }
}