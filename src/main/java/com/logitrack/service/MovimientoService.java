package com.logitrack.service;

import com.logitrack.dto.DetalleMovimientoRequest;
import com.logitrack.dto.MovimientoRequest;
import com.logitrack.exception.BadRequestException;
import com.logitrack.exception.ResourceNotFoundException;
import com.logitrack.model.*;
import com.logitrack.repository.*;
import com.logitrack.config.Auditable;

import jakarta.transaction.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class MovimientoService {

    private final MovimientoRepository movimientoRepository;
    private final ProductoRepository productoRepository;
    private final BodegaRepository bodegaRepository;
    private final InventarioBodegaRepository inventarioBodegaRepository;
    private final UsuarioRepository usuarioRepository;

    public MovimientoService(
            MovimientoRepository movimientoRepository,
            ProductoRepository productoRepository,
            BodegaRepository bodegaRepository,
            InventarioBodegaRepository inventarioBodegaRepository,
            UsuarioRepository usuarioRepository
    ) {
        this.movimientoRepository = movimientoRepository;
        this.productoRepository = productoRepository;
        this.bodegaRepository = bodegaRepository;
        this.inventarioBodegaRepository = inventarioBodegaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public List<Movimiento> listarTodos() {
        return movimientoRepository.findAll();
    }

    public Movimiento buscarPorId(Long id) {
        return movimientoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movimiento no encontrado con id: " + id));
    }

    public List<Movimiento> buscarPorRangoFechas(LocalDateTime inicio, LocalDateTime fin) {
        return movimientoRepository.findByFechaBetween(inicio, fin);
    }

    @Transactional
    @Auditable(operacion = TipoOperacion.INSERT, entidad = "Movimiento", entidadClase = Movimiento.class)
    public Movimiento registrarMovimiento(MovimientoRequest request) {
        validarRequest(request);
    
        Usuario usuarioResponsable = obtenerUsuarioAutenticado();
    
        Bodega bodegaOrigen = null;
        Bodega bodegaDestino = null;
    
        if (request.getBodegaOrigenId() != null) {
            bodegaOrigen = buscarBodega(request.getBodegaOrigenId());
        }
    
        if (request.getBodegaDestinoId() != null) {
            bodegaDestino = buscarBodega(request.getBodegaDestinoId());
        }
    
        validarBodegasSegunTipo(request.getTipoMovimiento(), bodegaOrigen, bodegaDestino);
    
        Movimiento movimiento = Movimiento.builder()
                .fecha(LocalDateTime.now())
                .tipoMovimiento(request.getTipoMovimiento())
                .usuarioResponsable(usuarioResponsable)
                .bodegaOrigen(bodegaOrigen)
                .bodegaDestino(bodegaDestino)
                .detalles(new ArrayList<>())
                .build();
    
        for (DetalleMovimientoRequest detalleRequest : request.getDetalles()) {
            Producto producto = buscarProducto(detalleRequest.getProductoId());
            Integer cantidad = detalleRequest.getCantidad();
        
            if (cantidad == null || cantidad <= 0) {
                throw new BadRequestException("La cantidad debe ser mayor a 0");
            }
        
            procesarInventario(
                    request.getTipoMovimiento(),
                    producto,
                    cantidad,
                    bodegaOrigen,
                    bodegaDestino
            );
        
            DetalleMovimiento detalle = DetalleMovimiento.builder()
                    .movimiento(movimiento)
                    .producto(producto)
                    .cantidad(cantidad)
                    .build();
        
            movimiento.getDetalles().add(detalle);
        }
    
        return movimientoRepository.save(movimiento);
    }

    private void validarRequest(MovimientoRequest request) {
        if (request == null) {
            throw new BadRequestException("La información del movimiento es obligatoria");
        }

        if (request.getTipoMovimiento() == null) {
            throw new BadRequestException("El tipo de movimiento es obligatorio");
        }

        if (request.getDetalles() == null || request.getDetalles().isEmpty()) {
            throw new BadRequestException("Debe agregar al menos un producto al movimiento");
        }
    }

    private void validarBodegasSegunTipo(
            TipoMovimiento tipoMovimiento,
            Bodega bodegaOrigen,
            Bodega bodegaDestino
    ) {
        if (tipoMovimiento == TipoMovimiento.ENTRADA) {
            if (bodegaDestino == null) {
                throw new BadRequestException("Para una ENTRADA debe indicar la bodega destino");
            }
        }

        if (tipoMovimiento == TipoMovimiento.SALIDA) {
            if (bodegaOrigen == null) {
                throw new BadRequestException("Para una SALIDA debe indicar la bodega origen");
            }
        }

        if (tipoMovimiento == TipoMovimiento.TRANSFERENCIA) {
            if (bodegaOrigen == null || bodegaDestino == null) {
                throw new BadRequestException("Para una TRANSFERENCIA debe indicar bodega origen y destino");
            }

            if (bodegaOrigen.getId().equals(bodegaDestino.getId())) {
                throw new BadRequestException("La bodega origen y destino no pueden ser la misma");
            }
        }
    }

    private void procesarInventario(
            TipoMovimiento tipoMovimiento,
            Producto producto,
            Integer cantidad,
            Bodega bodegaOrigen,
            Bodega bodegaDestino
    ) {
        switch (tipoMovimiento) {
            case ENTRADA:
                procesarEntrada(producto, cantidad, bodegaDestino);
                break;

            case SALIDA:
                procesarSalida(producto, cantidad, bodegaOrigen);
                break;

            case TRANSFERENCIA:
                procesarTransferencia(producto, cantidad, bodegaOrigen, bodegaDestino);
                break;

            default:
                throw new BadRequestException("Tipo de movimiento no válido");
        }
    }

    private void procesarEntrada(Producto producto, Integer cantidad, Bodega bodegaDestino) {
        InventarioBodega inventarioDestino = obtenerOCrearInventario(producto, bodegaDestino);

        if (producto.getStock() == null) {
            producto.setStock(0);
        }

        inventarioDestino.setStock(inventarioDestino.getStock() + cantidad);
        producto.setStock(producto.getStock() + cantidad);

        inventarioBodegaRepository.save(inventarioDestino);
        productoRepository.save(producto);
    }

    private void procesarSalida(Producto producto, Integer cantidad, Bodega bodegaOrigen) {
        InventarioBodega inventarioOrigen = obtenerInventarioExistente(producto, bodegaOrigen);

        validarStockDisponible(inventarioOrigen, cantidad);

        if (producto.getStock() == null) {
            producto.setStock(0);
        }

        inventarioOrigen.setStock(inventarioOrigen.getStock() - cantidad);
        producto.setStock(producto.getStock() - cantidad);

        inventarioBodegaRepository.save(inventarioOrigen);
        productoRepository.save(producto);
    }

    private void procesarTransferencia(
            Producto producto,
            Integer cantidad,
            Bodega bodegaOrigen,
            Bodega bodegaDestino
    ) {
        InventarioBodega inventarioOrigen = obtenerInventarioExistente(producto, bodegaOrigen);
        InventarioBodega inventarioDestino = obtenerOCrearInventario(producto, bodegaDestino);

        validarStockDisponible(inventarioOrigen, cantidad);

        inventarioOrigen.setStock(inventarioOrigen.getStock() - cantidad);
        inventarioDestino.setStock(inventarioDestino.getStock() + cantidad);

        inventarioBodegaRepository.save(inventarioOrigen);
        inventarioBodegaRepository.save(inventarioDestino);
    }

    private InventarioBodega obtenerOCrearInventario(Producto producto, Bodega bodega) {
        return inventarioBodegaRepository.findByProductoAndBodega(producto, bodega)
                .orElseGet(() -> InventarioBodega.builder()
                        .producto(producto)
                        .bodega(bodega)
                        .stock(0)
                        .build());
    }

    private InventarioBodega obtenerInventarioExistente(Producto producto, Bodega bodega) {
        return inventarioBodegaRepository.findByProductoAndBodega(producto, bodega)
                .orElseThrow(() -> new BadRequestException(
                        "No existe inventario del producto " + producto.getNombre()
                                + " en la bodega " + bodega.getNombre()
                ));
    }

    private void validarStockDisponible(InventarioBodega inventario, Integer cantidad) {
        if (inventario.getStock() < cantidad) {
            throw new BadRequestException(
                    "Stock insuficiente. Stock disponible: " + inventario.getStock()
            );
        }
    }

    private Producto buscarProducto(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con id: " + id));
    }

    private Bodega buscarBodega(Long id) {
        return bodegaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bodega no encontrada con id: " + id));
    }

    private Usuario obtenerUsuarioAutenticado() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario autenticado no encontrado"));
    }
}