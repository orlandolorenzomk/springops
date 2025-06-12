package org.kreyzon.springops.core.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kreyzon.springops.common.exception.SpringOpsException;
import org.kreyzon.springops.common.utils.PortUtils;
import org.kreyzon.springops.core.application.entity.Application;
import org.kreyzon.springops.core.application.repository.ApplicationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Represents the service layer for managing Application entities.
 * This service provides methods to interact with the Application repository
 *
 * @author Lorenzo Orlando
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ApplicationLookupService {

    private final ApplicationRepository applicationRepository;

    /**
     * Finds an Application entity by its ID.
     *
     * @param id the ID of the Application to find
     * @return the Application entity if found
     * @throws SpringOpsException with {@link HttpStatus#NOT_FOUND} if the Application with the given ID does not exist
     */
    public Application findEntityById(Integer id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> new SpringOpsException("Application with ID '" + id + "' does not exist", HttpStatus.NOT_FOUND));
    }


    /**
     * Checks if a port is already in use by another application.
     *
     * @param port the port number to check
     * @return true if the port is already in use, false otherwise
     * @throws SpringOpsException with {@link HttpStatus#CONFLICT} if the port is already in use by another application
     */
    public Boolean isPortAlreadyInUseByOtherApplications(Integer port) {
        PortUtils.validatePort(port);
        List<Application> applications = applicationRepository.findAll();
        boolean isPortInUseDb = applications.stream()
                .anyMatch(application -> application.getPort().equals(port));

        boolean isPortInUseOs = PortUtils.isPortOccupied(port);

        if (isPortInUseDb && isPortInUseOs) {
            log.warn("Port {} is already in use by another application", port);
            throw new SpringOpsException("Port " + port + " is already in use by another application", HttpStatus.CONFLICT);
        }

        return false;
    }
}
