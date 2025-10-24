package com.patojunit.helpers.reserva;

import com.patojunit.dto.request.ProductoCantidadCrearEditarDTO;
import com.patojunit.model.Producto;
import com.patojunit.model.ProductoCantidad;
import com.patojunit.model.Reserva;
import com.patojunit.model.enums.EstadoReserva;
import com.patojunit.validation.ReservaValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReservaValidatorTest {

    private ReservaValidator validator;

    @BeforeEach
    void setUp() {
        validator = new ReservaValidator();
    }

    @Test
    @DisplayName("Debe lanzar excepción si el producto ya está reservado")
    void validarProductoYaReservado_DeberiaLanzarExcepcion() {
        Producto producto = new Producto();
        producto.setId(1L);

        ProductoCantidad pc = new ProductoCantidad();
        pc.setProducto(producto);

        ProductoCantidadCrearEditarDTO dto = new ProductoCantidadCrearEditarDTO(1L, 2);

        assertThrows(IllegalArgumentException.class, () ->
                validator.validarProductoYaReservado(dto, List.of(pc)));
    }

    @Test
    @DisplayName("No debe lanzar excepción si el producto no está reservado")
    void validarProductoYaReservado_NoDeberiaLanzarExcepcion() {
        Producto producto = new Producto();
        producto.setId(1L);

        ProductoCantidad pc = new ProductoCantidad();
        pc.setProducto(producto);

        ProductoCantidadCrearEditarDTO dto = new ProductoCantidadCrearEditarDTO(2L, 1);

        assertDoesNotThrow(() ->
                validator.validarProductoYaReservado(dto, List.of(pc)));
    }

    @Test
    @DisplayName("Debe lanzar excepción si cambian los IDs de productos")
    void validarProductosNoCambiados_DeberiaLanzarExcepcion() {
        Producto p1 = new Producto(); p1.setId(1L);
        Producto p2 = new Producto(); p2.setId(2L);

        ProductoCantidad pc = new ProductoCantidad();
        pc.setProducto(p1);

        ProductoCantidadCrearEditarDTO dto = new ProductoCantidadCrearEditarDTO(2L, 1);

        assertThrows(IllegalArgumentException.class, () ->
                validator.validarProductosNoCambiados(List.of(pc), List.of(dto)));
    }

    @Test
    @DisplayName("No debe lanzar excepción si los IDs son los mismos")
    void validarProductosNoCambiados_NoDeberiaLanzarExcepcion() {
        Producto p1 = new Producto(); p1.setId(1L);

        ProductoCantidad pc = new ProductoCantidad();
        pc.setProducto(p1);

        ProductoCantidadCrearEditarDTO dto = new ProductoCantidadCrearEditarDTO(1L, 5);

        assertDoesNotThrow(() ->
                validator.validarProductosNoCambiados(List.of(pc), List.of(dto)));
    }

    @Test
    @DisplayName("Debe lanzar excepción si la reserva ya está cancelada")
    void validarEstadoNoCancelado_DeberiaLanzarExcepcion() {
        Reserva reserva = new Reserva();
        reserva.setEstado(EstadoReserva.CANCELADA);

        assertThrows(IllegalArgumentException.class, () ->
                validator.validarEstadoNoCancelado(reserva));
    }

    @Test
    @DisplayName("No debe lanzar excepción si la reserva está activa o pendiente")
    void validarEstadoNoCancelado_NoDeberiaLanzarExcepcion() {
        Reserva reserva = new Reserva();
        reserva.setEstado(EstadoReserva.PENDIENTE);

        assertDoesNotThrow(() ->
                validator.validarEstadoNoCancelado(reserva));
    }
}
