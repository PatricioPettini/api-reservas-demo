package com.patojunit.security.config.filter;

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.patojunit.utils.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtTokenValidatorTest {

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private DecodedJWT decodedJWT;

    @Mock
    private Claim claimAuthorities;

    private JwtTokenValidator jwtTokenValidator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        jwtTokenValidator = new JwtTokenValidator(jwtUtils);
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_DeberiaAutenticarUsuarioCuandoTokenValido() throws ServletException, IOException {
        // Arrange
        String token = "Bearer valid.token.jwt";
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(token);
        when(jwtUtils.validateToken("valid.token.jwt")).thenReturn(decodedJWT);
        when(jwtUtils.extractUsername(decodedJWT)).thenReturn("juan");
        when(jwtUtils.getSpecificClaim(decodedJWT, "authorities")).thenReturn(claimAuthorities);
        when(claimAuthorities.asString()).thenReturn("ROLE_ADMIN,ROLE_USER");

        // Act
        jwtTokenValidator.doFilterInternal(request, response, filterChain);

        // Assert
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertEquals("juan", auth.getName());
        assertTrue(auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void doFilterInternal_DeberiaContinuarCadenaSiNoHayToken() throws ServletException, IOException {
        // Arrange
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(null);

        // Act
        jwtTokenValidator.doFilterInternal(request, response, filterChain);

        // Assert
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain, times(1)).doFilter(request, response);
        verifyNoInteractions(jwtUtils);
    }

    @Test
    void doFilterInternal_DeberiaContinuarSiTokenSinFormatoBearer() throws ServletException, IOException {
        // Arrange
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("invalidtoken");

        // Act
        jwtTokenValidator.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain, times(1)).doFilter(request, response);
        // No debe haberse intentado validar token
        verify(jwtUtils, never()).validateToken(anyString());
    }
}
