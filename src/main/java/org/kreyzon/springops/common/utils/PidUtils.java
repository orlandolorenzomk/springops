package org.kreyzon.springops.common.utils;

import lombok.experimental.UtilityClass;
import org.kreyzon.springops.common.dto.application_stats.ApplicationStatsDto;
import oshi.SystemInfo;
import oshi.hardware.GlobalMemory;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Utility class for retrieving process resource usage statistics by PID.
 * This class provides methods to get CPU and memory usage for a specific process ID (PID)
 * using the `ps` command for CPU and memory per-process, and OSHI for available system memory.
 *
 * @author Lorenzo Orlando
 * @email orlandolorenzo@kreyzon.com
 */
@UtilityClass
public class PidUtils {

    /**
     * Retrieves CPU usage (%), process memory in bytes, and available system memory (MB)
     * for a given PID. Uses `ps` for CPU/mem percentage and OSHI for memory metrics.
     *
     * @param pid the process ID
     * @return a PidUsageDto with CPU%, memoryBytes, and available memory (MB)
     * @throws Exception if command or parsing fails
     */
    public static ApplicationStatsDto getCpuMemUsage(int pid) throws Exception {
        double cpuPercent, memPercent;

        Process p1 = new ProcessBuilder("ps", "-p", String.valueOf(pid),
                "-o", "pcpu=", "-o", "pmem=").start();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(p1.getInputStream()))) {
            String line = br.readLine();
            if (line == null || line.isBlank()) {
                throw new IllegalArgumentException("PID " + pid + " not found or no output");
            }
            String[] parts = line.trim().split("\\s+");
            cpuPercent = Double.parseDouble(parts[0]);
            memPercent = Double.parseDouble(parts[1]);
        }

        SystemInfo si = new SystemInfo();
        GlobalMemory mem = si.getHardware().getMemory();
        long totalBytes = mem.getTotal();
        double availMB = mem.getAvailable() / (1024.0 * 1024.0);

        // Convert memory percentage to megabytes
        long processMemMb = (long) ((memPercent / 100.0) * totalBytes / (1024.0 * 1024.0));

        return new ApplicationStatsDto(processMemMb, cpuPercent, availMB, null);
    }
}
