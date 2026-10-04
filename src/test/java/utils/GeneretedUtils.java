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

  }
