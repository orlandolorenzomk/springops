package org.kreyzon.springops.common.utils;

import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Key;
import java.util.Base64;

/**
 * Utility class for encryption and decryption using AES.
 *
 * @author Lorenzo Orlando
 * @email orlandolorenzo@kreyzon.com
 */
@Slf4j
public class EncryptionUtils {

    private static final String ALGORITHM = "AES";

    /**
     * Encrypts the given plain text using the provided secret key.
     *
     * @param plainText the text to encrypt.
     * @param secretKey the secret key for encryption.
     * @return the encrypted text in Base64 format.
     * @throws Exception if encryption fails.
     */
    public static String encrypt(String plainText, String secretKey) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        Key key = new SecretKeySpec(secretKey.getBytes(), ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encryptedBytes = cipher.doFinal(plainText.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    /**
     * Decrypts the given encrypted text using the provided secret key.
     *
     * @param encryptedText the text to decrypt (Base64 format).
     * @param secretKey the secret key for decryption.
     * @return the decrypted plain text.
     * @throws Exception if decryption fails.
     */
    public static String decrypt(String encryptedText, String secretKey) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        Key key = new SecretKeySpec(secretKey.getBytes(), ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decodedBytes = Base64.getDecoder().decode(encryptedText);
        byte[] decryptedBytes = cipher.doFinal(decodedBytes);
        return new String(decryptedBytes);
    }

    /**
     * Reads the secret key from the specified file path.
     *
     * @param filePath the path to the secret key file.
     * @return the secret key as a string.
     * @throws Exception if reading the file fails.
     */
    public static String readSecretKey(String filePath) throws Exception {
        Path path = Path.of(filePath);
        return Files.readString(path).trim();
    }

    /**
     * Generates a new random AES secret key.
     *
     * @return the generated secret key as a string.
     * @throws Exception if key generation fails.
     */
    public static String generateSecretKey() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
        keyGenerator.init(128); // AES key size
        SecretKey secretKey = keyGenerator.generateKey();
        return Base64.getEncoder().encodeToString(secretKey.getEncoded());
    }
}
