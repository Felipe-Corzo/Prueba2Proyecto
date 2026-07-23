package com.logitrack.service;

import com.logitrack.config.Auditable;
import com.logitrack.exception.ResourceNotFoundException;
import com.logitrack.model.Bodega;
import com.logitrack.model.TipoOperacion;
import com.logitrack.repository.BodegaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BodegaService {

    private final BodegaRepository bodegaRepository;

    public BodegaService(BodegaRepository bodegaRepository) {
        this.bodegaRepository = bodegaRepository;
    }

    public List<Bodega> listarTodas() {
        return bodegaRepository.findAll();
    }

    public Bodega buscarPorId(Long id) {
        return bodegaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bodega no encontrada con id: " + id));
    }

    @Auditable(operacion = TipoOperacion.INSERT, entidad = "Bodega", entidadClase = Bodega.class)
    public Bodega crear(Bodega bodega) {
        return bodegaRepository.save(bodega);
    }

    @Auditable(operacion = TipoOperacion.UPDATE, entidad = "Bodega", entidadClase = Bodega.class)
    public Bodega actualizar(Long id, Bodega bodegaActualizada) {
        Bodega bodegaExistente = buscarPorId(id);

        bodegaExistente.setNombre(bodegaActualizada.getNombre());
        bodegaExistente.setUbicacion(bodegaActualizada.getUbicacion());
        bodegaExistente.setCapacidad(bodegaActualizada.getCapacidad());
        bodegaExistente.setEncargado(bodegaActualizada.getEncargado());

        return bodegaRepository.save(bodegaExistente);
    }

    @Auditable(operacion = TipoOperacion.DELETE, entidad = "Bodega", entidadClase = Bodega.class)
    public void eliminar(Long id) {
        Bodega bodega = buscarPorId(id);
        bodegaRepository.delete(bodega);
    }
}