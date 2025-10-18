package com.patojunit.controller;

import com.patojunit.dto.request.ReservaCrearEditarDTO;
import com.patojunit.dto.response.ReservaGetDTO;
import com.patojunit.service.IReservaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservaControllerTest {

    @Mock
    private IReservaService reservaService;

    @InjectMocks
    private ReservaController reservaController;

    private ReservaGetDTO reservaGet;
    private ReservaCrearEditarDTO crearEditar;

    @BeforeEach
    void setUp() {
        reservaGet = new ReservaGetDTO();
        reservaGet.setId(1L);
        reservaGet.setCodigoReserva("RES-12345-ABCD");
        reservaGet.setTelefonoCliente("1122334455");
        reservaGet.setEstado("reservado");
        reservaGet.setPagado(true);
        reservaGet.setPrecioTotal(BigDecimal.valueOf(500));
        reservaGet.setFechaInicio(LocalDateTime.now());
        reservaGet.setFechaFin(LocalDateTime.now().plusHours(2));

        crearEditar = new ReservaCrearEditarDTO();
        crearEditar.setTelefonoCliente("1122334455");
        crearEditar.setPagado(true);
        crearEditar.setFechaInicio(LocalDateTime.now());
        crearEditar.setFechaFin(LocalDateTime.now().plusHours(2));
    }

    @Test
    void getAllReservas_deberiaRetornarLista() {
        when(reservaService.getAll()).thenReturn(List.of(reservaGet));

        var result = reservaController.getAllReservas();

        assertEquals(1, result.size());
        assertEquals("RES-12345-ABCD", result.get(0).getCodigoReserva());
        verify(reservaService, times(1)).getAll();
    }

    @Test
    void getReserva_porId_deberiaRetornarDTO() {
        when(reservaService.get(1L)).thenReturn(reservaGet);

        var result = reservaController.getReserva(1L);

        assertEquals("en curso", result.getEstadoActual());
        assertEquals("1122334455", result.getTelefonoCliente());
        verify(reservaService, times(1)).get(1L);
    }

    @Test
    void crearReserva_deberiaRetornarReservaCreada() {
        when(reservaService.crear(any(ReservaCrearEditarDTO.class))).thenReturn(reservaGet);

        var result = reservaController.crearReserva(crearEditar);

        assertEquals("en curso", result.getEstadoActual());
        assertEquals(BigDecimal.valueOf(500), result.getPrecioTotal());
        verify(reservaService, times(1)).crear(any(ReservaCrearEditarDTO.class));
    }

    @Test
    void editarReserva_deberiaRetornarReservaEditada() {
        ReservaGetDTO editada = new ReservaGetDTO();
        editada.setId(1L);
        editada.setEstado("devuelto");
        editada.setFechaInicio(LocalDateTime.now().plusHours(-3));
        editada.setFechaFin(LocalDateTime.now().plusHours(-2));
        editada.setTelefonoCliente("1199887766");
        editada.setPrecioTotal(BigDecimal.valueOf(300));

        when(reservaService.editar(eq(1L), any(ReservaCrearEditarDTO.class))).thenReturn(editada);

        var result = reservaController.editarReserva(1L, crearEditar);

        assertEquals("devuelto", result.getEstadoActual());
        assertEquals("1199887766", result.getTelefonoCliente());
        verify(reservaService, times(1)).editar(eq(1L), any(ReservaCrearEditarDTO.class));
    }

    @Test
    void cancelarReserva_deberiaRetornarReservaCancelada() {
        ReservaGetDTO cancelada = new ReservaGetDTO();
        cancelada.setId(1L);
        cancelada.setEstado("cancelado");
        cancelada.setTelefonoCliente("1122334455");

        when(reservaService.cancelarReserva(1L)).thenReturn(cancelada);

        var result = reservaController.cancelarReserva(1L);

        assertEquals("cancelado", result.getEstadoActual());
        verify(reservaService, times(1)).cancelarReserva(1L);
    }

    @Test
    void eliminarReserva_deberiaRetornarMensajeYEliminar() {
        String mensaje = reservaController.eliminarReserva(1L);

        assertEquals("reserva eliminada!", mensaje);
        verify(reservaService, times(1)).eliminar(1L);
    }
}
