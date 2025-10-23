package com.patojunit.service.implementations;

import com.patojunit.dto.request.ReservaCrearEditarDTO;
import com.patojunit.dto.response.ReservaUserGetDTO;
import com.patojunit.helpers.ReservaPermissionValidator;
import com.patojunit.helpers.logger.reserva.ReservaLogger;
import com.patojunit.helpers.security.*;
import com.patojunit.model.Reserva;
import com.patojunit.model.UserSec;
import com.patojunit.repository.IReservaRepository;
import com.patojunit.service.operations.ReservaOperationService;
import com.patojunit.service.interfaces.IReservaService;
import com.patojunit.service.interfaces.IUserService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservaService implements IReservaService {

    private final IReservaRepository reservaRepository;
    private final IUserService userService;

    private final RoleBasedMapper roleBasedMapper;
    private final ReservaPermissionValidator permisoValidator;
    private final JwtUserProvider jwtUserProvider;
    private final JwtRoleValidator jwtRoleValidator;
    private final ReservaOperationService operationService;
    private final ReservaLogger reservaLogger;

    @Override
    @Transactional
    public ReservaUserGetDTO crear(ReservaCrearEditarDTO dto) {
        UserSec usuario = obtenerUsuarioAutenticado();
        permisoValidator.validarPermisosGenerales(usuario);

        try {
            Reserva reserva = operationService.crearReserva(dto, usuario);
            Reserva guardada = reservaRepository.save(reserva);

            reservaLogger.logCreacionExitosa(guardada);
            return mapearPorRol(guardada);
        } catch (Exception e) {
            reservaLogger.logErrorGeneral("Error al crear reserva", e);
            throw e;
        }
    }

    @Override
    @Transactional
    public ReservaUserGetDTO editar(Long id, ReservaCrearEditarDTO dto) {
        Reserva reserva = getEntity(id);
        permisoValidator.validarAccesoAReserva(reserva.getUsuario().getUsername());

        try {
            Reserva actualizada = operationService.editarReserva(reserva, dto);
            Reserva guardada = reservaRepository.save(actualizada);

            reservaLogger.logEdicionExitosa(guardada);
            return mapearPorRol(guardada);
        } catch (Exception e) {
            reservaLogger.logError(reserva, e);
            throw e;
        }
    }

    @Override
    @Transactional
    public ReservaUserGetDTO cancelarReserva(Long id) {
        Reserva reserva = getEntity(id);
        permisoValidator.validarAccesoAReserva(reserva.getUsuario().getUsername());

        try {
            Reserva cancelada = operationService.cancelarReserva(reserva);
            Reserva guardada = reservaRepository.save(cancelada);

            reservaLogger.logCancelacionExitosa(guardada);
            return mapearPorRol(guardada);
        } catch (Exception e) {
            reservaLogger.logError(reserva, e);
            throw e;
        }
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Reserva reserva = getEntity(id);
        permisoValidator.validarAccesoAReserva(reserva.getUsuario().getUsername());

        try {
            reservaRepository.delete(reserva);
            reservaLogger.logEliminacionExitosa(id);
        } catch (Exception e) {
            reservaLogger.logError(reserva, e);
            throw e;
        }
    }

    @Override
    public List<ReservaUserGetDTO> getAll() {
        boolean isAdmin = jwtRoleValidator.isAdmin();
        String username = jwtUserProvider.getUsuarioAutenticadoUsername();

        List<Reserva> reservas = isAdmin
                ? reservaRepository.findAll()
                : reservaRepository.findByUsuario_Username(username);

        return reservas.stream()
                .map(this::mapearPorRol)
                .toList();
    }

    @Override
    public ReservaUserGetDTO get(Long id) {
        Reserva reserva = getEntity(id);
        permisoValidator.validarAccesoAReserva(reserva.getUsuario().getUsername());
        return mapearPorRol(reserva);
    }

    @Override
    @Transactional
    public ReservaUserGetDTO eliminarProductos(Long idReserva, List<Long> idProductos) {
        Reserva reserva = getEntity(idReserva);
        permisoValidator.validarAccesoAReserva(reserva.getUsuario().getUsername());

        try {
            Reserva actualizada = operationService.eliminarProductosDeReserva(reserva, idProductos);
            reservaLogger.logProductosEliminados(reserva.getId(), idProductos.size());
            return mapearPorRol(actualizada);
        } catch (Exception e) {
            reservaLogger.logError(reserva, e);
            throw e;
        }
    }

    @Override
    public Reserva getEntity(Long id) {
        return reservaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No existe reserva con ID " + id));
    }

    private UserSec obtenerUsuarioAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userService.findByUsername(auth.getName());
    }

    private ReservaUserGetDTO mapearPorRol(Reserva reserva) {
        return roleBasedMapper.mapByRole(
                reserva,
                r -> operationService.getMapper().toUserGetDTO(r),
                r -> operationService.getMapper().toAdminGetDTO(r)
        );
    }
}