package com.giapp.backend.modelo;

import java.time.LocalDate;

/**
 * Periodo de facturación: del día 1 de un mes al último día de otro, ambos
 * incluidos, y el número de meses completos que cubre.
 */
public record Periodo(
        LocalDate inicio,
        LocalDate fin,
        int meses
) {
}
