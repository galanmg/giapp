package com.giapp.backend.dto;

// Resultado de lanzar a mano el proceso diario: primero se marcan los
// impagados (RF-33) y después se generan los recibos nuevos (RF-28)
public record GeneracionRespuesta(
        int recibosMarcadosImpagados,
        int recibosCreados
) {
}
