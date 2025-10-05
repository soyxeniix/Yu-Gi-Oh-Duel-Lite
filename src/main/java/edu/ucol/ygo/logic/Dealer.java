package edu.ucol.ygo.logic;

import edu.ucol.ygo.model.Card;
import edu.ucol.ygo.net.YgoApiClient;

import java.util.ArrayList;
import java.util.List;

/**
 * Repartidor simple que obtiene cartas Monster de la API.
 * Captura cualquier excepción del cliente HTTP y usa un Fallback
 * para no interrumpir el juego/compilación.
 */
public class Dealer {

    private final YgoApiClient api;

    public Dealer(YgoApiClient api) {
        this.api = api;
    }

    /** Devuelve "count" cartas Monster desde la API, con fallback en caso de error. */
    public List<Card> drawMonsters(int count) {
        List<Card> out = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            try {
                out.add(api.fetchRandomMonster());
            } catch (Exception e) {
                // Fallback para no romper el flujo si la API falla puntual
                out.add(new Card("Fallback " + (i + 1), 1000, 1000, ""));
            }
        }
        return out;
    }
}