package com.patojunit.validation;

import jakarta.validation.Constraint;

import java.lang.annotation.*;

@Documented
@Target({ ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ProductoValidoValidator.class)
public @interface ProductoValido {
    String message() default "{reserva.estado.invalido}";

    Class[] groups() default {};
    Class[] payload() default {};

    String[] value();
}
