package com.logitrack.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockBodegaResponse {

    private Long bodegaId;

    private String bodega;

    private Integer stockTotal;
}