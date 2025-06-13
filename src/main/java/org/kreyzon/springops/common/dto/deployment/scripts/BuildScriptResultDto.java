package org.kreyzon.springops.common.dto.deployment.scripts;

import java.util.List;

public record BuildScriptResultDto(
        boolean status,

        String message,
        List<String> artifacts
) {
}
