package com.patojunit.model;

import com.patojunit.validation.ProductoValido;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public final class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private String codigoProducto;

    private LocalDateTime fechaAlta;

    private LocalDateTime fechaUltimaActualizacion;

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
