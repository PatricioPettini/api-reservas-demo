package com.patojunit.dto.request;

import com.patojunit.validation.FechaPosterior;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FechaPosterior(fechaInicio = "fechaInicio", fechaFin = "fechaFin",
        message = "La fecha de fin debe ser posterior a la fecha de inicio.")
public class ReservaCrearEditarDTO {

    private List<ProductoCantidadCrearEditarDTO> productos = new ArrayList<>();

    @NotNull(message = "La fecha de inicio es obligatoria")
    @FutureOrPresent(message = "La fecha de inicio no puede ser anterior al día de hoy")
    private LocalDateTime fechaInicio;

    @NotNull(message = "La fecha de fin es obligatoria")
    private LocalDateTime fechaFin;

    private Boolean pagado=false;

}

