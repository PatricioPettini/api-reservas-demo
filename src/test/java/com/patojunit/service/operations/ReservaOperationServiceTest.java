package com.patojunit.service.operations;

import com.patojunit.dto.request.ReservaCrearEditarDTO;
import com.patojunit.factory.ReservaFactory;
import com.patojunit.helpers.reserva.ReservaCalculoService;
import com.patojunit.helpers.reserva.ReservaMapper;
import com.patojunit.helpers.reserva.ReservaStockHandler;
import com.patojunit.validation.ReservaValidator;
import com.patojunit.helpers.logger.reserva.ReservaLogger;
import com.patojunit.model.*;
import com.patojunit.model.enums.EstadoReserva;
import com.patojunit.repository.IReservaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReservaOperationServiceTest {

    @Mock private ReservaFactory reservaFactory;
    @Mock private ReservaValidator validator;
    @Mock private ReservaMapper mapper;
    @Mock private ReservaStockHandler stockHandler;
    @Mock private ReservaCalculoService calculoService;
    @Mock private ReservaLogger reservaLogger;
    @Mock private ProductoOperationService productoOperationService;
    @Mock private IReservaRepository reservaRepository;

    @InjectMocks
    private ReservaOperationService reservaOperationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Debe crear una reserva correctamente")
    void crearReserva_DeberiaCrearYCalcularPrecio() {
        ReservaCrearEditarDTO dto = new ReservaCrearEditarDTO();
        UserSec usuario = new UserSec();

        Reserva reserva = new Reserva();
        when(reservaFactory.crearReserva(dto, usuario)).thenReturn(reserva);
        when(stockHandler.generarProductosConValidacion(dto, reserva)).thenReturn(List.of(new ProductoCantidad()));
        when(calculoService.calcularPrecioTotal(reserva)).thenReturn(BigDecimal.valueOf(2000));

        Reserva result = reservaOperationService.crearReserva(dto, usuario);

        verify(reservaLogger).logCreacionInicio(usuario);
        verify(reservaLogger).logCreacionExitosa(reserva);
        verify(stockHandler).generarProductosConValidacion(dto, reserva);
        verify(calculoService).calcularPrecioTotal(reserva);
        assertThat(result.getPrecioTotal()).isEqualByComparingTo("2000");
    }

    @Test
    @DisplayName("Debe editar una reserva correctamente y recalcular precio")
    void editarReserva_DeberiaEditarYActualizarDatos() {
        ReservaCrearEditarDTO dto = new ReservaCrearEditarDTO();
        Reserva reserva = new Reserva();
        reserva.setId(10L);
        reserva.setProductos(new ArrayList<>(List.of(new ProductoCantidad())));

        List<ProductoCantidad> nuevosProductos = List.of(new ProductoCantidad());
        when(stockHandler.procesarProductos(dto, reserva)).thenReturn(nuevosProductos);
        when(calculoService.calcularPrecioTotal(reserva)).thenReturn(BigDecimal.valueOf(5000));

        Reserva result = reservaOperationService.editarReserva(reserva, dto);

        verify(reservaLogger).logEdicionInicio(10L);
        verify(validator).validarProductosNoCambiados(any(), any());
        verify(mapper).actualizarDatosBasicos(reserva, dto);
        verify(reservaLogger).logEdicionExitosa(reserva);
        assertThat(result.getPrecioTotal()).isEqualByComparingTo("5000");
    }

    @Test
    @DisplayName("Debe cancelar una reserva correctamente y restablecer stock")
    void cancelarReserva_DeberiaActualizarEstadoYLoguear() {
        Reserva reserva = new Reserva();
        reserva.setId(1L);
        reserva.setEstado(EstadoReserva.ACTIVA);

        Reserva result = reservaOperationService.cancelarReserva(reserva);

        verify(reservaLogger).logCancelacionInicio(1L);
        verify(validator).validarEstadoNoCancelado(reserva);
        verify(stockHandler).restablecerStockProductos(reserva);
        verify(reservaLogger).logCambioEstado(reserva, EstadoReserva.ACTIVA, EstadoReserva.CANCELADA);
        verify(reservaLogger).logCancelacionExitosa(reserva);
        assertThat(result.getEstado()).isEqualTo(EstadoReserva.CANCELADA);
    }

    @Test
    @DisplayName("Debe eliminar productos de la reserva y recalcular precio")
    void eliminarProductosDeReserva_DeberiaEliminarYGuardar() {
        Producto prod1 = new Producto(); prod1.setId(1L); prod1.setNombre("Reposera");
        Producto prod2 = new Producto(); prod2.setId(2L); prod2.setNombre("Carpa");

        ProductoCantidad pc1 = new ProductoCantidad(); pc1.setProducto(prod1); pc1.setCantidad(2);
        ProductoCantidad pc2 = new ProductoCantidad(); pc2.setProducto(prod2); pc2.setCantidad(1);

        Reserva reserva = new Reserva();
        reserva.setId(10L);
        reserva.setProductos(new ArrayList<>(List.of(pc1, pc2)));

        when(calculoService.calcularPrecioTotal(reserva)).thenReturn(BigDecimal.valueOf(1500));

        Reserva result = reservaOperationService.eliminarProductosDeReserva(reserva, List.of(1L));

        verify(reservaLogger).logProductosEliminados(10L, 1);
        verify(reservaRepository).save(reserva);
        assertThat(result.getProductos()).hasSize(1);
        assertThat(result.getPrecioTotal()).isEqualByComparingTo("1500");
    }

    @Test
    @DisplayName("Debe lanzar excepción si la lista de IDs está vacía")
    void eliminarProductosDeReserva_DeberiaLanzarErrorPorListaVacia() {
        Reserva reserva = new Reserva();
        assertThatThrownBy(() -> reservaOperationService.eliminarProductosDeReserva(reserva, List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Debe especificar al menos un producto");
    }
}
