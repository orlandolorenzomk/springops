package org.kreyzon.springops.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.kreyzon.springops.common.exception.SpringOpsException;
import org.springframework.http.HttpStatus;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

/**
 * Utility class for encryption and decryption using AES.
 *
 * @author Lorenzo Orlando
 * @email orlandolorenzo@kreyzon.com
 */
@Slf4j
public class EncryptionUtils {

    /**
     * Encrypts the given plain text using the provided secret key.
     *
     * @param plainText the text to encrypt.
     * @param hexSecret the secret key in hexadecimal format.
     * @param algorithm the encryption algorithm (e.g., "AES").
     * @return the encrypted text in Base64 format.
     * @throws SpringOpsException if encryption fails.
     */
    public static String encrypt(String plainText, String hexSecret, String algorithm) throws Exception {
        byte[] secretBytes = hexStringToByteArray(hexSecret);
        if (secretBytes.length != 32) { // Validate 256-bit key length
            throw new SpringOpsException("Invalid AES key length: " + secretBytes.length, HttpStatus.BAD_REQUEST);
        }
        Cipher cipher = Cipher.getInstance(algorithm);
        SecretKeySpec keySpec = new SecretKeySpec(secretBytes, algorithm);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);
        byte[] encryptedBytes = cipher.doFinal(plainText.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    /**
     * Decrypts the given encrypted text using the provided secret key.
     *
     * @param encryptedText the text to decrypt (Base64 format).
     * @param hexSecret the secret key in hexadecimal format.
     * @param algorithm the encryption algorithm (e.g., "AES").
     * @return the decrypted plain text.
     * @throws SpringOpsException with {@link HttpStatus#BAD_REQUEST} if decryption fails due to an invalid key length.
     */
    public static String decrypt(String encryptedText, String hexSecret, String algorithm) throws Exception {
        byte[] secretBytes = hexStringToByteArray(hexSecret);
        if (secretBytes.length != 32) { // Validate 256-bit key length
            throw new SpringOpsException("Invalid AES key length: " + secretBytes.length, HttpStatus.BAD_REQUEST);
        }
        Cipher cipher = Cipher.getInstance(algorithm);
        SecretKeySpec keySpec = new SecretKeySpec(secretBytes, algorithm);
        cipher.init(Cipher.DECRYPT_MODE, keySpec);
        byte[] decodedBytes = Base64.getDecoder().decode(encryptedText);
        byte[] decryptedBytes = cipher.doFinal(decodedBytes);
        return new String(decryptedBytes);
    }

    /**
     * Converts a hexadecimal string into a byte array.
     *
     * @param hex the hexadecimal string.
     * @return the byte array.
     */
    private static byte[] hexStringToByteArray(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }
}

