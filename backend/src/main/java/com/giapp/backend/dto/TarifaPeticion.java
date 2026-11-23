package com.giapp.backend.dto;

import com.giapp.backend.modelo.enums.TipoVehiculo;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TarifaPeticion(

        @NotNull(message = "Hay que indicar el aparcamiento")
        Long aparcamientoId,

        @NotNull(message = "Hay que indicar el tipo de vehículo")
        TipoVehiculo tipoVehiculo,

        // Cabe en la columna DECIMAL(8,2): hasta 6 cifras enteras y 2 decimales
        @NotNull(message = "El precio mensual es obligatorio")
        @DecimalMin(value = "0.00", message = "El precio no puede ser negativo")
        @Digits(integer = 6, fraction = 2,
                message = "El precio admite como máximo 6 cifras enteras y 2 decimales")
        BigDecimal precioMensual,

        @NotNull(message = "Hay que indicar desde cuándo está vigente la tarifa")
        LocalDate vigenteDesde
) {
}
