package com.patojunit.service;

import com.patojunit.model.Producto;
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
public class ReservaService implements IReservaService{

    private final IReservaRepository reservaRepository;

    private final IProductoService productoService;

    @Override
    public void eliminar(Long id) {
        if(get(id) != null) {
            reservaRepository.deleteById(id);
        }
    }

    @Override
    @Transactional
    public Reserva editar(Long id, Reserva objeto) {

        Reserva reserva=get(id);

        List<Producto> productosActualizados = new ArrayList<>();

        for (Producto p : objeto.getProductos()) {
            verificarReserva(p.getId());
            Producto producto = productoService.get(p.getId());
            productosActualizados.add(producto);
        }
        reserva.setProductos(productosActualizados);
        reserva.setEstado(objeto.getEstado());
        reserva.setFechaInicio(objeto.getFechaInicio());
        reserva.setFechaFin(objeto.getFechaFin());
        reserva.setPrecioTotal(calcularPrecioTotalReserva(reserva));
        reserva.setCodigoReserva(generarCodigoReserva(reserva.getTelefonoCliente()));
        reserva.setPagado(objeto.getPagado());
        reserva.setTelefonoCliente(objeto.getTelefonoCliente());

        return reservaRepository.save(reserva);
    }

    @Override
    @Transactional
    public Reserva crear(Reserva objeto) {

        List<Producto> productosActualizados = new ArrayList<>();

        for (Producto p : objeto.getProductos()) {
            verificarReserva(p.getId());
            Producto producto = productoService.get(p.getId());
            productosActualizados.add(producto);
        }

        objeto.setCodigoReserva(generarCodigoReserva(objeto.getTelefonoCliente()));
        objeto.setProductos(productosActualizados);
        objeto.setPrecioTotal(calcularPrecioTotalReserva(objeto));
        return reservaRepository.save(objeto);
    }

    String generarCodigoReserva(String telefonoCliente){
        String prefijo = telefonoCliente.length() >= 5 ? telefonoCliente.substring(0, 5).toUpperCase() : telefonoCliente.toUpperCase();
        String sufijo = UUID.randomUUID().toString().substring(0, 4).toUpperCase();

        return "RES-" + prefijo + "-" + sufijo;
    }

    public void verificarReserva(Long idProducto){

        Producto producto=productoService.get(idProducto);

        if(producto.getStockDisponible() == 0){
            throw new IllegalArgumentException("No hay stock disponible para este producto!");
        }

        modificarStock(producto);
    }

    public void modificarStock(Producto producto) {
        productoService.modificarStock(producto);
    }

    @Override
    public List<Reserva> getAll() {
        return reservaRepository.findAll();
    }

    @Override
    public Reserva get(Long id) {
        return reservaRepository.findById(id).orElseThrow(()->new IllegalArgumentException("No existe reserva con ese id"));
    }

    @Override
    @Transactional
    public Reserva cancelarReserva(Long id) {
        Reserva reserva=get(id);

        if(reserva.getEstado().equals("cancelado")) throw new IllegalArgumentException("La reserva ya se encuentra cancelada");

        reserva.setEstado("cancelado");

        for (Producto producto : reserva.getProductos()){
                Producto producto1=productoService.get(producto.getId());
                restablecerStock(producto1);
            }

        return reservaRepository.save(reserva);
    }

    public void restablecerStock(Producto producto) {
        productoService.restablecerStock(producto);
    }

    public BigDecimal calcularPrecioTotalReserva(Reserva reserva) {
        long duracionReserva=Duration.between(reserva.getFechaInicio(), reserva.getFechaFin()).toHours();
        BigDecimal precioTotal = BigDecimal.ZERO;
        BigDecimal horas = BigDecimal.valueOf(duracionReserva);

        for (Producto producto:reserva.getProductos()){
            Producto producto1=productoService.get(producto.getId());
            precioTotal=precioTotal.add(producto1.getPrecioHora().multiply(horas));
        }

        return precioTotal;
    }
}
