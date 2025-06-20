package org.kreyzon.springops.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kreyzon.springops.auth.service.UserService;
import org.kreyzon.springops.common.dto.auth.AdminUserResponseDto;
import org.kreyzon.springops.common.dto.auth.UserDto;
import org.kreyzon.springops.common.exception.SpringOpsException;
import org.kreyzon.springops.common.utils.FileUtils;
import org.kreyzon.springops.config.ApplicationConfig;
import org.kreyzon.springops.setup.domain.Setup;
import org.kreyzon.springops.setup.repository.SetupRepository;
import org.kreyzon.springops.setup.service.SetupService;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SetupServiceTest {

    @Mock private SetupRepository setupRepository;
    @Mock private UserService userService;
    @Mock private ApplicationConfig applicationConfig;

    @InjectMocks private SetupService setupService;

    private Setup setup;

    @BeforeEach
    void init() {
        setup = new Setup();
        setup.setIsFirstAdminInitialized(false);
        setup.setIsFilesRootInitialized(false);
    }

    @Test
    void initializeSystemInfo_shouldSaveSystemInfo() {
        when(setupRepository.findSetup()).thenReturn(setup);

        Boolean result = setupService.initializeSystemInfo("127.0.0.1", "srv", "dev");

        assertTrue(result);
        verify(setupRepository).save(setup);
        assertEquals("127.0.0.1", setup.getIpAddress());
        assertEquals("srv", setup.getServerName());
        assertEquals("dev", setup.getEnvironment());
    }

    @Test
    void getSetup_shouldReturnSetup() {
        when(setupRepository.findSetup()).thenReturn(setup);

        Setup result = setupService.getSetup();

        assertNotNull(result);
    }

    @Test
    void getSetup_shouldThrowIfNull() {
        when(setupRepository.findSetup()).thenReturn(null);

        assertThrows(SpringOpsException.class, () -> setupService.getSetup());
    }

    @Test
    void initializeFirstAdminUser_shouldCreateAdmin() {
        setup.setIsFirstAdminInitialized(false);
        when(setupRepository.findSetup()).thenReturn(setup);
        when(userService.findAll()).thenReturn(List.of());
        when(applicationConfig.getStandardAdminUsername()).thenReturn("admin");
        when(applicationConfig.getStandardAdminPasswordLength()).thenReturn(10);
        when(applicationConfig.getStandardAdminEmail()).thenReturn("admin@kreyzon.com");

        UserDto savedUser = UserDto.builder().username("admin").email("admin@kreyzon.com").build();
        when(userService.create(any())).thenReturn(savedUser);

        AdminUserResponseDto result = setupService.initializeFirstAdminUser();

        assertEquals("admin", result.getUser().getUsername());
        verify(setupRepository).save(setup);
    }

    @Test
    void initializeFirstAdminUser_shouldThrowIfAlreadyComplete() {
        setup.setIsFirstAdminInitialized(true);
        when(setupRepository.findSetup()).thenReturn(setup);

        assertThrows(SpringOpsException.class, () -> setupService.initializeFirstAdminUser());
    }

    @Test
    void initializeFirstAdminUser_shouldThrowIfUserAlreadyExists() {
        setup.setIsFirstAdminInitialized(false);
        when(setupRepository.findSetup()).thenReturn(setup);
        when(applicationConfig.getStandardAdminUsername()).thenReturn("admin");
        when(userService.findAll()).thenReturn(List.of(UserDto.builder().username("admin").build()));

        assertThrows(SpringOpsException.class, () -> setupService.initializeFirstAdminUser());
    }

    @Test
    void initializeFiles_shouldSucceed() {
        when(setupRepository.findSetup()).thenReturn(setup);
        when(applicationConfig.getRootDirectoryName()).thenReturn("root"); // No trailing slash
        when(applicationConfig.getDirectoryApplications()).thenReturn("apps");
        when(applicationConfig.getDirectoryApplicationLogs()).thenReturn("logs");

        mockStatic(FileUtils.class);
        try (var mockedFiles = mockStatic(Files.class)) {
            when(FileUtils.createDirectory("/root")).thenReturn(true); // Adjusted path
            mockedFiles.when(() -> Files.createDirectories(Path.of("/root/apps"))).thenReturn(Path.of("/root/apps"));
            mockedFiles.when(() -> Files.createDirectories(Path.of("/root/apps/logs"))).thenReturn(Path.of("/root/apps/logs"));

            assertDoesNotThrow(() -> setupService.initializeFiles("/"));
        }
    }
}
