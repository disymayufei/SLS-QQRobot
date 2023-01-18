package cn.starlight.disy.qqbot.utils;

import java.security.SecureRandom;

public class PasswordGenerator {

    private static final String CONST_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789_+*";

    public static String gen(){
        SecureRandom random = new SecureRandom();
        StringBuilder resultBuilder = new StringBuilder();

        for(int i = 0; i < 16; i++){
            resultBuilder.append(CONST_ALPHABET.charAt(random.nextInt(CONST_ALPHABET.length())));
        }

        return resultBuilder.toString();
    }
}
