package com.patojunit.service;

import com.patojunit.model.Producto;
import com.patojunit.repository.IProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductoService implements IProductoService{


    private final IProductoRepository productoRepository;

    @Override
    public void eliminar(Long id) {
        if(get(id) != null) {
            productoRepository.deleteById(id);
        }
    }

    @Override
    public Producto editar(Long id, Producto objeto) {
        Producto producto=get(id);

        producto.setNombre(objeto.getNombre());
        producto.setPrecioHora(objeto.getPrecioHora());
        producto.setCantidadReservada(objeto.getCantidadReservada());
        producto.setStockDisponible(objeto.getStockDisponible());
        producto.setFechaUltimaActualizacion(LocalDateTime.now());
        producto.setCodigoProducto(generarCodigoProducto(objeto.getNombre()));

        return guardar(producto);
    }

    String generarCodigoProducto(String nombre){
        String prefijo = nombre.length() >= 3 ? nombre.substring(0, 3).toUpperCase() : nombre.toUpperCase();
        String sufijo = UUID.randomUUID().toString().substring(0, 4).toUpperCase();

        return "PROD-" + prefijo + "-" + sufijo;
    }

    @Override
    public Producto crear(Producto objeto) {
        objeto.setCodigoProducto(generarCodigoProducto(objeto.getNombre()));
        objeto.setFechaAlta(LocalDateTime.now());
        objeto.setFechaUltimaActualizacion(null);



        return guardar(objeto);
    }

    @Override
    public List<Producto> getAll() {
        return productoRepository.findAll();
    }

    @Override
    public Producto get(Long id) {
        return productoRepository.findById(id).orElseThrow(()->new IllegalArgumentException("No existe producto con ese id"));
    }

    @Override
    public int getStock(Long id) {
        return get(id).getStockDisponible();
    }

    @Override
    public Producto guardar(Producto producto) {
        return productoRepository.save(producto);
    }

    @Override
    public void modificarStock(Producto producto) {
        producto.setStockDisponible(producto.getStockDisponible()-1);
        producto.setCantidadReservada(producto.getCantidadReservada()+1);

        if (producto.getStockDisponible() < 0) {
            throw new IllegalArgumentException("Error de stock: no puede quedar stock negativo.");
        }
    }

    @Override
    public void restablecerStock(Producto producto) {
        producto.setCantidadReservada(producto.getCantidadReservada()-1);
        producto.setStockDisponible(producto.getStockDisponible()+1);
    }
}
