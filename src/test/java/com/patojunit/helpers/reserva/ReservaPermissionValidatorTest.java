package com.patojunit.helpers.reserva;

import com.patojunit.helpers.ReservaPermissionValidator;
import com.patojunit.helpers.security.JwtRoleValidator;
import com.patojunit.helpers.security.JwtUserProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.access.AccessDeniedException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReservaPermissionValidatorTest {

    @Mock private JwtRoleValidator jwtRoleValidator;
    @Mock private JwtUserProvider jwtUserProvider;

    @InjectMocks
    private ReservaPermissionValidator permissionValidator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(jwtUserProvider.getUsuarioAutenticadoUsername()).thenReturn("juan");
    }

    @Test
    @DisplayName("Debe lanzar excepción si el usuario no tiene roles válidos")
    void validarPermisosGenerales_DeberiaLanzarExcepcionSiSinRoles() {
        when(jwtRoleValidator.isAdmin()).thenReturn(false);
        when(jwtRoleValidator.isUser()).thenReturn(false);

        AccessDeniedException ex = assertThrows(AccessDeniedException.class, () ->
                permissionValidator.validarPermisosGenerales("usuario"));

        assertEquals("No tenés permisos para gestionar reservas.", ex.getMessage());
        verify(jwtUserProvider).getUsuarioAutenticadoUsername();
    }

    @Test
    @DisplayName("Debe permitir acceso si el usuario es ADMIN")
    void validarPermisosGenerales_AdminDebePasar() {
        when(jwtRoleValidator.isAdmin()).thenReturn(true);

        assertDoesNotThrow(() ->
                permissionValidator.validarPermisosGenerales("usuario"));

        verify(jwtUserProvider).getUsuarioAutenticadoUsername();
    }

    @Test
    @DisplayName("Debe permitir acceso si el usuario es USER")
    void validarPermisosGenerales_UserDebePasar() {
        when(jwtRoleValidator.isUser()).thenReturn(true);

        assertDoesNotThrow(() ->
                permissionValidator.validarPermisosGenerales("usuario"));

        verify(jwtUserProvider).getUsuarioAutenticadoUsername();
    }

    @Test
    @DisplayName("Debe permitir acceso si es ADMIN")
    void validarAccesoAReserva_AdminDebePasar() {
        when(jwtRoleValidator.isAdmin()).thenReturn(true);

        assertDoesNotThrow(() ->
                permissionValidator.validarAccesoAReserva("otroUsuario"));

        verify(jwtRoleValidator).isAdmin();
        verifyNoInteractions(jwtUserProvider);
    }

    @Test
    @DisplayName("Debe permitir acceso si el usuario es dueño de la reserva")
    void validarAccesoAReserva_MismoUsuarioDebePasar() {
        when(jwtRoleValidator.isAdmin()).thenReturn(false);
        when(jwtUserProvider.getUsuarioAutenticadoUsername()).thenReturn("juan");

        assertDoesNotThrow(() ->
                permissionValidator.validarAccesoAReserva("juan"));
    }

    @Test
    @DisplayName("Debe lanzar excepción si intenta acceder a una reserva ajena")
    void validarAccesoAReserva_DeberiaLanzarExcepcionSiUsuarioDistinto() {
        when(jwtRoleValidator.isAdmin()).thenReturn(false);
        when(jwtUserProvider.getUsuarioAutenticadoUsername()).thenReturn("pepe");

        AccessDeniedException ex = assertThrows(AccessDeniedException.class, () ->
                permissionValidator.validarAccesoAReserva("juan"));

        assertEquals("No tenés permiso para acceder o modificar esta reserva.", ex.getMessage());
        verify(jwtUserProvider).getUsuarioAutenticadoUsername();
    }
}
