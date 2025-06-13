package org.kreyzon.springops.core.deployment_status.enums;

import lombok.Getter;

@Getter
public enum DeploymentStatusEnum {
    SUCCESS("Success"),
    FAILURE("Failure"),
    NOT_RUN("Not Run");

    private final String label;

    DeploymentStatusEnum(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
