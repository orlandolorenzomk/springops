package org.kreyzon.springops.common.dto.setup;

import lombok.Builder;
import lombok.Data;
import lombok.Value;

/**
 * DTO representing the setup status and pending initializations.
 *
 * @author Lorenzo Orlando
 * @email orlandolorenzo@kreyzon.com
 */
@Value
@Builder
public class SetupStatusDto {
    Boolean isSetupComplete;
    Boolean isFirstAdminInitialized;
    Boolean isFilesRootInitialized;
}