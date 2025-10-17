package com.patojunit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.patojunit.model.Permission;
import com.patojunit.security.config.TestSecurityConfig;
import com.patojunit.service.IPermissionService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PermissionController.class)
@Import(TestSecurityConfig.class)
class PermissionControllerIntTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IPermissionService permissionService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getAllPermissions_DeberiaRetornarListaYStatusOk() throws Exception {
        Mockito.when(permissionService.findAll()).thenReturn(
                Arrays.asList(
                        new Permission(1L, "READ_PRIVILEGES"),
                        new Permission(2L, "WRITE_PRIVILEGES")
                )
        );

        mockMvc.perform(get("/api/permissions")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].permissionName").value("READ_PRIVILEGES"))
                .andExpect(jsonPath("$[1].permissionName").value("WRITE_PRIVILEGES"));
    }

    @Test
    void getPermissionById_DeberiaRetornarOkSiExiste() throws Exception {
        Mockito.when(permissionService.findById(1L))
                .thenReturn(Optional.of(new Permission(1L, "READ_PRIVILEGES")));

        mockMvc.perform(get("/api/permissions/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.permissionName").value("READ_PRIVILEGES"));
    }

    @Test
    void getPermissionById_DeberiaRetornarNotFoundSiNoExiste() throws Exception {
        Mockito.when(permissionService.findById(99L))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/permissions/99")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void createPermission_DeberiaRetornarOkYObjetoCreado() throws Exception {
        Permission savedPermission = new Permission(3L, "DELETE_PRIVILEGES");

        Mockito.when(permissionService.save(any(Permission.class)))
                .thenReturn(savedPermission);

        mockMvc.perform(post("/api/permissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new Permission(null, "DELETE_PRIVILEGES"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.permissionName").value("DELETE_PRIVILEGES"));
    }
}
