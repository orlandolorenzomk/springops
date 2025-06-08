package org.kreyzon.springops.common.dto.deployment;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CommandResultDto {
    private int exitCode;
    private String output;
}
