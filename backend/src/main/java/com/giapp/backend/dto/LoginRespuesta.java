package com.giapp.backend.dto;

public record LoginRespuesta(
        String token,
        String email,
        String nombre,
        String rol
) {
}