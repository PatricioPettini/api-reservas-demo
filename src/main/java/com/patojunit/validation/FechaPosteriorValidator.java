package com.patojunit.validation;

import com.patojunit.model.Reserva;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
public class FechaPosteriorValidator implements ConstraintValidator<FechaPosterior, Reserva> {

    @Override
    public boolean isValid(Reserva reserva, ConstraintValidatorContext context) {
        if (reserva.getFechaInicio() == null || reserva.getFechaFin() == null) {
            return true; // evita fallar si están vacías
        }
        return reserva.getFechaFin().isAfter(reserva.getFechaInicio());
    }
}