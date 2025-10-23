package com.patojunit.service.implementations;

import com.patojunit.model.Permission;
import com.patojunit.model.Role;
import com.patojunit.repository.IRoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RoleServiceTest {

    @Mock
    private IRoleRepository roleRepository;

    @InjectMocks
    private RoleService roleService;

    private Role role1;
    private Role role2;
    private Permission permission1;
    private Permission permission2;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        permission1 = new Permission(1L, "READ_PRIVILEGES");
        permission2 = new Permission(2L, "WRITE_PRIVILEGES");

        role1 = new Role(1L, "ADMIN", Set.of(permission1));
        role2 = new Role(2L, "USER", Set.of(permission2));
    }

    @Test
    void findAll_DeberiaRetornarListaDeRoles() {
        // Arrange
        when(roleRepository.findAll()).thenReturn(Arrays.asList(role1, role2));

        // Act
        List<Role> result = roleService.findAll();

        // Assert
        assertEquals(2, result.size());
        assertEquals("ADMIN", result.get(0).getName());
        verify(roleRepository, times(1)).findAll();
    }

    @Test
    void findById_DeberiaRetornarOptionalConRol() {
        // Arrange
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role1));

        // Act
        Optional<Role> result = roleService.findById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("ADMIN", result.get().getName());
        verify(roleRepository, times(1)).findById(1L);
    }

    @Test
    void findById_DeberiaRetornarOptionalVacioSiNoExiste() {
        // Arrange
        when(roleRepository.findById(99L)).thenReturn(Optional.empty());

        // Act
        Optional<Role> result = roleService.findById(99L);

        // Assert
        assertFalse(result.isPresent());
        verify(roleRepository, times(1)).findById(99L);
    }

    @Test
    void save_DeberiaGuardarYRetornarRol() {
        // Arrange
        Role newRole = new Role(null, "MANAGER", Set.of(permission1, permission2));
        Role savedRole = new Role(3L, "MANAGER", Set.of(permission1, permission2));
        when(roleRepository.save(newRole)).thenReturn(savedRole);

        // Act
        Role result = roleService.save(newRole);

        // Assert
        assertEquals(3L, result.getId());
        assertEquals("MANAGER", result.getName());
        verify(roleRepository, times(1)).save(newRole);
    }

    @Test
    void deleteById_DeberiaLlamarAlRepositorio() {
        // Act
        roleService.deleteById(1L);

        // Assert
        verify(roleRepository, times(1)).deleteById(1L);
    }

    @Test
    void update_DeberiaGuardarRolActualizado() {
        // Arrange
        Role updated = new Role(1L, "ADMIN_UPDATED", Set.of(permission1));
        when(roleRepository.save(updated)).thenReturn(updated);

        // Act
        Role result = roleService.update(updated);

        // Assert
        assertEquals("ADMIN_UPDATED", result.getName());
        verify(roleRepository, times(1)).save(updated);
    }
}
