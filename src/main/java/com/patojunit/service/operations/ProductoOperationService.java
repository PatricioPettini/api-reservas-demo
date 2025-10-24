package com.patojunit.service.operations;

import com.patojunit.dto.request.ProductoCrearEditarDTO;
import com.patojunit.factory.ProductoFactory;
import com.patojunit.helpers.logger.producto.ProductoLogger;
import com.patojunit.helpers.producto.ProductoMapper;
import com.patojunit.helpers.producto.ProductoStockService;
import com.patojunit.helpers.producto.ProductoValidator;
import com.patojunit.model.Producto;
import com.patojunit.repository.IProductoRepository;
import com.patojunit.repository.IReservaRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Maneja la lógica central del dominio de Producto.
 * Se encarga de las operaciones de negocio puras.
 */
@Service
@RequiredArgsConstructor
public class ProductoOperationService {

    private final IProductoRepository productoRepository;
    private final IReservaRepository reservaRepository;
    private final ProductoFactory productoFactory;
    private final ProductoValidator validator;
    @Getter
    private final ProductoMapper mapper;
    private final ProductoLogger productoLogger;
    private final ProductoStockService stockService;

    @Transactional
    public Producto crearProducto(ProductoCrearEditarDTO dto) {
        try {
            validator.validarProductoNoExiste(dto.getNombre());
            Producto producto = productoFactory.crearProducto(dto);
            Producto guardado = productoRepository.save(producto);
            productoLogger.logCreacionExitosa(guardado);
            return guardado;
        } catch (Exception e) {
            productoLogger.logErrorGeneral("Error al crear producto", e);
            throw e;
        }
    }

    @Transactional
    public Producto editarProducto(Long id, ProductoCrearEditarDTO dto) {
        Producto producto = getEntity(id);
        try {
            if (!producto.getNombre().equalsIgnoreCase(dto.getNombre())) {
                validator.validarProductoNoExiste(dto.getNombre());
            }
            productoFactory.actualizarProductoDesdeDTO(producto, dto);
            Producto actualizado = productoRepository.save(producto);
            productoLogger.logEdicionExitosa(actualizado);
            return actualizado;
        } catch (Exception e) {
            productoLogger.logError(producto, e);
            throw e;
        }
    }

    /**
     * Elimina un producto solo si no está asociado a reservas.
     * Si la eliminación es directa sin validaciones, debería quedar en el Service.
     */
    @Transactional
    public void eliminarProducto(Long id) {
        Producto producto = getEntity(id);
        if (reservaRepository.existsByProductos_Producto_Id(id)) {
            productoLogger.logAdvertencia("Intento de eliminar producto asociado a una reserva. ID={}", id);
            throw new IllegalStateException("No se puede eliminar el producto porque está asociado a una reserva.");
        }
        productoRepository.delete(producto);
        productoLogger.logEliminacionExitosa(id);
    }

    /**
     * Delegación a ProductoStockService para modificar stock.
     */
    @Transactional
    public void descontarStock(Producto producto, int cantidad) {
        try {
            stockService.descontarStock(producto, cantidad);
            productoLogger.logDescuentoStock(producto, cantidad);
        } catch (Exception e) {
            productoLogger.logError(producto, e);
            throw e;
        }
    }

    @Transactional
    public void restablecerStock(Producto producto, int cantidad) {
        try {
            stockService.reponerStock(producto, cantidad);
            productoLogger.logReposicionStock(producto, cantidad);
        } catch (Exception e) {
            productoLogger.logError(producto, e);
            throw e;
        }
    }

    public Producto getEntity(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> {
                    productoLogger.logAdvertencia("Intento de acceder a producto inexistente ID={}", id);
                    return new EntityNotFoundException("No existe producto con ese id");
                });
    }
}