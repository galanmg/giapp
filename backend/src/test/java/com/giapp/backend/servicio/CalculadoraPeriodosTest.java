package com.giapp.backend.servicio;

import com.giapp.backend.modelo.Periodo;
import com.giapp.backend.modelo.enums.Periodicidad;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

// Reglas de la sección «Periodos de facturación alineados al calendario» del CLAUDE.md (RNF-14)
class CalculadoraPeriodosTest {

    private static LocalDate dia(int anio, int mes, int dia) {
        return LocalDate.of(anio, mes, dia);
    }

    private static void comprobar(Periodo periodo, LocalDate inicio, LocalDate fin, int meses) {
        assertEquals(inicio, periodo.inicio(), "inicio del periodo");
        assertEquals(fin, periodo.fin(), "fin del periodo");
        assertEquals(meses, periodo.meses(), "meses del periodo");
    }

    @Nested
    @DisplayName("Inicio de la facturación")
    class InicioFacturacion {

        @Test
        @DisplayName("Si se entra un día 1, se factura desde ese mismo día")
        void entradaDiaUno() {
            assertEquals(dia(2026, 3, 1), CalculadoraPeriodos.inicioFacturacion(dia(2026, 3, 1)));
        }

        @Test
        @DisplayName("Si se entra otro día, se factura desde el día 1 del mes siguiente")
        void entradaOtroDia() {
            assertEquals(dia(2026, 4, 1), CalculadoraPeriodos.inicioFacturacion(dia(2026, 3, 17)));
        }

        @Test
        @DisplayName("Si se entra el 31 de diciembre, se factura desde el 1 de enero del año siguiente")
        void entradaFinDeAnio() {
            assertEquals(dia(2027, 1, 1), CalculadoraPeriodos.inicioFacturacion(dia(2026, 12, 31)));
        }
    }

    @Nested
    @DisplayName("Ejemplos con pago semestral de la tabla del CLAUDE.md")
    class TablaSemestral {

        @Test
        @DisplayName("Entrada el 1 de marzo: primer recibo de marzo a junio (4 meses), el siguiente de julio a diciembre")
        void unoDeMarzo() {
            Periodo primero = CalculadoraPeriodos.primerPeriodo(dia(2026, 3, 1), Periodicidad.SEMESTRAL);
            comprobar(primero, dia(2026, 3, 1), dia(2026, 6, 30), 4);

            Periodo siguiente = CalculadoraPeriodos.periodoFacturable(
                    primero.fin().plusDays(1), Periodicidad.SEMESTRAL, dia(2026, 3, 1));
            comprobar(siguiente, dia(2026, 7, 1), dia(2026, 12, 31), 6);
        }

        @Test
        @DisplayName("Entrada el 17 de marzo: primer recibo de abril a junio (3 meses), el siguiente de julio a diciembre")
        void diecisieteDeMarzo() {
            Periodo primero = CalculadoraPeriodos.primerPeriodo(dia(2026, 3, 17), Periodicidad.SEMESTRAL);
            comprobar(primero, dia(2026, 4, 1), dia(2026, 6, 30), 3);

            Periodo siguiente = CalculadoraPeriodos.periodoFacturable(
                    primero.fin().plusDays(1), Periodicidad.SEMESTRAL, dia(2026, 4, 1));
            comprobar(siguiente, dia(2026, 7, 1), dia(2026, 12, 31), 6);
        }

        @Test
        @DisplayName("Entrada el 1 de julio: primer recibo de julio a diciembre (6 meses), el siguiente de enero a junio")
        void unoDeJulio() {
            Periodo primero = CalculadoraPeriodos.primerPeriodo(dia(2026, 7, 1), Periodicidad.SEMESTRAL);
            comprobar(primero, dia(2026, 7, 1), dia(2026, 12, 31), 6);

            Periodo siguiente = CalculadoraPeriodos.periodoFacturable(
                    primero.fin().plusDays(1), Periodicidad.SEMESTRAL, dia(2026, 7, 1));
            comprobar(siguiente, dia(2027, 1, 1), dia(2027, 6, 30), 6);
        }

        @Test
        @DisplayName("Entrada el 31 de diciembre: primer recibo de enero a junio del año siguiente (6 meses), el siguiente de julio a diciembre")
        void treintaYUnoDeDiciembre() {
            Periodo primero = CalculadoraPeriodos.primerPeriodo(dia(2026, 12, 31), Periodicidad.SEMESTRAL);
            comprobar(primero, dia(2027, 1, 1), dia(2027, 6, 30), 6);

            Periodo siguiente = CalculadoraPeriodos.periodoFacturable(
                    primero.fin().plusDays(1), Periodicidad.SEMESTRAL, dia(2027, 1, 1));
            comprobar(siguiente, dia(2027, 7, 1), dia(2027, 12, 31), 6);
        }
    }

    @Nested
    @DisplayName("Primer periodo con otras periodicidades")
    class OtrasPeriodicidades {

        @Test
        @DisplayName("Con pago mensual el primer recibo es siempre de 1 mes, se entre el día que se entre")
        void mensualSiempreUnMes() {
            comprobar(CalculadoraPeriodos.primerPeriodo(dia(2026, 3, 1), Periodicidad.MENSUAL),
                    dia(2026, 3, 1), dia(2026, 3, 31), 1);
            comprobar(CalculadoraPeriodos.primerPeriodo(dia(2026, 3, 15), Periodicidad.MENSUAL),
                    dia(2026, 4, 1), dia(2026, 4, 30), 1);
            comprobar(CalculadoraPeriodos.primerPeriodo(dia(2026, 11, 30), Periodicidad.MENSUAL),
                    dia(2026, 12, 1), dia(2026, 12, 31), 1);
        }

