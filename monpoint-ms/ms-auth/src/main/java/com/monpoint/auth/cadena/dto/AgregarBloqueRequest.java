package com.monpoint.auth.cadena.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Cuerpo de {@code POST /auth/cadena}: el texto que se registrará en el nuevo bloque. */
public record AgregarBloqueRequest(
        @NotBlank(message = "Los datos del bloque son obligatorios")
        @Size(max = 500, message = "Los datos no pueden superar 500 caracteres")
        String datos) {
}
