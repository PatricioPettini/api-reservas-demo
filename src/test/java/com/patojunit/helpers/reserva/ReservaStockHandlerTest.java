package com.patojunit.helpers.reserva;

import com.patojunit.dto.request.ReservaCrearEditarDTO;
import com.patojunit.model.Producto;
import com.patojunit.model.ProductoCantidad;
import com.patojunit.model.Reserva;
import com.patojunit.model.enums.EstadoReserva;
import com.patojunit.service.operations.ProductoOperationService;
import com.patojunit.validation.ReservaValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReservaStockHandlerTest {

    @Mock private ReservaMapper mapper;
    @Mock private ReservaValidator validator;
    @Mock private ProductoOperationService productoOperationService;

    @InjectMocks
    private ReservaStockHandler stockHandler;

    private Producto producto;
    private Reserva reserva;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Reposera");
        producto.setStockDisponible(10);

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
    @DisplayName("Debe devolver lista vacía si no hay productos")
    void procesarProductos_DeberiaRetornarListaVacia() {
        var dto = mock(ReservaCrearEditarDTO.class);
        when(dto.getProductos()).thenReturn(List.of());

        var result = stockHandler.procesarProductos(dto, null);

        assertTrue(result.isEmpty());
        verifyNoInteractions(mapper, validator);
    }
}
