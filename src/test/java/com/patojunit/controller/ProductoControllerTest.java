package com.patojunit.controller;

import com.patojunit.dto.request.ProductoCrearEditarDTO;
import com.patojunit.dto.response.ProductoGetDTO;
import com.patojunit.service.IProductoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductoControllerTest {

    @Mock
    private IProductoService productoService;

    @InjectMocks
    private ProductoController productoController;

    private ProductoGetDTO productoGet;
    private ProductoCrearEditarDTO crearEditar;

    @BeforeEach
    void setUp() {
        productoGet = new ProductoGetDTO();
        productoGet.setId(1L);
        productoGet.setNombre("carpa");
        productoGet.setPrecioHora(BigDecimal.valueOf(100));
        productoGet.setStockDisponible(10);

        crearEditar = new ProductoCrearEditarDTO();
        crearEditar.setNombre("reposera");
        crearEditar.setPrecioHora(BigDecimal.valueOf(100));
        crearEditar.setStockDisponible(10);
    }

    @Test
    void getAllProductos_devuelveLista() {
        when(productoService.getAll()).thenReturn(List.of(productoGet));

        var result = productoController.getAllProductos();

        assertEquals(1, result.size());
        assertEquals("carpa", result.get(0).getNombre());
        verify(productoService, times(1)).getAll();
    }

    @Test
    void getProducto_porId_devuelveDTO() {
        when(productoService.get(1L)).thenReturn(productoGet);

        var result = productoController.getProducto(1L);

        assertEquals("carpa", result.getNombre());
        verify(productoService).get(1L);
    }

    @Test
    void getStockProducto_devuelveEntero() {
        when(productoService.getStock(1L)).thenReturn(7);

        int stock = productoController.getStockProducto(1L);

        assertEquals(7, stock);
        verify(productoService).getStock(1L);
    }

    @Test
    void crearProducto_invocaServicioYDevuelveDTO() {
        when(productoService.crear(any(ProductoCrearEditarDTO.class))).thenReturn(productoGet);

        var creado = productoController.crearProducto(crearEditar);

        assertEquals("carpa", creado.getNombre());
        verify(productoService).crear(any(ProductoCrearEditarDTO.class));
    }

    @Test
    void editarProducto_invocaServicioYDevuelveDTO() {
        var editado = new ProductoGetDTO();
        editado.setId(1L);
        editado.setNombre("reposera");
        editado.setPrecioHora(BigDecimal.valueOf(120));
        editado.setStockDisponible(8);

        when(productoService.editar(eq(1L), any(ProductoCrearEditarDTO.class))).thenReturn(editado);

        var result = productoController.editarProducto(1L, crearEditar);

        assertEquals("reposera", result.getNombre());
        verify(productoService).editar(eq(1L), any(ProductoCrearEditarDTO.class));
    }

    @Test
    void eliminarProducto_devuelveMensajeYLlamaAlServicio() {
        String msg = productoController.eliminarProducto(1L);

        assertEquals("producto eliminado!", msg);
        verify(productoService).eliminar(1L);
    }
}
