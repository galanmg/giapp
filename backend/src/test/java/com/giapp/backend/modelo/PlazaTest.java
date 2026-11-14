package com.giapp.backend.modelo;

import com.giapp.backend.modelo.enums.EstadoPlaza;
import com.giapp.backend.modelo.enums.TipoVehiculo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlazaTest {

    @Test
    @DisplayName("Una plaza normal vacía admite un coche")
    void plazaVaciaAdmiteCoche() {
        Plaza plaza = new Plaza("S1-01", 2, true, null);

        assertTrue(plaza.admite(TipoVehiculo.COCHE, 0));
    }

    @Test
    @DisplayName("Una plaza con una moto ya no admite un coche")
    void plazaConMotoNoAdmiteCoche() {
        Plaza plaza = new Plaza("S1-02", 2, true, null);

        assertFalse(plaza.admite(TipoVehiculo.COCHE, 1));
    }

    @Test
    @DisplayName("Una plaza con una moto sí admite una segunda moto")
    void plazaConMotoAdmiteOtraMoto() {
        Plaza plaza = new Plaza("S1-02", 2, true, null);

        assertTrue(plaza.admite(TipoVehiculo.MOTO, 1));
    }

    @Test
    @DisplayName("Una plaza llena no admite nada más")
    void plazaLlenaNoAdmiteNada() {
        Plaza plaza = new Plaza("S1-01", 2, true, null);

        assertFalse(plaza.admite(TipoVehiculo.MOTO, 2));
        assertFalse(plaza.admite(TipoVehiculo.COCHE, 2));
    }

    @Test
    @DisplayName("Una plaza que no admite coches los rechaza aunque esté vacía")
    void plazaSinAccesoParaCochesRechazaCoche() {
        Plaza plaza = new Plaza("S1-03", 2, false, null);

        assertFalse(plaza.admite(TipoVehiculo.COCHE, 0));
        assertTrue(plaza.admite(TipoVehiculo.MOTO, 0));
    }

    @Test
    @DisplayName("Un recoveco de capacidad 1 solo admite una moto")
    void recovecoSoloAdmiteUnaMoto() {
        Plaza plaza = new Plaza("S1-04", 1, false, null);

        assertTrue(plaza.admite(TipoVehiculo.MOTO, 0));
        assertFalse(plaza.admite(TipoVehiculo.MOTO, 1));
    }

    @Test
    @DisplayName("Una plaza fuera de servicio no admite nada")
    void plazaFueraDeServicioNoAdmiteNada() {
        Plaza plaza = new Plaza("S1-01", 2, true, null);
        plaza.setEstado(EstadoPlaza.FUERA_DE_SERVICIO);

        assertFalse(plaza.admite(TipoVehiculo.COCHE, 0));
        assertFalse(plaza.admite(TipoVehiculo.MOTO, 0));
    }
}