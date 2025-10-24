package com.patojunit.helpers.producto;

import com.patojunit.model.Producto;
import com.patojunit.repository.IProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductoStockServiceTest {

    @Mock
    private IProductoRepository productoRepository;

    @Mock
    private ProductoValidator validator;

    @InjectMocks
    private ProductoStockService stockService;

    private Producto producto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Galletitas");
        producto.setStockDisponible(10);
        producto.setCantidadReservadaActual(2);
    }

    @Test
    @DisplayName("Debe descontar stock correctamente y actualizar cantidad reservada")
    void descontarStock_DeberiaActualizarCorrectamente() {
        // Act
        stockService.descontarStock(producto, 3);

        // Assert
        assertEquals(7, producto.getStockDisponible());
        assertEquals(5, producto.getCantidadReservadaActual());
        verify(validator).validarStockNoNegativo(7);
        verify(productoRepository).save(producto);
    }

    @Test
    @DisplayName("Debe lanzar excepción si el stock quedaría negativo")
    void descontarStock_DeberiaLanzarExcepcionSiStockNegativo() {
        doThrow(new IllegalArgumentException("Stock no puede ser negativo"))
                .when(validator).validarStockNoNegativo(anyInt());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                stockService.descontarStock(producto, 20));

        assertEquals("Stock no puede ser negativo", ex.getMessage());
        verify(validator).validarStockNoNegativo(-10);
        verify(productoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe reponer stock correctamente y actualizar cantidad reservada")
    void reponerStock_DeberiaActualizarCorrectamente() {
        // Act
        stockService.reponerStock(producto, 4);

        // Assert
        assertEquals(14, producto.getStockDisponible());
        assertEquals(-2, producto.getCantidadReservadaActual());
        verify(productoRepository).save(producto);
        verifyNoInteractions(validator);
    }

    @Test
    @DisplayName("Debe invocar el repositorio correctamente al reponer stock")
    void reponerStock_DeberiaGuardarProducto() {
        stockService.reponerStock(producto, 1);

        verify(productoRepository, times(1)).save(producto);
    }
}