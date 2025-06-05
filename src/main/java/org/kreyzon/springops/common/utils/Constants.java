package org.kreyzon.springops.common.utils;

import lombok.experimental.UtilityClass;

/**
 * Utility class for storing application-wide constants.
 * Provides standard values used across the application.
 *
 * @author Lorenzo Orlando
 * @email orlandolorenzo@kreyzon.com
 */
@UtilityClass
public class Constants {

    /**
     * Standard username for the default admin user.
     */
    public final String STANDARD_ADMIN_USERNAME = "admin";

    /**
     * Standard email address for the default admin user.
     */
    public final String STANDARD_ADMIN_EMAIL = "springops@kreyzon.com";

    /**
     * Standard password length for the default admin user.
     * This is used to generate a random password for the admin user.
     */
    public final int STANDARD_ADMIN_PASSWORD_LENGTH = 8;

    /**
     * Name of the root directory where application files are stored.
     * This is used to create a dedicated directory for the application.
     */
    public final String ROOT_DIRECTORY_NAME = "springops";

    /**
     * Path to the file where the secret key is stored.
     * This file is used for encryption and decryption operations.
     */
    public final String SECRET_KEY_FILE_PATH = "secret_key.txt";

    /**
     * Path to the SSH key file used for Git operations.
     * This file is used to authenticate with Git repositories.
     */
    public final String SSH_KEY_FILE_PATH = "ssh-key.txt";
}