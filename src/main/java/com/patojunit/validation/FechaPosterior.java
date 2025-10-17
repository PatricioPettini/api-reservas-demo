package com.patojunit.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = FechaPosteriorValidator.class)
@Target(ElementType.TYPE) // porque valida el objeto completo
@Retention(RetentionPolicy.RUNTIME)
public @interface FechaPosterior {

    String message() default "La fecha final debe ser posterior a la fecha inicial.";

    String fechaInicio(); // nombre del campo de inicio
    String fechaFin();    // nombre del campo de fin

    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}