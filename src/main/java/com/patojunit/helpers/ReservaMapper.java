package com.patojunit.helpers;

import com.patojunit.dto.request.ReservaCrearEditarDTO;
import com.patojunit.dto.request.ProductoCantidadCrearEditarDTO;
import com.patojunit.dto.response.*;
import com.patojunit.model.*;
import com.patojunit.service.operations.ProductoOperationService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ReservaMapper {

    private final ModelMapper modelMapper;

    private final ProductoOperationService productoOperationService;

    public Reserva toEntity(ReservaCrearEditarDTO dto) {
        Reserva r = new Reserva();
        r.setFechaInicio(dto.getFechaInicio());
        r.setFechaFin(dto.getFechaFin());
        r.setPagado(dto.getPagado());
        return r;
    }

    public void actualizarDatosBasicos(Reserva reserva, ReservaCrearEditarDTO dto) {
        reserva.setFechaInicio(dto.getFechaInicio());
        reserva.setFechaFin(dto.getFechaFin());
        reserva.setPagado(dto.getPagado());
    }

    public ProductoCantidad toProductoCantidad(ProductoCantidadCrearEditarDTO dto, Reserva reserva) {
        ProductoCantidad pc = new ProductoCantidad();
        Producto producto = productoOperationService.getEntity(dto.getIdProducto());
        pc.setProducto(producto);
        pc.setCantidad(dto.getCantidad());
        pc.setReserva(reserva);
        return pc;
    }

    public ReservaUserGetDTO toUserGetDTO(Reserva reserva) {
        ReservaUserGetDTO dto = new ReservaUserGetDTO();
        dto.setId(reserva.getId());
        dto.setCodigoReserva(reserva.getCodigoReserva());
        dto.setEstado(reserva.getEstado() != null ? reserva.getEstado().toString() : null);
        dto.setPagado(reserva.getPagado());
        dto.setFechaInicio(reserva.getFechaInicio());
        dto.setFechaFin(reserva.getFechaFin());
        dto.setPrecioTotal(reserva.getPrecioTotal());
        dto.setProductos(mapearProductos(reserva));
        return dto;
    }

    public ReservaAdminGetDTO toAdminGetDTO(Reserva reserva) {
        return modelMapper.map(reserva,ReservaAdminGetDTO.class);
    }

    private List<ProductoCantidadGetDTO> mapearProductos(Reserva reserva) {
        return reserva.getProductos().stream()
                .map(this::mapToProductoCantidadGetDTO)
                .toList();
    }

    private UsuarioGetDTO mapearUsuario(Reserva reserva) {
        if (reserva.getUsuario() == null) return null;
        return new UsuarioGetDTO(reserva.getUsuario().getId(), reserva.getUsuario().getUsername());
    }

    private ProductoCantidadGetDTO mapToProductoCantidadGetDTO(ProductoCantidad pc) {
        ProductoCantidadGetDTO dto = new ProductoCantidadGetDTO();
        dto.setNombreProducto(pc.getProducto().getNombre());
        dto.setCantidad(pc.getCantidad());
        return dto;
    }
}
