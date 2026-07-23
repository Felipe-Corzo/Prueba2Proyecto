package com.logitrack.controller;

import com.logitrack.dto.ReporteResumenResponse;
import com.logitrack.service.ReporteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/reportes")
@Tag(name = "Reportes", description = "Operaciones relacionadas con la generación de reportes")
public class ReporteController {

    private final ReporteService reporteService;

    public ReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    @GetMapping("/resumen")
    public ResponseEntity<ReporteResumenResponse> obtenerResumenGeneral() {
        return ResponseEntity.ok(reporteService.generarResumenGeneral());
    }
}