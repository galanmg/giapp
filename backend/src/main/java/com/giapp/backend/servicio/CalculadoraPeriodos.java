package com.giapp.backend.servicio;

import com.giapp.backend.modelo.Periodo;
import com.giapp.backend.modelo.enums.Periodicidad;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Cálculo de los periodos de facturación (RF-29, RF-30, RNF-14).
 *
 * Los periodos se alinean al calendario natural, no a la fecha de alta: todos
 * los contratos semestrales facturan en enero y en julio; los trimestrales, en
 * enero, abril, julio y octubre; los anuales, en enero.
 *
 * Se factura siempre por meses completos, nunca por días. Los días entre la
 * entrada del cliente y el inicio de la facturación son de cortesía.
 *
 * Sin dependencias de Spring ni de base de datos, para poder probarla con JUnit
 * directamente. Solo métodos estáticos: no guarda estado.
 */
public final class CalculadoraPeriodos {

    private CalculadoraPeriodos() {
    }

    // El día 1 del mes de entrada si se entra un día 1; si no, el día 1 del mes siguiente
    public static LocalDate inicioFacturacion(LocalDate fechaEntrada) {
        if (fechaEntrada.getDayOfMonth() == 1) {
            return fechaEntrada;
        }

        return fechaEntrada.withDayOfMonth(1).plusMonths(1);
    }

    // El periodo natural completo que contiene la fecha.
    // Semestral y 15 de abril: del 1 de enero al 30 de junio.
    public static Periodo periodoNatural(LocalDate fecha, Periodicidad periodicidad) {
        int meses = periodicidad.getMeses();

        // Primer mes del bloque de 'meses' meses en el que cae la fecha (división entera)
        int mesInicio = ((fecha.getMonthValue() - 1) / meses) * meses + 1;

        LocalDate inicio = LocalDate.of(fecha.getYear(), mesInicio, 1);
        LocalDate fin = inicio.plusMonths(meses).minusDays(1);

        return new Periodo(inicio, fin, meses);
    }

    // Lo que cubre el recibo del periodo que contiene la fecha: el periodo natural,
    // pero empezando en el inicio de la facturación si es posterior. Así el primer
    // recibo no cobra los meses anteriores a la entrada, y el segundo no vuelve a
    // cobrar los del primero.
    public static Periodo periodoFacturable(LocalDate fecha, Periodicidad periodicidad,
                                            LocalDate inicioFacturacion) {
        Periodo natural = periodoNatural(fecha, periodicidad);

        LocalDate inicio = natural.inicio().isAfter(inicioFacturacion)
                ? natural.inicio()
                : inicioFacturacion;

        if (inicio.isAfter(natural.fin())) {
            throw new IllegalArgumentException("La fecha " + fecha
                    + " es anterior al inicio de la facturación (" + inicioFacturacion + ")");
        }

        // Los dos extremos son día 1, así que los meses salen exactos
        int meses = (int) ChronoUnit.MONTHS.between(inicio, natural.fin().plusDays(1));

        return new Periodo(inicio, natural.fin(), meses);
    }

    // El primer recibo: desde el inicio de la facturación hasta el cierre del periodo natural
    public static Periodo primerPeriodo(LocalDate fechaEntrada, Periodicidad periodicidad) {
        LocalDate inicio = inicioFacturacion(fechaEntrada);

        return periodoFacturable(inicio, periodicidad, inicio);
    }
}
