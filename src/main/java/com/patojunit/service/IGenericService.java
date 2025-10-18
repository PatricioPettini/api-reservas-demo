package com.patojunit.service;

import java.util.List;

public interface IGenericService<CreateEditDTO, GetDTO> {
    void eliminar(Long id);

    GetDTO editar(Long id, CreateEditDTO objeto);

    GetDTO crear(CreateEditDTO objeto);

    List<GetDTO> getAll();

    GetDTO get(Long id);
}
