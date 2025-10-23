package com.patojunit.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = FechaPosteriorValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface FechaPosterior {

    String message() default "La fecha final debe ser posterior a la fecha inicial.";

    String fechaInicio();
    String fechaFin();

    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}