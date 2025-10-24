package com.patojunit.helpers.producto;

import com.patojunit.dto.request.ProductoCrearEditarDTO;
import com.patojunit.dto.response.ProductoAdminGetDTO;
import com.patojunit.dto.response.ProductoUserGetDTO;
import com.patojunit.model.Producto;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductoMapper {

    private final ModelMapper modelMapper;

    public Producto toEntity(ProductoCrearEditarDTO dto) {
        Producto producto = new Producto();
        producto.setNombre(dto.getNombre());
        producto.setPrecioHora(dto.getPrecioHora());
        producto.setStockDisponible(dto.getStockDisponible());
        producto.setCantidadReservadaActual(dto.getCantidadReservada());
        return producto;
    }

    public ProductoUserGetDTO toUserGetDTO(Producto producto) {
        ProductoUserGetDTO dto = new ProductoUserGetDTO();
        dto.setId(producto.getId());
        dto.setNombre(producto.getNombre());
        dto.setPrecioHora(producto.getPrecioHora());
        dto.setStockDisponible(producto.getStockDisponible());
        dto.setCodigoProducto(producto.getCodigoProducto());
        return dto;
    }

    public ProductoAdminGetDTO toAdminGetDTO(Producto producto) {
        return modelMapper.map(producto,ProductoAdminGetDTO.class);
    }

    public void actualizarEntidadDesdeDTO(Producto producto, ProductoCrearEditarDTO dto) {
        producto.setNombre(dto.getNombre());
        producto.setPrecioHora(dto.getPrecioHora());
        producto.setCantidadReservadaActual(dto.getCantidadReservada());
        producto.setStockDisponible(dto.getStockDisponible());
    }
}
