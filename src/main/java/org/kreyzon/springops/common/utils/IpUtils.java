package org.kreyzon.springops.common.utils;

import lombok.experimental.UtilityClass;
import org.kreyzon.springops.common.exception.SpringOpsException;
import org.springframework.http.HttpStatus;

/**
 * Utility class for IP address related operations.
 * Provides methods to retrieve the local IP address of the machine.
 *
 * @author Lorenzo Orlando
 */
@UtilityClass
public class IpUtils {

    /**
     * Retrieves the local IP address of the machine.
     *
     * @return A string representing the local IP address.
     * @throws SpringOpsException with {@link HttpStatus#INTERNAL_SERVER_ERROR} if the local host cannot be determined.
     */
    public String getLocalIpAddress() {
        try {
            return java.net.InetAddress.getLocalHost().getHostAddress();
        } catch (java.net.UnknownHostException e) {
            throw new SpringOpsException("Unable to retrieve local IP address", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
