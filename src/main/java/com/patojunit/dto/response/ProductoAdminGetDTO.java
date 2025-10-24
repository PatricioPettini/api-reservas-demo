package com.patojunit.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductoAdminGetDTO extends ProductoUserGetDTO{

    private LocalDateTime fechaAlta;

    private LocalDateTime fechaUltimaActualizacion;

    private int cantidadReservadaActual;
}