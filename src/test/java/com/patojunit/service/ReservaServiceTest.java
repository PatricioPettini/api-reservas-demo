package com.patojunit.service;

import com.patojunit.dto.request.ProductoCantidadCrearEditarDTO;
import com.patojunit.dto.request.ReservaCrearEditarDTO;
import com.patojunit.dto.response.ProductoGetDTO;
import com.patojunit.dto.response.ReservaGetDTO;
import com.patojunit.model.Producto;
import com.patojunit.model.ProductoCantidad;
import com.patojunit.model.Reserva;
import com.patojunit.repository.IReservaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservaServiceTest {

    @Mock
    private IReservaRepository reservaRepository;

    @Mock
    private IProductoService productoService;

    @InjectMocks
    private ReservaService reservaService;

    private Producto producto;
    private ProductoGetDTO productoGetDTO;
    private Reserva reserva;
    private ReservaCrearEditarDTO crearDTO;
    private ProductoCantidadCrearEditarDTO pcDTO;

    @BeforeEach
    void setUp() {
        producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Reposera");
        producto.setStockDisponible(10);
        producto.setPrecioHora(BigDecimal.valueOf(50));

        productoGetDTO = new ProductoGetDTO();
        productoGetDTO.setId(1L);
        productoGetDTO.setNombre("Reposera");
        productoGetDTO.setPrecioHora(BigDecimal.valueOf(50));

        pcDTO = new ProductoCantidadCrearEditarDTO();
        pcDTO.setIdProducto(1L);
        pcDTO.setCantidad(2);

        crearDTO = new ReservaCrearEditarDTO();
        crearDTO.setTelefonoCliente("1122334455");
        crearDTO.setPagado(true);
        crearDTO.setFechaInicio(LocalDateTime.now());
        crearDTO.setFechaFin(LocalDateTime.now().plusHours(2));
        crearDTO.setProductos(List.of(pcDTO));

        ProductoCantidad pc = new ProductoCantidad();
        pc.setId(1L);
        pc.setProducto(producto);
        pc.setCantidad(2);

        reserva = new Reserva();
        reserva.setId(1L);
        reserva.setTelefonoCliente("1122334455");
        reserva.setEstado("reservado");
        reserva.setPagado(true);
        reserva.setFechaInicio(crearDTO.getFechaInicio());
        reserva.setFechaFin(crearDTO.getFechaFin());
        reserva.setProductos(List.of(pc));
    }

    @Test
    void crear_DeberiaGuardarYRetornarDTO() {
        when(productoService.getEntity(1L)).thenReturn(producto);
        when(productoService.get(1L)).thenReturn(productoGetDTO);
        when(reservaRepository.save(any(Reserva.class))).thenReturn(reserva);

        ReservaGetDTO result = reservaService.crear(crearDTO);

        assertNotNull(result);
        assertEquals("en curso", result.getEstadoActual());
        assertEquals("1122334455", result.getTelefonoCliente());
        verify(productoService).modificarStock(producto, 2);
        verify(reservaRepository).save(any(Reserva.class));
    }

    @Test
    void editar_DeberiaActualizarYGuardar() {
        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));
        when(productoService.getEntity(1L)).thenReturn(producto);
        when(productoService.get(1L)).thenReturn(productoGetDTO);
        when(reservaRepository.save(any(Reserva.class))).thenReturn(reserva);

        ReservaGetDTO result = reservaService.editar(1L, crearDTO);

        assertEquals("en curso", result.getEstadoActual());
        verify(productoService).modificarStock(producto, 2);
        verify(reservaRepository).save(any(Reserva.class));
    }

    @Test
    void editar_DeberiaLanzarExcepcionSiNoExiste() {
        when(reservaRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> reservaService.editar(1L, crearDTO));
    }

    @Test
    void cancelarReserva_DeberiaCambiarEstadoACancelado() {
        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));
        when(reservaRepository.save(any(Reserva.class))).thenReturn(reserva);

        ReservaGetDTO result = reservaService.cancelarReserva(1L);

        assertEquals("cancelado", result.getEstadoActual());
        verify(productoService).restablecerStock(producto, 2);
    }

    @Test
    void cancelarReserva_DeberiaLanzarExcepcionSiYaEstaCancelada() {
        reserva.setEstado("cancelado");
        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));

        assertThrows(IllegalArgumentException.class, () -> reservaService.cancelarReserva(1L));
    }

    @Test
    void eliminar_DeberiaEliminarSiExiste() {
        // Simulamos que la reserva existe
        Reserva reservaMock = new Reserva();
        reservaMock.setId(1L);

        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reservaMock));

        reservaService.eliminar(1L);

        verify(reservaRepository).deleteById(1L);
    }

    @Test
    void eliminar_DeberiaLanzarExcepcionSiNoExiste() {
        // Simulamos que findById devuelve vacío
        when(reservaRepository.findById(1L)).thenReturn(Optional.empty());

        // Verificamos que lance la excepción
        assertThrows(IllegalArgumentException.class, () -> reservaService.eliminar(1L));

        // Y que nunca se intente eliminar
        verify(reservaRepository, never()).deleteById(anyLong());
    }

    @Test
    void calcularPrecioTotalReserva_DeberiaMultiplicarHorasYPrecio() {
        when(productoService.get(1L)).thenReturn(productoGetDTO);

        BigDecimal total = reservaService.calcularPrecioTotalReserva(reserva);

        assertEquals(BigDecimal.valueOf(200), total); // 2h * 50 * 2 unidades
    }

    @Test
    void getAll_DeberiaRetornarListaDeReservas() {
        when(reservaRepository.findAll()).thenReturn(List.of(reserva));

        List<ReservaGetDTO> result = reservaService.getAll();

        assertEquals(1, result.size());
        assertEquals("en curso", result.get(0).getEstadoActual());
    }

    @Test
    void get_DeberiaRetornarReserva() {
        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));

        ReservaGetDTO result = reservaService.get(1L);

        assertEquals("en curso", result.getEstadoActual());
        assertEquals("1122334455", result.getTelefonoCliente());
    }

    @Test
    void get_DeberiaLanzarExcepcionSiNoExiste() {
        when(reservaRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> reservaService.get(1L));
    }

    @Test
    void crear_DeberiaLanzarExcepcionSiStockInsuficiente() {
        producto.setStockDisponible(1);
        when(productoService.getEntity(1L)).thenReturn(producto);

        assertThrows(IllegalArgumentException.class, () -> reservaService.crear(crearDTO));
    }
}
