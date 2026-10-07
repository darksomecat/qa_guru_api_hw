package utils;

import java.security.SecureRandom;

public class GeneretedUtils {

        public static String getRandomString ( int leght){
            String LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
            StringBuilder result = new StringBuilder();
            SecureRandom rnd = new SecureRandom();
            for (int i = 0; i < leght; i++)
                result
                        .append
                                (LETTERS.charAt
                                        (rnd.nextInt
                                                (LETTERS.length())));


            return result.toString();
        }

        public static String getRandomHex () {
            return Long.toHexString(new SecureRandom().nextLong());
        }

        public static String getUserIdFromToken (String accessToken) {
            String payload = new String(java.util.Base64.getUrlDecoder()
                    .decode(accessToken.split("\\.")[1]), java.nio.charset.StandardCharsets.UTF_8);

            return payload.substring(payload.indexOf("\"user_id\":") + 10)
                    .replaceAll("[^0-9].*", "");
        }

  }