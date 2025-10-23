package com.patojunit.service.implementations;

import com.patojunit.dto.request.ReservaCrearEditarDTO;
import com.patojunit.dto.response.ReservaUserGetDTO;
import com.patojunit.helpers.logger.reserva.ReservaLogger;
import com.patojunit.helpers.ReservaPermissionValidator;
import com.patojunit.helpers.security.JwtRoleValidator;
import com.patojunit.helpers.security.JwtUserProvider;
import com.patojunit.helpers.security.RoleBasedMapper;
import com.patojunit.model.Reserva;
import com.patojunit.model.UserSec;
import com.patojunit.repository.IReservaRepository;
import com.patojunit.service.interfaces.IUserService;
import com.patojunit.service.operations.ReservaOperationService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class ReservaServiceTest {

    @Mock private IReservaRepository reservaRepository;
    @Mock private IUserService userService;
    @Mock private RoleBasedMapper roleBasedMapper;
    @Mock private ReservaPermissionValidator permisoValidator;
    @Mock private JwtUserProvider jwtUserProvider;
    @Mock private JwtRoleValidator jwtRoleValidator;
    @Mock private ReservaOperationService operationService;
    @Mock private ReservaLogger reservaLogger;

    @InjectMocks
    private ReservaService reservaService;

    @Mock private Authentication authentication;
    @Mock private SecurityContext securityContext;

    private Reserva reserva;
    private ReservaCrearEditarDTO dto;
    private UserSec usuario;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        usuario = new UserSec();
        usuario.setUsername("juan");

        reserva = new Reserva();
        reserva.setId(1L);
        reserva.setUsuario(usuario);

        dto = new ReservaCrearEditarDTO(List.of(), null, null, false);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("juan");
        SecurityContextHolder.setContext(securityContext);
        when(userService.findByUsername("juan")).thenReturn(usuario);
    }

    @Test
    @DisplayName("Debe crear una reserva correctamente")
    void crear_DeberiaGuardarYRetornarDTO() {
        ReservaCrearEditarDTO dto = new ReservaCrearEditarDTO();
        Reserva reserva = new Reserva();
        ReservaUserGetDTO dtoResponse = new ReservaUserGetDTO();
        UserSec usuario = new UserSec();

        when(userService.findByUsername(any())).thenReturn(usuario);
        when(operationService.crearReserva(dto, usuario)).thenReturn(reserva);
        when(reservaRepository.save(reserva)).thenReturn(reserva);
        when(roleBasedMapper.mapByRole(any(), any(), any())).thenReturn(dtoResponse);

        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("user1");
        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(context);

        ReservaUserGetDTO result = reservaService.crear(dto);

        verify(permisoValidator).validarPermisosGenerales(usuario);
        verify(operationService).crearReserva(dto, usuario);
        verify(reservaRepository).save(reserva);
        verify(reservaLogger).logCreacionExitosa(reserva);
        assertThat(result).isEqualTo(dtoResponse);
    }

    @Test
    @DisplayName("Debe editar una reserva correctamente")
    void editar_DeberiaActualizarYRetornarDTO() {
        ReservaCrearEditarDTO dto = new ReservaCrearEditarDTO();
        Reserva reserva = new Reserva();
        reserva.setId(10L);
        reserva.setUsuario(new UserSec());

        ReservaUserGetDTO dtoResponse = new ReservaUserGetDTO();
        when(reservaRepository.findById(10L)).thenReturn(Optional.of(reserva));
        when(operationService.editarReserva(reserva, dto)).thenReturn(reserva);
        when(reservaRepository.save(reserva)).thenReturn(reserva);
        when(roleBasedMapper.mapByRole(eq(reserva), any(), any()))
                .thenReturn(dtoResponse);

        ReservaUserGetDTO result = reservaService.editar(10L, dto);

        verify(permisoValidator).validarAccesoAReserva(any());
        verify(reservaLogger).logEdicionExitosa(reserva);
        assertThat(result).isEqualTo(dtoResponse);
    }

    @Test
    @DisplayName("Debe cancelar una reserva correctamente")
    void cancelarReserva_DeberiaCancelarYRetornarDTO() {
        Reserva reserva = new Reserva();
        reserva.setId(5L);
        reserva.setUsuario(new UserSec());
        ReservaUserGetDTO dtoResponse = new ReservaUserGetDTO();

        when(reservaRepository.findById(5L)).thenReturn(Optional.of(reserva));
        when(operationService.cancelarReserva(reserva)).thenReturn(reserva);
        when(reservaRepository.save(reserva)).thenReturn(reserva);
        when(roleBasedMapper.mapByRole(eq(reserva), any(), any()))
                .thenReturn(dtoResponse);

        ReservaUserGetDTO result = reservaService.cancelarReserva(5L);

        verify(permisoValidator).validarAccesoAReserva(any());
        verify(reservaLogger).logCancelacionExitosa(reserva);
        assertThat(result).isEqualTo(dtoResponse);
    }

    @Test
    @DisplayName("Debe eliminar una reserva correctamente")
    void eliminar_DeberiaEliminarYLoguear() {
        Reserva reserva = new Reserva();
        reserva.setId(7L);
        reserva.setUsuario(new UserSec());
        when(reservaRepository.findById(7L)).thenReturn(Optional.of(reserva));

        reservaService.eliminar(7L);

        verify(permisoValidator).validarAccesoAReserva(any());
        verify(reservaRepository).delete(reserva);
        verify(reservaLogger).logEliminacionExitosa(7L);
    }

    @Test
    @DisplayName("Debe devolver todas las reservas si el usuario es admin")
    void getAll_DeberiaRetornarTodasLasReservas() {
        Reserva r1 = new Reserva();
        when(jwtRoleValidator.isAdmin()).thenReturn(true);
        when(reservaRepository.findAll()).thenReturn(List.of(r1));
        when(roleBasedMapper.mapByRole(any(), any(), any())).thenReturn(new ReservaUserGetDTO());

        List<ReservaUserGetDTO> result = reservaService.getAll();

        verify(reservaRepository).findAll();
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("Debe devolver solo reservas del usuario si no es admin")
    void getAll_DeberiaFiltrarPorUsuario() {
        Reserva r1 = new Reserva();
        when(jwtRoleValidator.isAdmin()).thenReturn(false);
        when(jwtUserProvider.getUsuarioAutenticadoUsername()).thenReturn("user1");
        when(reservaRepository.findByUsuario_Username("user1")).thenReturn(List.of(r1));
        when(roleBasedMapper.mapByRole(any(), any(), any())).thenReturn(new ReservaUserGetDTO());

        List<ReservaUserGetDTO> result = reservaService.getAll();

        verify(reservaRepository).findByUsuario_Username("user1");
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("Debe devolver una reserva por ID")
    void get_DeberiaRetornarReservaPorId() {
        Reserva reserva = new Reserva();
        reserva.setUsuario(new UserSec());
        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));
        when(roleBasedMapper.mapByRole(any(), any(), any())).thenReturn(new ReservaUserGetDTO());

        ReservaUserGetDTO result = reservaService.get(1L);

        verify(permisoValidator).validarAccesoAReserva(any());
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("Debe lanzar excepción si la reserva no existe")
    void getEntity_DeberiaLanzarEntityNotFound() {
        when(reservaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservaService.getEntity(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("No existe reserva");
    }

    @Test
    @DisplayName("Debe eliminar productos de una reserva correctamente")
    void eliminarProductos_DeberiaActualizarYLoguear() {
        Reserva reserva = new Reserva();
        reserva.setId(3L);
        reserva.setUsuario(new UserSec());
        ReservaUserGetDTO dtoResponse = new ReservaUserGetDTO();

        when(reservaRepository.findById(3L)).thenReturn(Optional.of(reserva));
        when(operationService.eliminarProductosDeReserva(reserva, List.of(1L))).thenReturn(reserva);
        when(roleBasedMapper.mapByRole(eq(reserva), any(), any()))
                .thenReturn(dtoResponse);

        ReservaUserGetDTO result = reservaService.eliminarProductos(3L, List.of(1L));

        verify(operationService).eliminarProductosDeReserva(reserva, List.of(1L));
        verify(reservaLogger).logProductosEliminados(3L, 1);
        assertThat(result).isEqualTo(dtoResponse);
    }

    @Test
    @DisplayName("Debe loguear error y relanzar excepción en crear()")
    void crear_DeberiaLanzarExcepcionYLoguear() {
        when(operationService.crearReserva(dto, usuario))
                .thenThrow(new RuntimeException("Error al crear reserva"));

        assertThrows(RuntimeException.class, () -> reservaService.crear(dto));

        verify(reservaLogger).logErrorGeneral(eq("Error al crear reserva"), any(RuntimeException.class));
    }

    @Test
    @DisplayName("Debe loguear error y relanzar excepción en editar()")
    void editar_DeberiaLanzarExcepcionYLoguear() {
        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));
        when(operationService.editarReserva(reserva, dto))
                .thenThrow(new RuntimeException("Error al editar"));

        assertThrows(RuntimeException.class, () -> reservaService.editar(1L, dto));

        verify(reservaLogger).logError(eq(reserva), any(RuntimeException.class));
    }

    @Test
    @DisplayName("Debe loguear error y relanzar excepción en cancelarReserva()")
    void cancelar_DeberiaLanzarExcepcionYLoguear() {
        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));
        when(operationService.cancelarReserva(reserva))
                .thenThrow(new RuntimeException("Error al cancelar"));

        assertThrows(RuntimeException.class, () -> reservaService.cancelarReserva(1L));

        verify(reservaLogger).logError(eq(reserva), any(RuntimeException.class));
    }

    @Test
    @DisplayName("Debe loguear error y relanzar excepción en eliminar()")
    void eliminar_DeberiaLanzarExcepcionYLoguear() {
        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));
        doThrow(new RuntimeException("Error al eliminar")).when(reservaRepository).delete(reserva);

        assertThrows(RuntimeException.class, () -> reservaService.eliminar(1L));

        verify(reservaLogger).logError(eq(reserva), any(RuntimeException.class));
    }

    @Test
    @DisplayName("Debe loguear error y relanzar excepción en eliminarProductos()")
    void eliminarProductos_DeberiaLanzarExcepcionYLoguear() {
        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));
        when(operationService.eliminarProductosDeReserva(reserva, List.of(10L)))
                .thenThrow(new RuntimeException("Error eliminando productos"));

        assertThrows(RuntimeException.class, () ->
                reservaService.eliminarProductos(1L, List.of(10L)));

        verify(reservaLogger).logError(eq(reserva), any(RuntimeException.class));
    }

    @Test
    @DisplayName("Debe lanzar EntityNotFoundException si la reserva no existe")
    void getEntity_DeberiaLanzarEntityNotFoundException() {
        when(reservaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
                reservaService.getEntity(999L));
    }
}
