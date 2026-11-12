package com.giapp.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AparcamientoPeticion(

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
        String nombre,

        @NotBlank(message = "La dirección es obligatoria")
        @Size(max = 200)
        String direccion,

        @NotBlank(message = "La ciudad es obligatoria")
        @Size(max = 100)
        String ciudad,

        @NotNull(message = "Hay que indicar el gestor")
        Long gestorId
) {
}