package com.patojunit.controller;

import com.patojunit.dto.request.ProductoCrearEditarDTO;
import com.patojunit.dto.response.ProductoUserGetDTO;
import com.patojunit.service.interfaces.IProductoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/producto")
public class ProductoController {

    private final IProductoService productoService;

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/eliminar/{id}")
    public String eliminarProducto(@PathVariable Long id){
        productoService.eliminar(id);
        return ("producto eliminado!");
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping("/get")
    public List<ProductoUserGetDTO> getAllProductos(){
        return productoService.getAll();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping("/get/{id}")
    public ProductoUserGetDTO getProducto(@PathVariable Long id){
        return productoService.get(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/crear")
    public ProductoUserGetDTO crearProducto(@Valid @RequestBody ProductoCrearEditarDTO producto){
        return productoService.crear(producto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/editar/{id}")
    public ProductoUserGetDTO editarProducto(@PathVariable Long id, @Valid @RequestBody ProductoCrearEditarDTO producto){
        return productoService.editar(id, producto);
    }

}
