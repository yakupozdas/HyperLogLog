package com.hll.core;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashUtil {
    public static long hash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));

            // İlk 8 byte'ı long'a çeviriyoruz (64 bit)
            long value = 0;
            for (int i = 0; i < 8; i++) {
                value = (value << 8) | (hashBytes[i] & 0xff);
            }
            return value;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Hash algoritması bulunamadı!");
        }
    }
}