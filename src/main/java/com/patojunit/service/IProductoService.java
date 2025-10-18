package com.patojunit.service;

import com.patojunit.dto.request.ProductoCrearEditarDTO;
import com.patojunit.dto.response.ProductoGetDTO;
import com.patojunit.model.Producto;

public interface IProductoService extends IGenericService<ProductoCrearEditarDTO, ProductoGetDTO>{
    public int getStock(Long id);
    public Producto guardar(Producto producto);
    public void modificarStock(Producto producto, int cantidad);
    public void restablecerStock(Producto producto, int cantidad);
    Producto getEntity(Long id);
}
