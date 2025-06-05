package org.kreyzon.springops.common.utils;

import lombok.experimental.UtilityClass;

/**
 * Utility class for database-related operations.
 * Provides helper methods for common database checks and operations.
 * This class cannot be instantiated.
 *
 * @author Lorenzo Orlando
 * @email orlandolorenzo@kreyzon.com
 */
@UtilityClass
public class DatabaseUtils {

    /**
     * Checks if a table is empty based on the number of rows.
     *
     * @param numberOfRows the number of rows in the table
     * @return {@code true} if the table is empty or {@code null}, {@code false} otherwise
     */
    public Boolean isTableEmpty(long numberOfRows) {
        return numberOfRows <= 0;
    }
}