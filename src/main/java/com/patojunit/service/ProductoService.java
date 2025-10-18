package com.patojunit.service;

import com.patojunit.dto.request.ProductoCrearEditarDTO;
import com.patojunit.dto.response.ProductoGetDTO;
import com.patojunit.model.Producto;
import com.patojunit.repository.IProductoRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductoService implements IProductoService {

    private final IProductoRepository productoRepository;

    @Override
    @Transactional
    public ProductoGetDTO crear(ProductoCrearEditarDTO dto) {
        Producto producto = mapToEntity(dto);
        producto.setCodigoProducto(generarCodigoProducto(dto.getNombre()));
        producto.setFechaAlta(LocalDateTime.now());
        producto.setFechaUltimaActualizacion(null);

        Producto guardado = productoRepository.save(producto);
        return mapToGetDTO(guardado);
    }

    @Override
    @Transactional
    public ProductoGetDTO editar(Long id, ProductoCrearEditarDTO dto) {
        Producto producto = getEntity(id);

        producto.setNombre(dto.getNombre());
        producto.setPrecioHora(dto.getPrecioHora());
        producto.setCantidadReservada(dto.getCantidadReservada());
        producto.setStockDisponible(dto.getStockDisponible());
        producto.setFechaUltimaActualizacion(LocalDateTime.now());
        producto.setCodigoProducto(generarCodigoProducto(dto.getNombre()));

        Producto actualizado = productoRepository.save(producto);
        return mapToGetDTO(actualizado);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        getEntity(id);
        productoRepository.deleteById(id);
    }

    @Override
    public List<ProductoGetDTO> getAll() {
        return productoRepository.findAll()
                .stream()
                .map(this::mapToGetDTO)
                .toList();
    }

    @Override
    public ProductoGetDTO get(Long id) {
        Producto producto = getEntity(id);
        return mapToGetDTO(producto);
    }

    @Override
    public int getStock(Long id) {
        return getEntity(id).getStockDisponible();
    }

    @Override
    public Producto guardar(Producto producto) {
        return productoRepository.save(producto);
    }

    @Override
    @Transactional
    public void modificarStock(Producto producto, int cantidad) {
        producto.setStockDisponible(producto.getStockDisponible() - cantidad);
        producto.setCantidadReservada(producto.getCantidadReservada() + cantidad);

        if (producto.getStockDisponible() < 0) {
            throw new IllegalArgumentException("Error de stock: no puede quedar stock negativo.");
        }
        productoRepository.save(producto);
    }

    @Override
    @Transactional
    public void restablecerStock(Producto producto, int cantidad) {
        producto.setCantidadReservada(producto.getCantidadReservada() - cantidad);
        producto.setStockDisponible(producto.getStockDisponible() + cantidad);
        productoRepository.save(producto);
    }

    private String generarCodigoProducto(String nombre) {
        String prefijo = nombre.length() >= 3 ? nombre.substring(0, 3).toUpperCase() : nombre.toUpperCase();
        String sufijo = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        return "PROD-" + prefijo + "-" + sufijo;
    }

    private Producto mapToEntity(ProductoCrearEditarDTO dto) {
        Producto producto = new Producto();
        producto.setNombre(dto.getNombre());
        producto.setPrecioHora(dto.getPrecioHora());
        producto.setStockDisponible(dto.getStockDisponible());
        producto.setCantidadReservada(dto.getCantidadReservada());
        return producto;
    }

    private ProductoGetDTO mapToGetDTO(Producto producto) {
        ProductoGetDTO dto = new ProductoGetDTO();
        dto.setId(producto.getId());
        dto.setNombre(producto.getNombre());
        dto.setPrecioHora(producto.getPrecioHora());
        dto.setStockDisponible(producto.getStockDisponible());
        dto.setCodigoProducto(producto.getCodigoProducto());
        return dto;
    }

    @Override
    public Producto getEntity(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No existe producto con ese id"));
    }

}
