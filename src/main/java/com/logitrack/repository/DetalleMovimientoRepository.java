package com.logitrack.repository;

import com.logitrack.model.DetalleMovimiento;
import com.logitrack.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DetalleMovimientoRepository extends JpaRepository<DetalleMovimiento, Long> {

    List<DetalleMovimiento> findByProducto(Producto producto);
}