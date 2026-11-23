package com.giapp.backend.dto;

public record VehiculoRespuesta(
        Long id,
        String matricula,
        String marca,
        String modelo,
        String tipo,
        String propietario
) {
}
