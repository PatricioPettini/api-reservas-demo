package com.patojunit.service.implementations;

import com.patojunit.dto.request.AuthLoginRequestDTO;
import com.patojunit.dto.response.AuthResponseDTO;
import com.patojunit.model.Permission;
import com.patojunit.model.Role;
import com.patojunit.model.UserSec;
import com.patojunit.repository.IUserRepository;
import com.patojunit.utils.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserDetailsServiceImpTest {

    @Mock
    private IUserRepository userRepo;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserDetailsServiceImp userDetailsService;

    private UserSec userSec;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        Permission permiso1 = new Permission();
        permiso1.setPermissionName("READ_PRIVILEGES");

        Role role1 = new Role();
        role1.setName("ADMIN");
        role1.setPermissionsList(Set.of(permiso1));

        userSec = new UserSec();
        userSec.setUsername("juan");
        userSec.setPassword("encoded123");
        userSec.setEnabled(true);
        userSec.setAccountNotExpired(true);
        userSec.setCredentialNotExpired(true);
        userSec.setAccountNotLocked(true);
        userSec.setRolesList(Set.of(role1));
    }

    @Test
    void loadUserByUsername_DeberiaRetornarUserDetailsConRolesYPermisos() {
        // Arrange
        when(userRepo.findByUsername("juan")).thenReturn(Optional.of(userSec));

        // Act
        UserDetails result = userDetailsService.loadUserByUsername("juan");

        // Assert
        assertEquals("juan", result.getUsername());
        assertEquals("encoded123", result.getPassword());
        assertTrue(result.isEnabled());
        assertTrue(result.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
        assertTrue(result.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("READ_PRIVILEGES")));
        verify(userRepo, times(1)).findByUsername("juan");
    }

    @Test
    void loadUserByUsername_DeberiaLanzarExcepcionSiNoExisteUsuario() {
        // Arrange
        when(userRepo.findByUsername("juan")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class,
                () -> userDetailsService.loadUserByUsername("juan"));
    }

    @Test
    void authenticate_DeberiaRetornarAuthenticationCuandoCredencialesSonValidas() {
        // Arrange
        when(userRepo.findByUsername("juan")).thenReturn(Optional.of(userSec));
        when(passwordEncoder.matches("1234", "encoded123")).thenReturn(true);

        // Act
        var auth = userDetailsService.authenticate("juan", "1234");

        // Assert
        assertTrue(auth instanceof UsernamePasswordAuthenticationToken);
        assertEquals("juan", auth.getPrincipal());
    }

    @Test
    void authenticate_DeberiaLanzarExcepcionCuandoPasswordInvalido() {
        // Arrange
        when(userRepo.findByUsername("juan")).thenReturn(Optional.of(userSec));
        when(passwordEncoder.matches("wrong", "encoded123")).thenReturn(false);

        // Act & Assert
        assertThrows(BadCredentialsException.class,
                () -> userDetailsService.authenticate("juan", "wrong"));
    }

    @Test
    void authenticate_DeberiaLanzarExcepcionCuandoUsuarioNoExiste() {
        // Arrange
        when(userRepo.findByUsername("juan")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class,
                () -> userDetailsService.authenticate("juan", "1234"));
    }

    @Test
    void loginUser_DeberiaRetornarAuthResponseDTOConToken() {
        // Arrange
        AuthLoginRequestDTO loginDTO = new AuthLoginRequestDTO("juan", "1234");

        when(userRepo.findByUsername("juan")).thenReturn(Optional.of(userSec));
        when(passwordEncoder.matches("1234", "encoded123")).thenReturn(true);
        when(jwtUtils.createToken(any())).thenReturn("token123");

        // Act
        AuthResponseDTO response = userDetailsService.loginUser(loginDTO);

        // Assert
        assertEquals("juan", response.username());
        assertEquals("login ok", response.message());
        assertEquals("token123", response.jwt());
        assertTrue(response.status());
        verify(jwtUtils, times(1)).createToken(any());
    }
}
