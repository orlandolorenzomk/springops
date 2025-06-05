package org.kreyzon.springops.common.utils;

import lombok.experimental.UtilityClass;

import java.security.SecureRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Utility class for generating random passwords.
 * Provides a method to generate secure passwords with a mix of characters.
 * @author Lorenzo Orlando
 * @email orlandolorenzo@kreyzon.com
 */
@UtilityClass
public class PasswordGenerator {

    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final String SPECIAL_CHARACTERS = "@$!%*?&";
    private static final String ALL_CHARACTERS = UPPERCASE + LOWERCASE + DIGITS + SPECIAL_CHARACTERS;
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Generates a random password with the specified length.
     * The password includes uppercase letters, lowercase letters, digits, and special characters.
     *
     * @param length the desired length of the password. Must be at least 8 characters.
     * @return a randomly generated password.
     * @throws IllegalArgumentException if the length is less than 8.
     */
    public String generateRandomPassword(int length) {
        if (length < 8) {
            throw new IllegalArgumentException("Password length must be at least 8 characters");
        }

        return IntStream.range(0, length)
                .mapToObj(i -> String.valueOf(ALL_CHARACTERS.charAt(RANDOM.nextInt(ALL_CHARACTERS.length()))))
                .collect(Collectors.joining());
    }
}