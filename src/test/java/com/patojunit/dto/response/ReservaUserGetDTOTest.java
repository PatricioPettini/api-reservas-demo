package com.patojunit.dto.response;

import com.patojunit.model.enums.EstadoReserva;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReservaUserGetDTOTest {

    @Test
    @DisplayName("Debe devolver 'cancelado' si el estado es CANCELADA")
    void getEstadoActual_DeberiaRetornarCancelado() {
        var dto = new ReservaUserGetDTO();
        dto.setEstado(EstadoReserva.CANCELADA.toString());

        String estado = dto.getEstadoActual();

        assertEquals("cancelado", estado);
    }

    @Test
    @DisplayName("Debe devolver 'reservado' si la fecha actual es antes de fechaInicio")
    void getEstadoActual_DeberiaRetornarReservadoAntesDeInicio() {
        var dto = new ReservaUserGetDTO();
        dto.setFechaInicio(LocalDateTime.now().plusHours(2));
        dto.setFechaFin(LocalDateTime.now().plusHours(4));

        String estado = dto.getEstadoActual();

        assertEquals("reservado", estado);
    }

    @Test
    @DisplayName("Debe devolver 'devuelto' si la fecha actual es después de fechaFin")
    void getEstadoActual_DeberiaRetornarDevueltoDespuesDeFin() {
        var dto = new ReservaUserGetDTO();
        dto.setFechaInicio(LocalDateTime.now().minusHours(4));
        dto.setFechaFin(LocalDateTime.now().minusHours(2));

        String estado = dto.getEstadoActual();

        assertEquals("devuelto", estado);
    }

    @Test
    @DisplayName("Debe devolver 'en curso' si la fecha actual está entre inicio y fin")
    void getEstadoActual_DeberiaRetornarEnCursoDuranteReserva() {
        var dto = new ReservaUserGetDTO();
        dto.setFechaInicio(LocalDateTime.now().minusHours(1));
        dto.setFechaFin(LocalDateTime.now().plusHours(1));

        String estado = dto.getEstadoActual();

        assertEquals("en curso", estado);
    }

    @Test
    @DisplayName("Debe devolver 'reservado' si las fechas son nulas")
    void getEstadoActual_DeberiaRetornarReservadoSiFechasNulas() {
        var dto = new ReservaUserGetDTO();
        dto.setEstado(null);
        dto.setFechaInicio(null);
        dto.setFechaFin(null);

        String estado = dto.getEstadoActual();

        assertEquals("reservado", estado);
    }
}
