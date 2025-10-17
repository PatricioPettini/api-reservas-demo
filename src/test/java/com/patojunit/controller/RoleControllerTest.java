package com.patojunit.controller;

import com.patojunit.model.Permission;
import com.patojunit.model.Role;
import com.patojunit.service.IPermissionService;
import com.patojunit.service.IRoleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RoleControllerTest {

    @Mock
    private IRoleService roleService;

    @Mock
    private IPermissionService permissionService;

    @InjectMocks
    private RoleController roleController;

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
    void getAllRoles_DeberiaRetornarListaYStatusOk() {
        when(roleService.findAll()).thenReturn(Arrays.asList(role1, role2));

        ResponseEntity<List<Role>> response = roleController.getAllRoles();

        assertEquals(200, response.getStatusCode().value());
        assertEquals(2, response.getBody().size());
        verify(roleService, times(1)).findAll();
    }

    @Test
    void getRoleById_DeberiaRetornarOkSiExiste() {
        when(roleService.findById(1L)).thenReturn(Optional.of(role1));

        ResponseEntity<Role> response = roleController.getRoleById(1L);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("ADMIN", response.getBody().getName());
        verify(roleService, times(1)).findById(1L);
    }

    @Test
    void getRoleById_DeberiaRetornarNotFoundSiNoExiste() {
        when(roleService.findById(99L)).thenReturn(Optional.empty());

        ResponseEntity<Role> response = roleController.getRoleById(99L);

        assertEquals(404, response.getStatusCode().value());
        assertNull(response.getBody());
        verify(roleService, times(1)).findById(99L);
    }

    @Test
    void createRole_DeberiaGuardarRolConPermisosExistentes() {
        Role newRole = new Role(null, "MANAGER", Set.of(permission1, permission2));
        when(permissionService.findById(1L)).thenReturn(Optional.of(permission1));
        when(permissionService.findById(2L)).thenReturn(Optional.of(permission2));
        when(roleService.save(any(Role.class))).thenReturn(new Role(3L, "MANAGER", Set.of(permission1, permission2)));

        ResponseEntity<Role> response = roleController.createRole(newRole);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("MANAGER", response.getBody().getName());
        assertEquals(2, response.getBody().getPermissionsList().size());
        verify(roleService, times(1)).save(any(Role.class));
    }

    @Test
    void updateRole_DeberiaActualizarRolExistente() {
        Role updated = new Role(1L, "ADMIN_UPDATED", Set.of(permission1));

        when(roleService.findById(1L)).thenReturn(Optional.of(role1));
        when(roleService.update(any(Role.class))).thenReturn(updated);

        ResponseEntity<Role> response = roleController.updateRole(1L, updated);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("ADMIN_UPDATED", response.getBody().getName());
        verify(roleService, times(1)).update(updated);
    }

    @Test
    void updateRole_DeberiaDevolverOkAunqueNoExista() {
        Role updated = new Role(99L, "NO_EXISTE", Set.of());
        when(roleService.findById(99L)).thenReturn(Optional.empty());

        ResponseEntity<Role> response = roleController.updateRole(99L, updated);

        assertEquals(200, response.getStatusCode().value());
        verify(roleService, times(1)).update(null);
    }
}
