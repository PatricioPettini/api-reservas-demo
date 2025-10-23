package com.patojunit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.patojunit.dto.request.ReservaCrearEditarDTO;
import com.patojunit.dto.response.ReservaUserGetDTO;
import com.patojunit.service.interfaces.IReservaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ReservaControllerIntTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private IReservaService reservaService;

    @Test
    @WithMockUser(username = "user1", roles = {"USER"})
    @DisplayName("Debe crear una reserva correctamente")
    void crearReserva_DeberiaRetornarOk() throws Exception {
        ReservaUserGetDTO mockResponse = new ReservaUserGetDTO();
        mockResponse.setId(1L);
        mockResponse.setPrecioTotal(BigDecimal.valueOf(2000));
        mockResponse.setEstado("pendiente");

        Mockito.when(reservaService.crear(any(ReservaCrearEditarDTO.class)))
                .thenReturn(mockResponse);

        ReservaCrearEditarDTO dto = new ReservaCrearEditarDTO(
                Collections.emptyList(),
                LocalDateTime.now().plusMinutes(5),
                LocalDateTime.now().plusHours(1),
                false
        );

        mockMvc.perform(post("/reserva/crear")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.precioTotal", is(2000)))
                .andExpect(jsonPath("$.estadoActual", is("reservado")));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Debe editar una reserva correctamente")
    void editarReserva_DeberiaEditarYRetornarOk() throws Exception {
        ReservaUserGetDTO response = new ReservaUserGetDTO();
        response.setId(2L);
        response.setEstado("reservado");

        Mockito.when(reservaService.editar(eq(2L), any(ReservaCrearEditarDTO.class)))
                .thenReturn(response);

        ReservaCrearEditarDTO dto = new ReservaCrearEditarDTO(
                Collections.emptyList(),
                LocalDateTime.now().plusMinutes(5),
                LocalDateTime.now().plusHours(1),
                false
        );

        mockMvc.perform(put("/reserva/editar/{id}", 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(2)))
                .andExpect(jsonPath("$.estadoActual", is("reservado")));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Debe eliminar una reserva correctamente")
    void eliminarReserva_DeberiaEliminarYRetornarMensaje() throws Exception {
        mockMvc.perform(delete("/reserva/eliminar/{id}", 10L))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("reserva eliminada!")));

        Mockito.verify(reservaService).eliminar(10L);
    }

    @Test
    @WithMockUser(username = "user1", roles = {"USER"})
    @DisplayName("Debe eliminar productos de una reserva correctamente")
    void eliminarProductos_DeberiaRetornarOk() throws Exception {
        ReservaUserGetDTO response = new ReservaUserGetDTO();
        response.setId(7L);
        response.setEstado("reservado");

        Mockito.when(reservaService.eliminarProductos(eq(7L), any()))
                .thenReturn(response);

        mockMvc.perform(delete("/reserva/eliminar-productos/{idReserva}", 7L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of(1L, 2L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(7)))
                .andExpect(jsonPath("$.estadoActual", is("reservado")));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Debe retornar lista de reservas")
    void getAllReservas_DeberiaRetornarLista() throws Exception {
        ReservaUserGetDTO dto = new ReservaUserGetDTO();
        dto.setId(1L);
        dto.setEstado("reservado");

        Mockito.when(reservaService.getAll()).thenReturn(List.of(dto));

        mockMvc.perform(get("/reserva/get"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].estadoActual", is("reservado")));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Debe retornar una reserva por ID")
    void getReserva_DeberiaRetornarUna() throws Exception {
        ReservaUserGetDTO dto = new ReservaUserGetDTO();
        dto.setId(5L);
        dto.setEstado("reservado");

        Mockito.when(reservaService.get(5L)).thenReturn(dto);

        mockMvc.perform(get("/reserva/get/{id}", 5L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(5)))
                .andExpect(jsonPath("$.estadoActual", is("reservado")));
    }
}
