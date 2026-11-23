package com.giapp.backend.dto;

import jakarta.validation.constraints.NotNull;

// Cambio de plaza de un contrato (RF-24)
public record ContratoPlazaPeticion(

        @NotNull(message = "Hay que indicar la plaza nueva")
        Long plazaId
) {
}
