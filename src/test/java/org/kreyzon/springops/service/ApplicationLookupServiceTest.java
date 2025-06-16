package org.kreyzon.springops.service;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kreyzon.springops.common.exception.SpringOpsException;
import org.kreyzon.springops.core.application.entity.Application;
import org.kreyzon.springops.core.application.repository.ApplicationRepository;
import org.kreyzon.springops.common.utils.PortUtils;
import org.kreyzon.springops.core.application.service.ApplicationLookupService;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicationLookupServiceTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @InjectMocks
    private ApplicationLookupService applicationLookupService;

    @Test
    void findEntityById_shouldReturnApplicationIfFound() {
        Application app = new Application();
        app.setId(1);
        when(applicationRepository.findById(1)).thenReturn(Optional.of(app));

        Application result = applicationLookupService.findEntityById(1);

        assertEquals(1, result.getId());
    }

    @Test
    void findEntityById_shouldThrowIfNotFound() {
        when(applicationRepository.findById(1)).thenReturn(Optional.empty());

        SpringOpsException ex = assertThrows(SpringOpsException.class,
                () -> applicationLookupService.findEntityById(1));

        assertEquals("Application with ID '1' does not exist", ex.getMessage());
    }

    @Test
    void isPortAlreadyInUseByOtherApplications_shouldThrowIfPortInUseInDbAndOs() {
        int port = 8080;
        Application app = new Application();
        app.setPort(port);

        when(applicationRepository.findAll()).thenReturn(List.of(app));

        try (MockedStatic<PortUtils> portUtilsMockedStatic = mockStatic(PortUtils.class)) {
            portUtilsMockedStatic.when(() -> PortUtils.validatePort(port)).thenCallRealMethod();
            portUtilsMockedStatic.when(() -> PortUtils.isPortOccupied(port)).thenReturn(true);

            SpringOpsException ex = assertThrows(SpringOpsException.class,
                    () -> applicationLookupService.isPortAlreadyInUseByOtherApplications(port));

            assertTrue(ex.getMessage().contains("already in use"));
        }
    }

    @Test
    void isPortAlreadyInUseByOtherApplications_shouldReturnFalseIfNotInUse() {
        int port = 8080;

        when(applicationRepository.findAll()).thenReturn(List.of());

        try (MockedStatic<PortUtils> portUtilsMockedStatic = mockStatic(PortUtils.class)) {
            portUtilsMockedStatic.when(() -> PortUtils.validatePort(port)).thenCallRealMethod();
            portUtilsMockedStatic.when(() -> PortUtils.isPortOccupied(port)).thenReturn(false);

            Boolean result = applicationLookupService.isPortAlreadyInUseByOtherApplications(port);

            assertFalse(result);
        }
    }
}

