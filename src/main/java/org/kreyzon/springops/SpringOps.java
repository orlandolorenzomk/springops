package org.kreyzon.springops;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * The entry point of the SpringOps application.
 * This class initializes and starts the Spring Boot application.
 *
 * @author Lorenzo Orlando
 * @email orlandolorenzo@kreyzon.com
 */
@SpringBootApplication
@EnableRetry
@EnableScheduling
@EnableAsync
@EnableCaching
@Slf4j
public class SpringOps {

    /**
     * The main method that serves as the entry point of the application.
     *
     * @param args Command-line arguments passed to the application.
     */
    public static void main(String[] args) {
        log.info("Starting SpringOps application...");
        log.info("Application is running as OS user: '{}'. Ensure this user has read/write permissions to all necessary directories (e.g., source folders, Git clone targets).", System.getProperty("user.name"));
        SpringApplication.run(SpringOps.class, args);
    }
}