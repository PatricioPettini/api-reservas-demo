package com.patojunit.helpers;

import com.patojunit.dto.request.ProductoCantidadCrearEditarDTO;
import com.patojunit.model.ProductoCantidad;
import com.patojunit.model.Reserva;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

import static com.patojunit.model.enums.EstadoReserva.CANCELADA;

@Component
public class ReservaValidator {

    public void validarProductoYaReservado(ProductoCantidadCrearEditarDTO p, List<ProductoCantidad> productosActualizados) {
        boolean yaReservado = productosActualizados.stream()
                .anyMatch(prod -> prod.getProducto().getId().equals(p.getIdProducto()));
        if (yaReservado)
            throw new IllegalArgumentException("El producto ya se encuentra en esta reserva.");
    }

    public void validarProductosNoCambiados(List<ProductoCantidad> productos, List<ProductoCantidadCrearEditarDTO> nuevos) {
        var idsOriginales = productos.stream().map(pc -> pc.getProducto().getId()).collect(Collectors.toSet());
        var idsNuevos = nuevos.stream().map(ProductoCantidadCrearEditarDTO::getIdProducto).collect(Collectors.toSet());
        if (!idsOriginales.equals(idsNuevos))
            throw new IllegalArgumentException("No puede modificar el ID de un producto.");
    }

    public void validarEstadoNoCancelado(Reserva reserva) {
        if (CANCELADA.equals(reserva.getEstado()))
            throw new IllegalArgumentException("La reserva ya se encuentra cancelada.");
    }
}
