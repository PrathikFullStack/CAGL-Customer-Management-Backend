package com.iexceed.appzillonbanking.cagl.cob.utils;


import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AESDecryptionUtil {

    private static final String AES_ALGORITHM = "AES/GCM/NoPadding";
    private static final String AES = "AES";

    // GCM recommended values
    private static final int IV_LENGTH = 12; // 96 bits
    private static final int TAG_LENGTH = 128; // 128 bits

    public static String encrypt(String plainText, String secretKey) throws Exception {

        if (secretKey == null || secretKey.length() != 16) {
            throw new IllegalArgumentException(
                    "AES secret key must be 16 characters");
        }

        // Create AES key
        SecretKeySpec secretKeySpec = new SecretKeySpec(
                secretKey.getBytes(StandardCharsets.UTF_8),
                AES);

        // Generate random IV
        byte[] iv = new byte[IV_LENGTH];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(iv);

        // Initialize cipher
        Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
        GCMParameterSpec gcmParameterSpec =
                new GCMParameterSpec(TAG_LENGTH, iv);

        cipher.init(
                Cipher.ENCRYPT_MODE,
                secretKeySpec,
                gcmParameterSpec);

        // Encrypt data
        byte[] encryptedBytes = cipher.doFinal(
                plainText.getBytes(StandardCharsets.UTF_8));

        // Store IV + CipherText together
        ByteBuffer byteBuffer =
                ByteBuffer.allocate(iv.length + encryptedBytes.length);

        byteBuffer.put(iv);
        byteBuffer.put(encryptedBytes);

        return Base64.getEncoder()
                .encodeToString(byteBuffer.array());
    }

    public static String decrypt(
            String encryptedText,
            String secretKey) throws Exception {

        if (secretKey == null || secretKey.length() != 16) {
            throw new IllegalArgumentException(
                    "AES secret key must be 16 characters");
        }

        // Decode Base64
        byte[] encryptedData =
                Base64.getDecoder().decode(encryptedText);

        // Extract IV
        ByteBuffer byteBuffer =
                ByteBuffer.wrap(encryptedData);

        byte[] iv = new byte[IV_LENGTH];
        byteBuffer.get(iv);

        // Extract encrypted bytes
        byte[] cipherText =
                new byte[byteBuffer.remaining()];
        byteBuffer.get(cipherText);

        // Create AES key
        SecretKeySpec secretKeySpec = new SecretKeySpec(
                secretKey.getBytes(StandardCharsets.UTF_8),
                AES);

        // Initialize cipher
        Cipher cipher = Cipher.getInstance(AES_ALGORITHM);

        GCMParameterSpec gcmParameterSpec =
                new GCMParameterSpec(TAG_LENGTH, iv);

        cipher.init(
                Cipher.DECRYPT_MODE,
                secretKeySpec,
                gcmParameterSpec);

        // Decrypt
        byte[] decryptedBytes =
                cipher.doFinal(cipherText);

        return new String(
                decryptedBytes,
                StandardCharsets.UTF_8);
    }
}
