package com.logitrack.service;

import com.logitrack.exception.ResourceNotFoundException;
import com.logitrack.model.Producto;
import com.logitrack.repository.ProductoRepository;
import org.springframework.stereotype.Service;
import com.logitrack.config.Auditable;
import com.logitrack.model.TipoOperacion;

import java.util.List;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;

    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    public List<Producto> listarTodos() {
        return productoRepository.findAll();
    }

    public Producto buscarPorId(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con id: " + id));
    }

    @Auditable(operacion = TipoOperacion.INSERT, entidad = "Producto", entidadClase = Producto.class)
    public Producto crear(Producto producto) {
        if (producto.getStock() == null) {
            producto.setStock(0);
        }
    
        return productoRepository.save(producto);
    }
    
    @Auditable(operacion = TipoOperacion.UPDATE, entidad = "Producto", entidadClase = Producto.class)
    public Producto actualizar(Long id, Producto productoActualizado) {
        Producto productoExistente = buscarPorId(id);
    
        productoExistente.setNombre(productoActualizado.getNombre());
        productoExistente.setCategoria(productoActualizado.getCategoria());
        productoExistente.setStock(productoActualizado.getStock());
        productoExistente.setPrecio(productoActualizado.getPrecio());
    
        return productoRepository.save(productoExistente);
    }
    
    @Auditable(operacion = TipoOperacion.DELETE, entidad = "Producto", entidadClase = Producto.class)
    public void eliminar(Long id) {
        Producto producto = buscarPorId(id);
        productoRepository.delete(producto);
    }

    public List<Producto> listarStockBajo() {
        return productoRepository.findByStockLessThan(10);
    }
}