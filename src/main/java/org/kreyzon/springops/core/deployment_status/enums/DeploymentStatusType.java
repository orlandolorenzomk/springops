package org.kreyzon.springops.core.deployment_status.enums;

import lombok.Getter;

@Getter
public enum DeploymentStatusType {
    UPDATE("Update"),
    BUILD("Build"),
    RUN("Run");

    private final String label;

    DeploymentStatusType(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
