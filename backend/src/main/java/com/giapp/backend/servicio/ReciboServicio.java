package com.giapp.backend.servicio;

import com.giapp.backend.dto.GeneracionRespuesta;
import com.giapp.backend.dto.IngresosRespuesta;
import com.giapp.backend.dto.PagoPeticion;
import com.giapp.backend.dto.ReciboRespuesta;
import com.giapp.backend.modelo.Contrato;
import com.giapp.backend.modelo.Periodo;
import com.giapp.backend.modelo.Plaza;
import com.giapp.backend.modelo.Recibo;
import com.giapp.backend.modelo.Usuario;
import com.giapp.backend.modelo.enums.EstadoContrato;
import com.giapp.backend.modelo.enums.EstadoRecibo;
import com.giapp.backend.repositorio.ContratoRepositorio;
import com.giapp.backend.repositorio.ReciboRepositorio;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

/**
 * Recibos: generación (RF-28, RF-31), pagos (RF-32), impagados (RF-33, RF-34)
 * e ingresos (RF-35).
 *
 * Los periodos los calcula CalculadoraPeriodos; aquí solo se decide qué
 * contratos facturan y se evita emitir dos veces el mismo periodo.
 */
@Service
public class ReciboServicio {

    private final ReciboRepositorio reciboRepositorio;
    private final ContratoRepositorio contratoRepositorio;
    private final AparcamientoServicio aparcamientoServicio;
    private final UsuarioActualServicio usuarioActualServicio;

    public ReciboServicio(ReciboRepositorio reciboRepositorio,
                          ContratoRepositorio contratoRepositorio,
                          AparcamientoServicio aparcamientoServicio,
                          UsuarioActualServicio usuarioActualServicio) {
        this.reciboRepositorio = reciboRepositorio;
        this.contratoRepositorio = contratoRepositorio;
        this.aparcamientoServicio = aparcamientoServicio;
        this.usuarioActualServicio = usuarioActualServicio;
    }

    // ---- Generación ----

    // El primer recibo de un contrato nuevo, emitido hoy (RF-30)
    @Transactional
    public void emitirPrimero(Contrato contrato) {
        Periodo periodo = CalculadoraPeriodos.primerPeriodo(
                contrato.getFechaInicio(), contrato.getPeriodicidad());

        reciboRepositorio.save(new Recibo(contrato, periodo, LocalDate.now()));
    }

    // Proceso diario de todos los aparcamientos: lo usa la tarea programada.
    // Primero los impagados, para no marcar como vencido nada de lo que se emite hoy.
    @Transactional
    public GeneracionRespuesta procesarTodos(LocalDate fecha) {
        int impagados = marcarImpagados(
                reciboRepositorio.findByEstadoAndFechaVencimientoBefore(EstadoRecibo.PENDIENTE, fecha));
        int creados = generar(contratoRepositorio.findByEstado(EstadoContrato.ACTIVO), fecha);

        return new GeneracionRespuesta(impagados, creados);
    }

    // El mismo proceso, solo para los aparcamientos del gestor autenticado (endpoint manual)
    @Transactional
    public GeneracionRespuesta procesarDeGestor(LocalDate fecha) {
        Usuario gestor = usuarioActualServicio.obtener();

        int impagados = marcarImpagados(reciboRepositorio
                .findByEstadoAndFechaVencimientoBeforeAndContratoPlazaPlantaAparcamientoGestor(
                        EstadoRecibo.PENDIENTE, fecha, gestor));
        int creados = generar(contratoRepositorio.findByEstadoAndPlazaPlantaAparcamientoGestor(
                EstadoContrato.ACTIVO, gestor), fecha);

        return new GeneracionRespuesta(impagados, creados);
    }

    // ---- Gestor: consulta y pagos ----

    // RF-34: los impagados son este listado filtrado por estado
    @Transactional(readOnly = true)
    public List<ReciboRespuesta> listar(Long contratoId, Long clienteId,
                                        EstadoRecibo estado, Long aparcamientoId) {
        if (aparcamientoId != null) {
            aparcamientoServicio.buscarPropio(aparcamientoId);
        }

        return reciboRepositorio
                .buscarDeGestor(usuarioActualServicio.obtener(), contratoId, clienteId, estado, aparcamientoId)
                .stream()
                .map(this::aRespuesta)
                .toList();
    }

    // RF-32. Se puede pagar un recibo pendiente o uno ya impagado; uno pagado, no.
    @Transactional
    public ReciboRespuesta pagar(Long id, PagoPeticion peticion) {
        Recibo recibo = buscarPropio(id);

        if (recibo.getEstado() == EstadoRecibo.PAGADO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El recibo " + id + " ya está pagado desde el " + recibo.getFechaPago());
        }

        recibo.setFechaPago(peticion.fechaPago());
        recibo.setFormaPago(peticion.formaPago());
        recibo.setEstado(EstadoRecibo.PAGADO);

        return aRespuesta(recibo);
    }

    // RF-35. Lo cobrado en mis aparcamientos con fecha de pago entre desde y hasta, incluidos.
    @Transactional(readOnly = true)
    public IngresosRespuesta ingresos(LocalDate desde, LocalDate hasta) {
        if (desde.isAfter(hasta)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La fecha desde no puede ser posterior a la fecha hasta");
        }

        Usuario gestor = usuarioActualServicio.obtener();

        return new IngresosRespuesta(
                desde,
                hasta,
                reciboRepositorio.sumarIngresos(gestor, desde, hasta),
                reciboRepositorio.countByContratoPlazaPlantaAparcamientoGestorAndEstadoAndFechaPagoBetween(
                        gestor, EstadoRecibo.PAGADO, desde, hasta));
    }

    // ---- Usuario autenticado ----

    // RF-36: el histórico de recibos propios, de todos sus aparcamientos
    @Transactional(readOnly = true)
    public List<ReciboRespuesta> listarPropios() {
        return reciboRepositorio.findByContratoTitularOrderByFechaEmisionDescIdDesc(usuarioActualServicio.obtener())
                .stream()
                .map(this::aRespuesta)
                .toList();
    }

    // Métodos internos

    // RF-33: todo recibo pendiente cuyo vencimiento ya ha pasado pasa a impagado
    private int marcarImpagados(List<Recibo> vencidos) {
        vencidos.forEach(recibo -> recibo.setEstado(EstadoRecibo.IMPAGADO));

        return vencidos.size();
    }

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

    // 404 si no existe; 403 si es de un aparcamiento de otro gestor
    private Recibo buscarPropio(Long id) {
        Recibo recibo = reciboRepositorio.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No existe el recibo con id " + id));

        aparcamientoServicio.buscarPropio(
                recibo.getContrato().getPlaza().getPlanta().getAparcamiento().getId());

        return recibo;
    }

    private ReciboRespuesta aRespuesta(Recibo recibo) {
        Contrato contrato = recibo.getContrato();
        Plaza plaza = contrato.getPlaza();
        Usuario titular = contrato.getTitular();

        return new ReciboRespuesta(
                recibo.getId(),
                contrato.getId(),
                plaza.getPlanta().getAparcamiento().getNombre(),
                plaza.getNumero(),
                titular.getNombre() + " " + titular.getApellidos(),
                recibo.getPeriodoInicio(),
                recibo.getPeriodoFin(),
                recibo.getNumeroMeses(),
                recibo.getImporteTotal(),
                recibo.getFechaEmision(),
                recibo.getFechaVencimiento(),
                recibo.getFechaPago(),
                recibo.getFormaPago() == null ? null : recibo.getFormaPago().name(),
                recibo.getEstado().name());
    }
}
