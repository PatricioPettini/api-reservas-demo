package com.patojunit.controller;

import com.patojunit.dto.request.ReservaCrearEditarDTO;
import com.patojunit.dto.response.ReservaUserGetDTO;
import com.patojunit.service.interfaces.IReservaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ReservaControllerTest {

    @Mock
    private IReservaService reservaService;

    @InjectMocks
    private ReservaController reservaController;

    private ReservaUserGetDTO dtoResponse;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        dtoResponse = new ReservaUserGetDTO();
        dtoResponse.setId(1L);
    }

    @Test
    @DisplayName("crearReserva() debe delegar en el servicio y retornar DTO")
    void crearReserva_DeberiaDelegarYRetornarDTO() {
        when(reservaService.crear(any(ReservaCrearEditarDTO.class))).thenReturn(dtoResponse);

        ReservaCrearEditarDTO dto = new ReservaCrearEditarDTO(List.of(), null, null, false);
        ReservaUserGetDTO result = reservaController.crearReserva(dto);

        assertThat(result).isEqualTo(dtoResponse);
        verify(reservaService).crear(dto);
    }

    @Test
    @DisplayName("editarReserva() debe delegar en el servicio y retornar DTO actualizado")
    void editarReserva_DeberiaEditarYRetornarDTO() {
        when(reservaService.editar(eq(5L), any(ReservaCrearEditarDTO.class))).thenReturn(dtoResponse);

        ReservaCrearEditarDTO dto = new ReservaCrearEditarDTO(List.of(), null, null, false);
        ReservaUserGetDTO result = reservaController.editarReserva(5L, dto);

        assertThat(result).isEqualTo(dtoResponse);
        verify(reservaService).editar(5L, dto);
    }

    @Test
    @DisplayName("cancelarReserva() debe delegar en el servicio y retornar DTO cancelado")
    void cancelarReserva_DeberiaCancelarYRetornarDTO() {
        when(reservaService.cancelarReserva(7L)).thenReturn(dtoResponse);

        ReservaUserGetDTO result = reservaController.cancelarReserva(7L);

        assertThat(result).isEqualTo(dtoResponse);
        verify(reservaService).cancelarReserva(7L);
    }

    @Test
    @DisplayName("eliminarReserva() debe eliminar la reserva y retornar mensaje")
    void eliminarReserva_DeberiaEliminarYRetornarMensaje() {
        doNothing().when(reservaService).eliminar(10L);

        String result = reservaController.eliminarReserva(10L);

        assertThat(result).isEqualTo("reserva eliminada!");
        verify(reservaService).eliminar(10L);
    }

    @Test
    @DisplayName("getAllReservas() debe retornar lista de reservas")
    void getAllReservas_DeberiaRetornarLista() {
        when(reservaService.getAll()).thenReturn(List.of(dtoResponse));

        List<ReservaUserGetDTO> result = reservaController.getAllReservas();

        assertThat(result).hasSize(1).contains(dtoResponse);
        verify(reservaService).getAll();
    }

    @Test
    @DisplayName("getReserva() debe retornar reserva específica")
    void getReserva_DeberiaRetornarDTO() {
        when(reservaService.get(3L)).thenReturn(dtoResponse);

        ReservaUserGetDTO result = reservaController.getReserva(3L);

        assertThat(result).isEqualTo(dtoResponse);
        verify(reservaService).get(3L);
    }

    @Test
    @DisplayName("eliminarProducto() debe delegar en el servicio con IDs correctos")
    void eliminarProducto_DeberiaEliminarProductosCorrectamente() {
        List<Long> productos = List.of(1L, 2L, 3L);
        when(reservaService.eliminarProductos(8L, productos)).thenReturn(dtoResponse);

        ReservaUserGetDTO result = reservaController.eliminarProducto(8L, productos);

        assertThat(result).isEqualTo(dtoResponse);
        verify(reservaService).eliminarProductos(8L, productos);
    }
}
