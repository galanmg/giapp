package com.giapp.backend.dto;

import com.giapp.backend.modelo.enums.TipoVehiculo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record VehiculoPeticion(

        @NotBlank(message = "La matrícula es obligatoria")
        @Size(max = 15, message = "La matrícula no puede superar los 15 caracteres")
        String matricula,

        @NotBlank(message = "La marca es obligatoria")
        @Size(max = 50, message = "La marca no puede superar los 50 caracteres")
        String marca,

        @NotBlank(message = "El modelo es obligatorio")
        @Size(max = 50, message = "El modelo no puede superar los 50 caracteres")
        String modelo,

        @NotNull(message = "El tipo de vehículo es obligatorio")
        TipoVehiculo tipo
) {
}
