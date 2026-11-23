package com.giapp.backend.modelo;

import com.giapp.backend.modelo.enums.EstadoRecibo;
import com.giapp.backend.modelo.enums.Periodicidad;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ReciboTest {

    private static Contrato contratoConPrecio(String precioMensual) {
        return new Contrato(LocalDate.of(2026, 3, 1), Periodicidad.SEMESTRAL,
                new BigDecimal(precioMensual), null, null, null);
    }

    @Test
    @DisplayName("El importe es el precio mensual por el número de meses: 45,00 × 4 = 180,00")
    void importeDeCuatroMeses() {
        assertEquals(new BigDecimal("180.00"), Recibo.calcularImporte(new BigDecimal("45.00"), 4));
    }

    @Test
    @DisplayName("Un recibo de un mes cuesta exactamente el precio mensual")
    void importeDeUnMes() {
        assertEquals(new BigDecimal("25.00"), Recibo.calcularImporte(new BigDecimal("25.00"), 1));
    }

    @Test
    @DisplayName("El importe no redondea: 33,33 × 3 = 99,99")
    void importeSinRedondeo() {
        assertEquals(new BigDecimal("99.99"), Recibo.calcularImporte(new BigDecimal("33.33"), 3));
    }

    @Test
    @DisplayName("Al emitir un recibo semestral de 6 meses a 45,00, el importe es 270,00")
    void emitirCalculaElImporte() {
        Periodo periodo = new Periodo(LocalDate.of(2026, 7, 1), LocalDate.of(2026, 12, 31), 6);

        Recibo recibo = new Recibo(contratoConPrecio("45.00"), periodo, LocalDate.of(2026, 7, 1));

        assertEquals(new BigDecimal("270.00"), recibo.getImporteTotal());
        assertEquals(6, recibo.getNumeroMeses());
        assertEquals(LocalDate.of(2026, 7, 1), recibo.getPeriodoInicio());
        assertEquals(LocalDate.of(2026, 12, 31), recibo.getPeriodoFin());
    }

    @Test
    @DisplayName("El recibo vence 10 días naturales después de su emisión, aunque cambie de mes")
    void venceDiezDiasDespues() {
        Periodo periodo = new Periodo(LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 31), 1);

        Recibo recibo = new Recibo(contratoConPrecio("25.00"), periodo, LocalDate.of(2026, 11, 25));

        assertEquals(LocalDate.of(2026, 12, 5), recibo.getFechaVencimiento());
    }

    @Test
    @DisplayName("Un recibo recién emitido está pendiente y sin datos de pago")
    void reciboNuevoPendiente() {
        Periodo periodo = new Periodo(LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 31), 1);

        Recibo recibo = new Recibo(contratoConPrecio("25.00"), periodo, LocalDate.of(2026, 12, 1));

        assertEquals(EstadoRecibo.PENDIENTE, recibo.getEstado());
        assertNull(recibo.getFechaPago());
        assertNull(recibo.getFormaPago());
    }
}
