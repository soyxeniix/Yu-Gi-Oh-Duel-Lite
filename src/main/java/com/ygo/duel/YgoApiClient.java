package com.ygo.duel;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class YgoApiClient {
    private static final String RANDOM_ENDPOINT = "https://db.ygoprodeck.com/api/v7/randomcard.php";
    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private final Random rnd = new Random();

    public Card getRandomMonsterCard() throws IOException, InterruptedException {
        // Reintenta hasta conseguir Monster con ATK/DEF válidos
        for (int i = 0; i < 30; i++) {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(RANDOM_ENDPOINT))
                    .timeout(Duration.ofSeconds(15))
                    .GET()
                    .build();
            HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() != 200) continue;

            JSONObject json = new JSONObject(resp.body());

            // Algunas respuestas vienen como un objeto único
            String type = json.optString("type", "");
            if (!type.contains("Monster")) continue;

            int atk = json.optInt("atk", -1);
            int def = json.optInt("def", -1);
            if (atk < 0 || def < 0) continue;

            String name = json.optString("name", "Unknown");

            JSONArray images = json.optJSONArray("card_images");
            String small = null, full = null;
            if (images != null && images.length() > 0) {
                JSONObject img0 = images.getJSONObject(0);
                small = img0.optString("image_url_small", null);
                full = img0.optString("image_url", null);
            }
            if (small == null) small = full;
            if (small == null) continue;

            return new Card(name, atk, def, small, full);
        }
        throw new IOException("No se pudo obtener una carta Monster válida tras varios intentos.");
    }

    public List<Card> getInitialHand(int n) throws IOException, InterruptedException {
        List<Card> hand = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            hand.add(getRandomMonsterCard());
            // pequeño jitter para no golpear el endpoint tan seguido
            try { Thread.sleep(150 + rnd.nextInt(200)); } catch (InterruptedException ignored) {}
        }
        return hand;
    }
}