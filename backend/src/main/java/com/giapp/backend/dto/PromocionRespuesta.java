package com.giapp.backend.dto;

import java.time.LocalDate;

public record PromocionRespuesta(
        Long id,
        String codigo,
        int porcentajeDescuento,
        LocalDate fechaCaducidad,
        boolean activa,
        Long aparcamientoId,
        String aparcamiento
) {
}
