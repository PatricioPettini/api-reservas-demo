package com.patojunit.service;

import com.patojunit.model.Producto;
import com.patojunit.model.Reserva;
import com.patojunit.repository.IReservaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservaServiceTest {

    @Mock
    private IReservaRepository reservaRepository;

    @Mock
    private ProductoService productoService;

    @Spy
    @InjectMocks
    private ReservaService reservaService;

    private Producto producto;



    @BeforeEach
    void setUp() {
        producto = new Producto();
        producto.setId(1L);
        producto.setPrecioHora(BigDecimal.valueOf(100)); // 👈 Precio válido
        producto.setNombre("Producto test");

    }

    @Test
    void eliminar_DeberiaEliminarCuandoExiste() {
        // Simula que la reserva existe
        Reserva reserva = new Reserva();
        reserva.setId(1L);
        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));

        // Ejecuta el método
        reservaService.eliminar(1L);

        // Verifica que se haya eliminado
        verify(reservaRepository, times(1)).deleteById(1L);
    }

    @Test
    void eliminar_DeberiaLanzarExcepcionCuandoNoExiste() {
        when(reservaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            reservaService.eliminar(99L);
        });

        verify(reservaRepository, never()).deleteById(anyLong());
    }

    @Test
    void editar_DeberiaActualizarReservaCuandoExiste() {

        Reserva reservaExistente = new Reserva();
        reservaExistente.setId(1L);
        reservaExistente.setProductos(new ArrayList<>());
        reservaExistente.setTelefonoCliente("111111");

        Reserva reservaEditada = new Reserva();
        reservaEditada.setEstado("CONFIRMADA");
        reservaEditada.setFechaInicio(LocalDateTime.now().plusMinutes(2));
        reservaEditada.setFechaFin(LocalDateTime.now().plusMinutes(3));
        reservaEditada.setPagado(true);
        reservaEditada.setTelefonoCliente("222222");
        reservaEditada.setProductos(List.of(producto));
        // Simula que existe una reserva con el ID dado
        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reservaExistente));

        // Simula que el producto existe
        when(productoService.get(1L)).thenReturn(producto);

        doNothing().when(reservaService).verificarReserva(anyLong());

        // Simula el guardado exitoso
        when(reservaRepository.save(any(Reserva.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Ejecuta el método
        Reserva resultado = reservaService.editar(1L, reservaEditada);

        // Validaciones
        assertNotNull(resultado);
        assertEquals("CONFIRMADA", resultado.getEstado());
        assertEquals(1, resultado.getProductos().size());
        assertTrue(resultado.getPagado());
        assertEquals("222222", resultado.getTelefonoCliente());

        // Verifica que haya guardado
        verify(reservaRepository, times(1)).save(any(Reserva.class));
    }

    @Test
    void editar_DeberiaLanzarExcepcionCuandoNoExisteReserva() {

        Reserva reservaEditada = new Reserva();
        reservaEditada.setEstado("CONFIRMADA");
        reservaEditada.setFechaInicio(LocalDateTime.now().plusMinutes(2));
        reservaEditada.setFechaFin(LocalDateTime.now().plusMinutes(3));
        reservaEditada.setPagado(true);
        reservaEditada.setTelefonoCliente("222222");
        reservaEditada.setProductos(List.of(producto));

        // Simula que no se encuentra la reserva
        when(reservaRepository.findById(99L)).thenReturn(Optional.empty());

        // Verifica que lanza excepción
        assertThrows(IllegalArgumentException.class, () -> {
            reservaService.editar(99L, reservaEditada);
        });

        verify(reservaRepository, never()).save(any());
    }

    @Test
    void crear_DeberiaCrearReservaConCodigoYPrecio() {
        // Simula que la verificación de stock no lanza excepción
        doNothing().when(reservaService).verificarReserva(anyLong());

        // Simula que el producto existe
        when(productoService.get(1L)).thenReturn(producto);

        // Simula el cálculo del precio total
        doReturn(BigDecimal.valueOf(300)).when(reservaService).calcularPrecioTotalReserva(any(Reserva.class));

        // Simula el guardado
        when(reservaRepository.save(any(Reserva.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Crea una reserva nueva con 1 producto
        Reserva nueva = new Reserva();
        nueva.setTelefonoCliente("123456789");
        nueva.setProductos(List.of(producto));

        // Ejecuta el método
        Reserva resultado = reservaService.crear(nueva);

        // Validaciones
        assertNotNull(resultado);
        assertNotNull(resultado.getCodigoReserva());
        assertTrue(resultado.getCodigoReserva().startsWith("RES-"));
        assertEquals(1, resultado.getProductos().size());
        assertEquals(BigDecimal.valueOf(300), resultado.getPrecioTotal());

        // Verifica que se haya guardado
        verify(reservaRepository, times(1)).save(any(Reserva.class));
    }

    @Test
    void crear_DeberiaLanzarExcepcionCuandoNoHayStock() {
        // 🔹 Producto con stock 0
        Producto sinStock = new Producto();
        sinStock.setId(1L);
        sinStock.setNombre("Producto sin stock");
        sinStock.setStockDisponible(0); // 👈 clave

        // 🔹 Reserva con ese producto
        Reserva reservaSinStock = new Reserva();
        reservaSinStock.setTelefonoCliente("123456789");
        reservaSinStock.setProductos(List.of(sinStock));

        // 🔹 Mock: cuando productoService.get() se llama, devuelve el producto sin stock
        when(productoService.get(1L)).thenReturn(sinStock);

        // ✅ Esperamos que lance IllegalArgumentException
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            reservaService.crear(reservaSinStock);
        });

        assertEquals("No hay stock disponible para este producto!", ex.getMessage());

        // Verifica que nunca se haya guardado la reserva
        verify(reservaRepository, never()).save(any(Reserva.class));
    }

    @Test
    void modificarStock_DeberiaLlamarAlServicioDeProducto() {
        // Simula un producto cualquiera
        producto = new Producto();
        producto.setId(1L);

        // Ejecuta el método
        reservaService.modificarStock(producto);

        // Verifica que el servicio de producto fue llamado
        verify(productoService, times(1)).modificarStock(producto);
    }

    @Test
    void getAll_DeberiaRetornarListaDeReservas() {
        // Mockea la respuesta del repositorio
        Reserva r1 = new Reserva();
        Reserva r2 = new Reserva();
        when(reservaRepository.findAll()).thenReturn(List.of(r1, r2));

        // Ejecuta el método
        List<Reserva> resultado = reservaService.getAll();

        // Validaciones
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(reservaRepository, times(1)).findAll();
    }

    @Test
    void get_DeberiaRetornarReservaCuandoExiste() {
        Reserva reserva = new Reserva();
        reserva.setId(1L);
        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));

        Reserva resultado = reservaService.get(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        verify(reservaRepository, times(1)).findById(1L);
    }

    @Test
    void get_DeberiaLanzarExcepcionCuandoNoExiste() {
        when(reservaRepository.findById(99L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            reservaService.get(99L);
        });

        assertEquals("No existe reserva con ese id", ex.getMessage());
        verify(reservaRepository, times(1)).findById(99L);
    }

    @Test
    void cancelarReserva_DeberiaCancelarYRestablecerStock() {
        // 🔹 Producto en la reserva
        producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Producto A");

        // 🔹 Reserva existente con estado distinto de "cancelado"
        Reserva reserva = new Reserva();
        reserva.setId(1L);
        reserva.setEstado("confirmada");
        reserva.setProductos(List.of(producto));

        // 🔹 Mock: get(id)
        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));

        // 🔹 Mock: productoService.get(productoId)
        when(productoService.get(1L)).thenReturn(producto);

        // 🔹 Mock: evitar lógica real del stock
        doNothing().when(reservaService).restablecerStock(any(Producto.class));

        // 🔹 Mock: guardar reserva
        when(reservaRepository.save(any(Reserva.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Ejecuta el método
        Reserva resultado = reservaService.cancelarReserva(1L);

        // ✅ Validaciones
        assertEquals("cancelado", resultado.getEstado());
        verify(reservaService, times(1)).restablecerStock(producto);
        verify(reservaRepository, times(1)).save(reserva);
    }

    @Test
    void cancelarReserva_DeberiaLanzarExcepcionSiYaEstaCancelada() {
        // 🔹 Reserva ya cancelada
        Reserva reserva = new Reserva();
        reserva.setId(1L);
        reserva.setEstado("cancelado");

        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));

        // Ejecuta y valida excepción
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            reservaService.cancelarReserva(1L);
        });

        assertEquals("La reserva ya se encuentra cancelada", ex.getMessage());

        // Verifica que NO se haya guardado ni modificado stock
        verify(reservaRepository, never()).save(any());
        verify(productoService, never()).restablecerStock(any());
    }

    @Test
    void restablecerStock_DeberiaLlamarAlServicioDeProducto() {
        producto = new Producto();
        producto.setId(1L);

        reservaService.restablecerStock(producto);

        verify(productoService, times(1)).restablecerStock(producto);
    }

    @Test
    void calcularPrecioTotalReserva_DeberiaRetornarTotalCorrecto() {
        // 🔹 Simula un producto con precio por hora
        producto = new Producto();
        producto.setId(1L);
        producto.setPrecioHora(BigDecimal.valueOf(100));

        // 🔹 Reserva de 5 horas con ese producto
        Reserva reserva = new Reserva();
        reserva.setProductos(List.of(producto));
        reserva.setFechaInicio(LocalDateTime.now());
        reserva.setFechaFin(reserva.getFechaInicio().plusHours(5));

        // 🔹 Mock: obtener el producto desde el service
        when(productoService.get(1L)).thenReturn(producto);

        // Ejecuta el cálculo
        BigDecimal total = reservaService.calcularPrecioTotalReserva(reserva);

        // ✅ 100 * 5 = 500
        assertEquals(BigDecimal.valueOf(500), total);
        verify(productoService, times(1)).get(1L);
    }

    @Test
    void getEstadoActual_DeberiaCalcularSegunFechas() {
        Reserva reserva = new Reserva();
        reserva.setFechaInicio(LocalDateTime.now().plusDays(1));
        reserva.setFechaFin(LocalDateTime.now().plusDays(2));

        assertEquals("reservado", reserva.getEstadoActual());

        reserva.setFechaInicio(LocalDateTime.now().minusDays(1));
        reserva.setFechaFin(LocalDateTime.now().plusDays(1));
        assertEquals("en curso", reserva.getEstadoActual());

        reserva.setFechaInicio(LocalDateTime.now().minusDays(3));
        reserva.setFechaFin(LocalDateTime.now().minusDays(1));
        assertEquals("devuelto", reserva.getEstadoActual());

        reserva.setFechaInicio(null);
        reserva.setFechaFin(null);
        assertEquals("reservado", reserva.getEstadoActual());
    }

}
