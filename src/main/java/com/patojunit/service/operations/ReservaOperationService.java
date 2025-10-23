package com.patojunit.service.operations;

import com.patojunit.dto.request.ReservaCrearEditarDTO;
import com.patojunit.factory.ReservaFactory;
import com.patojunit.helpers.logger.reserva.ReservaLogger;
import com.patojunit.helpers.ReservaCalculoService;
import com.patojunit.helpers.ReservaMapper;
import com.patojunit.helpers.ReservaStockHandler;
import com.patojunit.helpers.ReservaValidator;
import com.patojunit.model.Reserva;
import com.patojunit.model.UserSec;
import com.patojunit.model.enums.EstadoReserva;
import com.patojunit.repository.IReservaRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservaOperationService {

    private final ReservaFactory reservaFactory;
    private final ReservaValidator validator;
    @Getter
    private final ReservaMapper mapper;
    private final ReservaStockHandler stockHandler;
    private final ReservaCalculoService calculoService;
    private final ReservaLogger reservaLogger;
    private final ProductoOperationService productoOperationService;
    private final IReservaRepository reservaRepository;

    public Reserva crearReserva(ReservaCrearEditarDTO dto, UserSec usuario) {
        reservaLogger.logCreacionInicio(usuario);

        Reserva reserva = reservaFactory.crearReserva(dto, usuario);

        var productos = stockHandler.generarProductosConValidacion(dto, reserva);
        reserva.setProductos(productos);

        reserva.setPrecioTotal(calculoService.calcularPrecioTotal(reserva));

        reservaLogger.logCreacionExitosa(reserva);
        return reserva;
    }

    public Reserva editarReserva(Reserva reserva, ReservaCrearEditarDTO dto) {
        reservaLogger.logEdicionInicio(reserva.getId());

        validator.validarProductosNoCambiados(reserva.getProductos(), dto.getProductos());

        var productosActualizados = stockHandler.procesarProductos(dto, reserva);
        reserva.getProductos().clear();
        reserva.getProductos().addAll(productosActualizados);

        mapper.actualizarDatosBasicos(reserva, dto);
        reserva.setPrecioTotal(calculoService.calcularPrecioTotal(reserva));

        reservaLogger.logEdicionExitosa(reserva);
        return reserva;
    }

    public Reserva cancelarReserva(Reserva reserva) {
        reservaLogger.logCancelacionInicio(reserva.getId());

        validator.validarEstadoNoCancelado(reserva);
        EstadoReserva estadoAnterior = reserva.getEstado();

        reserva.setEstado(EstadoReserva.CANCELADA);
        stockHandler.restablecerStockProductos(reserva);

        reservaLogger.logCambioEstado(reserva, estadoAnterior, EstadoReserva.CANCELADA);
        reservaLogger.logCancelacionExitosa(reserva);
        return reserva;
    }

    public Reserva eliminarProductosDeReserva(Reserva reserva, List<Long> idProductos) {
        if (idProductos == null || idProductos.isEmpty()) {
            throw new IllegalArgumentException("Debe especificar al menos un producto a eliminar.");
        }

        int cantidadInicial = reserva.getProductos().size();

        reserva.getProductos().removeIf(pc -> {
            boolean eliminar = idProductos.contains(pc.getProducto().getId());
            if (eliminar) {
                try {
                    productoOperationService.restablecerStock(pc.getProducto(), pc.getCantidad());
                } catch (Exception e) {
                    reservaLogger.logError(reserva, e);
                    throw new IllegalStateException(String.format(
                            "Error al restablecer stock del producto '%s' (ID=%d): %s",
                            pc.getProducto().getNombre(),
                            pc.getProducto().getId(),
                            e.getMessage()
                    ));
                }
            }
            return eliminar;
        });

        int cantidadEliminada = cantidadInicial - reserva.getProductos().size();
        reservaLogger.logProductosEliminados(reserva.getId(), cantidadEliminada);

        reserva.setPrecioTotal(calculoService.calcularPrecioTotal(reserva));

        reservaRepository.save(reserva);
        return reserva;
    }
}