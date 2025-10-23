package com.patojunit.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EstadoValidoValidatorTest {

    private EstadoValidoValidator validator;

    @BeforeEach
    void setUp() {
        validator = new EstadoValidoValidator();

        EstadoValido mockAnn = new EstadoValido() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return EstadoValido.class;
            }

            @Override
            public String[] value() {
                return new String[]{"pendiente", "activa", "cancelada"};
            }

            @Override
            public String message() {
                return "Estado inválido";
            }

            @Override
            public Class<?>[] groups() {
                return new Class[0];
            }

            @Override
            public Class<? extends jakarta.validation.Payload>[] payload() {
                return new Class[0];
            }
        };

        validator.initialize(mockAnn);
    }

    @Test
    @DisplayName("Debe retornar true para valores válidos (case-insensitive)")
    void isValid_DeberiaAceptarValoresPermitidos() {
        ConstraintValidatorContext ctx = mock(ConstraintValidatorContext.class);

        assertTrue(validator.isValid("pendiente", ctx));
        assertTrue(validator.isValid("ACTIVA", ctx));
        assertTrue(validator.isValid("Cancelada", ctx));
    }

    @Test
    @DisplayName("Debe retornar false para valores no permitidos")
    void isValid_DeberiaRechazarValoresInvalidos() {
        ConstraintValidatorContext ctx = mock(ConstraintValidatorContext.class);

        assertFalse(validator.isValid("finalizada", ctx));
        assertFalse(validator.isValid("en curso", ctx));
    }

    @Test
    @DisplayName("Debe retornar true si el valor es null o vacío")
    void isValid_DeberiaRetornarTrueParaNullOVacio() {
        ConstraintValidatorContext ctx = mock(ConstraintValidatorContext.class);

        assertTrue(validator.isValid(null, ctx));
        assertTrue(validator.isValid("", ctx));
        assertTrue(validator.isValid("   ", ctx));
    }

    @Test
    @DisplayName("Debe inicializar correctamente la lista de estados permitidos")
    void initialize_DeberiaConfigurarEstadosCorrectamente() {
        ConstraintValidatorContext ctx = mock(ConstraintValidatorContext.class);

        assertTrue(validator.isValid("pendiente", ctx));
        assertFalse(validator.isValid("otro", ctx));
    }
}
