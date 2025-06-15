package org.kreyzon.springops.core.email.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class EmailUtils {

    public static String getFileExtension(String mimeType) {
        return switch (mimeType) {
            case "application/pdf" -> "pdf";
            case "image/png" -> "png";
            case "image/jpeg" -> "jpg";
            case "text/plain" -> "txt";
            case "application/msword" -> "doc";
            case "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> "docx";
            default -> "bin"; // Default to a generic binary file
        };
    }
}
