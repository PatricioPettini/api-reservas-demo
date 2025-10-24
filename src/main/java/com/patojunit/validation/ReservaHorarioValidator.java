package com.patojunit.validation;

import com.patojunit.dto.request.ReservaCrearEditarDTO;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class ReservaHorarioValidator implements ConstraintValidator<ReservaHorario, ReservaCrearEditarDTO> {

    private static final LocalTime HORA_MINIMA = LocalTime.of(10, 0);
    private static final LocalTime HORA_MAXIMA = LocalTime.of(19, 0);

    @Override
    public boolean isValid(ReservaCrearEditarDTO reserva, ConstraintValidatorContext context) {
        if (reserva == null || reserva.getFechaInicio() == null || reserva.getFechaFin() == null)
            return true;

        LocalDateTime inicio = reserva.getFechaInicio();
        LocalDateTime fin = reserva.getFechaFin();

        if (!fin.isAfter(inicio)) {
            return buildViolation(context, "La fecha de fin debe ser posterior a la fecha de inicio.");
        }

        if (inicio.toLocalTime().isBefore(HORA_MINIMA) || inicio.toLocalTime().isAfter(HORA_MAXIMA)) {
            return buildViolation(context, "La hora de inicio debe estar entre las 10:00 y las 19:00.");
        }

        if (fin.toLocalTime().isBefore(HORA_MINIMA) || fin.toLocalTime().isAfter(HORA_MAXIMA)) {
            return buildViolation(context, "La hora de fin debe estar entre las 10:00 y las 19:00.");
        }

        if (inicio.getMinute() != 0 || fin.getMinute() != 0) {
            return buildViolation(context, "Las horas de inicio y fin deben ser exactas (por ejemplo: 13:00).");
        }

        long horas = Duration.between(inicio, fin).toHours();
        if (horas < 1) {
            return buildViolation(context, "La reserva debe durar al menos 1 hora.");
        }

        return true;
    }

    private boolean buildViolation(ConstraintValidatorContext context, String mensaje) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(mensaje).addConstraintViolation();
        return false;
    }
}
