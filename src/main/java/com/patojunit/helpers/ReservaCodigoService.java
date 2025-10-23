package com.patojunit.helpers;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ReservaCodigoService {

    public String generarCodigoReserva(Long idCliente) {
        String sufijo = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        return "RES-" + idCliente + "-" + sufijo;
    }
}
