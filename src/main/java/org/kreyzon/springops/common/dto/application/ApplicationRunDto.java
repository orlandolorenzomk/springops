package org.kreyzon.springops.common.dto.application;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents the data returned after pulling, building, and running a project.
 * Includes details about the application, branch, paths, and operation statuses.
 *
 * @author Lorenzo Orlando
 * @email orlandolorenzo@kreyzon.com
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationRunDto {
    private String applicationName;
    private String branchName;
    private String mavenPath;
    private String javaPath;
    private boolean cloneSuccess;
    private String cloneMessage;
    private boolean buildSuccess;
    private String buildMessage;
    private boolean runSuccess;
    private String runMessage;
}