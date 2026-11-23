package com.giapp.backend.dto;

import com.giapp.backend.modelo.enums.EstadoContrato;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

// Finalizar o rescindir un contrato (RF-25)
public record ContratoFinalizarPeticion(

        @NotNull(message = "La fecha de fin es obligatoria")
        LocalDate fechaFin,

        @NotNull(message = "Hay que indicar el estado final: FINALIZADO o RESCINDIDO")
        EstadoContrato estado
) {
}
