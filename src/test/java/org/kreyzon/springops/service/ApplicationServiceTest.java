package org.kreyzon.springops.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kreyzon.springops.common.dto.application.ApplicationDto;
import org.kreyzon.springops.common.dto.deployment.DeploymentStatusDto;
import org.kreyzon.springops.common.exception.SpringOpsException;
import org.kreyzon.springops.config.ApplicationConfig;
import org.kreyzon.springops.core.application.entity.Application;
import org.kreyzon.springops.core.application.repository.ApplicationRepository;
import org.kreyzon.springops.core.application.service.ApplicationService;
import org.kreyzon.springops.core.deployment.service.DeploymentManagerService;
import org.kreyzon.springops.core.system_version.service.SystemVersionService;
import org.kreyzon.springops.setup.service.SetupService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ApplicationServiceTest {

    private ApplicationRepository applicationRepository;
    private SystemVersionService systemVersionService;
    private ApplicationConfig applicationConfig;
    private SetupService setupService;
    private DeploymentManagerService deploymentManagerService;
    private ApplicationService applicationService;

    @BeforeEach
    void setUp() {
        applicationRepository = mock(ApplicationRepository.class);
        systemVersionService = mock(SystemVersionService.class);
        applicationConfig = mock(ApplicationConfig.class);
        setupService = mock(SetupService.class);
        deploymentManagerService = mock(DeploymentManagerService.class);

        applicationService = new ApplicationService(applicationRepository, systemVersionService, applicationConfig, setupService, deploymentManagerService);
    }

    @Test
    void findById_shouldReturnDto() {
        Application app = new Application();
        app.setId(1);
        when(applicationRepository.findById(1)).thenReturn(Optional.of(app));

        ApplicationDto dto = applicationService.findById(1);
        assertEquals(1, dto.getId());
    }

    @Test
    void findById_shouldThrowIfMissing() {
        when(applicationRepository.findById(1)).thenReturn(Optional.empty());
        assertThrows(SpringOpsException.class, () -> applicationService.findById(1));
    }

    @Test
    void findAll_shouldReturnList() {
        Application app = new Application();
        app.setId(1);
        when(applicationRepository.findAll()).thenReturn(List.of(app));

        List<ApplicationDto> list = applicationService.findAll();
        assertEquals(1, list.size());
        assertEquals(1, list.get(0).getId());
    }

    @Test
    void deleteById_shouldDeleteSuccessfully() {
        when(applicationRepository.existsById(1)).thenReturn(true);

        DeploymentStatusDto status = new DeploymentStatusDto();
        status.setIsRunning(false);

        when(deploymentManagerService.getDeploymentStatus(1)).thenReturn(status);

        applicationService.deleteById(1);

        verify(applicationRepository).deleteById(1);
    }

    @Test
    void deleteById_shouldThrowIfRunning() {
        when(applicationRepository.existsById(1)).thenReturn(true);
        DeploymentStatusDto status = new DeploymentStatusDto();
        status.setIsRunning(true);
        when(deploymentManagerService.getDeploymentStatus(1)).thenReturn(status);

        assertThrows(SpringOpsException.class, () -> applicationService.deleteById(1));
    }

    @Test
    void getEntityById_shouldReturnEntity() {
        Application app = new Application();
        app.setId(1);
        when(applicationRepository.findById(1)).thenReturn(Optional.of(app));

        Application result = applicationService.getEntityById(1);
        assertEquals(1, result.getId());
    }

    @Test
    void getEntityById_shouldThrowIfMissing() {
        when(applicationRepository.findById(1)).thenReturn(Optional.empty());
        assertThrows(SpringOpsException.class, () -> applicationService.getEntityById(1));
    }
}
