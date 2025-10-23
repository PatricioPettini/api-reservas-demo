package com.patojunit.helpers.reserva;

import com.patojunit.dto.request.ProductoCantidadCrearEditarDTO;
import com.patojunit.dto.request.ReservaCrearEditarDTO;
import com.patojunit.helpers.ReservaMapper;
import com.patojunit.helpers.ReservaStockHandler;
import com.patojunit.helpers.ReservaValidator;
import com.patojunit.model.Producto;
import com.patojunit.model.ProductoCantidad;
import com.patojunit.model.Reserva;
import com.patojunit.model.enums.EstadoReserva;
import com.patojunit.service.interfaces.IProductoService;
import com.patojunit.service.operations.ProductoOperationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReservaStockHandlerTest {

    @Mock private IProductoService productoService;
    @Mock private ReservaMapper mapper;
    @Mock private ReservaValidator validator;
    @Mock private ProductoOperationService productoOperationService;

    @InjectMocks
    private ReservaStockHandler stockHandler;

    private Producto producto;
    private ProductoCantidadCrearEditarDTO dtoProducto;
    private Reserva reserva;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Reposera");
        producto.setStockDisponible(10);

        dtoProducto = new ProductoCantidadCrearEditarDTO(1L, 3);

        reserva = new Reserva();
        reserva.setId(5L);
        reserva.setEstado(EstadoReserva.PENDIENTE);
        ProductoCantidad pc = new ProductoCantidad();
        pc.setProducto(producto);
        pc.setCantidad(2);
        reserva.setProductos(List.of(pc));
    }

    @Test
    @DisplayName("Debe descontar stock si la reserva está pendiente")
    void descontarStock_DeberiaDescontar() {
        stockHandler.descontarStockProductos(reserva);
        verify(productoOperationService).descontarStock(producto, 2);
    }

    @Test
    @DisplayName("Debe lanzar excepción si la reserva no está pendiente")
    void descontarStock_DeberiaLanzarExcepcionSiNoPendiente() {
        reserva.setEstado(EstadoReserva.CANCELADA);
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> stockHandler.descontarStockProductos(reserva));
        assertEquals("La reserva no está pendiente; no se puede descontar stock.", ex.getMessage());
        verifyNoInteractions(productoOperationService);
    }

    @Test
    @DisplayName("Debe restablecer stock correctamente")
    void restablecerStock_DeberiaRestablecer() {
        stockHandler.restablecerStockProductos(reserva);
        verify(productoOperationService).restablecerStock(producto, 2);
    }

    @Test
    @DisplayName("Debe descontar stock cuando se aumenta cantidad y hay stock suficiente")
    void actualizarStockProducto_DeberiaDescontarSiHayStock() {
        when(productoOperationService.getEntity(1L)).thenReturn(producto);
        when(mapper.toProductoCantidad(any(), any())).thenReturn(new ProductoCantidad());

        ReservaCrearEditarDTO dto = new ReservaCrearEditarDTO(List.of(dtoProducto), null, null, false);

        stockHandler.generarProductosConValidacion(dto, reserva);

        verify(productoOperationService).descontarStock(producto, 3);
    }

    @Test
    @DisplayName("Debe lanzar excepción si no hay stock suficiente")
    void actualizarStockProducto_DeberiaLanzarExcepcionPorStockInsuficiente() {
        producto.setStockDisponible(1);
        when(productoOperationService.getEntity(1L)).thenReturn(producto);

        ReservaCrearEditarDTO dto = new ReservaCrearEditarDTO(List.of(dtoProducto), null, null, false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> stockHandler.generarProductosConValidacion(dto, reserva));

        assertTrue(ex.getMessage().contains("Stock insuficiente"));
        verify(productoOperationService, never()).descontarStock(any(), anyInt());
    }

    @Test
    @DisplayName("Debe restablecer stock cuando se reduce la cantidad reservada")
    void actualizarStockProducto_DeberiaRestablecerSiReduceCantidad() {
        when(productoOperationService.getEntity(1L)).thenReturn(producto);
        when(mapper.toProductoCantidad(any(), any())).thenReturn(new ProductoCantidad());

        ProductoCantidadCrearEditarDTO dtoReducida = new ProductoCantidadCrearEditarDTO(1L, 1);
        ReservaCrearEditarDTO dto = new ReservaCrearEditarDTO(List.of(dtoReducida), null, null, false);

        stockHandler.procesarProductos(dto, reserva);

        verify(productoOperationService).restablecerStock(producto, 1);
    }

    @Test
    @DisplayName("Debe obtener correctamente la cantidad reservada actual")
    void obtenerCantidadReservadaActual_DeberiaDevolverCantidad() throws Exception {
        var method = ReservaStockHandler.class.getDeclaredMethod("obtenerCantidadReservadaActual", Reserva.class, Long.class);
        method.setAccessible(true);
        int cantidad = (int) method.invoke(stockHandler, reserva, 1L);
        assertEquals(2, cantidad);
    }
}
