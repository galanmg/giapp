package com.giapp.backend.dto;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PlazaPeticion(

        @NotBlank(message = "El numero de la plaza es obligatorio")
        @Size(max = 10,message = "El número de plaza no puede pasar de 10 caracteres")
        String numero,

        @NotNull(message = "Hay que indicar la capacidad de la plaza")
        @Min(value = 1,message = "La capacidad mínima es 1")
        Integer capacidad,

        @NotNull(message = "Indicar si la plaza admite coche es obligatorio")
        Boolean admiteCoche,

        @NotNull(message = "Hay que indicar el Id de la planta")
        Long plantaId
) {
}
