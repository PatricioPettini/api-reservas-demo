package com.patojunit.service;

import com.patojunit.dto.request.ReservaCrearEditarDTO;
import com.patojunit.dto.request.ProductoCantidadCrearEditarDTO;
import com.patojunit.dto.response.ProductoCantidadGetDTO;
import com.patojunit.dto.response.ProductoGetDTO;
import com.patojunit.dto.response.ReservaGetDTO;
import com.patojunit.model.Producto;
import com.patojunit.model.ProductoCantidad;
import com.patojunit.model.Reserva;
import com.patojunit.repository.IReservaRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReservaService implements IReservaService {

    private final IReservaRepository reservaRepository;
    private final IProductoService productoService;

    @Override
    @Transactional
    public ReservaGetDTO crear(ReservaCrearEditarDTO dto) {
        Reserva reserva = mapToEntity(dto);

        List<ProductoCantidad> productosActualizados = new ArrayList<>();
        for (ProductoCantidadCrearEditarDTO p : dto.getProductos()) {
            verificarReserva(p.getIdProducto(), p.getCantidad());
            productosActualizados.add(mapToProductoCantidad(p, reserva));
        }
        reserva.setProductos(productosActualizados);
        reserva.setCodigoReserva(generarCodigoReserva(dto.getTelefonoCliente()));
        reserva.setPrecioTotal(calcularPrecioTotalReserva(reserva));

        Reserva guardada = reservaRepository.save(reserva);
        return mapToGetDTO(guardada);
    }

    @Override
    @Transactional
    public ReservaGetDTO editar(Long id, ReservaCrearEditarDTO dto) {
        Reserva reserva = getEntity(id);

        if (dto.getProductos() != null && !dto.getProductos().isEmpty()) {
            List<ProductoCantidad> productosActualizados = new ArrayList<>();
            for (ProductoCantidadCrearEditarDTO p : dto.getProductos()) {
                verificarReserva(p.getIdProducto(), p.getCantidad());
                productosActualizados.add(mapToProductoCantidad(p, reserva));
            }
            reserva.setProductos(productosActualizados);
        }

        reserva.setFechaInicio(dto.getFechaInicio());
        reserva.setFechaFin(dto.getFechaFin());
        reserva.setPrecioTotal(calcularPrecioTotalReserva(reserva));
        reserva.setCodigoReserva(generarCodigoReserva(dto.getTelefonoCliente()));
        reserva.setPagado(dto.getPagado());
        reserva.setTelefonoCliente(dto.getTelefonoCliente());

        Reserva actualizada = reservaRepository.save(reserva);
        return mapToGetDTO(actualizada);
    }

    @Override
    public void eliminar(Long id) {
        getEntity(id);
        reservaRepository.deleteById(id);
    }

    @Override
    public List<ReservaGetDTO> getAll() {
        return reservaRepository.findAll()
                .stream()
                .map(this::mapToGetDTO)
                .toList();
    }

    @Override
    public ReservaGetDTO get(Long id) {
        Reserva reserva = getEntity(id);
        return mapToGetDTO(reserva);
    }

    @Override
    @Transactional
    public ReservaGetDTO cancelarReserva(Long id) {
        Reserva reserva = getEntity(id);

        if ("cancelado".equalsIgnoreCase(reserva.getEstado())) {
            throw new IllegalArgumentException("La reserva ya se encuentra cancelada");
        }

        reserva.setEstado("cancelado");

        for (ProductoCantidad productoCantidad : reserva.getProductos()) {
            Producto producto = productoCantidad.getProducto();
            restablecerStock(producto, productoCantidad.getCantidad());
        }

        Reserva cancelada = reservaRepository.save(reserva);
        return mapToGetDTO(cancelada);
    }

    @Override
    public Reserva getEntity(Long id) {
        return reservaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No existe reserva con ese id"));
    }

    private void verificarReserva(Long idProducto, int cantidad) {
        Producto producto = productoService.getEntity(idProducto);
        if (producto.getStockDisponible() < cantidad) {
            throw new IllegalArgumentException("Stock insuficiente para este producto!");
        }
        productoService.modificarStock(producto, cantidad);
    }

    private void restablecerStock(Producto producto, int cantidad) {
        productoService.restablecerStock(producto, cantidad);
    }

    private String generarCodigoReserva(String telefonoCliente) {
        String prefijo = telefonoCliente.length() >= 5
                ? telefonoCliente.substring(0, 5).toUpperCase()
                : telefonoCliente.toUpperCase();
        String sufijo = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        return "RES-" + prefijo + "-" + sufijo;
    }

    public BigDecimal calcularPrecioTotalReserva(Reserva reserva) {
        long duracion = Duration.between(reserva.getFechaInicio(), reserva.getFechaFin()).toHours();
        BigDecimal horas = BigDecimal.valueOf(duracion);
        BigDecimal total = BigDecimal.ZERO;

        for (ProductoCantidad productoCantidad : reserva.getProductos()) {
            ProductoGetDTO producto = productoService.get(productoCantidad.getProducto().getId());
            total = total.add(
                    producto.getPrecioHora()
                            .multiply(horas)
                            .multiply(BigDecimal.valueOf(productoCantidad.getCantidad()))
            );
        }
        return total;
    }

    private Reserva mapToEntity(ReservaCrearEditarDTO dto) {
        Reserva r = new Reserva();
        r.setFechaInicio(dto.getFechaInicio());
        r.setFechaFin(dto.getFechaFin());
        r.setPagado(dto.getPagado());
        r.setTelefonoCliente(dto.getTelefonoCliente());
        return r;
    }

    private ProductoCantidad mapToProductoCantidad(ProductoCantidadCrearEditarDTO dto, Reserva reserva) {
        ProductoCantidad pc = new ProductoCantidad();
        Producto producto = productoService.getEntity(dto.getIdProducto());
        pc.setProducto(producto);
        pc.setCantidad(dto.getCantidad());
        pc.setReserva(reserva);
        return pc;
    }


    private ReservaGetDTO mapToGetDTO(Reserva reserva) {
        ReservaGetDTO dto = new ReservaGetDTO();
        dto.setId(reserva.getId());
        dto.setCodigoReserva(reserva.getCodigoReserva());
        dto.setEstado(reserva.getEstado());
        dto.setTelefonoCliente(reserva.getTelefonoCliente());
        dto.setPagado(reserva.getPagado());
        dto.setFechaInicio(reserva.getFechaInicio());
        dto.setFechaFin(reserva.getFechaFin());
        dto.setPrecioTotal(reserva.getPrecioTotal());

        dto.setProductos(
                reserva.getProductos().stream()
                        .map(this::mapToProductoCantidadGetDTO)
                        .toList()
        );

        return dto;
    }

    private ProductoCantidadGetDTO mapToProductoCantidadGetDTO(ProductoCantidad pc) {
        ProductoCantidadGetDTO dto = new ProductoCantidadGetDTO();
        dto.setNombreProducto(pc.getProducto().getNombre());
        dto.setCantidad(pc.getCantidad());
        dto.setCantidad(pc.getCantidad());
        return dto;
    }


}
