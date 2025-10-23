package com.patojunit.service.scheduler;

import com.patojunit.helpers.ReservaStockHandler;
import com.patojunit.model.Reserva;
import com.patojunit.model.enums.EstadoReserva;
import com.patojunit.repository.IReservaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReservaScheduler {

    private final IReservaRepository reservaRepository;
    private final ReservaStockHandler stockHandler;

    @Scheduled(fixedRate = 60000)
    public void manejarReservasPorFecha() {
        LocalDateTime ahora = LocalDateTime.now();

        try {
            activarReservasPendientes(ahora);
            finalizarReservasActivas(ahora);
        } catch (Exception e) {
            log.error("[ReservaScheduler] Error general en scheduler: {}", e.getMessage(), e);
        }
    }

    private void activarReservasPendientes(LocalDateTime ahora) {
        List<Reserva> pendientes = reservaRepository.findByEstadoAndFechaInicioBefore(
                EstadoReserva.PENDIENTE, ahora
        );

        pendientes.forEach(reserva -> {
            try {
                stockHandler.descontarStockProductos(reserva);
                reserva.setEstado(EstadoReserva.ACTIVA);
                reservaRepository.save(reserva);

                log.info("[ReservaScheduler] Reserva ID={} activada. Stock descontado correctamente.",
                        reserva.getId());
            } catch (Exception e) {
                log.error("[ReservaScheduler] Error al activar reserva ID={}: {}", reserva.getId(), e.getMessage());
            }
        });
    }

    private void finalizarReservasActivas(LocalDateTime ahora) {
        List<Reserva> activas = reservaRepository.findByEstadoAndFechaFinBefore(
                EstadoReserva.ACTIVA, ahora
        );

        activas.forEach(reserva -> {
            try {
                stockHandler.restablecerStockProductos(reserva);
                reserva.setEstado(EstadoReserva.FINALIZADA);
                reservaRepository.save(reserva);

                log.info("[ReservaScheduler] Reserva ID={} finalizada. Stock restablecido correctamente.",
                        reserva.getId());
            } catch (Exception e) {
                log.error("[ReservaScheduler] Error al finalizar reserva ID={}: {}", reserva.getId(), e.getMessage());
            }
        });
    }
}