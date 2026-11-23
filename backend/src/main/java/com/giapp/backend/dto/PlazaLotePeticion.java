package com.giapp.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// Alta de plazas en lote (RF-14): de prefijo + numeroInicial a prefijo + numeroFinal
public record PlazaLotePeticion(

        @NotNull(message = "Hay que indicar el Id de la planta")
        Long plantaId,

        // Incluye el separador: con «S1-» y el número 7 sale «S1-07»
        @NotNull(message = "El prefijo es obligatorio (puede estar vacío)")
        @Size(max = 8, message = "El prefijo no puede superar los 8 caracteres")
        String prefijo,

        @NotNull(message = "El número inicial es obligatorio")
        @Min(value = 0, message = "El número inicial no puede ser negativo")
        Integer numeroInicial,

        @NotNull(message = "El número final es obligatorio")
        @Min(value = 0, message = "El número final no puede ser negativo")
        Integer numeroFinal,

        @NotNull(message = "Hay que indicar la capacidad de las plazas")
        @Min(value = 1, message = "La capacidad mínima es 1")
        Integer capacidad,

        @NotNull(message = "Indicar si las plazas admiten coche es obligatorio")
        Boolean admiteCoche
) {
}
