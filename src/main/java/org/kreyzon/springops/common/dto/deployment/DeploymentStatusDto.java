package org.kreyzon.springops.common.dto.deployment;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DeploymentStatusDto {
    private String port;
    private Boolean isRunning;
    private String pid;

    public DeploymentStatusDto(Boolean isRunning) {
        this.isRunning = isRunning;
    }
}
