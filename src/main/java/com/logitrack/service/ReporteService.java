package com.logitrack.service;

import com.logitrack.dto.ProductoMovidoResponse;
import com.logitrack.dto.ReporteResumenResponse;
import com.logitrack.dto.StockBodegaResponse;
import com.logitrack.model.DetalleMovimiento;
import com.logitrack.model.InventarioBodega;
import com.logitrack.repository.DetalleMovimientoRepository;
import com.logitrack.repository.InventarioBodegaRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ReporteService {

    private final InventarioBodegaRepository inventarioBodegaRepository;
    private final DetalleMovimientoRepository detalleMovimientoRepository;

    public ReporteService(
            InventarioBodegaRepository inventarioBodegaRepository,
            DetalleMovimientoRepository detalleMovimientoRepository
    ) {
        this.inventarioBodegaRepository = inventarioBodegaRepository;
        this.detalleMovimientoRepository = detalleMovimientoRepository;
    }

    public ReporteResumenResponse generarResumenGeneral() {
        List<StockBodegaResponse> stockPorBodega = obtenerStockTotalPorBodega();
        List<ProductoMovidoResponse> productosMasMovidos = obtenerProductosMasMovidos();

        return ReporteResumenResponse.builder()
                .stockPorBodega(stockPorBodega)
                .productosMasMovidos(productosMasMovidos)
                .build();
    }

    private List<StockBodegaResponse> obtenerStockTotalPorBodega() {
        List<InventarioBodega> inventarios = inventarioBodegaRepository.findAll();

        Map<Long, StockBodegaResponse> mapaStock = new HashMap<>();

        for (InventarioBodega inventario : inventarios) {
            Long bodegaId = inventario.getBodega().getId();
            String nombreBodega = inventario.getBodega().getNombre();
            Integer stock = inventario.getStock();

            if (stock == null) {
                stock = 0;
            }

            if (!mapaStock.containsKey(bodegaId)) {
                StockBodegaResponse response = StockBodegaResponse.builder()
                        .bodegaId(bodegaId)
                        .bodega(nombreBodega)
                        .stockTotal(0)
                        .build();

                mapaStock.put(bodegaId, response);
            }

            StockBodegaResponse response = mapaStock.get(bodegaId);
            response.setStockTotal(response.getStockTotal() + stock);
        }

        return new ArrayList<>(mapaStock.values());
    }

    private List<ProductoMovidoResponse> obtenerProductosMasMovidos() {
        List<DetalleMovimiento> detalles = detalleMovimientoRepository.findAll();

        Map<Long, ProductoMovidoResponse> mapaProductos = new HashMap<>();

        for (DetalleMovimiento detalle : detalles) {
            Long productoId = detalle.getProducto().getId();
            String nombreProducto = detalle.getProducto().getNombre();
            Integer cantidad = detalle.getCantidad();

            if (cantidad == null) {
                cantidad = 0;
            }

            if (!mapaProductos.containsKey(productoId)) {
                ProductoMovidoResponse response = ProductoMovidoResponse.builder()
                        .productoId(productoId)
                        .producto(nombreProducto)
                        .cantidadMovida(0)
                        .build();

                mapaProductos.put(productoId, response);
            }

            ProductoMovidoResponse response = mapaProductos.get(productoId);
            response.setCantidadMovida(response.getCantidadMovida() + cantidad);
        }

        List<ProductoMovidoResponse> productos = new ArrayList<>(mapaProductos.values());

        productos.sort((p1, p2) -> p2.getCantidadMovida().compareTo(p1.getCantidadMovida()));

        if (productos.size() > 5) {
        return productos.subList(0, 5);
        }

        return productos;
    }
}