package com.patojunit.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ReservaHorarioValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ReservaHorario {

    String message();

    String fechaInicio();
    String fechaFin();

    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}