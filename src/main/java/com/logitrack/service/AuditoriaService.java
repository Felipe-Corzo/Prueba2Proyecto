package com.logitrack.service;

import com.logitrack.exception.ResourceNotFoundException;
import com.logitrack.model.Auditoria;
import com.logitrack.model.TipoOperacion;
import com.logitrack.repository.AuditoriaRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuditoriaService {

    private final AuditoriaRepository auditoriaRepository;

    public AuditoriaService(AuditoriaRepository auditoriaRepository) {
        this.auditoriaRepository = auditoriaRepository;
    }

    public Auditoria registrarAuditoria(
            TipoOperacion operacion,
            String usuario,
            String entidadAfectada,
            Long entidadId,
            String valoresAnteriores,
            String valoresNuevos
    ) {
        Auditoria auditoria = Auditoria.builder()
                .operacion(operacion)
                .fechaHora(LocalDateTime.now())
                .usuario(usuario)
                .entidadAfectada(entidadAfectada)
                .entidadId(entidadId)
                .valoresAnteriores(valoresAnteriores)
                .valoresNuevos(valoresNuevos)
                .build();

        return auditoriaRepository.save(auditoria);
    }

    public List<Auditoria> listarTodas() {
        return auditoriaRepository.findAll();
    }

    public Auditoria buscarPorId(Long id) {
    return auditoriaRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Auditoría no encontrada con id: " + id));
    }

    public List<Auditoria> buscarPorUsuario(String usuario) {
        return auditoriaRepository.findByUsuario(usuario);
    }

    public List<Auditoria> buscarPorOperacion(TipoOperacion operacion) {
        return auditoriaRepository.findByOperacion(operacion);
    }
}