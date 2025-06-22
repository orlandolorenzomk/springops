package org.kreyzon.springops.common.utils;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Utility class for deployment-related operations.
 * Provides methods to check if a process is running and to get the ports on which a process is listening.
 *
 * @author Lorenzo Orlando
 * @email orlandolorenzo@kreyzon.com
 */
@UtilityClass
@Slf4j
public class DeploymentUtils {

    /**
     * Check if a process with the given PID is running.
     *
     * @param pid the process ID to check
     * @return true if running, false otherwise
     */
    public boolean isPidRunning(Integer pid) {
        if (pid == null) return false;
        try {
            Process process = new ProcessBuilder("kill", "-0", pid.toString()).start();
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get the port(s) on which the process with given PID is listening.
     * Returns a comma-separated string of ports or empty string if none found.
     *
     * @param os  the operating system name
     * @param pid the process ID
     * @return ports as string or empty string if none found
     */
    public String getListeningPorts(String os, Integer pid) {
        if (pid == null) return "";
        StringBuilder ports = new StringBuilder();
        try {
            Process process = new ProcessBuilder("lsof", "-Pan", "-p", pid.toString(), "-iTCP", "-sTCP:LISTEN").start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String name;
                    if (os.toLowerCase().contains("suse")) {
                        // SUSE-specific parsing logic
                        int idx = line.lastIndexOf(' ');
                        if (idx == -1 || idx + 1 >= line.length()) continue;
                        name = line.substring(idx + 1);
                    } else if (os.toLowerCase().contains("debian")) {
                        // Debian-specific parsing logic
                        String[] parts = line.split("\\s+");
                        if (parts.length <= 9) continue; // Adjusted index for Debian
                        name = parts[9];
                    } else {
                        // Default parsing logic
                        String[] parts = line.split("\\s+");
                        if (parts.length <= 8) continue;
                        name = parts[8];
                    }
                    int colonIndex = name.lastIndexOf(':');
                    if (colonIndex != -1 && colonIndex + 1 < name.length()) {
                        String port = name.substring(colonIndex + 1);
                        if (!ports.isEmpty()) ports.append(",");
                        ports.append(port);
                    }
                }
            }
            process.waitFor();
        } catch (Exception e) {
            log.error("Error while getting listening ports for PID {}: {}", pid, e.getMessage());
        }
        return ports.toString();
    }
}

