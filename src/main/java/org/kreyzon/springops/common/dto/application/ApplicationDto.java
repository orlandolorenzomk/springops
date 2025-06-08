package org.kreyzon.springops.common.dto.application;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Value;
import org.kreyzon.springops.core.application.entity.Application;
import org.kreyzon.springops.core.system_version.entity.SystemVersion;

import java.io.Serializable;
import java.time.Instant;

/**
 * DTO for {@link org.kreyzon.springops.core.application.entity.Application}
 */
@Value
public class ApplicationDto implements Serializable {
    Integer id;
    @NotNull
    @Size(max = 255)
    String name;
    String folderRoot;
    String description;
    Instant createdAt;
    Integer mvnSystemVersionId;
    Integer javaSystemVersionId;
    String gitProjectHttpsUrl;


    public static ApplicationDto fromEntity(Application application) {
        return new ApplicationDto(
                application.getId(),
                application.getName(),
                application.getFolderRoot(),
                application.getDescription(),
                application.getCreatedAt(),
                application.getMvnSystemVersion() != null ? application.getMvnSystemVersion().getId() : null,
                application.getJavaSystemVersion() != null ? application.getJavaSystemVersion().getId() : null,
                application.getGitProjectHttpsUrl()
        );
    }

    public static Application toEntity(ApplicationDto applicationDto) {
        return Application.builder()
                .id(applicationDto.getId())
                .name(applicationDto.getName())
                .folderRoot(applicationDto.getFolderRoot())
                .description(applicationDto.getDescription())
                .createdAt(applicationDto.getCreatedAt())
                .gitProjectHttpsUrl(applicationDto.getGitProjectHttpsUrl())

                .build();
    }
}