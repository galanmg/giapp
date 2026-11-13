package com.giapp.backend.dto;

public record PlazaRespuesta(
        Long id,
        String numero,
        int capacidad,
        boolean admiteCoche,
        String estado,
        String planta
) {
}
