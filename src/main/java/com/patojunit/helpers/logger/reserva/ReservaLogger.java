package com.patojunit.helpers.logger.reserva;

import com.patojunit.model.Reserva;
import com.patojunit.model.UserSec;
import com.patojunit.model.enums.EstadoReserva;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ReservaLogger {

    public void logCreacionInicio(UserSec usuario) {
        log.info("[ReservaService] Iniciando creación de reserva para el usuario '{}'", usuario.getUsername());
    }

    public void logCreacionExitosa(Reserva reserva) {
        log.info("[ReservaService] Reserva creada exitosamente (ID={}, usuario={}, estado={}, fechaInicio={}, fechaFin={})",
                reserva.getId(),
                reserva.getUsuario().getUsername(),
                reserva.getEstado(),
                reserva.getFechaInicio(),
                reserva.getFechaFin());
    }

    public void logEdicionInicio(Long id) {
        log.debug("[ReservaService] Iniciando edición de la reserva ID={}", id);
    }

    public void logEdicionExitosa(Reserva reserva) {
        log.info("[ReservaService] Reserva ID={} editada correctamente (nuevo estado={}, nuevo total={})",
                reserva.getId(),
                reserva.getEstado(),
                reserva.getPrecioTotal());
    }

    public void logCancelacionInicio(Long id) {
        log.warn("[ReservaService] Cancelando reserva ID={} ...", id);
    }

    public void logCancelacionExitosa(Reserva reserva) {
        log.warn("[ReservaService] Reserva ID={} cancelada exitosamente. Stock restablecido.", reserva.getId());
    }

    public void logEliminacionInicio(Long id) {
        log.warn("[ReservaService] Eliminando reserva ID={} ...", id);
    }

    public void logEliminacionExitosa(Long id) {
        log.info("[ReservaService] Reserva ID={} eliminada correctamente del sistema.", id);
    }

    public void logProductosEliminados(Long reservaId, int cantidadProductos) {
        log.debug("[ReservaService] {} productos eliminados de la reserva ID={}", cantidadProductos, reservaId);
    }

    public void logCambioEstado(Reserva reserva, EstadoReserva estadoAnterior, EstadoReserva nuevoEstado) {
        log.debug("[ReservaService] Estado de reserva ID={} cambiado de {} → {}",
                reserva.getId(), estadoAnterior, nuevoEstado);
    }

    public void logError(Reserva reserva, Exception e) {
        log.error("[ReservaService] Error en la reserva ID={}: {}", reserva.getId(), e.getMessage(), e);
    }

    public void logErrorGeneral(String mensaje, Exception e) {
        log.error("[ReservaService] {}", mensaje, e);
    }
}
