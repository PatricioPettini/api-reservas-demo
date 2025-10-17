package com.patojunit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.patojunit.model.Reserva;
import com.patojunit.repository.IReservaRepository;
import com.patojunit.security.config.TestSecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
class ReservaControllerIntTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private IReservaRepository reservaRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllReservas_DeberiaRetornarListaDeReservas() throws Exception {
        // 🔹 Datos simulados
        Reserva reserva1 = new Reserva();
        reserva1.setId(1L);
        reserva1.setTelefonoCliente("1122334455");
        reserva1.setFechaInicio(LocalDateTime.now().plusDays(1));
        reserva1.setFechaFin(LocalDateTime.now().plusDays(2));

        Reserva reserva2 = new Reserva();
        reserva2.setId(2L);
        reserva2.setTelefonoCliente("11560455");
        reserva2.setFechaInicio(LocalDateTime.now().plusDays(2));
        reserva2.setFechaFin(LocalDateTime.now().plusDays(3));

        // 🔹 Guardar en repositorio
        reservaRepository.saveAll(List.of(reserva1, reserva2));

        // 🔹 Petición GET
        mockMvc.perform(get("/reserva/get")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].telefonoCliente").value("1122334455"))
                .andExpect(jsonPath("$[1].telefonoCliente").value("11560455"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void eliminarReserva_DeberiaEliminarReservaExistente() throws Exception {
        // 🔹 Crear una reserva simulada
        Reserva reserva = new Reserva();
        reserva.setFechaInicio(LocalDateTime.now().plusDays(1));
        reserva.setFechaFin(LocalDateTime.now().plusDays(2));
        reserva.setTelefonoCliente("1122334455");

        reserva = reservaRepository.save(reserva);

        // 🔹 Petición DELETE
        mockMvc.perform(delete("/reserva/eliminar/" + reserva.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("reserva eliminada!"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getReservaPorId_DeberiaRetornarReservaExistente() throws Exception {
        // 🔹 Crear y guardar una reserva simulada
        Reserva reserva = new Reserva();
        reserva.setTelefonoCliente("1122334455");
        reserva.setFechaInicio(LocalDateTime.now().plusMinutes(3));
        reserva.setFechaFin(LocalDateTime.now().plusDays(1));
        reserva = reservaRepository.save(reserva);

        // 🔹 Ejecutar GET
        mockMvc.perform(get("/reserva/get/" + reserva.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reserva.getId()));
    }

    @Test
    @WithMockUser(roles = "USER")
    void crearReserva_DeberiaRetornarReservaCreada() throws Exception {
        Reserva reserva = new Reserva();
        reserva.setFechaInicio(LocalDateTime.now().plusDays(1));
        reserva.setFechaFin(LocalDateTime.now().plusDays(2));
        reserva.setPagado(true); // ✅ agregado
        reserva.setTelefonoCliente("1122334455"); // ✅ agregado

        String reservaJson = objectMapper.writeValueAsString(reserva);

        mockMvc.perform(post("/reserva/crear")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reservaJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.fechaInicio").exists())
                .andExpect(jsonPath("$.fechaFin").exists())
                .andExpect(jsonPath("$.pagado").value(true))
                .andExpect(jsonPath("$.telefonoCliente").value("1122334455"));
    }


    @Test
    @WithMockUser(roles = "ADMIN")
    void editarReserva_DeberiaActualizarReservaExistente() throws Exception {
        // 🔹 Crear una reserva inicial
        Reserva reserva = new Reserva();
        reserva.setFechaInicio(LocalDateTime.now().plusDays(1));
        reserva.setFechaFin(LocalDateTime.now().plusDays(2));
        reserva.setTelefonoCliente("1122334455");
        reserva.setPagado(true);
        reserva = reservaRepository.save(reserva);

        // 🔹 Crear una versión editada
        Reserva reservaEditada = new Reserva();
        reservaEditada.setFechaInicio(LocalDateTime.now().plusDays(3));
        reservaEditada.setTelefonoCliente("1121115");
        reservaEditada.setFechaFin(LocalDateTime.now().plusDays(4));

        String reservaJson = objectMapper.writeValueAsString(reservaEditada);

        // 🔹 Ejecutar PUT
        mockMvc.perform(put("/reserva/editar/" + reserva.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reservaJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reserva.getId()))
                .andExpect(jsonPath("$.fechaInicio").exists())
                .andExpect(jsonPath("$.fechaFin").exists());
    }

    @Test
    @WithMockUser(roles = "USER")
    void cancelarReserva_DeberiaCambiarEstadoACancelado() throws Exception {
        // 🔹 Crear una reserva activa
        Reserva reserva = new Reserva();
        reserva.setFechaInicio(LocalDateTime.now().plusDays(1));
        reserva.setFechaFin(LocalDateTime.now().plusDays(2));
        reserva.setTelefonoCliente("15517311");
        reserva.setEstado("en curso");
        reserva = reservaRepository.save(reserva);

        // 🔹 Ejecutar PATCH
        mockMvc.perform(patch("/reserva/" + reserva.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("cancelado"));
    }

}