        @Test
        @DisplayName("Trimestral con entrada el 1 de marzo: solo marzo (1 mes), porque el trimestre cierra en marzo")
        void trimestralUnoDeMarzo() {
            comprobar(CalculadoraPeriodos.primerPeriodo(dia(2026, 3, 1), Periodicidad.TRIMESTRAL),
                    dia(2026, 3, 1), dia(2026, 3, 31), 1);
        }

        @Test
        @DisplayName("Trimestral con entrada el 15 de marzo: de abril a junio (3 meses), marzo es de cortesía")
        void trimestralQuinceDeMarzo() {
            comprobar(CalculadoraPeriodos.primerPeriodo(dia(2026, 3, 15), Periodicidad.TRIMESTRAL),
                    dia(2026, 4, 1), dia(2026, 6, 30), 3);
        }

        @Test
        @DisplayName("Anual con entrada el 1 de noviembre: noviembre y diciembre (2 meses)")
        void anualUnoDeNoviembre() {
            comprobar(CalculadoraPeriodos.primerPeriodo(dia(2026, 11, 1), Periodicidad.ANUAL),
                    dia(2026, 11, 1), dia(2026, 12, 31), 2);
        }

        @ParameterizedTest(name = "{0}")
        @EnumSource(Periodicidad.class)
        @DisplayName("Con entrada el 31 de diciembre, cualquier periodicidad empieza en enero del año siguiente con su periodo completo")
        void finDeAnioConCualquierPeriodicidad(Periodicidad periodicidad) {
            Periodo primero = CalculadoraPeriodos.primerPeriodo(dia(2026, 12, 31), periodicidad);

            assertEquals(dia(2027, 1, 1), primero.inicio());
            assertEquals(periodicidad.getMeses(), primero.meses());
        }
    }

    @Nested
    @DisplayName("Periodos naturales")
    class PeriodosNaturales {

        @Test
        @DisplayName("El periodo semestral que contiene el 15 de abril va del 1 de enero al 30 de junio")
        void semestralQuinceDeAbril() {
            comprobar(CalculadoraPeriodos.periodoNatural(dia(2026, 4, 15), Periodicidad.SEMESTRAL),
                    dia(2026, 1, 1), dia(2026, 6, 30), 6);
        }

        @Test
        @DisplayName("Los trimestres empiezan en enero, abril, julio y octubre")
        void trimestresDelAnio() {
            comprobar(CalculadoraPeriodos.periodoNatural(dia(2026, 2, 10), Periodicidad.TRIMESTRAL),
                    dia(2026, 1, 1), dia(2026, 3, 31), 3);
            comprobar(CalculadoraPeriodos.periodoNatural(dia(2026, 5, 10), Periodicidad.TRIMESTRAL),
                    dia(2026, 4, 1), dia(2026, 6, 30), 3);
            comprobar(CalculadoraPeriodos.periodoNatural(dia(2026, 8, 10), Periodicidad.TRIMESTRAL),
                    dia(2026, 7, 1), dia(2026, 9, 30), 3);
            comprobar(CalculadoraPeriodos.periodoNatural(dia(2026, 12, 31), Periodicidad.TRIMESTRAL),
                    dia(2026, 10, 1), dia(2026, 12, 31), 3);
        }

        @Test
        @DisplayName("En un año bisiesto, el periodo de febrero acaba el día 29")
        void febreroBisiesto() {
            comprobar(CalculadoraPeriodos.periodoNatural(dia(2028, 2, 10), Periodicidad.MENSUAL),
                    dia(2028, 2, 1), dia(2028, 2, 29), 1);
        }

        @Test
        @DisplayName("En un año no bisiesto, el periodo de febrero acaba el día 28")
        void febreroNoBisiesto() {
            comprobar(CalculadoraPeriodos.periodoNatural(dia(2027, 2, 10), Periodicidad.MENSUAL),
                    dia(2027, 2, 1), dia(2027, 2, 28), 1);
        }
    }

    @Nested
    @DisplayName("Periodo facturable")
    class PeriodoFacturable {

        @Test
        @DisplayName("En una fecha posterior al primer periodo, se factura el periodo natural completo")
        void despuesDelPrimerPeriodoEsElNaturalCompleto() {
            // Entrada el 17 de marzo de 2026, semestral: se factura desde el 1 de abril
            comprobar(CalculadoraPeriodos.periodoFacturable(
                            dia(2027, 2, 15), Periodicidad.SEMESTRAL, dia(2026, 4, 1)),
                    dia(2027, 1, 1), dia(2027, 6, 30), 6);
        }

        @Test
        @DisplayName("Dentro del primer periodo, empieza en el inicio de la facturación y no cobra dos veces")
        void dentroDelPrimerPeriodoNoCobraMesesAnteriores() {
            // Mayo cae en el semestre de enero a junio, pero se factura desde abril
            comprobar(CalculadoraPeriodos.periodoFacturable(
                            dia(2026, 5, 20), Periodicidad.SEMESTRAL, dia(2026, 4, 1)),
                    dia(2026, 4, 1), dia(2026, 6, 30), 3);
        }

        @Test
        @DisplayName("Una fecha cuyo periodo termina antes del inicio de la facturación es un error")
        void fechaAnteriorAlInicioDeLaFacturacion() {
            // Entrada el 17 de marzo, mensual: marzo es de cortesía, no hay nada que facturar
            assertThrows(IllegalArgumentException.class, () -> CalculadoraPeriodos.periodoFacturable(
                    dia(2026, 3, 20), Periodicidad.MENSUAL, dia(2026, 4, 1)));
        }
    }
}
