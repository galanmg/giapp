package com.giapp.backend.dto;

import jakarta.validation.constraints.NotNull;

// Vincular a un usuario ya registrado con un aparcamiento (RF-55)
public record ClienteAparcamientoPeticion(

        @NotNull(message = "Hay que indicar el cliente")
        Long clienteId
) {
}
