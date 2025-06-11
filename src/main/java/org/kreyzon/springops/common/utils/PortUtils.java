package org.kreyzon.springops.common.utils;

import lombok.experimental.UtilityClass;
import org.kreyzon.springops.common.exception.SpringOpsException;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * Utility class for port-related operations.
 *
 * @author Lorenzo Orlando
 * @email orlandolorenzo@kreyzon.com
 */
@UtilityClass
public class PortUtils {

    /**
     * Checks if the specified port is occupied.
     *
     * @param port the port number to check
     * @return {@code true} if the port is occupied, {@code false} otherwise
     */
    public boolean isPortOccupied(int port) {
        validatePort(port);
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            serverSocket.setReuseAddress(true);
            return false; // Port is not occupied
        } catch (IOException e) {
            return true; // Port is occupied
        }
    }

    public void validatePort(int port) {
        if (port < 0 || port > 65535) {
            throw new SpringOpsException("Port number must be between 0 and 65535", HttpStatus.BAD_REQUEST);
        }
    }
}