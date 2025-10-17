package com.patojunit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.patojunit.dto.AuthLoginRequestDTO;
import com.patojunit.dto.AuthResponseDTO;
import com.patojunit.security.config.TestSecurityConfig;
import com.patojunit.service.UserDetailsServiceImp;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthenticationController.class)
@Import(TestSecurityConfig.class)
class AuthenticationControllerIntTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserDetailsServiceImp userDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void login_DeberiaRetornarOkYToken() throws Exception {
        // Arrange
        AuthResponseDTO expectedResponse = new AuthResponseDTO(
                "juan",           // username
                "Login exitoso",  // message
                "token123",       // jwt
                true              // status
        );        Mockito.when(userDetailsService.loginUser(any(AuthLoginRequestDTO.class)))
                .thenReturn(expectedResponse);

        AuthLoginRequestDTO requestDTO = new AuthLoginRequestDTO("juan","1234");

        // Act & Assert
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jwt").value("token123"))
                .andExpect(jsonPath("$.username").value("juan"));
    }
}
