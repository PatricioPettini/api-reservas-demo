package com.patojunit.helpers.reserva;

import com.patojunit.dto.response.ProductoUserGetDTO;
import com.patojunit.model.ProductoCantidad;
import com.patojunit.model.Reserva;
import com.patojunit.service.interfaces.IProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Duration;

@Component
@RequiredArgsConstructor
public class ReservaCalculoService {

    private final IProductoService productoService;

    public BigDecimal calcularPrecioTotal(Reserva reserva) {
        BigDecimal horas = calcularDuracionEnHoras(reserva);
        return reserva.getProductos().stream()
                .map(pc -> calcularSubtotalProducto(pc, horas))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calcularDuracionEnHoras(Reserva reserva) {
        if (reserva.getFechaInicio() == null || reserva.getFechaFin() == null)
            throw new IllegalArgumentException("Las fechas de la reserva no pueden ser nulas.");

        long horas = Duration.between(reserva.getFechaInicio(), reserva.getFechaFin()).toHours();
        return BigDecimal.valueOf(horas);
    }

    private BigDecimal calcularSubtotalProducto(ProductoCantidad pc, BigDecimal horas) {
        ProductoUserGetDTO producto = productoService.get(pc.getProducto().getId());
        return producto.getPrecioHora()
                .multiply(horas)
                .multiply(BigDecimal.valueOf(pc.getCantidad()));
    }
}