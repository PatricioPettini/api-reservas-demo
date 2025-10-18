package com.patojunit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.patojunit.dto.request.ReservaCrearEditarDTO;
import com.patojunit.model.Reserva;
import com.patojunit.repository.IReservaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ReservaControllerIntTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private IReservaRepository reservaRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        reservaRepository.deleteAll();
    }

    @Test
    @WithMockUser(roles = "USER")
    void crearReserva_DeberiaCrearYRetornarReserva() throws Exception {
        ReservaCrearEditarDTO dto = new ReservaCrearEditarDTO();
        dto.setTelefonoCliente("1122334455");
        dto.setPagado(true);
        dto.setFechaInicio(LocalDateTime.now().plusHours(1));
        dto.setFechaFin(LocalDateTime.now().plusHours(3));

        mockMvc.perform(post("/reserva/crear")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.telefonoCliente").value("1122334455"))
                .andExpect(jsonPath("$.estadoActual").value("reservado"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllReservas_DeberiaRetornarLista() throws Exception {
        // Crear una reserva en la BD
        Reserva reserva = new Reserva();
        reserva.setTelefonoCliente("1199887766");
        reserva.setEstado("reservado");
        reserva.setPagado(false);
        reserva.setFechaInicio(LocalDateTime.now().plusHours(2));
        reserva.setFechaFin(LocalDateTime.now().plusHours(4));
        reserva.setPrecioTotal(BigDecimal.valueOf(300));
        reservaRepository.save(reserva);

        mockMvc.perform(get("/reserva/get")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].telefonoCliente").value("1199887766"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getReserva_DeberiaRetornarReservaPorId() throws Exception {
        Reserva reserva = new Reserva();
        reserva.setTelefonoCliente("1100000000");
        reserva.setEstado("reservado");
        reserva.setPagado(true);
        reserva.setFechaInicio(LocalDateTime.now().plusHours(1));
        reserva.setFechaFin(LocalDateTime.now().plusHours(5));
        reserva.setPrecioTotal(BigDecimal.valueOf(250));
        reservaRepository.save(reserva);

        Long id = reservaRepository.findAll().get(0).getId();

        mockMvc.perform(get("/reserva/get/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.telefonoCliente").value("1100000000"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void editarReserva_DeberiaActualizarReserva() throws Exception {
        Reserva reserva = new Reserva();
        reserva.setTelefonoCliente("1111111111");
        reserva.setEstado("reservado");
        reserva.setPagado(true);
        reserva.setFechaInicio(LocalDateTime.now().plusHours(2));
        reserva.setFechaFin(LocalDateTime.now().plusHours(4));
        reserva.setPrecioTotal(BigDecimal.valueOf(400));
        reservaRepository.save(reserva);

        Long id = reservaRepository.findAll().get(0).getId();

        ReservaCrearEditarDTO dto = new ReservaCrearEditarDTO();
        dto.setTelefonoCliente("1111111111");
        dto.setPagado(true);
        dto.setFechaInicio(LocalDateTime.now().plusHours(3));
        dto.setFechaFin(LocalDateTime.now().plusHours(6));

        mockMvc.perform(put("/reserva/editar/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estadoActual").value("reservado"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void cancelarReserva_DeberiaCambiarEstadoACancelado() throws Exception {
        Reserva reserva = new Reserva();
        reserva.setTelefonoCliente("1155443322");
        reserva.setEstado("reservado");
        reserva.setPagado(false);
        reserva.setFechaInicio(LocalDateTime.now().plusHours(1));
        reserva.setFechaFin(LocalDateTime.now().plusHours(2));
        reserva.setPrecioTotal(BigDecimal.valueOf(150));
        reservaRepository.save(reserva);

        Long id = reservaRepository.findAll().get(0).getId();

        mockMvc.perform(patch("/reserva/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estadoActual").value("cancelado"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void eliminarReserva_DeberiaBorrarYRetornarMensaje() throws Exception {
        Reserva reserva = new Reserva();
        reserva.setTelefonoCliente("1166778899");
        reserva.setEstado("reservado");
        reserva.setPagado(false);
        reserva.setFechaInicio(LocalDateTime.now().plusHours(1));
        reserva.setFechaFin(LocalDateTime.now().plusHours(2));
        reservaRepository.save(reserva);

        Long id = reservaRepository.findAll().get(0).getId();

        mockMvc.perform(delete("/reserva/eliminar/" + id))
                .andExpect(status().isOk())
                .andExpect(content().string("reserva eliminada!"));
    }
}
