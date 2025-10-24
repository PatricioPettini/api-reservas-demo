package com.patojunit.factory;

import com.patojunit.dto.request.ProductoCantidadCrearEditarDTO;
import com.patojunit.dto.request.ReservaCrearEditarDTO;
import com.patojunit.helpers.reserva.ReservaCodigoService;
import com.patojunit.helpers.reserva.ReservaMapper;
import com.patojunit.validation.ReservaValidator;
import com.patojunit.model.Producto;
import com.patojunit.model.ProductoCantidad;
import com.patojunit.model.Reserva;
import com.patojunit.model.UserSec;
import com.patojunit.model.enums.EstadoReserva;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReservaFactoryTest {

    @Mock private ReservaMapper mapper;
    @Mock private ReservaCodigoService codigoService;
    @Mock private ReservaValidator validator;

    @InjectMocks
    private ReservaFactory factory;

    private UserSec usuario;
    private ReservaCrearEditarDTO dto;
    private ProductoCantidadCrearEditarDTO productoDTO;
    private Reserva reservaBase;
    private ProductoCantidad productoCantidad;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        usuario = new UserSec();
        usuario.setId(1L);
        usuario.setUsername("juan");

        productoDTO = new ProductoCantidadCrearEditarDTO(10L, 2);

        dto = new ReservaCrearEditarDTO(
                List.of(productoDTO),
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(1),
                false
        );

        reservaBase = new Reserva();
        productoCantidad = new ProductoCantidad();
        productoCantidad.setProducto(new Producto());
    }

    @Test
    @DisplayName("Debe crear una reserva correctamente con estado PENDIENTE")
    void crearReserva_DeberiaCrearCorrectamente() {
        // Arrange
        when(mapper.toEntity(dto)).thenReturn(reservaBase);
        when(codigoService.generarCodigoReserva(1L)).thenReturn("RES-ABC123");
        when(mapper.toProductoCantidad(any(), any())).thenReturn(productoCantidad);

        // Act
        Reserva result = factory.crearReserva(dto, usuario);

        // Assert
        assertNotNull(result);
        assertEquals(usuario, result.getUsuario());
        assertEquals(EstadoReserva.PENDIENTE, result.getEstado());
        assertEquals("RES-ABC123", result.getCodigoReserva());
        assertEquals(dto.getFechaInicio(), result.getFechaInicio());
        assertEquals(1, result.getProductos().size());

        verify(validator).validarProductoYaReservado(eq(productoDTO), anyList());
        verify(mapper).toEntity(dto);
        verify(mapper).toProductoCantidad(eq(productoDTO), any(Reserva.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción si el validator falla")
    void crearReserva_DeberiaLanzarExcepcionSiProductoDuplicado() {
        when(mapper.toEntity(dto)).thenReturn(reservaBase);
        doThrow(new IllegalArgumentException("Producto duplicado")).when(validator)
                .validarProductoYaReservado(any(), anyList());

        assertThrows(IllegalArgumentException.class, () ->
                factory.crearReserva(dto, usuario));

        verify(validator).validarProductoYaReservado(eq(productoDTO), anyList());
    }

    @Test
    @DisplayName("Debe invocar mapper y codigoService correctamente")
    void crearReserva_DeberiaInvocarDependencias() {
        when(mapper.toEntity(dto)).thenReturn(reservaBase);
        when(codigoService.generarCodigoReserva(1L)).thenReturn("RES-CODE-1");
        when(mapper.toProductoCantidad(any(), any())).thenReturn(productoCantidad);

        factory.crearReserva(dto, usuario);

        verify(mapper).toEntity(dto);
        verify(codigoService).generarCodigoReserva(1L);
        verify(mapper).toProductoCantidad(any(), any());
    }
}
