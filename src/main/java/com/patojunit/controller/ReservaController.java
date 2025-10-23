package com.patojunit.controller;

import com.patojunit.dto.request.ReservaCrearEditarDTO;
import com.patojunit.dto.response.ReservaUserGetDTO;
import com.patojunit.service.interfaces.IReservaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/reserva")
public class ReservaController {

    private final IReservaService reservaService;

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/eliminar/{id}")
    public String eliminarReserva(@PathVariable Long id){
        reservaService.eliminar(id);
        return ("reserva eliminada!");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/get")
    public List<ReservaUserGetDTO> getAllReservas(){
        return reservaService.getAll();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/get/{id}")
    public ReservaUserGetDTO getReserva(@PathVariable Long id){
        return reservaService.get(id);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @PostMapping("/crear")
    public ReservaUserGetDTO crearReserva(@Valid @RequestBody ReservaCrearEditarDTO reserva){
        return reservaService.crear(reserva);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/editar/{id}")
    public ReservaUserGetDTO editarReserva(@PathVariable Long id, @Valid @RequestBody ReservaCrearEditarDTO reserva){
        return reservaService.editar(id, reserva);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @PatchMapping("/cancelar/{id}")
    public ReservaUserGetDTO cancelarReserva(@PathVariable Long id){
        return reservaService.cancelarReserva(id);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @DeleteMapping("/eliminar-productos/{idReserva}")
    public ReservaUserGetDTO eliminarProducto(@PathVariable Long idReserva, @RequestBody List<Long> idProductos){
        return reservaService.eliminarProductos(idReserva, idProductos);
    }
}
