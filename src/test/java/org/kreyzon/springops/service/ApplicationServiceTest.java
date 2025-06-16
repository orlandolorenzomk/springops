package org.kreyzon.springops.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kreyzon.springops.common.dto.application.ApplicationDto;
import org.kreyzon.springops.common.dto.deployment.DeploymentStatusDto;
import org.kreyzon.springops.common.dto.system_version.SystemVersionDto;
import org.kreyzon.springops.common.exception.SpringOpsException;
import org.kreyzon.springops.config.ApplicationConfig;
import org.kreyzon.springops.core.application.entity.Application;
import org.kreyzon.springops.core.application.repository.ApplicationRepository;
import org.kreyzon.springops.core.application.service.ApplicationService;
import org.kreyzon.springops.core.deployment.service.DeploymentManagerService;
import org.kreyzon.springops.core.system_version.service.SystemVersionService;
import org.kreyzon.springops.setup.domain.Setup;
import org.kreyzon.springops.setup.service.SetupService;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

    private ApplicationDto validDto() {
        return new ApplicationDto(
                1, "MyApp", null, "desc", Instant.now(),
                10, 11, "https://git", 8080,
                "512m", "1024m"
        );
    }

    private void mockVersionSystems() {
        Instant now = Instant.now();
        when(systemVersionService.findById(10)).thenReturn(new SystemVersionDto(10, "maven", "3.9.2", "/opt/maven", now, "Maven"));
        when(systemVersionService.findById(11)).thenReturn(new SystemVersionDto(11, "java", "21", "/opt/java", now, "Java"));
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

    @Test
    void save_shouldThrowIfNameExists() {
        ApplicationDto dto = validDto();
        mockVersionSystems();

        when(applicationRepository.existsByName("MyApp")).thenReturn(true);

        assertThrows(SpringOpsException.class, () -> applicationService.save(dto));
    }
    @Test
    void update_shouldThrowIfAppIsRunning() {
        ApplicationDto dto = validDto();

        // Mock valid SystemVersionDto objects
        mockVersionSystems();

        Application existing = new Application();
        existing.setId(1);
        existing.setName("MyApp");

        when(applicationRepository.findById(1)).thenReturn(Optional.of(existing));
        when(applicationRepository.existsById(1)).thenReturn(true);
        when(deploymentManagerService.getDeploymentStatus(1)).thenReturn(new DeploymentStatusDto(true));

        assertThrows(SpringOpsException.class, () -> applicationService.update(1, dto));
    }

    @Test
    void update_shouldThrowIfNameExistsForAnotherApp() {
        mockVersionSystems();

        ApplicationDto dto = new ApplicationDto(
                2, "OtherApp", "valid-path", "desc", Instant.now(),
                10, 11, "https://git", 8080,
                "512m", "1024m"
        );

        Application existing = new Application();
        existing.setId(2);
        existing.setName("OldApp");

        when(applicationRepository.findById(2)).thenReturn(Optional.of(existing));
        when(applicationRepository.existsById(2)).thenReturn(true);
        when(deploymentManagerService.getDeploymentStatus(2)).thenReturn(new DeploymentStatusDto(false));
        when(applicationRepository.existsByName("OtherApp")).thenReturn(true);

        assertThrows(SpringOpsException.class, () -> applicationService.update(2, dto));
    }

    @Test
    void save_shouldSucceed() {
        ApplicationDto dto = validDto();
        mockVersionSystems();
        when(applicationRepository.existsByName("MyApp")).thenReturn(false);
        when(applicationConfig.getRootDirectoryName()).thenReturn("root");
        when(applicationConfig.getDirectoryApplications()).thenReturn("apps");
        when(applicationConfig.getDirectorySource()).thenReturn("src");
        when(applicationConfig.getDirectoryBackups()).thenReturn("backup");
        when(applicationConfig.getDirectoryApplicationLogs()).thenReturn("logs");
        when(setupService.getSetup()).thenReturn(
                Setup.builder()
                        .id(UUID.randomUUID())
                        .tenantName("base")
                        .isSetupComplete(true)
                        .createdAt(Instant.now())
                        .filesRoot("/tmp/springops")
                        .isFilesRootInitialized(true)
                        .isFirstAdminInitialized(true)
                        .ipAddress("127.0.0.1")
                        .serverName("test-server")
                        .environment("test")
                        .build()
        );

        Application saved = ApplicationDto.toEntity(dto);
        saved.setId(1);
        when(applicationRepository.save(any())).thenReturn(saved);

        ApplicationDto result = applicationService.save(dto);
        assertEquals(1, result.getId());
    }
}
