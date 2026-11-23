package com.giapp.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record PromocionPeticion(

        @NotBlank(message = "El código de la promoción es obligatorio")
        @Size(max = 20, message = "El código no puede superar los 20 caracteres")
        String codigo,

        @NotNull(message = "El porcentaje de descuento es obligatorio")
        @Min(value = 1, message = "El descuento mínimo es del 1 %")
        @Max(value = 100, message = "El descuento máximo es del 100 %")
        Integer porcentajeDescuento,

        @NotNull(message = "La fecha de caducidad es obligatoria")
        LocalDate fechaCaducidad,

        @NotNull(message = "Hay que indicar el aparcamiento")
        Long aparcamientoId
) {
}
