package com.patojunit.helpers.reserva;

import com.patojunit.dto.response.ProductoUserGetDTO;
import com.patojunit.helpers.ReservaCalculoService;
import com.patojunit.model.Producto;
import com.patojunit.model.ProductoCantidad;
import com.patojunit.model.Reserva;
import com.patojunit.service.interfaces.IProductoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReservaCalculoServiceTest {

    @Mock
    private IProductoService productoService;

    @InjectMocks
    private ReservaCalculoService calculoService;

    private Reserva reserva;
    private Producto producto;
    private ProductoCantidad productoCantidad;
    private ProductoUserGetDTO productoDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Reposera");

        productoDTO = new ProductoUserGetDTO();
        productoDTO.setId(1L);
        productoDTO.setPrecioHora(new BigDecimal("100"));

        productoCantidad = new ProductoCantidad();
        productoCantidad.setProducto(producto);
        productoCantidad.setCantidad(2);

        reserva = new Reserva();
        reserva.setProductos(List.of(productoCantidad));
        reserva.setFechaInicio(LocalDateTime.now());
        reserva.setFechaFin(reserva.getFechaInicio().plusHours(3));
    }

    @Test
    @DisplayName("Debe calcular el precio total correctamente para una reserva válida")
    void calcularPrecioTotal_DeberiaRetornarMontoCorrecto() {
        when(productoService.get(1L)).thenReturn(productoDTO);

        BigDecimal total = calculoService.calcularPrecioTotal(reserva);

        assertEquals(new BigDecimal("600"), total);
        verify(productoService).get(1L);
    }

    @Test
    @DisplayName("Debe calcular correctamente con múltiples productos")
    void calcularPrecioTotal_DeberiaSumarMultiplesProductos() {
        Producto p2 = new Producto();
        p2.setId(2L);

        ProductoCantidad pc2 = new ProductoCantidad();
        pc2.setProducto(p2);
        pc2.setCantidad(1);

        reserva.setProductos(List.of(productoCantidad, pc2));

        ProductoUserGetDTO dto2 = new ProductoUserGetDTO();
        dto2.setId(2L);
        dto2.setPrecioHora(new BigDecimal("200"));

        when(productoService.get(1L)).thenReturn(productoDTO);
        when(productoService.get(2L)).thenReturn(dto2);

        BigDecimal total = calculoService.calcularPrecioTotal(reserva);

        assertEquals(new BigDecimal("1200"), total);
    }

    @Test
    @DisplayName("Debe lanzar excepción si las fechas son nulas")
    void calcularPrecioTotal_DeberiaLanzarExcepcionSiFechasNulas() {
        reserva.setFechaInicio(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                calculoService.calcularPrecioTotal(reserva));

        assertEquals("Las fechas de la reserva no pueden ser nulas.", ex.getMessage());
        verifyNoInteractions(productoService);
    }

    @Test
    @DisplayName("Debe lanzar excepción si la duración es cero o negativa")
    void calcularPrecioTotal_DeberiaLanzarExcepcionSiDuracionInvalida() {
        reserva.setFechaFin(reserva.getFechaInicio());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                calculoService.calcularPrecioTotal(reserva));

        assertEquals("La duración de la reserva debe ser mayor a 0 horas.", ex.getMessage());
        verifyNoInteractions(productoService);
    }

    @Test
    @DisplayName("Debe multiplicar correctamente precio * horas * cantidad")
    void calcularSubtotalProducto_DeberiaCalcularCorrectamente() {
        when(productoService.get(1L)).thenReturn(productoDTO);

        BigDecimal total = calculoService.calcularPrecioTotal(reserva);

        assertEquals(new BigDecimal("600"), total);
        verify(productoService).get(1L);
    }
}
