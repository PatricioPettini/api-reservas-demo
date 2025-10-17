package com.patojunit.controller;

import com.patojunit.model.Reserva;
import com.patojunit.service.IReservaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservaControllerTest {

    @Mock
    private IReservaService reservaService;

    @InjectMocks
    private ReservaController reservaController;

    private Reserva reserva1;
    private Reserva reserva2;

    @BeforeEach
    void setUp() {
        reserva1 = new Reserva();
        reserva1.setId(1L);
        reserva1.setTelefonoCliente("1122334455");
        reserva1.setPagado(true);
        reserva1.setFechaInicio(LocalDateTime.now().plusDays(1));
        reserva1.setFechaFin(LocalDateTime.now().plusDays(2));

        reserva2 = new Reserva();
        reserva2.setId(2L);
        reserva2.setTelefonoCliente("11560455");
        reserva2.setPagado(false);
        reserva2.setFechaInicio(LocalDateTime.now().plusDays(2));
        reserva2.setFechaFin(LocalDateTime.now().plusDays(3));
    }

    @Test
    void getAllReservas_DeberiaRetornarListaDeReservas() {
        // Arrange
        List<Reserva> reservas = List.of(reserva1, reserva2);
        when(reservaService.getAll()).thenReturn(reservas);

        // Act
        List<Reserva> resultado = reservaController.getAllReservas();

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals("1122334455", resultado.get(0).getTelefonoCliente());
        verify(reservaService, times(1)).getAll();
    }

    @Test
    void getReserva_DeberiaRetornarReservaPorId() {
        // Arrange
        when(reservaService.get(1L)).thenReturn(reserva1);

        // Act
        Reserva resultado = reservaController.getReserva(1L);

        // Assert
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("1122334455", resultado.getTelefonoCliente());
        verify(reservaService, times(1)).get(1L);
    }

    @Test
    void cancelarReserva_DeberiaCancelarReservaExistente() {
        // Arrange
        Long id = 1L;
        reserva1.setEstado("cancelado");
        when(reservaService.cancelarReserva(id)).thenReturn(reserva1);

        // Act
        Reserva resultado = reservaController.cancelarReserva(id);

        // Assert
        assertNotNull(resultado);
        assertEquals("cancelado", resultado.getEstado());
        verify(reservaService, times(1)).cancelarReserva(id);
    }

    @Test
    void eliminarReserva_DeberiaEliminarReservaExistente() {
        // Arrange
        Long id = 1L;
        doNothing().when(reservaService).eliminar(id);

        // Act
        String resultado = reservaController.eliminarReserva(id);

        // Assert
        assertEquals("reserva eliminada!", resultado);
        verify(reservaService, times(1)).eliminar(id);
    }

    @Test
    void crearReserva_DeberiaRetornarReservaCreada() {
        // Arrange
        Reserva nuevaReserva = new Reserva();
        nuevaReserva.setTelefonoCliente("1122334455");
        nuevaReserva.setPagado(true);
        nuevaReserva.setFechaInicio(LocalDateTime.now().plusDays(1));
        nuevaReserva.setFechaFin(LocalDateTime.now().plusDays(2));

        when(reservaService.crear(nuevaReserva)).thenReturn(nuevaReserva);

        // Act
        Reserva resultado = reservaController.crearReserva(nuevaReserva);

        // Assert
        assertNotNull(resultado);
        assertEquals("1122334455", resultado.getTelefonoCliente());
        assertTrue(resultado.getPagado());
        verify(reservaService, times(1)).crear(nuevaReserva);
    }

    @Test
    void editarReserva_DeberiaActualizarReservaExistente() {
        // Arrange
        Long id = 1L;

        Reserva reservaEditada = new Reserva();
        reservaEditada.setTelefonoCliente("1199887766");
        reservaEditada.setPagado(false);
        reservaEditada.setFechaInicio(LocalDateTime.now().plusDays(3));
        reservaEditada.setFechaFin(LocalDateTime.now().plusDays(4));

        when(reservaService.editar(id, reservaEditada)).thenReturn(reservaEditada);

        // Act
        Reserva resultado = reservaController.editarReserva(id, reservaEditada);

        // Assert
        assertNotNull(resultado);
        assertEquals("1199887766", resultado.getTelefonoCliente());
        assertFalse(resultado.getPagado());
        verify(reservaService, times(1)).editar(id, reservaEditada);
    }

}
