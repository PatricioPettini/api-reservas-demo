package com.patojunit.service.implementations;

import com.patojunit.dto.request.ProductoCrearEditarDTO;
import com.patojunit.dto.response.ProductoAdminGetDTO;
import com.patojunit.dto.response.ProductoUserGetDTO;
import com.patojunit.helpers.logger.producto.ProductoLogger;
import com.patojunit.helpers.security.JwtRoleValidator;
import com.patojunit.helpers.security.RoleBasedMapper;
import com.patojunit.model.Producto;
import com.patojunit.repository.IProductoRepository;
import com.patojunit.service.operations.ProductoOperationService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class ProductoServiceTest {

    @Mock private ProductoOperationService operationService;
    @Mock private JwtRoleValidator jwtRoleValidator;
    @Mock private RoleBasedMapper roleBasedMapper;
    @Mock private ProductoLogger productoLogger;
    @Mock private IProductoRepository productoRepository;

    @InjectMocks
    private ProductoService productoService;
    private ProductoCrearEditarDTO dto;
    private Producto producto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        dto = new ProductoCrearEditarDTO("Reposera", null, 0, 0);
        producto = new Producto();
        producto.setId(1L);    }

    @Test
    @DisplayName("Debe crear un producto correctamente cuando el usuario es admin")
    void crear_DeberiaGuardarYRetornarDTO() {
        when(jwtRoleValidator.isAdmin()).thenReturn(true);

        var mapperMock = mock(com.patojunit.helpers.producto.ProductoMapper.class);
        when(operationService.getMapper()).thenReturn(mapperMock);
        when(mapperMock.toUserGetDTO(any())).thenReturn(new ProductoUserGetDTO());
        when(mapperMock.toAdminGetDTO(any())).thenReturn(new ProductoAdminGetDTO());

        ProductoCrearEditarDTO dto = new ProductoCrearEditarDTO();
        Producto producto = new Producto();
        producto.setId(1L);
        ProductoUserGetDTO dtoResponse = new ProductoUserGetDTO();

        when(operationService.crearProducto(dto)).thenReturn(producto);
        when(roleBasedMapper.mapByRole(eq(producto), any(), any())).thenReturn(dtoResponse);

        ProductoUserGetDTO result = productoService.crear(dto);

        verify(operationService).crearProducto(dto);
        verify(productoLogger).logAccion(contains("Creación de producto completada"), eq(1L));
        assertThat(result).isEqualTo(dtoResponse);
    }

    @Test
    @DisplayName("Debe lanzar excepción si un usuario no admin intenta crear un producto")
    void crear_DeberiaLanzarExcepcionSiNoEsAdmin() {
        when(jwtRoleValidator.isAdmin()).thenReturn(false);
        ProductoCrearEditarDTO dto = new ProductoCrearEditarDTO();

        assertThatThrownBy(() -> productoService.crear(dto))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Solo los administradores");

        verify(productoLogger).logAdvertencia(contains("Intento de acceso no autorizado"));
        verifyNoInteractions(operationService);
    }

    @Test
    @DisplayName("Debe editar un producto correctamente si el usuario es admin")
    void editar_DeberiaActualizarYRetornarDTO() {

        when(jwtRoleValidator.isAdmin()).thenReturn(true);
        var mapperMock = mock(com.patojunit.helpers.producto.ProductoMapper.class);
        when(operationService.getMapper()).thenReturn(mapperMock);
        when(mapperMock.toUserGetDTO(any())).thenReturn(new ProductoUserGetDTO());
        when(mapperMock.toAdminGetDTO(any())).thenReturn(new ProductoAdminGetDTO());

        ProductoCrearEditarDTO dto = new ProductoCrearEditarDTO();
        Producto producto = new Producto();
        producto.setId(2L);
        ProductoUserGetDTO dtoResponse = new ProductoUserGetDTO();

        when(operationService.editarProducto(2L, dto)).thenReturn(producto);
        when(roleBasedMapper.mapByRole(eq(producto), any(), any())).thenReturn(dtoResponse);

        ProductoUserGetDTO result = productoService.editar(2L, dto);

        verify(operationService).editarProducto(2L, dto);
        verify(productoLogger).logAccion(contains("Edición completada"), eq(2L));
        assertThat(result).isEqualTo(dtoResponse);
    }

    @Test
    @DisplayName("Debe lanzar excepción si un usuario no admin intenta editar")
    void editar_DeberiaLanzarAccessDeniedSiNoEsAdmin() {
        when(jwtRoleValidator.isAdmin()).thenReturn(false);

        assertThatThrownBy(() -> productoService.editar(1L, new ProductoCrearEditarDTO()))
                .isInstanceOf(AccessDeniedException.class);
        verify(productoLogger).logAdvertencia(contains("Intento de acceso no autorizado"));
        verifyNoInteractions(operationService);
    }

    @Test
    @DisplayName("Debe eliminar un producto correctamente si el usuario es admin")
    void eliminar_DeberiaEliminarYLoguear() {
        when(jwtRoleValidator.isAdmin()).thenReturn(true);

        productoService.eliminar(5L);

        verify(operationService).eliminarProducto(5L);
        verify(productoLogger).logAccion(contains("Producto eliminado correctamente"), eq(5L));
    }

    @Test
    @DisplayName("Debe lanzar excepción si un usuario no admin intenta eliminar")
    void eliminar_DeberiaLanzarExcepcionSiNoEsAdmin() {
        when(jwtRoleValidator.isAdmin()).thenReturn(false);

        assertThatThrownBy(() -> productoService.eliminar(3L))
                .isInstanceOf(AccessDeniedException.class);
        verify(productoLogger).logAdvertencia(contains("Intento de acceso no autorizado"));
        verifyNoInteractions(operationService);
    }

    @Test
    @DisplayName("Debe devolver todos los productos correctamente")
    void getAll_DeberiaRetornarListaDeProductos() {
        Producto p1 = new Producto();
        p1.setId(1L);
        when(productoRepository.findAll()).thenReturn(List.of(p1));
        when(roleBasedMapper.mapByRole(eq(p1), any(), any())).thenReturn(new ProductoUserGetDTO());

        var mapperMock = mock(com.patojunit.helpers.producto.ProductoMapper.class);
        when(operationService.getMapper()).thenReturn(mapperMock);
        when(mapperMock.toUserGetDTO(any())).thenReturn(new ProductoUserGetDTO());
        when(mapperMock.toAdminGetDTO(any())).thenReturn(new ProductoAdminGetDTO());

        List<ProductoUserGetDTO> result = productoService.getAll();

        verify(productoRepository).findAll();
        verify(productoLogger).logAccion(contains("Consulta de todos los productos realizada"), eq(1));
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("Debe devolver un producto por ID si existe")
    void get_DeberiaRetornarProductoExistente() {
        Producto p = new Producto();
        p.setId(10L);
        when(productoRepository.findById(10L)).thenReturn(Optional.of(p));
        when(roleBasedMapper.mapByRole(eq(p), any(), any())).thenReturn(new ProductoUserGetDTO());

        var mapperMock = mock(com.patojunit.helpers.producto.ProductoMapper.class);
        when(operationService.getMapper()).thenReturn(mapperMock);
        when(mapperMock.toUserGetDTO(any())).thenReturn(new ProductoUserGetDTO());
        when(mapperMock.toAdminGetDTO(any())).thenReturn(new ProductoAdminGetDTO());

        ProductoUserGetDTO result = productoService.get(10L);

        verify(productoLogger).logAccion(contains("Consulta individual de producto"), eq(10L));
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("Debe lanzar EntityNotFoundException si el producto no existe")
    void get_DeberiaLanzarExcepcionSiNoExiste() {
        when(productoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productoService.get(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("No existe producto");

        verify(productoLogger).logErrorGeneral(contains("Error al obtener producto"), any());
    }

    @Test
    @DisplayName("Debe loguear error y relanzar excepción al fallar en crear()")
    void crear_DeberiaLanzarExcepcionYLoguear() {
        when(jwtRoleValidator.isAdmin()).thenReturn(true);
        when(operationService.crearProducto(dto)).thenThrow(new RuntimeException("Error en crear producto"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> productoService.crear(dto));

        assertEquals("Error en crear producto", ex.getMessage());
        verify(productoLogger).logErrorGeneral(eq("Error al crear producto"), any(RuntimeException.class));
    }

    @Test
    @DisplayName("Debe loguear error y relanzar excepción al fallar en editar()")
    void editar_DeberiaLanzarExcepcionYLoguear() {
        when(jwtRoleValidator.isAdmin()).thenReturn(true);
        when(operationService.editarProducto(eq(1L), eq(dto)))
                .thenThrow(new RuntimeException("Error al editar producto"));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> productoService.editar(1L, dto));

        assertEquals("Error al editar producto", ex.getMessage());
        verify(productoLogger).logErrorGeneral(contains("Error al editar producto con ID="), any(RuntimeException.class));
    }

    @Test
    @DisplayName("Debe loguear error y relanzar excepción al fallar en eliminar()")
    void eliminar_DeberiaLanzarExcepcionYLoguear() {
        when(jwtRoleValidator.isAdmin()).thenReturn(true);
        doThrow(new RuntimeException("Error eliminando")).when(operationService).eliminarProducto(1L);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> productoService.eliminar(1L));

        assertEquals("Error eliminando", ex.getMessage());
        verify(productoLogger).logErrorGeneral(contains("Error al eliminar producto con ID="), any(RuntimeException.class));
    }

    @Test
    @DisplayName("Debe loguear error y relanzar excepción al fallar en getAll()")
    void getAll_DeberiaLanzarExcepcionYLoguear() {
        when(productoRepository.findAll()).thenThrow(new RuntimeException("Error en DB"));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> productoService.getAll());

        assertEquals("Error en DB", ex.getMessage());
        verify(productoLogger).logErrorGeneral(eq("Error al obtener la lista de productos"), any(RuntimeException.class));
    }

    @Test
    @DisplayName("Debe loguear error y relanzar excepción al fallar en get()")
    void get_DeberiaLanzarExcepcionYLoguear() {
        when(productoRepository.findById(1L)).thenThrow(new RuntimeException("Error inesperado"));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> productoService.get(1L));

        assertEquals("Error inesperado", ex.getMessage());
        verify(productoLogger).logErrorGeneral(contains("Error al obtener producto con ID="), any(RuntimeException.class));
    }

    @Test
    @DisplayName("Debe lanzar AccessDeniedException si no es admin")
    void validarAccesoAdmin_DeberiaLanzarAccessDeniedException() {
        when(jwtRoleValidator.isAdmin()).thenReturn(false);

        AccessDeniedException ex = assertThrows(AccessDeniedException.class,
                () -> productoService.eliminar(1L));

        assertEquals("Solo los administradores pueden realizar esta acción.", ex.getMessage());
        verify(productoLogger).logAdvertencia(contains("Intento de acceso no autorizado"));
    }
}
