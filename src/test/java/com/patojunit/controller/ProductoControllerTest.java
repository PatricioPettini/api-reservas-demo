package com.patojunit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.patojunit.dto.request.ProductoCrearEditarDTO;
import com.patojunit.dto.response.ProductoUserGetDTO;
import com.patojunit.service.interfaces.IProductoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ProductoController.class)
class ProductoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IProductoService productoService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("Debe retornar todos los productos")
    void getAllProductos_ShouldReturnList() throws Exception {
        ProductoUserGetDTO p1 = new ProductoUserGetDTO(1L, "P-001", "reposera", BigDecimal.valueOf(1000), 10);
        ProductoUserGetDTO p2 = new ProductoUserGetDTO(2L, "P-002", "sombrilla", BigDecimal.valueOf(800), 5);

        Mockito.when(productoService.getAll()).thenReturn(List.of(p1, p2));

        mockMvc.perform(get("/producto/get"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].nombre", is("reposera")))
                .andExpect(jsonPath("$[1].nombre", is("sombrilla")));
    }

    @Test
    @WithMockUser(roles = {"USER"})
    @DisplayName("Debe retornar un producto por ID")
    void getProducto_ShouldReturnProducto() throws Exception {
        ProductoUserGetDTO dto = new ProductoUserGetDTO(1L, "P-001", "reposera", BigDecimal.valueOf(1000), 10);

        Mockito.when(productoService.get(1L)).thenReturn(dto);

        mockMvc.perform(get("/producto/get/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre", is("reposera")))
                .andExpect(jsonPath("$.precioHora", is(1000)));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("Debe crear un producto nuevo")
    void crearProducto_ShouldReturnCreated() throws Exception {
        ProductoCrearEditarDTO request = new ProductoCrearEditarDTO("reposera", BigDecimal.valueOf(1000), 5, 0);
        ProductoUserGetDTO response = new ProductoUserGetDTO(1L, "P-001","reposera", BigDecimal.valueOf(1000), 5);

        Mockito.when(productoService.crear(Mockito.any())).thenReturn(response);

        mockMvc.perform(post("/producto/crear")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre", is("reposera")))
                .andExpect(jsonPath("$.stockDisponible", is(5)));

    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("Debe editar un producto existente")
    void editarProducto_ShouldReturnUpdated() throws Exception {
        ProductoCrearEditarDTO request = new ProductoCrearEditarDTO("carpa", BigDecimal.valueOf(1500), 8, 0);
        ProductoUserGetDTO response = new ProductoUserGetDTO(1L, "P-001","carpa", BigDecimal.valueOf(1500), 8 );

        Mockito.when(productoService.editar(Mockito.eq(1L), Mockito.any())).thenReturn(response);

        mockMvc.perform(put("/producto/editar/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre", is("carpa")))
                .andExpect(jsonPath("$.stockDisponible", is(8)));

    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("Debe eliminar un producto por ID")
    void eliminarProducto_ShouldReturnSuccessMessage() throws Exception {
        mockMvc.perform(delete("/producto/eliminar/1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("producto eliminado!"));
    }
}
