package com.patojunit.dto.request;

import com.patojunit.validation.ProductoValido;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public final class ProductoCrearEditarDTO {

    @NotBlank(message = "El nombre no puede estar vacio")
    @ProductoValido(
            value = {"reposera","sombrilla","tabla","carpa"},
            message = "Los estados validos son reposera, sombrilla, tabla y carpa"

    )
    private String nombre;

    @Positive(message = "El precio debe ser positivo")
    @Digits(integer = 5,fraction = 2)
    private BigDecimal precioHora;

    @PositiveOrZero(message = "El stock disponible debe ser mayor o igual a 0")
    private int stockDisponible;

    @PositiveOrZero(message = "la cantidad reservada debe ser mayor o igual a 0")
    private int cantidadReservada;

}
