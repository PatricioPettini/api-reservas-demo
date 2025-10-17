package com.patojunit.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.patojunit.validation.EstadoValido;
import com.patojunit.validation.FechaPosterior;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@FechaPosterior(fechaInicio = "fechaInicio", fechaFin = "fechaFin",
        message = "La fecha de fin debe ser posterior a la fecha de inicio.")
public class Reserva {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private String codigoReserva;

    @ManyToMany
    @JoinTable(
            name = "producto_reserva",
            joinColumns = @JoinColumn(name = "reserva_id"),
            inverseJoinColumns = @JoinColumn(name = "producto_id")
    )
    private List<Producto> productos = new ArrayList<>();


    @NotNull(message = "La fecha de inicio es obligatoria")
    @FutureOrPresent
    private LocalDateTime fechaInicio;

    @NotNull(message = "La fecha de fin es obligatoria")
    @Future(message = "La fecha de fin debe ser futura")
    private LocalDateTime fechaFin;

    @EstadoValido(
            value = {"reservado","devuelto","en curso","cancelado"},
            message = "Los estados validos son reservado, devuelto, en curso o cancelado"

    )
    private String estado;

    @PositiveOrZero(message = "El precio total no puede ser negativo")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private BigDecimal precioTotal;

    private Boolean pagado=false;

    @NotBlank
    private String telefonoCliente;

    @Transient
    @JsonProperty("estadoActual")
    public String getEstadoActual() {
        if ("cancelado".equalsIgnoreCase(this.estado)) {
            return "cancelado";
        }

        LocalDateTime ahora = LocalDateTime.now();

        if (fechaInicio != null && fechaFin != null) {
            if (ahora.isBefore(fechaInicio)) {
                return "reservado";
            } else if (ahora.isAfter(fechaFin)) {
                return "devuelto";
            } else {
                return "en curso";
            }
        }

        return "reservado";
    }
}
