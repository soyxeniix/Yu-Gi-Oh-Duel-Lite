package edu.ucol.ygo.net;

import edu.ucol.ygo.model.Card;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

public class YgoApiClient {

    private static final String RANDOM_URL = "https://db.ygoprodeck.com/api/v7/randomcard.php";
    private static final String CARDINFO_URL = "https://db.ygoprodeck.com/api/v7/cardinfo.php";

    private final HttpClient http = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.ALWAYS)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public Card fetchRandomMonster() throws Exception {
        // ------------ Intento A: randomcard.php (hasta 40 veces) ------------
        for (int i = 0; i < 40; i++) {
            try {
                HttpRequest req = HttpRequest.newBuilder(URI.create(RANDOM_URL))
                        .header("User-Agent", "ygo-duel-lite/1.0 (+edu.ucol)")
                        .timeout(Duration.ofSeconds(15))
                        .GET()
                        .build();

                HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
                if (res.statusCode() / 100 != 2) { Thread.sleep(150); continue; }

                JSONObject obj = new JSONObject(res.body());
                String type = obj.optString("type", "");
                if (!type.contains("Monster")) { Thread.sleep(100); continue; }

                return toCard(obj);
            } catch (Exception ignore) {
                Thread.sleep(150);
            }
        }

        // ------------ Intento B: cardinfo.php?num=1&offset=<aleatorio> ------------
        // Probamos varios offsets aleatorios y tomamos el primer objeto con 'Monster'
        for (int i = 0; i < 40; i++) {
            int offset = ThreadLocalRandom.current().nextInt(0, 10000);
            String url = CARDINFO_URL + "?num=1&offset=" + offset;

            try {
                HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                        .header("User-Agent", "ygo-duel-lite/1.0 (+edu.ucol)")
                        .timeout(Duration.ofSeconds(15))
                        .GET()
                        .build();

                HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
                if (res.statusCode() / 100 != 2) { Thread.sleep(150); continue; }

                JSONObject root = new JSONObject(res.body());
                JSONArray data = root.optJSONArray("data");
                if (data == null || data.length() == 0) { Thread.sleep(100); continue; }

                JSONObject obj = data.getJSONObject(0);
                String type = obj.optString("type", "");
                if (!type.contains("Monster")) { Thread.sleep(100); continue; }

                // Aseguramos tener ATK/DEF
                if (!obj.has("atk") && !obj.has("def")) { Thread.sleep(100); continue; }

                return toCard(obj);
            } catch (Exception ignore) {
                Thread.sleep(150);
            }
        }

        // Si llegamos aquí, realmente no hubo suerte
        throw new RuntimeException("No se pudo obtener una carta Monster tras varios intentos.");
    }

    private Card toCard(JSONObject obj) {
        String name = obj.optString("name", "Unknown");
        int atk = obj.optInt("atk", 0);
        int def = obj.optInt("def", 0);
        String imageUrl = "";
        JSONArray imgs = obj.optJSONArray("card_images");
        if (imgs != null && imgs.length() > 0) {
            // usa image_url si está, si no, intenta image_url_small
            JSONObject first = imgs.getJSONObject(0);
            imageUrl = first.optString("image_url", first.optString("image_url_small", ""));
        }
        if (imageUrl == null) imageUrl = "";
        return new Card(name, atk, def, imageUrl);
    }

    // util opcional
    public static boolean looksLikeValidImageUrl(String url) {
        try { new URL(url).toURI(); return url != null && url.startsWith("http"); }
        catch (Exception e) { return false; }
    }
}