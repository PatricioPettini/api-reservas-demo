package com.patojunit.validation;

import com.patojunit.dto.request.ReservaCrearEditarDTO;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class FechaPosteriorValidator implements ConstraintValidator<FechaPosterior, ReservaCrearEditarDTO> {

    @Override
    public boolean isValid(ReservaCrearEditarDTO reserva, ConstraintValidatorContext context) {
        if (reserva.getFechaInicio() == null || reserva.getFechaFin() == null) {
            return true; // evita fallar si están vacías, otras validaciones se encargan
        }
        return reserva.getFechaFin().isAfter(reserva.getFechaInicio());
    }
}
