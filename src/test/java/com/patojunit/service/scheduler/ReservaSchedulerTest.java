package com.patojunit.service.scheduler;

import com.patojunit.helpers.reserva.ReservaStockHandler;
import com.patojunit.model.Reserva;
import com.patojunit.model.enums.EstadoReserva;
import com.patojunit.repository.IReservaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.slf4j.Logger;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;

class ReservaSchedulerTest {

    @Mock
    private IReservaRepository reservaRepository;

    @Mock
    private ReservaStockHandler stockHandler;

    @Mock
    private Logger log;

    @InjectMocks
    private ReservaScheduler reservaScheduler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Debe activar las reservas pendientes y descontar stock")
    void manejarReservasPorFecha_DeberiaActivarPendientes() {
        // Arrange
        Reserva reservaPendiente = new Reserva();
        reservaPendiente.setId(1L);
        reservaPendiente.setEstado(EstadoReserva.PENDIENTE);
        reservaPendiente.setFechaInicio(LocalDateTime.now().minusMinutes(10));
        reservaPendiente.setFechaFin(LocalDateTime.now().plusHours(1));

        when(reservaRepository.findByEstadoAndFechaInicioBefore(eq(EstadoReserva.PENDIENTE), any()))
                .thenReturn(List.of(reservaPendiente));

        // Act
        reservaScheduler.manejarReservasPorFecha();

        // Assert
        verify(stockHandler).descontarStockProductos(reservaPendiente);
        verify(reservaRepository).save(reservaPendiente);
        assert(reservaPendiente.getEstado() == EstadoReserva.ACTIVA);
    }

    @Test
    @DisplayName("Debe finalizar las reservas activas y restablecer stock")
    void manejarReservasPorFecha_DeberiaFinalizarActivas() {
        // Arrange
        Reserva reservaActiva = new Reserva();
        reservaActiva.setId(2L);
        reservaActiva.setEstado(EstadoReserva.ACTIVA);
        reservaActiva.setFechaInicio(LocalDateTime.now().minusHours(2));
        reservaActiva.setFechaFin(LocalDateTime.now().minusMinutes(5));

        when(reservaRepository.findByEstadoAndFechaFinBefore(eq(EstadoReserva.ACTIVA), any()))
                .thenReturn(List.of(reservaActiva));

        // Act
        reservaScheduler.manejarReservasPorFecha();

        // Assert
        verify(stockHandler).restablecerStockProductos(reservaActiva);
        verify(reservaRepository).save(reservaActiva);
        assert(reservaActiva.getEstado() == EstadoReserva.FINALIZADA);
    }

    @Test
    @DisplayName("Debe manejar errores al descontar stock sin interrumpir el proceso")
    void manejarReservasPorFecha_DeberiaRegistrarErrorActivacion() {
        Reserva reservaPendiente = new Reserva();
        reservaPendiente.setId(3L);
        reservaPendiente.setEstado(EstadoReserva.PENDIENTE);
        reservaPendiente.setFechaInicio(LocalDateTime.now().minusMinutes(1));

        when(reservaRepository.findByEstadoAndFechaInicioBefore(eq(EstadoReserva.PENDIENTE), any()))
                .thenReturn(List.of(reservaPendiente));

        doThrow(new RuntimeException("Fallo de stock"))
                .when(stockHandler).descontarStockProductos(reservaPendiente);

        reservaScheduler.manejarReservasPorFecha();

        verify(reservaRepository, never()).save(reservaPendiente);
    }

    @Test
    @DisplayName("Debe manejar errores al restablecer stock sin interrumpir el proceso")
    void manejarReservasPorFecha_DeberiaRegistrarErrorFinalizacion() {
        Reserva reservaActiva = new Reserva();
        reservaActiva.setId(4L);
        reservaActiva.setEstado(EstadoReserva.ACTIVA);
        reservaActiva.setFechaFin(LocalDateTime.now().minusMinutes(1));

        when(reservaRepository.findByEstadoAndFechaFinBefore(eq(EstadoReserva.ACTIVA), any()))
                .thenReturn(List.of(reservaActiva));

        doThrow(new RuntimeException("Error al restablecer stock"))
                .when(stockHandler).restablecerStockProductos(reservaActiva);

        reservaScheduler.manejarReservasPorFecha();

        verify(reservaRepository, never()).save(reservaActiva);
    }
}
