package com.logitrack.controller;

import com.logitrack.dto.ReporteResumenResponse;
import com.logitrack.service.ReporteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reportes")
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