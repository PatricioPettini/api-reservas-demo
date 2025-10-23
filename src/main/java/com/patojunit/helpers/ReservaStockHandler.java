package com.patojunit.helpers;

import com.patojunit.dto.request.ReservaCrearEditarDTO;
import com.patojunit.model.Producto;
import com.patojunit.model.ProductoCantidad;
import com.patojunit.model.Reserva;
import com.patojunit.model.enums.EstadoReserva;
import com.patojunit.service.operations.ProductoOperationService;
import com.patojunit.service.interfaces.IProductoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReservaStockHandler {

    private final IProductoService productoService;
    private final ReservaMapper mapper;
    private final ReservaValidator validator;
    private final ProductoOperationService productoOperationService;

    public List<ProductoCantidad> generarProductosConValidacion(ReservaCrearEditarDTO dto, Reserva reserva) {
        return procesarProductos(dto, reserva, true);
    }

    public List<ProductoCantidad> procesarProductos(ReservaCrearEditarDTO dto, Reserva reserva) {
        return procesarProductos(dto, reserva, false);
    }

    private List<ProductoCantidad> procesarProductos(ReservaCrearEditarDTO dto, Reserva reserva, boolean esNueva) {
        List<ProductoCantidad> productos = new ArrayList<>();

        dto.getProductos().forEach(p -> {
            if (!esNueva) {
                validator.validarProductoYaReservado(p, productos);
            }

            int cantidadReservada = esNueva ? 0 : obtenerCantidadReservadaActual(reserva, p.getIdProducto());
            actualizarStockProducto(p.getIdProducto(), p.getCantidad(), cantidadReservada);
            productos.add(mapper.toProductoCantidad(p, reserva));
        });

        return productos;
    }

    public void descontarStockProductos(Reserva reserva) {
        if (reserva.getEstado() != EstadoReserva.PENDIENTE) {
            throw new IllegalStateException("La reserva no está pendiente; no se puede descontar stock.");
        }

        reserva.getProductos().forEach(pc -> {
            productoOperationService.descontarStock(pc.getProducto(), pc.getCantidad());
            log.info("[StockHandler] Descontado stock de '{}' ({} unidades) para reserva ID={}",
                    pc.getProducto().getNombre(), pc.getCantidad(), reserva.getId());
        });
    }

    public void restablecerStockProductos(Reserva reserva) {
        reserva.getProductos().forEach(pc -> {
            productoOperationService.restablecerStock(pc.getProducto(), pc.getCantidad());
            log.info("[StockHandler] Restablecido stock de '{}' (+{} unidades) tras finalizar reserva ID={}",
                    pc.getProducto().getNombre(), pc.getCantidad(), reserva.getId());
        });
    }

    private void actualizarStockProducto(Long idProducto, int cantidadNueva, int cantidadReservada) {
        Producto producto = productoOperationService.getEntity(idProducto);
        int stockDisponible = producto.getStockDisponible();
        int stockTotalPosible = stockDisponible + cantidadReservada;

        if (cantidadNueva > cantidadReservada) {
            if (cantidadNueva > stockTotalPosible) {
                throw new IllegalArgumentException(String.format(
                        "Stock insuficiente para '%s' (disponible: %d, solicitado: %d)",
                        producto.getNombre(), stockTotalPosible, cantidadNueva
                ));
            }
            int cantidadADescontar = cantidadNueva - cantidadReservada;
            productoOperationService.descontarStock(producto, cantidadADescontar);
        } else if (cantidadNueva < cantidadReservada) {
            int cantidadAReponer = cantidadReservada - cantidadNueva;
            productoOperationService.restablecerStock(producto, cantidadAReponer);
        }
    }

    private int obtenerCantidadReservadaActual(Reserva reserva, Long idProducto) {
        return reserva.getProductos().stream()
                .filter(pc -> pc.getProducto().getId().equals(idProducto))
                .map(ProductoCantidad::getCantidad)
                .findFirst()
                .orElse(0);
    }
}