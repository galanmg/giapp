package com.giapp.backend.dto;

public record AparcamientoRespuesta(
        Long id,
        String nombre,
        String direccion,
        String ciudad,
        String gestor
) {
}