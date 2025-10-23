package com.patojunit.helpers.producto;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ProductoCodigoService {

    public String generarCodigoProducto(String nombre) {
        String prefijo = nombre.length() >= 3
                ? nombre.substring(0, 3).toUpperCase()
                : nombre.toUpperCase();

        String sufijo = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        return "PROD-" + prefijo + "-" + sufijo;
    }
}
