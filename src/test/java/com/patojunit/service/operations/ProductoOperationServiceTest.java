package com.patojunit.service.operations;

import com.patojunit.dto.request.ProductoCrearEditarDTO;
import com.patojunit.factory.ProductoFactory;
import com.patojunit.helpers.logger.producto.ProductoLogger;
import com.patojunit.helpers.producto.ProductoMapper;
import com.patojunit.helpers.producto.ProductoStockService;
import com.patojunit.helpers.producto.ProductoValidator;
import com.patojunit.model.Producto;
import com.patojunit.repository.IProductoRepository;
import com.patojunit.repository.IReservaRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductoOperationServiceTest {

    @Mock
    private IProductoRepository productoRepository;

    @Mock
    private IReservaRepository reservaRepository;

    @Mock
    private ProductoFactory productoFactory;

    @Mock
    private ProductoValidator validator;

    @Mock
    private ProductoLogger productoLogger;

    @Mock
    private ProductoStockService stockService;

    @Mock
    private ProductoMapper mapper;

    @InjectMocks
    private ProductoOperationService productoOperationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Debe crear un producto exitosamente")
    void crearProducto_DeberiaGuardarYLoguear() {
        ProductoCrearEditarDTO dto = new ProductoCrearEditarDTO("reposera", BigDecimal.valueOf(1500), 10, 0);
        Producto producto = new Producto();
        producto.setNombre("reposera");

        when(productoFactory.crearProducto(dto)).thenReturn(producto);
        when(productoRepository.save(producto)).thenReturn(producto);

        Producto result = productoOperationService.crearProducto(dto);

        assertEquals("reposera", result.getNombre());
        verify(validator).validarProductoNoExiste("reposera");
        verify(productoRepository).save(producto);
        verify(productoLogger).logCreacionExitosa(producto);
    }

    @Test
    @DisplayName("Debe loguear y relanzar excepción si falla la creación")
    void crearProducto_DeberiaRegistrarError() {
        ProductoCrearEditarDTO dto = new ProductoCrearEditarDTO("carpa", BigDecimal.valueOf(1000), 5, 0);
        when(productoFactory.crearProducto(dto)).thenThrow(new RuntimeException("Error al crear"));

        assertThrows(RuntimeException.class, () -> productoOperationService.crearProducto(dto));

        verify(productoLogger).logErrorGeneral(eq("Error al crear producto"), any());
    }

    @Test
    @DisplayName("Debe editar un producto existente y loguear correctamente")
    void editarProducto_DeberiaActualizarYLoguear() {
        ProductoCrearEditarDTO dto = new ProductoCrearEditarDTO("nueva reposera", BigDecimal.valueOf(1800), 8, 0);
        Producto productoExistente = new Producto();
        productoExistente.setId(1L);
        productoExistente.setNombre("reposera");

        when(productoRepository.findById(1L)).thenReturn(Optional.of(productoExistente));
        when(productoRepository.save(productoExistente)).thenReturn(productoExistente);

        Producto result = productoOperationService.editarProducto(1L, dto);

        verify(productoFactory).actualizarProductoDesdeDTO(productoExistente, dto);
        verify(productoRepository).save(productoExistente);
        verify(productoLogger).logEdicionExitosa(productoExistente);
        assertEquals(productoExistente, result);
    }

    @Test
    @DisplayName("Debe lanzar excepción si el producto no existe al editar")
    void editarProducto_DeberiaLanzarEntityNotFound() {
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class,
                () -> productoOperationService.editarProducto(99L, new ProductoCrearEditarDTO()));
    }

    @Test
    @DisplayName("Debe loguear error si ocurre excepción durante edición")
    void editarProducto_DeberiaLoguearError() {
        ProductoCrearEditarDTO dto = new ProductoCrearEditarDTO("carpa", BigDecimal.valueOf(1200), 5, 0);
        Producto producto = new Producto();
        producto.setId(1L);
        producto.setNombre("carpa");

        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        doThrow(new RuntimeException("Error inesperado"))
                .when(productoRepository).save(producto);

        assertThrows(RuntimeException.class, () -> productoOperationService.editarProducto(1L, dto));

        verify(productoLogger).logError(eq(producto), any());
    }

    @Test
    @DisplayName("Debe eliminar producto si no está asociado a reservas")
    void eliminarProducto_DeberiaEliminarCorrectamente() {
        Producto producto = new Producto();
        producto.setId(10L);
        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));
        when(reservaRepository.existsByProductos_Producto_Id(10L)).thenReturn(false);

        productoOperationService.eliminarProducto(10L);

        verify(productoRepository).delete(producto);
        verify(productoLogger).logEliminacionExitosa(10L);
    }

    @Test
    @DisplayName("Debe lanzar excepción si producto está asociado a reservas")
    void eliminarProducto_DeberiaLanzarExcepcionPorReserva() {
        Producto producto = new Producto();
        producto.setId(20L);
        when(productoRepository.findById(20L)).thenReturn(Optional.of(producto));
        when(reservaRepository.existsByProductos_Producto_Id(20L)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> productoOperationService.eliminarProducto(20L));
        verify(productoLogger).logAdvertencia(contains("Intento de eliminar producto asociado"), eq(20L));
    }

    @Test
    @DisplayName("Debe descontar stock correctamente y loguear")
    void descontarStock_DeberiaLlamarStockServiceYLoguear() {
        Producto producto = new Producto();
        producto.setNombre("reposera");

        productoOperationService.descontarStock(producto, 2);

        verify(stockService).descontarStock(producto, 2);
        verify(productoLogger).logDescuentoStock(producto, 2);
    }

    @Test
    @DisplayName("Debe manejar error al descontar stock y loguear el fallo")
    void descontarStock_DeberiaLoguearErrorSiFalla() {
        Producto producto = new Producto();
        doThrow(new RuntimeException("Error en stock")).when(stockService).descontarStock(producto, 3);

        assertThrows(RuntimeException.class, () -> productoOperationService.descontarStock(producto, 3));
        verify(productoLogger).logError(eq(producto), any());
    }

    @Test
    @DisplayName("Debe restablecer stock correctamente y loguear")
    void restablecerStock_DeberiaLlamarStockServiceYLoguear() {
        Producto producto = new Producto();

        productoOperationService.restablecerStock(producto, 5);

        verify(stockService).reponerStock(producto, 5);
        verify(productoLogger).logReposicionStock(producto, 5);
    }

    @Test
    @DisplayName("Debe manejar error al reponer stock y loguear el fallo")
    void restablecerStock_DeberiaLoguearErrorSiFalla() {
        Producto producto = new Producto();
        doThrow(new RuntimeException("Error al reponer")).when(stockService).reponerStock(producto, 2);

        assertThrows(RuntimeException.class, () -> productoOperationService.restablecerStock(producto, 2));
        verify(productoLogger).logError(eq(producto), any());
    }

    @Test
    @DisplayName("Debe devolver el producto si existe")
    void getEntity_DeberiaRetornarProducto() {
        Producto producto = new Producto();
        producto.setId(1L);
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

        Producto result = productoOperationService.getEntity(1L);

        assertEquals(producto, result);
    }

    @Test
    @DisplayName("Debe lanzar EntityNotFoundException si el producto no existe")
    void getEntity_DeberiaLanzarEntityNotFound() {
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> productoOperationService.getEntity(99L));
        verify(productoLogger).logAdvertencia(contains("Intento de acceder a producto inexistente"), eq(99L));
    }
}
