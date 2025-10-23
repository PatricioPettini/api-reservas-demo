package com.patojunit.controller;

import com.patojunit.dto.request.AuthLoginRequestDTO;
import com.patojunit.dto.response.AuthResponseDTO;
import com.patojunit.service.implementations.UserDetailsServiceImp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class AuthenticationControllerTest {

    @Mock
    private UserDetailsServiceImp userDetailsService;

    @InjectMocks
    private AuthenticationController authenticationController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void login_DeberiaRetornarOkYResponseDTO() {
        // Arrange
        AuthLoginRequestDTO request = new AuthLoginRequestDTO("juan","1234");

        AuthResponseDTO expectedResponse = new AuthResponseDTO(
                "juan",
                "Login exitoso",
                "token123",
                true
        );
        when(userDetailsService.loginUser(request)).thenReturn(expectedResponse);

        // Act
        ResponseEntity<AuthResponseDTO> response = authenticationController.login(request);

        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertEquals(expectedResponse, response.getBody());
        verify(userDetailsService, times(1)).loginUser(request);
    }
}
