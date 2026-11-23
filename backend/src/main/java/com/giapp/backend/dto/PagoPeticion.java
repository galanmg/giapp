package com.giapp.backend.dto;

import com.giapp.backend.modelo.enums.FormaPago;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;

// Registro manual de un pago por el gestor (RF-32)
public record PagoPeticion(

        // Un pago no puede estar fechado en el futuro: contaría en ingresos que aún no existen
        @NotNull(message = "La fecha de pago es obligatoria")
        @PastOrPresent(message = "La fecha de pago no puede ser posterior a hoy")
        LocalDate fechaPago,

        @NotNull(message = "La forma de pago es obligatoria: EFECTIVO o TRANSFERENCIA_BIZUM")
        FormaPago formaPago
) {
}
