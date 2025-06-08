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
     * Name of the root directory where application files are stored.
     * This is used to create a dedicated directory for the application.
     */
    public final String ROOT_DIRECTORY_NAME = "/springops";

    /**
     * Subdirectory within the root directory where application data is stored.
     * This is used to organize application-specific files.
     */
    public final String DIRECTORY_APPLICATIONS = "/applications";

    /**
     * Subdirectory within the root directory where deployment data is stored.
     * This is used to organize deployment-specific files.
     */
    public static final String STATUS_RUNNING = "RUNNING";

    /**
     * Subdirectory within the root directory where deployment data is stored.
     * This is used to organize deployment-specific files.
     */
    public static final String TYPE_LATEST = "LATEST";
}