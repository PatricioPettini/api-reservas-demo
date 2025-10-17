package com.patojunit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.patojunit.model.Permission;
import com.patojunit.model.Role;
import com.patojunit.security.config.TestSecurityConfig;
import com.patojunit.service.IPermissionService;
import com.patojunit.service.IRoleService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RoleController.class)
@Import(TestSecurityConfig.class)
class RoleControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IRoleService roleService;

    @MockBean
    private IPermissionService permissionService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = {"ADMIN", "USER"})
    void getAllRoles_DeberiaRetornarListaYStatusOk() throws Exception {
        Mockito.when(roleService.findAll()).thenReturn(
                Arrays.asList(
                        new Role(1L, "ADMIN", Set.of(new Permission(1L, "READ_PRIVILEGES"))),
                        new Role(2L, "USER", Set.of(new Permission(2L, "WRITE_PRIVILEGES")))
                )
        );

        mockMvc.perform(get("/api/roles")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("ADMIN"))
                .andExpect(jsonPath("$[1].name").value("USER"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "USER"})
    void getRoleById_DeberiaRetornarOkSiExiste() throws Exception {
        Mockito.when(roleService.findById(1L))
                .thenReturn(Optional.of(new Role(1L, "ADMIN", Set.of(new Permission(1L, "READ_PRIVILEGES")))));

        mockMvc.perform(get("/api/roles/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("ADMIN"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "USER"})
    void getRoleById_DeberiaRetornarNotFoundSiNoExiste() throws Exception {
        Mockito.when(roleService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/roles/99")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createRole_DeberiaCrearRolConPermisos() throws Exception {
        Permission p1 = new Permission(1L, "READ_PRIVILEGES");
        Permission p2 = new Permission(2L, "WRITE_PRIVILEGES");

        Role input = new Role(null, "MANAGER", Set.of(p1, p2));
        Role saved = new Role(3L, "MANAGER", Set.of(p1, p2));

        Mockito.when(permissionService.findById(1L)).thenReturn(Optional.of(p1));
        Mockito.when(permissionService.findById(2L)).thenReturn(Optional.of(p2));
        Mockito.when(roleService.save(any(Role.class))).thenReturn(saved);

        mockMvc.perform(post("/api/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("MANAGER"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateRole_DeberiaActualizarYRetornarOk() throws Exception {
        Role updated = new Role(1L, "ADMIN_UPDATED", Set.of(new Permission(1L, "READ_PRIVILEGES")));

        Mockito.when(roleService.findById(1L))
                .thenReturn(Optional.of(updated));
        Mockito.when(roleService.update(any(Role.class)))
                .thenReturn(updated);

        mockMvc.perform(patch("/api/roles/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("ADMIN_UPDATED"));
    }
}
