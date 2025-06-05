package org.kreyzon.springops.common.utils;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.io.File;

/**
 * Utility class for file-related operations.
 * @author Lorenzo Orlando
 * @email orlandolorenzo@kreyzon.com
 */
@UtilityClass
@Slf4j
public class FileUtils {

    /**
     * Creates a directory at the specified file path if it does not already exist.
     *
     * @param filePath the path of the directory to create.
     * @return {@code true} if the directory was created successfully or already exists, {@code false} otherwise.
     */
    public boolean createDirectory(String filePath) {
        File directory = new File(filePath);
        if (directory.exists()) {
            log.info("Directory already exists: {}", filePath);
            return true;
        }
        return directory.mkdirs();
    }
}