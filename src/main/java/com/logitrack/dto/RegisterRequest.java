package com.logitrack.dto;

import com.logitrack.model.Rol;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {

    private String nombre;

    private String email;

    private String password;

    private Rol rol;
}