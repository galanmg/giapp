package com.giapp.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ReciboRespuesta(
        Long id,
        Long contratoId,
        String aparcamiento,
        String plaza,
        String titular,
        LocalDate periodoInicio,
        LocalDate periodoFin,
        int numeroMeses,
        BigDecimal importeTotal,
        LocalDate fechaEmision,
        LocalDate fechaVencimiento,
        LocalDate fechaPago,
        String formaPago,
        String estado
) {
}
