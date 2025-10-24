package com.patojunit.helpers.producto;

import com.patojunit.model.Producto;
import com.patojunit.repository.IProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductoStockService {

    private final IProductoRepository productoRepository;
    private final ProductoValidator validator;

    public void descontarStock(Producto producto, int cantidad) {
        int nuevoStock = producto.getStockDisponible() - cantidad;
        validator.validarStockNoNegativo(nuevoStock);

        producto.setStockDisponible(nuevoStock);
        producto.setCantidadReservadaActual(producto.getCantidadReservadaActual() + cantidad);
        productoRepository.save(producto);
    }

    public void reponerStock(Producto producto, int cantidad) {
        producto.setCantidadReservadaActual(producto.getCantidadReservadaActual() - cantidad);
        producto.setStockDisponible(producto.getStockDisponible() + cantidad);
        productoRepository.save(producto);
    }
}
