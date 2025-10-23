package com.patojunit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.patojunit.dto.request.ProductoCrearEditarDTO;
import com.patojunit.model.Producto;
import com.patojunit.repository.IProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test de integración para ProductoController.
 * Usa la base de datos H2 en memoria, MockMvc y contexto completo.
 */
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ProductoControllerIntTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private IProductoRepository productoRepository;

    @BeforeEach
    void setup() {
        productoRepository.deleteAll();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Debe crear un producto correctamente")
    void crearProducto_DeberiaCrearYRetornarDTO() throws Exception {
        ProductoCrearEditarDTO dto = new ProductoCrearEditarDTO(
                "reposera",
                BigDecimal.valueOf(1200.00),
                10,
                0
        );

        mockMvc.perform(post("/producto/crear")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre", is("reposera")))
                .andExpect(jsonPath("$.precioHora", is(1200.00)))
                .andExpect(jsonPath("$.stockDisponible", is(10)))
                .andExpect(jsonPath("$.codigoProducto", notNullValue()));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Debe obtener la lista completa de productos")
    void getAllProductos_DeberiaRetornarLista() throws Exception {
        productoRepository.saveAll(List.of(
                new Producto(null, "P-001", null, null, "reposera", BigDecimal.valueOf(1000), 10, 0),
                new Producto(null, "P-002", null, null, "sombrilla", BigDecimal.valueOf(800), 5, 0)
        ));

        mockMvc.perform(get("/producto/get"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].nombre", is("reposera")))
                .andExpect(jsonPath("$[1].nombre", is("sombrilla")));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Debe retornar un producto por su ID")
    void getProductoPorId_DeberiaRetornarProducto() throws Exception {
        Producto producto = productoRepository.save(
                new Producto(null, "P-001", null, null, "carpa", BigDecimal.valueOf(1500), 8, 0)
        );

        mockMvc.perform(get("/producto/get/" + producto.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre", is("carpa")))
                .andExpect(jsonPath("$.precioHora", is(1500.0)))
                .andExpect(jsonPath("$.stockDisponible", is(8)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Debe editar un producto existente")
    void editarProducto_DeberiaActualizarDatos() throws Exception {
        Producto producto = productoRepository.save(
                new Producto(null, "P-001", null, null, "reposera", BigDecimal.valueOf(1000), 10, 0)
        );

        ProductoCrearEditarDTO dto = new ProductoCrearEditarDTO(
                "reposera",
                BigDecimal.valueOf(1500.00),
                8,
                0
        );

        mockMvc.perform(put("/producto/editar/" + producto.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre", is("reposera")))
                .andExpect(jsonPath("$.precioHora", is(1500.00)))
                .andExpect(jsonPath("$.stockDisponible", is(8)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Debe eliminar un producto y retornar mensaje de éxito")
    void eliminarProducto_DeberiaEliminarYRetornarMensaje() throws Exception {
        Producto producto = productoRepository.save(
                new Producto(null, "P-001", null, null, "carpa", BigDecimal.valueOf(1500), 4, 0)
        );

        mockMvc.perform(delete("/producto/eliminar/" + producto.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string("producto eliminado!"));
    }

    @Test
    @DisplayName("Debe devolver 403 si intenta acceder sin rol")
    void crearProducto_SinRol_DeberiaRetornar403() throws Exception {
        ProductoCrearEditarDTO dto = new ProductoCrearEditarDTO("carpa", BigDecimal.valueOf(1200), 10, 0);

        mockMvc.perform(post("/producto/crear")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }
}
