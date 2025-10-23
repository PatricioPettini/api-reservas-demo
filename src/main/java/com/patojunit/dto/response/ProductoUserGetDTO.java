package com.patojunit.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductoUserGetDTO {

    private Long id;

    private String codigoProducto;

    private String nombre;

    private BigDecimal precioHora;

    private int stockDisponible;
}
