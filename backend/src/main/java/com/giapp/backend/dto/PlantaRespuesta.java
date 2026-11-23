package com.giapp.backend.dto;

public record PlantaRespuesta(
        Long id,
        String identificador,
        int nivel,
        Long aparcamientoId,
        String aparcamiento
) {
}
