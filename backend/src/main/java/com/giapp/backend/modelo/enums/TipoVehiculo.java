package com.giapp.backend.modelo.enums;

public enum TipoVehiculo {
    COCHE(2),
    MOTO(1);

    private final int mediasPlazas;

    TipoVehiculo(int mediasPlazas) {
        this.mediasPlazas = mediasPlazas;
    }

    public int getMediasPlazas() {
        return mediasPlazas;
    }
}
