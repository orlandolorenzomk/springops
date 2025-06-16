package org.kreyzon.springops.common.dto.dashboard;

import lombok.Value;

@Value
public class DashboardDto {
    Integer registeredApps;
    Integer runningApps;
    Integer environments;
}
