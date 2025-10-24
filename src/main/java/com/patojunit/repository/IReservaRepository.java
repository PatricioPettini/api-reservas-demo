package com.patojunit.repository;

import com.patojunit.model.Reserva;
import com.patojunit.model.enums.EstadoReserva;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IReservaRepository extends JpaRepository<Reserva,Long> {
    List<Reserva> findByUsuario_Username(String username);
    boolean existsByProductos_Producto_Id(Long id);
    @EntityGraph(attributePaths = {"productos", "productos.producto"})
    List<Reserva> findByEstadoAndFechaInicioBefore(EstadoReserva estado, LocalDateTime fecha);

    @EntityGraph(attributePaths = {"productos", "productos.producto"})
    List<Reserva> findByEstadoAndFechaFinBefore(EstadoReserva estado, LocalDateTime fecha);
}