package org.kreyzon.springops.common.utils;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility class for generating a secure AES secret key.
 * This class provides a method to generate a valid AES secret key of 16 bytes (128 bits),
 *
 * @author Lorenzo Orlando
 * @email orlandolorenzo@kreyzon.com
 */
public class SecretKeyGenerator {

    /**
     * Generates a valid AES secret key of 16 bytes (128 bits).
     *
     * @return the generated secret key as a Base64-encoded string.
     */
    public static String generateAesSecretKey() {
        byte[] key = new byte[16]; // 16 bytes = 128 bits
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(key);
        return Base64.getEncoder().encodeToString(key);
    }
}