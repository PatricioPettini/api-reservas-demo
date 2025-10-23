package com.patojunit.factory;

import com.patojunit.dto.request.ProductoCrearEditarDTO;
import com.patojunit.helpers.producto.ProductoCodigoService;
import com.patojunit.helpers.producto.ProductoMapper;
import com.patojunit.model.Producto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class ProductoFactory {

    private final ProductoMapper mapper;
    private final ProductoCodigoService codigoService;

    public Producto crearProducto(ProductoCrearEditarDTO dto) {
        Producto producto = mapper.toEntity(dto);
        producto.setCodigoProducto(codigoService.generarCodigoProducto(dto.getNombre()));
        producto.setFechaAlta(LocalDateTime.now());
        producto.setFechaUltimaActualizacion(null);
        return producto;
    }

    public void actualizarProductoDesdeDTO(Producto producto, ProductoCrearEditarDTO dto) {
        mapper.actualizarEntidadDesdeDTO(producto, dto);
        producto.setCodigoProducto(codigoService.generarCodigoProducto(dto.getNombre()));
        producto.setFechaUltimaActualizacion(LocalDateTime.now());
    }
}
