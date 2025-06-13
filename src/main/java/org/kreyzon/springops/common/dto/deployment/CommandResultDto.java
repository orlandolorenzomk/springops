package org.kreyzon.springops.common.dto.deployment;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommandResultDto {
    private int exitCode;
    private String output;
    private String status;
    private String message;
    private List<Object> data;
    @JsonIgnore
    private DeploymentContextDto deploymentContext;
}
