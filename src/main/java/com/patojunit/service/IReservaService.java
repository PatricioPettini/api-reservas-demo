package com.patojunit.service;

import com.patojunit.dto.request.ReservaCrearEditarDTO;
import com.patojunit.dto.response.ReservaGetDTO;
import com.patojunit.model.Reserva;

public interface IReservaService extends IGenericService<ReservaCrearEditarDTO, ReservaGetDTO>{
    ReservaGetDTO cancelarReserva(Long id);
    Reserva getEntity(Long id);
}
