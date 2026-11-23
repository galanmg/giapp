package com.giapp.backend.dto;

import com.giapp.backend.modelo.enums.EstadoPlaza;
import jakarta.validation.constraints.NotNull;

// Poner una plaza fuera de servicio o devolverla al servicio (RF-15)
public record PlazaEstadoPeticion(

        @NotNull(message = "Hay que indicar el estado de la plaza")
        EstadoPlaza estado
) {
}
