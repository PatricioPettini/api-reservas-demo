package com.patojunit.helpers.reserva;

import com.patojunit.dto.request.ProductoCantidadCrearEditarDTO;
import com.patojunit.dto.request.ReservaCrearEditarDTO;
import com.patojunit.dto.response.ReservaUserGetDTO;
import com.patojunit.helpers.ReservaMapper;
import com.patojunit.model.*;
import com.patojunit.model.enums.EstadoReserva;
import com.patojunit.service.operations.ProductoOperationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReservaMapperTest {

    @Mock
    private ProductoOperationService productoOperationService;

    @InjectMocks
    private ReservaMapper mapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Debe convertir ReservaCrearEditarDTO en entidad Reserva correctamente")
    void toEntity_DeberiaMapearCamposBasicos() {
        var dto = new ReservaCrearEditarDTO(List.of(), LocalDateTime.now(), LocalDateTime.now().plusHours(2), false);

        Reserva reserva = mapper.toEntity(dto);

        assertEquals(dto.getFechaInicio(), reserva.getFechaInicio());
        assertEquals(dto.getFechaFin(), reserva.getFechaFin());
        assertEquals(dto.getPagado(), reserva.getPagado());
    }

    @Test
    @DisplayName("Debe mapear correctamente ProductoCantidad desde DTO")
    void toProductoCantidad_DeberiaMapearCamposCorrectamente() {
        var dto = new ProductoCantidadCrearEditarDTO(10L, 3);
        var producto = new Producto();
        producto.setId(10L);
        producto.setNombre("Reposera");

        var reserva = new Reserva();
        reserva.setId(1L);

        when(productoOperationService.getEntity(10L)).thenReturn(producto);

        ProductoCantidad pc = mapper.toProductoCantidad(dto, reserva);

        assertEquals(producto, pc.getProducto());
        assertEquals(3, pc.getCantidad());
        assertEquals(reserva, pc.getReserva());
    }

    @Test
    @DisplayName("Debe mapear Reserva a ReservaUserGetDTO correctamente")
    void toUserGetDTO_DeberiaMapearCamposCorrectamente() {
        var producto = new Producto();
        producto.setNombre("Silla de playa");

        var pc = new ProductoCantidad();
        pc.setProducto(producto);
        pc.setCantidad(2);

        var reserva = new Reserva();
        reserva.setId(5L);
        reserva.setCodigoReserva("RES123");
        reserva.setEstado(EstadoReserva.CANCELADA);
        reserva.setPagado(true);
        reserva.setProductos(List.of(pc));
        reserva.setPrecioTotal(BigDecimal.valueOf(1500));

        ReservaUserGetDTO dto = mapper.toUserGetDTO(reserva);

        assertEquals(5L, dto.getId());
        assertEquals("RES123", dto.getCodigoReserva());
        assertEquals("CANCELADA", dto.getEstado());
        assertTrue(dto.getPagado());
        assertEquals(1, dto.getProductos().size());
        assertEquals("Silla de playa", dto.getProductos().get(0).getNombreProducto());
        assertEquals(2, dto.getProductos().get(0).getCantidad());
    }
}
