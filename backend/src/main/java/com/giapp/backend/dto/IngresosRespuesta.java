package com.giapp.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

// Ingresos cobrados en un rango de fechas de pago (RF-35)
public record IngresosRespuesta(
        LocalDate desde,
        LocalDate hasta,
        BigDecimal total,
        long numeroRecibos
) {
}
