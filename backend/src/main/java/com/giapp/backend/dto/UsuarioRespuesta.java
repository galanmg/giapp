package com.giapp.backend.dto;

public record UsuarioRespuesta(
        Long id,
        String nombre,
        String apellidos,
        String email,
        String rol
) {
}