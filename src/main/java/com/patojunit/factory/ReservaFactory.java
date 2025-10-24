package com.patojunit.factory;

import com.patojunit.dto.request.ReservaCrearEditarDTO;
import com.patojunit.dto.request.ProductoCantidadCrearEditarDTO;
import com.patojunit.helpers.reserva.ReservaCodigoService;
import com.patojunit.helpers.reserva.ReservaMapper;
import com.patojunit.validation.ReservaValidator;
import com.patojunit.model.ProductoCantidad;
import com.patojunit.model.Reserva;
import com.patojunit.model.UserSec;
import com.patojunit.model.enums.EstadoReserva;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ReservaFactory {

    private final ReservaMapper mapper;
    private final ReservaCodigoService codigoService;
    private final ReservaValidator validator;

    public Reserva crearReserva(ReservaCrearEditarDTO dto, UserSec usuario) {
        Reserva reserva = mapper.toEntity(dto);
        reserva.setUsuario(usuario);
        reserva.setEstado(EstadoReserva.PENDIENTE);
        reserva.setFechaInicio(dto.getFechaInicio());
        reserva.setCodigoReserva(codigoService.generarCodigoReserva(usuario.getId()));

        List<ProductoCantidad> productos = mapearProductos(dto.getProductos(), reserva);
        reserva.setProductos(productos);

        return reserva;
    }

    private List<ProductoCantidad> mapearProductos(List<ProductoCantidadCrearEditarDTO> productosDTO, Reserva reserva) {
        List<ProductoCantidad> lista = new ArrayList<>();

        for (ProductoCantidadCrearEditarDTO dto : productosDTO) {
            validator.validarProductoYaReservado(dto, lista);
            lista.add(mapper.toProductoCantidad(dto, reserva));
        }

        return lista;
    }
}
