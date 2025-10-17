package com.patojunit.service;

import com.patojunit.model.Producto;
import com.patojunit.repository.IProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class ProductoServiceTest {

    @Mock
    private IProductoRepository productoRepository;

    @InjectMocks
    private ProductoService productoService;

    private Producto producto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Sombrilla");
        producto.setPrecioHora(BigDecimal.valueOf(100));
        producto.setStockDisponible(10);
        producto.setCantidadReservada(2);
    }

    // ✅ eliminar(Long id)
    @Test
    void eliminar_DeberiaEliminarProductoSiExiste() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

        productoService.eliminar(1L);

        verify(productoRepository, times(1)).deleteById(1L);
    }

    @Test
    void eliminar_NoDeberiaEliminarSiNoExiste() {
        when(productoRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> productoService.eliminar(99L));
    }

    // ✅ editar(Long id, Producto objeto)
    @Test
    void editar_DeberiaActualizarCamposYGuardarProducto() {
        Producto datosNuevos = new Producto();
        datosNuevos.setNombre("Reposera");
        datosNuevos.setPrecioHora(BigDecimal.valueOf(300));
        datosNuevos.setStockDisponible(15);
        datosNuevos.setCantidadReservada(5);

        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(productoRepository.save(any(Producto.class))).thenReturn(producto);

        Producto actualizado = productoService.editar(1L, datosNuevos);

        assertEquals("Reposera", actualizado.getNombre());
        assertEquals(BigDecimal.valueOf(300), actualizado.getPrecioHora());
        assertEquals(15, actualizado.getStockDisponible());
        assertEquals(5, actualizado.getCantidadReservada());
        assertNotNull(actualizado.getFechaUltimaActualizacion());
        assertTrue(actualizado.getCodigoProducto().startsWith("PROD-"));

        verify(productoRepository).save(any(Producto.class));
    }

    // ✅ generarCodigoProducto(String nombre)
    @Test
    void generarCodigoProducto_DeberiaGenerarCodigoValido() {
        String codigo = productoService.generarCodigoProducto("Tabla");
        assertTrue(codigo.startsWith("PROD-TAB-"));
        assertEquals(13, codigo.length()); // PROD-TAB-XXXX
    }

    // ✅ crear(Producto objeto)
    @Test
    void crear_DeberiaAsignarCodigoYFechaAltaYGuardar() {
        when(productoRepository.save(any(Producto.class))).thenReturn(producto);

        Producto creado = productoService.crear(producto);

        assertNotNull(creado.getCodigoProducto());
        assertNotNull(creado.getFechaAlta());
        assertNull(creado.getFechaUltimaActualizacion());

        verify(productoRepository).save(any(Producto.class));
    }

    // ✅ getAll()
    @Test
    void getAll_DeberiaRetornarListaDeProductos() {
        when(productoRepository.findAll()).thenReturn(List.of(producto));

        List<Producto> resultado = productoService.getAll();

        assertEquals(1, resultado.size());
        verify(productoRepository).findAll();
    }

    // ✅ get(Long id)
    @Test
    void get_DeberiaRetornarProductoSiExiste() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

        Producto resultado = productoService.get(1L);

        assertEquals("Sombrilla", resultado.getNombre());
        verify(productoRepository).findById(1L);
    }

    @Test
    void get_DeberiaLanzarExcepcionSiNoExiste() {
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> productoService.get(99L));
    }

    // ✅ getStock(Long id)
    @Test
    void getStock_DeberiaRetornarStockDisponible() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

        int stock = productoService.getStock(1L);

        assertEquals(10, stock);
    }

    // ✅ guardar(Producto producto)
    @Test
    void guardar_DeberiaGuardarProducto() {
        when(productoRepository.save(producto)).thenReturn(producto);

        Producto resultado = productoService.guardar(producto);

        assertEquals(producto, resultado);
        verify(productoRepository).save(producto);
    }

    // ✅ modificarStock(Producto producto)
    @Test
    void modificarStock_DeberiaReducirStockYAumentarReservado() {
        productoService.modificarStock(producto);

        assertEquals(9, producto.getStockDisponible());
        assertEquals(3, producto.getCantidadReservada());
    }

    @Test
    void modificarStock_DeberiaLanzarExcepcionSiSuperaInventario() {
        producto.setStockDisponible(0);
        producto.setCantidadReservada(10);

        assertThrows(IllegalArgumentException.class, () -> productoService.modificarStock(producto));
    }

    // ✅ restablecerStock(Producto producto)
    @Test
    void restablecerStock_DeberiaRevertirStockCorrectamente() {
        productoService.restablecerStock(producto);

        assertEquals(11, producto.getStockDisponible());
        assertEquals(1, producto.getCantidadReservada());
    }
}

