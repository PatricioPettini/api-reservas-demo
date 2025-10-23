package com.patojunit.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.patojunit.model.enums.EstadoReserva;
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
public class ReservaUserGetDTO {

    private Long id;

    private String codigoReserva;

    private List<ProductoCantidadGetDTO> productos = new ArrayList<>();

    private LocalDateTime fechaInicio;

    private LocalDateTime fechaFin;

    private BigDecimal precioTotal;

    private Boolean pagado;

    @JsonIgnore
    private String estado;

    public String getEstadoActual() {
        if (estado != null && estado.equalsIgnoreCase(EstadoReserva.CANCELADA.toString())) {
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
