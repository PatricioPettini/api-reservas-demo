package com.patojunit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.patojunit.model.Producto;
import com.patojunit.repository.IProductoRepository;
import com.patojunit.security.config.TestSecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
class ProductoControllerIntTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private IProductoRepository productoRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "ADMIN")
    void eliminarProducto_DeberiaEliminarProductoExistente() throws Exception {
        // 🔹 Primero guardamos un producto de prueba
        Producto producto = new Producto();
        producto.setNombre("sombrilla");
        producto.setPrecioHora(new BigDecimal("250.00"));
        producto.setStockDisponible(10);
        producto.setCantidadReservada(0);
        productoRepository.save(producto);

        // 🔹 Luego ejecutamos la petición DELETE al endpoint
        mockMvc.perform(delete("/producto/eliminar/{id}", producto.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // Esperamos 200 OK
                .andExpect(content().string("producto eliminado!"));

        // 🔹 Finalmente verificamos que el producto ya no exista
        boolean existe = productoRepository.findById(producto.getId()).isPresent();
        assertThat(existe).isFalse();
    }

    @Test
    @WithMockUser(roles = "USER")
    void getProducto_DeberiaRetornarProductoExistente() throws Exception {
        // 🔹 Crear producto de prueba
        Producto producto = new Producto();
        producto.setNombre("sombrilla");
        producto.setPrecioHora(new BigDecimal("200.50"));
        producto.setStockDisponible(15);
        producto.setCantidadReservada(2);
        productoRepository.save(producto);

        mockMvc.perform(get("/producto/get/{id}", producto.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(producto.getId()))
                .andExpect(jsonPath("$.nombre").value("sombrilla"))
                .andExpect(jsonPath("$.precioHora").value(200.50))
                .andExpect(jsonPath("$.stockDisponible").value(15));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAllProductos_DeberiaRetornarListaDeProductos() throws Exception {
        // 🔹 Limpiamos la BD antes del test
        productoRepository.deleteAll();

        // 🔹 Insertamos algunos productos
        Producto p1 = new Producto();
        p1.setNombre("sombrilla");
        p1.setPrecioHora(new BigDecimal("150.00"));
        p1.setStockDisponible(10);
        p1.setCantidadReservada(2);

        Producto p2 = new Producto();
        p2.setNombre("carpa");
        p2.setPrecioHora(new BigDecimal("300.00"));
        p2.setStockDisponible(5);
        p2.setCantidadReservada(1);

        productoRepository.saveAll(List.of(p1, p2));

        // 🔹 Ejecutar GET /get
        mockMvc.perform(get("/producto/get")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                // Verifica que haya al menos 2 elementos
                .andExpect(jsonPath("$.length()").value(2))
                // Verifica los nombres de los productos
                .andExpect(jsonPath("$[0].nombre").value("sombrilla"))
                .andExpect(jsonPath("$[1].nombre").value("carpa"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getStockProducto_DeberiaRetornarStockCorrecto() throws Exception {
        // 🔹 Crear producto de prueba
        Producto producto = new Producto();
        producto.setNombre("sombrilla");
        producto.setPrecioHora(new BigDecimal("180.00"));
        producto.setStockDisponible(25);
        producto.setCantidadReservada(5);
        productoRepository.save(producto);

        mockMvc.perform(get("/producto/get/stock/{id}", producto.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("25")); // El stockDisponible esperado
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void crearProducto_DeberiaGuardarYRetornarProducto() throws Exception {

        // 🔹 Limpiamos la BD antes del test
        productoRepository.deleteAll();

        // 🔹 Crear producto en memoria
        Producto producto = new Producto();
        producto.setNombre("carpa");
        producto.setPrecioHora(new BigDecimal("350.00"));
        producto.setStockDisponible(8);
        producto.setCantidadReservada(2);

        // 🔹 Convertir a JSON
        String productoJson = objectMapper.writeValueAsString(producto);

        // 🔹 Ejecutar POST /crear
        mockMvc.perform(post("/producto/crear")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productoJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("carpa"))
                .andExpect(jsonPath("$.precioHora").value(350.00))
                .andExpect(jsonPath("$.stockDisponible").value(8));

        // 🔹 Verificar en BD
        List<Producto> productos = productoRepository.findAll();
        assertThat(productos).hasSize(1);
        assertThat(productos.get(0).getNombre()).isEqualTo("carpa");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void editarProducto_DeberiaActualizarDatosCorrectamente() throws Exception {
        // 🔹 Crear producto inicial
        Producto productoOriginal = new Producto();
        productoOriginal.setNombre("carpa");
        productoOriginal.setPrecioHora(new BigDecimal("300.00"));
        productoOriginal.setStockDisponible(5);
        productoOriginal.setCantidadReservada(1);
        productoRepository.save(productoOriginal);

        // 🔹 Crear objeto con los nuevos valores (edición)
        Producto productoEditado = new Producto();
        productoEditado.setNombre("carpa"); // sigue siendo válido
        productoEditado.setPrecioHora(new BigDecimal("350.00"));
        productoEditado.setStockDisponible(10);
        productoEditado.setCantidadReservada(2);

        // 🔹 Convertir a JSON
        String productoJson = objectMapper.writeValueAsString(productoEditado);

        mockMvc.perform(put("/producto/editar/{id}", productoOriginal.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productoJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.precioHora").value(350.00))
                .andExpect(jsonPath("$.stockDisponible").value(10));

        // 🔹 Verificar en base de datos
        Producto actualizado = productoRepository.findById(productoOriginal.getId()).orElseThrow();
        assertThat(actualizado.getPrecioHora()).isEqualByComparingTo("350.00");
        assertThat(actualizado.getStockDisponible()).isEqualTo(10);
    }

}
