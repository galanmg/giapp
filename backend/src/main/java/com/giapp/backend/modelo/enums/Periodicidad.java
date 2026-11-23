package com.giapp.backend.modelo.enums;

// Cada periodicidad sabe cuántos meses dura su periodo, igual que
// TipoVehiculo sabe cuántas medias plazas ocupa
public enum Periodicidad {
    MENSUAL(1),
    TRIMESTRAL(3),
    SEMESTRAL(6),
    ANUAL(12);

    private final int meses;

    Periodicidad(int meses) {
        this.meses = meses;
    }

    public int getMeses() {
        return meses;
    }
}
