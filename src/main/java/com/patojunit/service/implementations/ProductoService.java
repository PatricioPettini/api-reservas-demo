package com.patojunit.service.implementations;

import com.patojunit.dto.request.ProductoCrearEditarDTO;
import com.patojunit.dto.response.ProductoUserGetDTO;
import com.patojunit.helpers.logger.producto.ProductoLogger;
import com.patojunit.helpers.security.JwtRoleValidator;
import com.patojunit.helpers.security.RoleBasedMapper;
import com.patojunit.model.Producto;
import com.patojunit.repository.IProductoRepository;
import com.patojunit.service.operations.ProductoOperationService;
import com.patojunit.service.interfaces.IProductoService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * ProductoService: capa de orquestación.
 * Valida roles, maneja logs de alto nivel y delega la lógica al ProductoOperationService.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductoService implements IProductoService {

    private final ProductoOperationService operationService;
    private final JwtRoleValidator jwtRoleValidator;
    private final RoleBasedMapper roleBasedMapper;
    private final ProductoLogger productoLogger;
    private final IProductoRepository productoRepository;

    @Override
    @Transactional
    public ProductoUserGetDTO crear(ProductoCrearEditarDTO dto) {
        validarAccesoAdmin();
        try {
            Producto guardado = operationService.crearProducto(dto);
            productoLogger.logAccion("Creación de producto completada. ID={}", guardado.getId());
            return mapearPorRol(guardado);
        } catch (Exception e) {
            productoLogger.logErrorGeneral("Error al crear producto", e);
            throw e;
        }
    }

    @Override
    @Transactional
    public ProductoUserGetDTO editar(Long id, ProductoCrearEditarDTO dto) {
        validarAccesoAdmin();
        try {
            Producto actualizado = operationService.editarProducto(id, dto);
            productoLogger.logAccion("Edición completada para producto ID={}", actualizado.getId());
            return mapearPorRol(actualizado);
        } catch (Exception e) {
            productoLogger.logErrorGeneral("Error al editar producto con ID=" + id, e);
            throw e;
        }
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        validarAccesoAdmin();
        try {
            operationService.eliminarProducto(id);
            productoLogger.logAccion("Producto eliminado correctamente. ID={}", id);
        } catch (Exception e) {
            productoLogger.logErrorGeneral("Error al eliminar producto con ID=" + id, e);
            throw e;
        }
    }

    @Override
    public List<ProductoUserGetDTO> getAll() {
        try {
            List<Producto> productos = productoRepository.findAll();
            productoLogger.logAccion("Consulta de todos los productos realizada. Total={}", productos.size());

            return productos.stream()
                    .map(this::mapearPorRol)
                    .toList();
        } catch (Exception e) {
            productoLogger.logErrorGeneral("Error al obtener la lista de productos", e);
            throw e;
        }
    }

    @Override
    public ProductoUserGetDTO get(Long id) {
        try {
            Producto producto = productoRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("No existe producto con ID " + id));

            productoLogger.logAccion("Consulta individual de producto. ID={}", id);
            return mapearPorRol(producto);
        } catch (Exception e) {
            productoLogger.logErrorGeneral("Error al obtener producto con ID=" + id, e);
            throw e;
        }
    }

    /**
     * Mapea el producto al DTO correspondiente según el rol del usuario autenticado.
     */
    private ProductoUserGetDTO mapearPorRol(Producto producto) {
        return roleBasedMapper.mapByRole(
                producto,
                operationService.getMapper()::toUserGetDTO,
                operationService.getMapper()::toAdminGetDTO
        );
    }

    /**
     * Valida que el usuario autenticado sea administrador.
     */
    private void validarAccesoAdmin() {
        if (!jwtRoleValidator.isAdmin()) {
            productoLogger.logAdvertencia("Intento de acceso no autorizado a acción administrativa.");
            throw new AccessDeniedException("Solo los administradores pueden realizar esta acción.");
        }
    }
}