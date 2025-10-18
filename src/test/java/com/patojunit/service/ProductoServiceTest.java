package com.patojunit.service;

import com.patojunit.dto.request.ProductoCrearEditarDTO;
import com.patojunit.dto.response.ProductoGetDTO;
import com.patojunit.model.Producto;
import com.patojunit.repository.IProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private IProductoRepository productoRepository;

    @InjectMocks
    private ProductoService productoService;

    private Producto producto;
    private ProductoCrearEditarDTO crearEditarDTO;

    @BeforeEach
    void setUp() {
        producto = new Producto();
        producto.setId(1L);
        producto.setNombre("reposera");
        producto.setPrecioHora(BigDecimal.valueOf(100));
        producto.setStockDisponible(10);
        producto.setCantidadReservada(0);
        producto.setCodigoProducto("PROD-GAL-1234");

        crearEditarDTO = new ProductoCrearEditarDTO();
        crearEditarDTO.setNombre("carpa");
        crearEditarDTO.setPrecioHora(BigDecimal.valueOf(100));
        crearEditarDTO.setStockDisponible(10);
        crearEditarDTO.setCantidadReservada(0);
    }

    @Test
    void crear_DeberiaGuardarYRetornarDTO() {
        when(productoRepository.save(any(Producto.class))).thenReturn(producto);

        ProductoGetDTO result = productoService.crear(crearEditarDTO);

        assertNotNull(result);
        assertEquals("reposera", result.getNombre());
        verify(productoRepository).save(any(Producto.class));
    }

    @Test
    void editar_DeberiaActualizarYRetornarDTO() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(productoRepository.save(any(Producto.class))).thenReturn(producto);

        ProductoGetDTO result = productoService.editar(1L, crearEditarDTO);

        assertEquals("carpa", result.getNombre());
        verify(productoRepository, times(1)).save(any(Producto.class));
    }

    @Test
    void editar_DeberiaLanzarExcepcionSiNoExiste() {
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                productoService.editar(99L, crearEditarDTO)
        );
    }

    @Test
    void eliminar_DeberiaEliminarSiExiste() {
        Producto producto1= new Producto();
        producto1.setId(1L);
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

        productoService.eliminar(1L);

        verify(productoRepository).deleteById(1L);
    }

    @Test
    void eliminar_DeberiaLanzarExcepcionSiNoExiste() {
        when(productoRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                productoService.eliminar(1L)
        );

        verify(productoRepository, never()).deleteById(anyLong());
    }

    @Test
    void getAll_DeberiaRetornarListaDeProductos() {
        when(productoRepository.findAll()).thenReturn(List.of(producto));

        List<ProductoGetDTO> result = productoService.getAll();

        assertEquals(1, result.size());
        assertEquals("reposera", result.get(0).getNombre());
        verify(productoRepository).findAll();
    }

    @Test
    void get_DeberiaRetornarProductoDTO() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

        ProductoGetDTO result = productoService.get(1L);

        assertEquals("reposera", result.getNombre());
        verify(productoRepository).findById(1L);
    }

    @Test
    void get_DeberiaLanzarExcepcionSiNoExiste() {
        when(productoRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> productoService.get(1L));
    }

    @Test
    void getStock_DeberiaRetornarStock() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

        int stock = productoService.getStock(1L);

        assertEquals(10, stock);
        verify(productoRepository).findById(1L);
    }

    @Test
    void modificarStock_DeberiaActualizarValoresYGuardar() {
        productoService.modificarStock(producto, 2);

        assertEquals(8, producto.getStockDisponible());
        assertEquals(2, producto.getCantidadReservada());
        verify(productoRepository).save(producto);
    }

    @Test
    void modificarStock_DeberiaLanzarExcepcionSiStockNegativo() {
        producto.setStockDisponible(1);

        assertThrows(IllegalArgumentException.class, () ->
                productoService.modificarStock(producto, 5)
        );
    }

    @Test
    void restablecerStock_DeberiaSumarStockYGuardar() {
        producto.setCantidadReservada(5);
        producto.setStockDisponible(10);

        productoService.restablecerStock(producto, 3);

        assertEquals(13, producto.getStockDisponible()); // ✅ 10 + 3
        assertEquals(2, producto.getCantidadReservada()); // ✅ 5 - 3
        verify(productoRepository).save(producto);
    }

    @Test
    void getEntity_DeberiaRetornarProducto() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

        Producto result = productoService.getEntity(1L);

        assertEquals("reposera", result.getNombre());
        verify(productoRepository).findById(1L);
    }

    @Test
    void getEntity_DeberiaLanzarExcepcionSiNoExiste() {
        when(productoRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> productoService.getEntity(1L));
    }
}
