package com.patojunit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.patojunit.dto.request.ProductoCrearEditarDTO;
import com.patojunit.dto.response.ProductoGetDTO;
import com.patojunit.service.IProductoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = ProductoController.class)
class ProductoControllerIntTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IProductoService productoService;

    @Autowired
    private ObjectMapper objectMapper;

    private ProductoGetDTO productoDTO;
    private ProductoCrearEditarDTO crearEditarDTO;

    @BeforeEach
    void setUp() {
        productoDTO = new ProductoGetDTO();
        productoDTO.setId(1L);
        productoDTO.setNombre("reposera");
        productoDTO.setPrecioHora(BigDecimal.valueOf(100));
        productoDTO.setStockDisponible(10);
        productoDTO.setCodigoProducto("PROD-GAL-1234");

        crearEditarDTO = new ProductoCrearEditarDTO();
        crearEditarDTO.setNombre("carpa");
        crearEditarDTO.setPrecioHora(BigDecimal.valueOf(100));
        crearEditarDTO.setStockDisponible(10);
        crearEditarDTO.setCantidadReservada(0);
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAllProductos_DeberiaRetornarLista() throws Exception {
        when(productoService.getAll()).thenReturn(List.of(productoDTO));

        mockMvc.perform(get("/producto/get"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre", is("reposera")))
                .andExpect(jsonPath("$[0].stockDisponible", is(10)));

        verify(productoService).getAll();
    }

    @Test
    @WithMockUser(roles = "USER")
    void getProducto_DeberiaRetornarProducto() throws Exception {
        when(productoService.get(1L)).thenReturn(productoDTO);

        mockMvc.perform(get("/producto/get/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre", is("reposera")))
                .andExpect(jsonPath("$.precioHora", is(100)));

        verify(productoService).get(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getStockProducto_DeberiaRetornarEntero() throws Exception {
        when(productoService.getStock(1L)).thenReturn(8);

        mockMvc.perform(get("/producto/get/stock/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("8"));

        verify(productoService).getStock(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void crearProducto_DeberiaRetornarProductoCreado() throws Exception {
        when(productoService.crear(any(ProductoCrearEditarDTO.class))).thenReturn(productoDTO);

        mockMvc.perform(post("/producto/crear")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(crearEditarDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre", is("reposera")))
                .andExpect(jsonPath("$.codigoProducto", startsWith("PROD-")));

        verify(productoService).crear(any(ProductoCrearEditarDTO.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void editarProducto_DeberiaRetornarProductoEditado() throws Exception {
        when(productoService.editar(eq(1L), any(ProductoCrearEditarDTO.class))).thenReturn(productoDTO);

        mockMvc.perform(put("/producto/editar/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(crearEditarDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre", is("reposera")));

        verify(productoService).editar(eq(1L), any(ProductoCrearEditarDTO.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void eliminarProducto_DeberiaRetornarMensaje() throws Exception {
        Mockito.doNothing().when(productoService).eliminar(1L);

        mockMvc.perform(delete("/producto/eliminar/1")
                        .with(csrf())) // ✅ agrega token CSRF válido
                .andExpect(status().isOk())
                .andExpect(content().string("producto eliminado!"));

        verify(productoService).eliminar(1L);
    }

    @Test
    void crearProducto_SinRol_DeberiaDevolver403() throws Exception {
        mockMvc.perform(post("/producto/crear")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(crearEditarDTO)))
                .andExpect(status().isForbidden());
    }
}
