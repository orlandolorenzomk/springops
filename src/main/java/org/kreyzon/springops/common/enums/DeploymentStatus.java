package org.kreyzon.springops.common.enums;

public enum DeploymentStatus {
    RUNNING("RUNNING"),
    STOPPED("STOPPED"),

    FAILED("FAILED"),

    SUCCEEDED("SUCCEEDED");

    DeploymentStatus(String value) {

    }
}
