package com.logitrack.repository;

import com.logitrack.model.Bodega;
import com.logitrack.model.InventarioBodega;
import com.logitrack.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InventarioBodegaRepository extends JpaRepository<InventarioBodega, Long> {

    Optional<InventarioBodega> findByProductoAndBodega(Producto producto, Bodega bodega);

    List<InventarioBodega> findByBodega(Bodega bodega);

    List<InventarioBodega> findByProducto(Producto producto);
}