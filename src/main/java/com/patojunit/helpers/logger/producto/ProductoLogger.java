package com.patojunit.helpers.logger.producto;

import com.patojunit.model.Producto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Centraliza todos los logs relacionados con la entidad Producto.
 * Mantiene consistencia entre operaciones (crear, editar, eliminar, stock, errores).
 */
@Slf4j
@Component
public class ProductoLogger {

    /**
     * Log de creación exitosa de producto.
     */
    public void logCreacionExitosa(Producto producto) {
        log.info("[ProductoLogger] Producto creado exitosamente: ID={}, Nombre='{}', Stock={}",
                producto.getId(), producto.getNombre(), producto.getStockDisponible());
    }

    /**
     * Log de edición exitosa de producto.
     */
    public void logEdicionExitosa(Producto producto) {
        log.info("[ProductoLogger] Producto editado correctamente: ID={}, Nombre='{}', Nuevo stock={}",
                producto.getId(), producto.getNombre(), producto.getStockDisponible());
    }

    /**
     * Log de eliminación exitosa.
     */
    public void logEliminacionExitosa(Long idProducto) {
        log.warn("[ProductoLogger] Producto eliminado exitosamente: ID={}", idProducto);
    }

    /**
     * Log al descontar stock.
     */
    public void logDescuentoStock(Producto producto, int cantidad) {
        log.debug("[ProductoLogger] Stock descontado para producto ID={}, Nombre='{}', Cantidad descontada={}, Stock restante={}",
                producto.getId(), producto.getNombre(), cantidad, producto.getStockDisponible());
    }

    /**
     * Log al reponer stock.
     */
    public void logReposicionStock(Producto producto, int cantidad) {
        log.debug("[ProductoLogger] Stock repuesto para producto ID={}, Nombre='{}', Cantidad repuesta={}, Stock actual={}",
                producto.getId(), producto.getNombre(), cantidad, producto.getStockDisponible());
    }

    /**
     * Log de error controlado.
     */
    public void logError(Producto producto, Exception e) {
        log.error("[ProductoLogger] Error en producto ID={}, Nombre='{}'. Causa: {}",
                producto != null ? producto.getId() : "desconocido",
                producto != null ? producto.getNombre() : "desconocido",
                e.getMessage(), e);
    }

    /**
     * Log de error general sin producto asociado.
     */
    public void logErrorGeneral(String mensaje, Exception e) {
        log.error("[ProductoLogger] {}. Causa: {}", mensaje, e.getMessage(), e);
    }

    /**
     * Log informativo para acciones genéricas (por ejemplo, validaciones).
     */
    public void logAccion(String mensaje, Object... args) {
        log.info("[ProductoLogger] " + mensaje, args);
    }

    public void logAccion(Producto producto, String mensaje, Object... args) {
        log.info("[ProductoLogger] Producto='{}' (ID={}) - {}",
                producto.getNombre(), producto.getId(),
                String.format(mensaje, args));
    }


    /**
     * Log de advertencia (sin cortar la ejecución).
     */
    public void logAdvertencia(String mensaje, Object... args) {
        log.warn("[ProductoLogger] " + mensaje, args);
    }
}
