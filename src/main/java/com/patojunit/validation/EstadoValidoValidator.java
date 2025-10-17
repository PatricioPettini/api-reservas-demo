package com.patojunit.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public class EstadoValidoValidator implements ConstraintValidator<EstadoValido,String> {

    private Set<String> permitidas;

    @Override
    public void initialize(EstadoValido ann) {
        this.permitidas = Arrays.stream(ann.value())
                .map(s -> s.toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
// Convención: si viene null o en blanco, no fallamos acá; eso lo maneja @NotBlank/@NotNull si las usás.
        if (value == null || value.isBlank()) return true;
        return permitidas.contains(value.toLowerCase(Locale.ROOT));    }
}
