package com.giapp.backend.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ErrorRespuesta(
        LocalDateTime fecha,
        int estado,
        String error,
        String mensaje,
        String ruta,
        List<ErrorCampo> errores
) {

    // Error de validación de un campo concreto de la petición
    public record ErrorCampo(
            String campo,
            String mensaje
    ) {
    }
}
