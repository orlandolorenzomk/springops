package org.kreyzon.springops.common.dto.deployment.scripts;

public record UpdateScriptResultDto(
        boolean status,
        String message,
        String deployBranch
) {
}
