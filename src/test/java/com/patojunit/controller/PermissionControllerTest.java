package com.patojunit.controller;

import com.patojunit.model.Permission;
import com.patojunit.service.IPermissionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PermissionControllerTest {

    @Mock
    private IPermissionService permissionService;

    @InjectMocks
    private PermissionController permissionController;

    private Permission permission1;
    private Permission permission2;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        permission1 = new Permission(1L, "READ_PRIVILEGES");
        permission2 = new Permission(2L, "WRITE_PRIVILEGES");
    }

    @Test
    void getAllPermissions_DeberiaRetornarListaYStatusOk() {
        // Arrange
        when(permissionService.findAll()).thenReturn(Arrays.asList(permission1, permission2));

        // Act
        ResponseEntity<List<Permission>> response = permissionController.getAllPermissions();

        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertEquals(2, response.getBody().size());
        assertEquals("READ_PRIVILEGES", response.getBody().get(0).getPermissionName());
        verify(permissionService, times(1)).findAll();
    }

    @Test
    void getPermissionById_DeberiaRetornarOkSiExiste() {
        // Arrange
        when(permissionService.findById(1L)).thenReturn(Optional.of(permission1));

        // Act
        ResponseEntity<Permission> response = permissionController.getPermissionById(1L);

        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertEquals("READ_PRIVILEGES", response.getBody().getPermissionName());
        verify(permissionService, times(1)).findById(1L);
    }

    @Test
    void getPermissionById_DeberiaRetornarNotFoundSiNoExiste() {
        // Arrange
        when(permissionService.findById(99L)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<Permission> response = permissionController.getPermissionById(99L);

        // Assert
        assertEquals(404, response.getStatusCode().value());
        assertNull(response.getBody());
        verify(permissionService, times(1)).findById(99L);
    }

    @Test
    void createPermission_DeberiaGuardarYRetornarOk() {
        // Arrange
        Permission newPermission = new Permission(null, "DELETE_PRIVILEGES");
        Permission savedPermission = new Permission(3L, "DELETE_PRIVILEGES");
        when(permissionService.save(newPermission)).thenReturn(savedPermission);

        // Act
        ResponseEntity<Permission> response = permissionController.createPermission(newPermission);

        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertEquals("DELETE_PRIVILEGES", response.getBody().getPermissionName());
        assertEquals(3L, response.getBody().getId());
        verify(permissionService, times(1)).save(newPermission);
    }
}
