package com.logitrack.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetalleMovimientoRequest {

    private Long productoId;

    private Integer cantidad;
}