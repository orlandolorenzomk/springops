package org.kreyzon.springops.common.dto.deployment;

import org.kreyzon.springops.core.system_version.entity.SystemVersion;

public record DeploymentContextDto(
        String authenticatedUrl,
        String sourcePath,
        SystemVersion javaVersion,
        SystemVersion mavenVersion,
        String branchName,
        String environmentVariables,
        Integer port
) {

}