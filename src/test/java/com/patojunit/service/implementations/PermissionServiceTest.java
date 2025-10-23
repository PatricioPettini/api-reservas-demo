package com.patojunit.service.implementations;

import com.patojunit.model.Permission;
import com.patojunit.repository.IPermissionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PermissionServiceTest {

    @Mock
    private IPermissionRepository permissionRepository;

    @InjectMocks
    private PermissionService permissionService;

    private Permission permission1;
    private Permission permission2;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        permission1 = new Permission(1L, "READ_PRIVILEGES");
        permission2 = new Permission(2L, "WRITE_PRIVILEGES");
    }

    @Test
    void findAll_DeberiaRetornarListaDePermisos() {
        // Arrange
        when(permissionRepository.findAll()).thenReturn(Arrays.asList(permission1, permission2));

        // Act
        List<Permission> result = permissionService.findAll();

        // Assert
        assertEquals(2, result.size());
        assertEquals("READ_PRIVILEGES", result.get(0).getPermissionName());
        verify(permissionRepository, times(1)).findAll();
    }

    @Test
    void findById_DeberiaRetornarOptionalConPermiso() {
        // Arrange
        when(permissionRepository.findById(1L)).thenReturn(Optional.of(permission1));

        // Act
        Optional<Permission> result = permissionService.findById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("READ_PRIVILEGES", result.get().getPermissionName());
        verify(permissionRepository, times(1)).findById(1L);
    }

    @Test
    void findById_DeberiaRetornarOptionalVacioSiNoExiste() {
        // Arrange
        when(permissionRepository.findById(99L)).thenReturn(Optional.empty());

        // Act
        Optional<Permission> result = permissionService.findById(99L);

        // Assert
        assertFalse(result.isPresent());
        verify(permissionRepository, times(1)).findById(99L);
    }

    @Test
    void save_DeberiaGuardarYRetornarPermiso() {
        // Arrange
        Permission newPermission = new Permission(null, "DELETE_PRIVILEGES");
        Permission savedPermission = new Permission(3L, "DELETE_PRIVILEGES");
        when(permissionRepository.save(newPermission)).thenReturn(savedPermission);

        // Act
        Permission result = permissionService.save(newPermission);

        // Assert
        assertEquals(3L, result.getId());
        assertEquals("DELETE_PRIVILEGES", result.getPermissionName());
        verify(permissionRepository, times(1)).save(newPermission);
    }

    @Test
    void deleteById_DeberiaLlamarAlRepositorio() {
        // Act
        permissionService.deleteById(1L);

        // Assert
        verify(permissionRepository, times(1)).deleteById(1L);
    }

    @Test
    void update_DeberiaGuardarPermisoActualizado() {
        // Arrange
        Permission updated = new Permission(1L, "UPDATED_PRIVILEGES");
        when(permissionRepository.save(updated)).thenReturn(updated);

        // Act
        Permission result = permissionService.update(updated);

        // Assert
        assertEquals("UPDATED_PRIVILEGES", result.getPermissionName());
        verify(permissionRepository, times(1)).save(updated);
    }
}
