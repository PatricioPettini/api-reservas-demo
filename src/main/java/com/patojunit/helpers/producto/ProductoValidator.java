package com.patojunit.helpers.producto;

import com.patojunit.repository.IProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductoValidator {

    private final IProductoRepository productoRepository;

    public void validarProductoNoExiste(String nombre) {
        if (productoRepository.existsByNombre(nombre)) {
            throw new IllegalArgumentException("El producto ya se encuentra registrado.");
        }
    }

    public void validarStockNoNegativo(int nuevoStock) {
        if (nuevoStock < 0) {
            throw new IllegalArgumentException("Error de stock: no puede quedar stock negativo.");
        }
    }
}
