package com.logitrack.controller;

import com.logitrack.model.TipoOperacion;
import com.logitrack.service.AuditoriaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/auditorias")
@Tag(name = "Auditorías", description = "Operaciones relacionadas con las auditorías")
public class AuditoriaController {

    private final AuditoriaService auditoriaService;

    public AuditoriaController(AuditoriaService auditoriaService) {
        this.auditoriaService = auditoriaService;
    }

    @GetMapping
    public ResponseEntity<?> listarTodas() {
        return ResponseEntity.ok(auditoriaService.listarTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(auditoriaService.buscarPorId(id));
    }

    @GetMapping("/usuario/{usuario}")
    public ResponseEntity<?> buscarPorUsuario(@PathVariable String usuario) {
        return ResponseEntity.ok(auditoriaService.buscarPorUsuario(usuario));
    }

    @GetMapping("/operacion/{operacion}")
    public ResponseEntity<?> buscarPorOperacion(@PathVariable TipoOperacion operacion) {
        return ResponseEntity.ok(auditoriaService.buscarPorOperacion(operacion));
    }
}