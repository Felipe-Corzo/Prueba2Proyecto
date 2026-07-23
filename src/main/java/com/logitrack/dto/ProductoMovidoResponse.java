package com.logitrack.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductoMovidoResponse {

    private Long productoId;

    private String producto;

    private Integer cantidadMovida;
}