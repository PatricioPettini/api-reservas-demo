package com.patojunit.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductoCantidadCrearEditarDTO{

    @NotNull(message = "El ID del producto es obligatorio")
    private Long idProducto;

    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    @NotBlank
    private int cantidad;
}
