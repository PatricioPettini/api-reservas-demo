package com.patojunit.service;

import java.util.List;

public interface IGenericService<T> {
    void eliminar(Long id);
    T editar(Long id, T objeto);
    T crear(T objeto);
    List<T> getAll();
    T get(Long id);
}
