package com.patojunit.helpers;

import com.patojunit.helpers.security.JwtRoleValidator;
import com.patojunit.helpers.security.JwtUserProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReservaPermissionValidator {

    private final JwtRoleValidator jwtRoleValidator;
    private final JwtUserProvider jwtUserProvider;

    public void validarPermisosGenerales(Object usuario) {
        String username = jwtUserProvider.getUsuarioAutenticadoUsername();

        if (!jwtRoleValidator.isAdmin() && !jwtRoleValidator.isUser()) {
            log.warn("Acceso denegado: usuario={} no tiene roles válidos para crear reservas.", username);
            throw new AccessDeniedException("No tenés permisos para gestionar reservas.");
        }

        log.debug("Permiso general validado correctamente para usuario={}", username);
    }

    public void validarAccesoAReserva(String usernameReserva) {
        if (jwtRoleValidator.isAdmin()) return;

        String usernameActual = jwtUserProvider.getUsuarioAutenticadoUsername();

        if (!usernameReserva.equals(usernameActual)) {
            log.warn("Acceso denegado: usuario={} intentó acceder a la reserva de {}", usernameActual, usernameReserva);
            throw new AccessDeniedException("No tenés permiso para acceder o modificar esta reserva.");
        }
    }
}
