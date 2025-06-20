package org.kreyzon.springops.core.os_info.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Service for retrieving OS information.
 *
 * @author Lorenzo Orlando
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OsInfoService {

    /**
     * Path to the shell script that retrieves OS information.
     * This script should be placed in the resources directory of the project.
     */
    private static final String OS_INFO_SCRIPT_PATH = "resources/script/os_info.sh";

    /**
     * Retrieves OS information by executing a shell script.
     *
     * @return A map containing OS information with keys as property names and values as property values.
     * @throws IOException If an error occurs while executing the script or reading its output.
     */
    public Map<String, String> getOsInfo() throws IOException {
        log.info("Getting OS information");

        String scriptOutput = executeScript("os_info.sh");

        Map<String, String> osInfoMap = parseScriptOutput(scriptOutput);

        log.info("Successfully retrieved OS information");
        return osInfoMap;
    }

    private String executeScript(String scriptName) throws IOException {
        InputStream scriptStream = getClass().getClassLoader().getResourceAsStream("scripts/" + scriptName);
        if (scriptStream == null) {
            log.error("Script not found: {}", scriptName);
            throw new IOException("Script not found in resources/scripts/: " + scriptName);
        }

        File tempScript = File.createTempFile("springops-script-", ".sh");
        tempScript.deleteOnExit();
        Files.copy(scriptStream, tempScript.toPath(), StandardCopyOption.REPLACE_EXISTING);
        tempScript.setExecutable(true);

        CommandLine cmdLine = new CommandLine(tempScript.getAbsolutePath());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream);

        DefaultExecutor executor = new DefaultExecutor();
        executor.setStreamHandler(streamHandler);

        int exitValue = executor.execute(cmdLine);
        if (exitValue != 0) {
            throw new IOException("Script execution failed with exit code: " + exitValue);
        }

        log.info("Script executed successfully: {}", scriptName);
        return outputStream.toString();
    }

    private Map<String, String> parseScriptOutput(String scriptOutput) {
        Map<String, String> osInfoMap = new HashMap<>();

        String[] lines = scriptOutput.split("\\r?\\n");
        for (String line : lines) {
            String[] parts = line.split("=", 2);
            if (parts.length == 2) {
                osInfoMap.put(parts[0], parts[1]);
            }
        }

        return osInfoMap;
    }
}