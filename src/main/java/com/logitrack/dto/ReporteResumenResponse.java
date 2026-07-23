package com.logitrack.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReporteResumenResponse {

    private List<StockBodegaResponse> stockPorBodega;

    private List<ProductoMovidoResponse> productosMasMovidos;
}