package com.patojunit.helpers.producto;

import com.patojunit.repository.IProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductoValidatorTest {

    @Mock
    private IProductoRepository productoRepository;

    @InjectMocks
    private ProductoValidator validator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Debe lanzar excepción si el producto ya existe")
    void validarProductoNoExiste_DeberiaLanzarExcepcionSiYaExiste() {
        when(productoRepository.existsByNombre("Reposera")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> validator.validarProductoNoExiste("Reposera"));

        assertEquals("El producto ya se encuentra registrado.", ex.getMessage());
        verify(productoRepository).existsByNombre("Reposera");
    }

    @Test
    @DisplayName("No debe lanzar excepción si el producto no existe")
    void validarProductoNoExiste_DeberiaPasarSiNoExiste() {
        when(productoRepository.existsByNombre("Silla")).thenReturn(false);

        assertDoesNotThrow(() -> validator.validarProductoNoExiste("Silla"));
        verify(productoRepository).existsByNombre("Silla");
    }

    @Test
    @DisplayName("Debe lanzar excepción si el stock es negativo")
    void validarStockNoNegativo_DeberiaLanzarExcepcion() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> validator.validarStockNoNegativo(-1));

        assertEquals("Error de stock: no puede quedar stock negativo.", ex.getMessage());
    }

    @Test
    @DisplayName("No debe lanzar excepción si el stock es positivo")
    void validarStockNoNegativo_DeberiaPasarSiEsPositivo() {
        assertDoesNotThrow(() -> validator.validarStockNoNegativo(10));
    }
}
