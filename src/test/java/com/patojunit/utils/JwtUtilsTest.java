package com.patojunit.utils;

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilsTest {

    private JwtUtils jwtUtils;

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();

        ReflectionTestUtils.setField(jwtUtils, "privateKey", "mySuperSecretKey123");
        ReflectionTestUtils.setField(jwtUtils, "userGenerator", "testUserGenerator");
    }

    @Test
    void createToken_Y_ValidateToken_DeberianGenerarYValidarCorrectamente() {
        // Arrange
        var authorities = List.of(new SimpleGrantedAuthority("ROLE_ADMIN"), new SimpleGrantedAuthority("ROLE_USER"));
        var authentication = new UsernamePasswordAuthenticationToken("juan", null, authorities);

        // Act
        String token = jwtUtils.createToken(authentication);
        assertNotNull(token);
        assertTrue(token.startsWith("ey"));

        // Validate
        DecodedJWT decoded = jwtUtils.validateToken(token);

        // Assert
        assertEquals("juan", decoded.getSubject());
        assertEquals("testUserGenerator", decoded.getIssuer());
        assertNotNull(decoded.getClaim("authorities"));
    }

    @Test
    void validateToken_DeberiaLanzarExcepcionSiTokenInvalido() {
        // Arrange
        String invalidToken = "bad.token.value";

        // Act & Assert
        assertThrows(Exception.class, () -> jwtUtils.validateToken(invalidToken));
    }

    @Test
    void extractUsername_DeberiaDevolverSubjectDelToken() {
        // Arrange
        var authorities = List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
        var authentication = new UsernamePasswordAuthenticationToken("maria", null, authorities);
        String token = jwtUtils.createToken(authentication);
        DecodedJWT decoded = jwtUtils.validateToken(token);

        // Act
        String username = jwtUtils.extractUsername(decoded);

        // Assert
        assertEquals("maria", username);
    }

    @Test
    void getSpecificClaim_DeberiaDevolverClaimCorrecto() {
        // Arrange
        var authorities = List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
        var authentication = new UsernamePasswordAuthenticationToken("lucas", null, authorities);
        String token = jwtUtils.createToken(authentication);
        DecodedJWT decoded = jwtUtils.validateToken(token);

        // Act
        Claim claim = jwtUtils.getSpecificClaim(decoded, "authorities");

        // Assert
        assertNotNull(claim);
        assertEquals("ROLE_ADMIN", claim.asString());
    }

    @Test
    void returnAllClaims_DeberiaDevolverMapaDeClaims() {
        // Arrange
        var authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        var authentication = new UsernamePasswordAuthenticationToken("roberto", null, authorities);
        String token = jwtUtils.createToken(authentication);
        DecodedJWT decoded = jwtUtils.validateToken(token);

        // Act
        Map<String, Claim> claims = jwtUtils.returnAllClaims(decoded);

        // Assert
        assertTrue(claims.containsKey("authorities"));
        assertEquals("ROLE_USER", claims.get("authorities").asString());
    }
}
