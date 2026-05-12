package com.bragari.util;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class PasswordUtil {

    private static final int SALT_LENGTH = 16;
    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH = 256;
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordUtil() {
    }

    public static String hashPassword(String parola) {
        try {
            byte[] salt = new byte[SALT_LENGTH];
            RANDOM.nextBytes(salt);

            byte[] hash = hash(parola.toCharArray(), salt, ITERATIONS, KEY_LENGTH);

            return ITERATIONS + ":"
                    + Base64.getEncoder().encodeToString(salt) + ":"
                    + Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Eroare la criptarea parolei: " + e.getMessage());
        }
    }

    public static boolean verifyPassword(String parola, String hashSalvat) {
        try {
            if (parola == null || hashSalvat == null || hashSalvat.isBlank()) {
                return false;
            }

            String[] parts = hashSalvat.split(":");
            if (parts.length != 3) {
                return false;
            }

            int iterations = Integer.parseInt(parts[0]);
            byte[] salt = Base64.getDecoder().decode(parts[1]);
            byte[] hashSalvatBytes = Base64.getDecoder().decode(parts[2]);
            byte[] hashParola = hash(parola.toCharArray(), salt, iterations, hashSalvatBytes.length * 8);

            return MessageDigest.isEqual(hashSalvatBytes, hashParola);
        } catch (Exception e) {
            return false;
        }
    }

    private static byte[] hash(char[] parola, byte[] salt, int iterations, int keyLength) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(parola, salt, iterations, keyLength);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        return factory.generateSecret(spec).getEncoded();
    }
}
