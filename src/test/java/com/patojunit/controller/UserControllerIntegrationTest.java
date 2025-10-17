package com.patojunit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.patojunit.model.Role;
import com.patojunit.model.UserSec;
import com.patojunit.security.config.TestSecurityConfig;
import com.patojunit.service.IRoleService;
import com.patojunit.service.IUserService;
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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(TestSecurityConfig.class)
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IUserService userService;

    @MockBean
    private IRoleService roleService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = {"ADMIN", "USER"})
    void getAllUsers_DeberiaRetornarListaYStatusOk() throws Exception {
        Mockito.when(userService.findAll()).thenReturn(
                Arrays.asList(
                        new UserSec(1L, "juan", "1234", true, true, true, true, Set.of()),
                        new UserSec(2L, "maria", "abcd", true, true, true, true, Set.of())
                )
        );

        mockMvc.perform(get("/api/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("juan"))
                .andExpect(jsonPath("$[1].username").value("maria"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "USER"})
    void getUserById_DeberiaRetornarOkSiExiste() throws Exception {
        Mockito.when(userService.findById(1L))
                .thenReturn(Optional.of(new UserSec(1L, "juan", "1234", true, true, true, true, Set.of())));

        mockMvc.perform(get("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("juan"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "USER"})
    void getUserById_DeberiaRetornarNotFoundSiNoExiste() throws Exception {
        Mockito.when(userService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/99")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createUser_DeberiaCrearUsuarioConRoles() throws Exception {
        Role roleAdmin = new Role(1L, "ADMIN", Set.of());
        UserSec input = new UserSec(null, "lucas", "pass", true, true, true, true, Set.of(roleAdmin));
        UserSec saved = new UserSec(3L, "lucas", "hashedPass", true, true, true, true, Set.of(roleAdmin));

        Mockito.when(userService.encriptPassword("pass")).thenReturn("hashedPass");
        Mockito.when(roleService.findById(1L)).thenReturn(Optional.of(roleAdmin));
        Mockito.when(userService.save(any(UserSec.class))).thenReturn(saved);

        mockMvc.perform(post("/api/users")
                        .with(csrf()) // ✅ necesario para POST
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("lucas"))
                .andExpect(jsonPath("$.password").value("hashedPass"));
    }
}
