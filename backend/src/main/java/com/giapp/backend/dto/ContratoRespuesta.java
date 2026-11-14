package com.giapp.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ContratoRespuesta(
        Long id,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        String periodicidad,
        BigDecimal precioMensual,
        String estado,
        String titular,
        String plaza,
        String vehiculo,
        int ocupacion
) {
}