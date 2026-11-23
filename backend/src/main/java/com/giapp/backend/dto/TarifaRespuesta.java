package com.giapp.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TarifaRespuesta(
        Long id,
        Long aparcamientoId,
        String aparcamiento,
        String tipoVehiculo,
        BigDecimal precioMensual,
        LocalDate vigenteDesde
) {
}
