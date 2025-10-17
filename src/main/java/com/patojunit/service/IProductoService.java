package com.patojunit.service;

import com.patojunit.model.Producto;

public interface IProductoService extends IGenericService<Producto>{
    public int getStock(Long id);
    public Producto guardar(Producto producto);
    public void modificarStock(Producto producto);
    public void restablecerStock(Producto producto);
}
