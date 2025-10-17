package com.patojunit.controller;

import com.patojunit.model.Producto;
import com.patojunit.service.IProductoService;
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

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/get")
    public List<Producto> getAllProductos(){
        return productoService.getAll();
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/get/{id}")
    public Producto getProducto(@PathVariable Long id){
        return productoService.get(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/get/stock/{id}")
    public int getStockProducto(@PathVariable Long id){
        return productoService.getStock(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/crear")
    public Producto crearProducto(@Valid @RequestBody Producto producto){
        return productoService.crear(producto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/editar/{id}")
    public Producto editarProducto(@PathVariable Long id,@Valid @RequestBody Producto producto){
        return productoService.editar(id, producto);
    }

}
