package com.giapp.backend.dto;

import com.giapp.backend.modelo.enums.Periodicidad;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ContratoPeticion(

        @NotNull(message = "La fecha de inicio es obligatoria")
        LocalDate fechaInicio,

        @NotNull(message = "La periodicidad es obligatoria")
        Periodicidad periodicidad,

        @NotNull(message = "El precio mensual es obligatorio")
        @DecimalMin(value = "0.00", message = "El precio no puede ser negativo")
        BigDecimal precioMensual,

        @NotNull(message = "Hay que indicar el titular")
        Long titularId,

        @NotNull(message = "Hay que indicar la plaza")
        Long plazaId,

        @NotNull(message = "Hay que indicar el vehículo")
        Long vehiculoId
) {
}