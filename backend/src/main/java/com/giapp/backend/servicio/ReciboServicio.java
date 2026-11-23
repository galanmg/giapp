package com.giapp.backend.servicio;

import com.giapp.backend.modelo.Contrato;
import com.giapp.backend.modelo.Periodo;
import com.giapp.backend.modelo.Recibo;
import com.giapp.backend.modelo.enums.EstadoContrato;
import com.giapp.backend.repositorio.ContratoRepositorio;
import com.giapp.backend.repositorio.ReciboRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Generación de recibos (RF-28, RF-31).
 *
 * Los periodos los calcula CalculadoraPeriodos; aquí solo se decide qué
 * contratos facturan y se evita emitir dos veces el mismo periodo.
 */
@Service
public class ReciboServicio {

    private final ReciboRepositorio reciboRepositorio;
    private final ContratoRepositorio contratoRepositorio;
    private final UsuarioActualServicio usuarioActualServicio;

    public ReciboServicio(ReciboRepositorio reciboRepositorio,
                          ContratoRepositorio contratoRepositorio,
                          UsuarioActualServicio usuarioActualServicio) {
        this.reciboRepositorio = reciboRepositorio;
        this.contratoRepositorio = contratoRepositorio;
        this.usuarioActualServicio = usuarioActualServicio;
    }

    // El primer recibo de un contrato nuevo, emitido hoy (RF-30)
    @Transactional
    public void emitirPrimero(Contrato contrato) {
        Periodo periodo = CalculadoraPeriodos.primerPeriodo(
                contrato.getFechaInicio(), contrato.getPeriodicidad());

        reciboRepositorio.save(new Recibo(contrato, periodo, LocalDate.now()));
    }

    // Todos los contratos activos. Lo usa la tarea programada de cada día.
    @Transactional
    public int generarPendientes(LocalDate fecha) {
        return generar(contratoRepositorio.findByEstado(EstadoContrato.ACTIVO), fecha);
    }

    // Solo los contratos de los aparcamientos del gestor autenticado (endpoint manual)
    @Transactional
    public int generarPendientesDeGestor(LocalDate fecha) {
        return generar(contratoRepositorio.findByEstadoAndPlazaPlantaAparcamientoGestor(
                EstadoContrato.ACTIVO, usuarioActualServicio.obtener()), fecha);
    }

    // Métodos internos

    // Para cada contrato que ya factura, el recibo del periodo que contiene la fecha,
    // si no existe ya. Devuelve cuántos ha creado: lanzarlo dos veces no duplica nada.
    private int generar(List<Contrato> contratos, LocalDate fecha) {
        int creados = 0;

        for (Contrato contrato : contratos) {
            LocalDate inicioFacturacion = contrato.inicioFacturacion();

            if (inicioFacturacion.isAfter(fecha)) {
                continue;
            }

            Periodo periodo = CalculadoraPeriodos.periodoFacturable(
                    fecha, contrato.getPeriodicidad(), inicioFacturacion);

            if (!reciboRepositorio.existsByContratoAndPeriodoInicio(contrato, periodo.inicio())) {
                reciboRepositorio.save(new Recibo(contrato, periodo, fecha));
                creados++;
            }
        }

        return creados;
    }
}
