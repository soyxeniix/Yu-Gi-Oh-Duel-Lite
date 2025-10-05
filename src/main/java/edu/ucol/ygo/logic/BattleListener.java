package edu.ucol.ygo.logic;

import edu.ucol.ygo.model.Card;

public interface BattleListener {
    // Firma existente (mantener 5 parámetros):
    void onTurn(String playerCard, String aiCard, String modePlayer, String modeAI, String winner);

    void onScoreChanged(int playerScore, int aiScore);

    void onDuelEnded(String winner);

    // NUEVO: notifica cuáles objetos Card fueron usados para poder quitar sus paneles
    void onCardsUsed(Card playerCard, Card aiCard);
}