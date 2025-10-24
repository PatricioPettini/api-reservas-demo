package com.patojunit.model;

import jakarta.persistence.*;
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

    private String nombre;

    private BigDecimal precioHora;

    private int stockDisponible;

    private int cantidadReservadaActual;
}
