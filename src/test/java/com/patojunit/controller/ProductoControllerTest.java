package com.patojunit.controller;

import com.patojunit.model.Producto;
import com.patojunit.service.IProductoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class ProductoControllerTest {

    @Mock
    private IProductoService productoService;

    @InjectMocks
    private ProductoController productoController;

    @Test
    void getAllProductos_DeberiaRetornarListaDeProductos() {
        // 🔹 Datos simulados
        Producto p1 = new Producto();
        p1.setId(1L);
        p1.setNombre("sombrilla");

        Producto p2 = new Producto();
        p2.setId(2L);
        p2.setNombre("carpa");

        List<Producto> productos = Arrays.asList(p1, p2);

        // 🔹 Mock del servicio
        when(productoService.getAll()).thenReturn(productos);

        // 🔹 Llamada al controlador
        List<Producto> resultado = productoController.getAllProductos();

        // 🔹 Verificaciones
        assertEquals(2, resultado.size());
        assertEquals("sombrilla", resultado.get(0).getNombre());
        assertEquals("carpa", resultado.get(1).getNombre());
        verify(productoService, times(1)).getAll();
    }

    @Test
    void getProducto_DeberiaRetornarProductoPorId() {
        // 🔹 Datos simulados
        Producto producto = new Producto();
        producto.setId(1L);
        producto.setNombre("carpa");

        // 🔹 Mock del servicio
        when(productoService.get(1L)).thenReturn(producto);

        // 🔹 Llamada al controlador
        Producto resultado = productoController.getProducto(1L);

        // 🔹 Verificaciones
        assertNotNull(resultado);
        assertEquals("carpa", resultado.getNombre());
        assertEquals(1L, resultado.getId());
        verify(productoService, times(1)).get(1L);
    }

    @Test
    void getStockProducto_DeberiaRetornarStockDelProducto() {
        // 🔹 Datos simulados
        Long id = 1L;
        int stockEsperado = 15;

        // 🔹 Mock del servicio
        when(productoService.getStock(id)).thenReturn(stockEsperado);

        // 🔹 Llamada al controlador
        int resultado = productoController.getStockProducto(id);

        // 🔹 Verificaciones
        assertEquals(stockEsperado, resultado);
        verify(productoService, times(1)).getStock(id);
    }

    @Test
    void crearProducto_DeberiaRetornarProductoCreado() {
        // 🔹 Datos simulados
        Producto producto = new Producto();
        producto.setNombre("sombrilla");
        producto.setPrecioHora(new BigDecimal("250.00"));
        producto.setStockDisponible(10);
        producto.setCantidadReservada(0);

        // 🔹 Mock del servicio (simula que devuelve el mismo producto con un ID)
        Producto productoCreado = new Producto();
        productoCreado.setId(1L);
        productoCreado.setNombre("sombrilla");
        productoCreado.setPrecioHora(new BigDecimal("250.00"));
        productoCreado.setStockDisponible(10);
        productoCreado.setCantidadReservada(0);

        when(productoService.crear(producto)).thenReturn(productoCreado);

        // 🔹 Llamada al controlador
        Producto resultado = productoController.crearProducto(producto);

        // 🔹 Verificaciones
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("sombrilla", resultado.getNombre());
        verify(productoService, times(1)).crear(producto);
    }

    @Test
    void eliminarProducto_DeberiaRetornarMensajeDeExito() {
        // 🔹 ID a eliminar
        Long id = 1L;

        // 🔹 No hace falta devolver nada, solo verificar la llamada
        doNothing().when(productoService).eliminar(id);

        // 🔹 Llamada al controlador
        String resultado = productoController.eliminarProducto(id);

        // 🔹 Verificaciones
        assertEquals("producto eliminado!", resultado);
        verify(productoService, times(1)).eliminar(id);
    }

    @Test
    void editarProducto_DeberiaRetornarProductoEditado() {
        // 🔹 Datos simulados
        Long id = 1L;

        Producto productoOriginal = new Producto();
        productoOriginal.setId(id);
        productoOriginal.setNombre("sombrilla");
        productoOriginal.setPrecioHora(new BigDecimal("200.00"));
        productoOriginal.setStockDisponible(10);
        productoOriginal.setCantidadReservada(2);

        Producto productoEditado = new Producto();
        productoEditado.setId(id);
        productoEditado.setNombre("sombrilla premium");
        productoEditado.setPrecioHora(new BigDecimal("300.00"));
        productoEditado.setStockDisponible(8);
        productoEditado.setCantidadReservada(2);

        // 🔹 Mock del servicio (simula la edición)
        when(productoService.editar(id, productoOriginal)).thenReturn(productoEditado);

        // 🔹 Llamada al controlador
        Producto resultado = productoController.editarProducto(id, productoOriginal);

        // 🔹 Verificaciones
        assertNotNull(resultado);
        assertEquals("sombrilla premium", resultado.getNombre());
        assertEquals(new BigDecimal("300.00"), resultado.getPrecioHora());
        assertEquals(8, resultado.getStockDisponible());
        verify(productoService, times(1)).editar(id, productoOriginal);
    }

}
