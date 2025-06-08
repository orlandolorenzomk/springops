package org.kreyzon.springops.common.dto.deployment;

import lombok.Data;

@Data
public class DeploymentStatusDto {
    private String port;
    private Boolean isRunning;
    private String pid;
}
