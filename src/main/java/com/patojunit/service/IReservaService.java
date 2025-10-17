package com.patojunit.service;

import com.patojunit.model.Reserva;

public interface IReservaService extends IGenericService<Reserva>{
    Reserva cancelarReserva(Long id);
}
