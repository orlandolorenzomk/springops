package org.kreyzon.springops.common.dto.deployment;

import lombok.Data;

@Data
public class DeploymentResultDto {
    private boolean success;
    private CommandResultDto updateResult;
    private CommandResultDto buildResult;
    private CommandResultDto runResult;
    private String builtJar;
}
