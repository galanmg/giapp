package com.giapp.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PlantaPeticion(

        @NotBlank(message = "El identificador de la planta es obligatorio")
        @Size(max = 20, message = "El identificador no puede superar los 20 caracteres")
        String identificador,

        @NotNull(message = "Hay que indicar el nivel de la planta")
        Integer nivel,

        @NotNull(message = "Hay que indicar el aparcamiento")
        Long aparcamientoId
) {
}
