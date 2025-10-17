package com.patojunit.controller;

import com.patojunit.model.Reserva;
import com.patojunit.service.IReservaService;
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
    public List<Reserva> getAllReservas(){
        return reservaService.getAll();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/get/{id}")
    public Reserva getReserva(@PathVariable Long id){
        return reservaService.get(id);
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/crear")
    public Reserva crearReserva(@Valid @RequestBody Reserva reserva){
        return reservaService.crear(reserva);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/editar/{id}")
    public Reserva editarReserva(@PathVariable Long id, @Valid @RequestBody Reserva reserva){
        return reservaService.editar(id, reserva);
    }

    @PreAuthorize("hasRole('USER')")
    @PatchMapping("/{id}")
    public Reserva cancelarReserva(@PathVariable Long id){
        return reservaService.cancelarReserva(id);
    }

}
