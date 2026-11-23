package com.giapp.backend.dto;

import java.time.LocalDate;

public record ClienteAparcamientoRespuesta(
        Long id,
        Long clienteId,
        String cliente,
        String email,
        Long aparcamientoId,
        String aparcamiento,
        LocalDate fechaAlta,
        boolean activo
) {
}
