package com.giapp.backend.modelo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PromocionTest {

    private static final LocalDate CADUCIDAD = LocalDate.of(2026, 12, 31);

    private static Promocion promocion(int porcentaje) {
        return new Promocion("PRUEBA", porcentaje, CADUCIDAD, null);
    }

    // Descuento (RF-23)

    @Test
    @DisplayName("Un 10 % sobre 45,00 deja el precio en 40,50")
    void diezPorCiento() {
        assertEquals(new BigDecimal("40.50"), promocion(10).aplicarA(new BigDecimal("45.00")));
    }

    @Test
    @DisplayName("Un 15 % sobre 25,00 deja el precio en 21,25")
    void quincePorCiento() {
        assertEquals(new BigDecimal("21.25"), promocion(15).aplicarA(new BigDecimal("25.00")));
    }

    @Test
    @DisplayName("Se redondea a 2 decimales hacia abajo si el tercero es menor que 5: 12 % sobre 33,33 = 29,33")
    void redondeoHaciaAbajo() {
        // 33,33 × 0,88 = 29,3304
        assertEquals(new BigDecimal("29.33"), promocion(12).aplicarA(new BigDecimal("33.33")));
    }

    @Test
    @DisplayName("Se redondea a 2 decimales hacia arriba (HALF_UP): 25 % sobre 33,33 = 25,00")
    void redondeoHaciaArriba() {
        // 33,33 × 0,75 = 24,9975
        assertEquals(new BigDecimal("25.00"), promocion(25).aplicarA(new BigDecimal("33.33")));
    }

    @Test
    @DisplayName("Un 100 % deja el precio a cero")
    void cienPorCien() {
        assertEquals(new BigDecimal("0.00"), promocion(100).aplicarA(new BigDecimal("45.00")));
    }

    // Validez

    @Test
    @DisplayName("Una promoción activa vale antes de su caducidad y el mismo día que caduca")
    void validaHastaElDiaDeCaducidad() {
        assertTrue(promocion(10).esValidaEn(LocalDate.of(2026, 11, 1)));
        assertTrue(promocion(10).esValidaEn(CADUCIDAD));
    }

    @Test
    @DisplayName("Una promoción ya no vale el día siguiente a su caducidad")
    void caducada() {
        assertFalse(promocion(10).esValidaEn(CADUCIDAD.plusDays(1)));
    }

    @Test
    @DisplayName("Una promoción desactivada no vale aunque no haya caducado")
    void desactivada() {
        Promocion promocion = promocion(10);
        promocion.setActiva(false);

        assertFalse(promocion.esValidaEn(LocalDate.of(2026, 11, 1)));
    }
}
