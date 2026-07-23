package com.logitrack.dto;

import com.logitrack.model.TipoMovimiento;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovimientoRequest {

    private TipoMovimiento tipoMovimiento;

    private Long bodegaOrigenId;

    private Long bodegaDestinoId;

    private List<DetalleMovimientoRequest> detalles;
}