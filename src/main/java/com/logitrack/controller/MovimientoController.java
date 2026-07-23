package com.logitrack.controller;

import com.logitrack.dto.MovimientoRequest;
import com.logitrack.model.Movimiento;
import com.logitrack.service.MovimientoService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/movimientos")
@Tag(name = "Movimientos", description = "Operaciones relacionadas con los movimientos")
public class MovimientoController {

    private final MovimientoService movimientoService;

    public MovimientoController(MovimientoService movimientoService) {
        this.movimientoService = movimientoService;
    }

    @GetMapping
    public ResponseEntity<?> listarTodos() {
        return ResponseEntity.ok(movimientoService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(movimientoService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<?> registrarMovimiento(@RequestBody MovimientoRequest request) {
        Movimiento movimiento = movimientoService.registrarMovimiento(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(movimiento);
    }

    @GetMapping("/fecha")
    public ResponseEntity<?> buscarPorRangoFechas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin
    ) {
        return ResponseEntity.ok(movimientoService.buscarPorRangoFechas(inicio, fin));
    }
}