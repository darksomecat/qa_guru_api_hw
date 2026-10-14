package utils;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.Base64;

public class GeneratedUtils {

    public static String getRandomString(int length) {
        String letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        StringBuilder result = new StringBuilder();
        SecureRandom rnd = new SecureRandom();
        for (int i = 0; i < length; i++) {
            result.append(letters.charAt(rnd.nextInt(letters.length())));
        }
        return result.toString();
    }

    public static String getRandomHex() {
        return Long.toHexString(new SecureRandom().nextLong());
    }

    public static String getUserIdFromToken(String accessToken) {
        String payload = new String(Base64.getUrlDecoder()
                .decode(accessToken.split("\\.")[1]), StandardCharsets.UTF_8);

        return payload.substring(payload.indexOf("\"user_id\":") + 10)
                .replaceAll("[^0-9].*", "");
    }

}
