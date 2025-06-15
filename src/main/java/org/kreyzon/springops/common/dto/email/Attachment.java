package org.kreyzon.springops.common.dto.email;

import lombok.Builder;
import lombok.Getter;

import java.util.Objects;

@Builder
@Getter
public class Attachment {
    private final String fileName; // File name
    private final String fileType; // MIME type (e.g., application/pdf, image/png)
    private final String base64; // Base64-encoded file content


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Attachment)) return false;
        Attachment that = (Attachment) o;
        return Objects.equals(fileType, that.fileType) &&
                Objects.equals(base64, that.base64);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileType, base64);
    }

    @Override
    public String toString() {
        return "Attachment{" +
                "fileType='" + fileType + '\'' +
                ", base64='" + (base64.length() > 30 ? base64.substring(0, 30) + "..." : base64) + '\'' +
                '}';
    }
}
