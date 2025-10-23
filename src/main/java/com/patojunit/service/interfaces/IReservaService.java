package com.patojunit.service.interfaces;

import com.patojunit.dto.request.ReservaCrearEditarDTO;
import com.patojunit.dto.response.ReservaUserGetDTO;
import com.patojunit.model.Reserva;

import java.util.List;

public interface IReservaService extends IGenericService<ReservaCrearEditarDTO, ReservaUserGetDTO>{
    ReservaUserGetDTO cancelarReserva(Long id);
    Reserva getEntity(Long id);
    ReservaUserGetDTO eliminarProductos(Long idReserva, List<Long> idProductos);
}
