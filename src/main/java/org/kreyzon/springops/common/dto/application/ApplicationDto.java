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
    String gitProjectSshUrl;
    Integer port;
    String javaMinimumMemory;
    String javaMaximumMemory;


    /**
     * Converts an {@link Application} entity to an {@link ApplicationDto}.
     *
     * @param application the application entity to convert
     * @return the converted ApplicationDto
     */
    public static ApplicationDto fromEntity(Application application) {
        return new ApplicationDto(
                application.getId(),
                application.getName(),
                application.getFolderRoot(),
                application.getDescription(),
                application.getCreatedAt(),
                application.getMvnSystemVersion() != null ? application.getMvnSystemVersion().getId() : null,
                application.getJavaSystemVersion() != null ? application.getJavaSystemVersion().getId() : null,
                application.getGitProjectHttpsUrl(),
                application.getGitProjectSshUrl(),
                application.getPort() != null ? application.getPort() : 0,
                application.getJavaMinimumMemory() != null ? application.getJavaMinimumMemory() : "512m",
                application.getJavaMaximumMemory() != null ? application.getJavaMaximumMemory() : "1024m"
        );
    }

    /**
     * Converts an {@link ApplicationDto} to an {@link Application} entity.
     *
     * @param applicationDto the application DTO to convert
     * @return the converted Application entity
     */
    public static Application toEntity(ApplicationDto applicationDto, boolean updateName) {
        return Application.builder()
                .id(applicationDto.getId())
                .name(updateName ? applicationDto.getName() : null)
                .folderRoot(applicationDto.getFolderRoot())
                .description(applicationDto.getDescription())
                .createdAt(applicationDto.getCreatedAt())
                .gitProjectHttpsUrl(applicationDto.getGitProjectHttpsUrl())
                .gitProjectSshUrl(applicationDto.getGitProjectSshUrl())
                .port(applicationDto.getPort())
                .javaMinimumMemory(applicationDto.getJavaMinimumMemory())
                .javaMaximumMemory(applicationDto.getJavaMaximumMemory())
                .build();
    }
}
