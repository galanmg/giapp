package com.giapp.backend.servicio;

import com.giapp.backend.dto.ContratoFinalizarPeticion;
import com.giapp.backend.dto.ContratoPeticion;
import com.giapp.backend.dto.ContratoRespuesta;
import com.giapp.backend.modelo.Aparcamiento;
import com.giapp.backend.modelo.Contrato;
import com.giapp.backend.modelo.Plaza;
import com.giapp.backend.modelo.TarifaAlquiler;
import com.giapp.backend.modelo.Usuario;
import com.giapp.backend.modelo.Vehiculo;
import com.giapp.backend.modelo.enums.EstadoContrato;
import com.giapp.backend.modelo.enums.TipoVehiculo;
import com.giapp.backend.repositorio.ClienteAparcamientoRepositorio;
import com.giapp.backend.repositorio.ContratoRepositorio;
import com.giapp.backend.repositorio.TarifaAlquilerRepositorio;
import com.giapp.backend.repositorio.UsuarioRepositorio;
import com.giapp.backend.repositorio.VehiculoRepositorio;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Contratos de alquiler (RF-20 a RF-27).
 *
 * La ocupación de una plaza solo cuenta los contratos ACTIVOS, así que al
 * finalizar un contrato la plaza se libera sola, sin tocarla.
 */
@Service
public class ContratoServicio {

    private final ContratoRepositorio contratoRepositorio;
    private final VehiculoRepositorio vehiculoRepositorio;
    private final UsuarioRepositorio usuarioRepositorio;
    private final ClienteAparcamientoRepositorio clienteAparcamientoRepositorio;
    private final TarifaAlquilerRepositorio tarifaAlquilerRepositorio;
    private final PlazaServicio plazaServicio;
    private final AparcamientoServicio aparcamientoServicio;
    private final UsuarioActualServicio usuarioActualServicio;
    private final ReciboServicio reciboServicio;

    public ContratoServicio(ContratoRepositorio contratoRepositorio,
                            VehiculoRepositorio vehiculoRepositorio,
                            UsuarioRepositorio usuarioRepositorio,
                            ClienteAparcamientoRepositorio clienteAparcamientoRepositorio,
                            TarifaAlquilerRepositorio tarifaAlquilerRepositorio,
                            PlazaServicio plazaServicio,
                            AparcamientoServicio aparcamientoServicio,
                            UsuarioActualServicio usuarioActualServicio,
                            ReciboServicio reciboServicio) {
        this.contratoRepositorio = contratoRepositorio;
        this.vehiculoRepositorio = vehiculoRepositorio;
        this.usuarioRepositorio = usuarioRepositorio;
        this.clienteAparcamientoRepositorio = clienteAparcamientoRepositorio;
        this.tarifaAlquilerRepositorio = tarifaAlquilerRepositorio;
        this.plazaServicio = plazaServicio;
        this.aparcamientoServicio = aparcamientoServicio;
        this.usuarioActualServicio = usuarioActualServicio;
        this.reciboServicio = reciboServicio;
    }

    // ---- Gestor ----

    @Transactional
    public ContratoRespuesta crear(ContratoPeticion peticion) {

        Usuario titular = buscarTitular(peticion.titularId());
        Vehiculo vehiculo = buscarVehiculo(peticion.vehiculoId());
        Plaza plaza = plazaServicio.buscarPropia(peticion.plazaId());

        if (!vehiculo.getPropietario().getId().equals(titular.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El vehículo no pertenece al titular del contrato");
        }

        // El titular tiene que ser cliente activo del aparcamiento de la plaza
        Aparcamiento aparcamiento = plaza.getPlanta().getAparcamiento();

        if (!clienteAparcamientoRepositorio.existsByClienteAndAparcamientoAndActivoTrue(
                titular, aparcamiento)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El titular no está dado de alta como cliente en el aparcamiento "
                            + aparcamiento.getNombre());
        }

        comprobarQueCabe(plaza, vehiculo);

        BigDecimal precioMensual = peticion.precioMensual() != null
                ? peticion.precioMensual()
                : precioDeTarifa(aparcamiento, vehiculo.getTipo(), peticion.fechaInicio());

        Contrato contrato = contratoRepositorio.save(new Contrato(
                peticion.fechaInicio(),
                peticion.periodicidad(),
                precioMensual,
                titular,
                plaza,
                vehiculo));

        // El primer recibo se emite al crear el contrato, en la misma transacción:
        // si fallara, tampoco se crearía el contrato
        reciboServicio.emitirPrimero(contrato);

        return aRespuesta(contrato);
    }

    // Contratos de mis aparcamientos (RF-26). Filtros opcionales.
    @Transactional(readOnly = true)
    public List<ContratoRespuesta> listar(Long aparcamientoId, Long clienteId, EstadoContrato estado) {
        if (aparcamientoId != null) {
            aparcamientoServicio.buscarPropio(aparcamientoId);
        }

        return contratoRepositorio
                .buscarDeGestor(usuarioActualServicio.obtener(), aparcamientoId, clienteId, estado)
                .stream()
                .map(this::aRespuesta)
                .toList();
    }

    @Transactional(readOnly = true)
    public ContratoRespuesta buscarPorId(Long id) {
        return aRespuesta(buscarPropio(id));
    }

    // RF-25. El cambio de estado es inmediato aunque la fecha de fin sea posterior.
    @Transactional
    public ContratoRespuesta finalizar(Long id, ContratoFinalizarPeticion peticion) {
        if (peticion.estado() == EstadoContrato.ACTIVO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El estado final tiene que ser FINALIZADO o RESCINDIDO");
        }

        Contrato contrato = buscarActivo(id);

        if (peticion.fechaFin().isBefore(contrato.getFechaInicio())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La fecha de fin no puede ser anterior a la de inicio (" + contrato.getFechaInicio() + ")");
        }

        contrato.setFechaFin(peticion.fechaFin());
        contrato.setEstado(peticion.estado());

        return aRespuesta(contrato);
    }

    // RF-24. Solo dentro del mismo aparcamiento: cambiar de aparcamiento es
    // finalizar este contrato y crear otro con la tarifa del nuevo.
    // Pendiente: no se guarda el histórico de plazas anteriores.
    @Transactional
    public ContratoRespuesta cambiarPlaza(Long id, Long plazaId) {
        Contrato contrato = buscarActivo(id);
        Plaza actual = contrato.getPlaza();
        Plaza nueva = plazaServicio.buscarPropia(plazaId);

        if (nueva.getId().equals(actual.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El contrato ya está en la plaza " + nueva.getNumero());
        }

        if (!nueva.getPlanta().getAparcamiento().getId()
                .equals(actual.getPlanta().getAparcamiento().getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "La plaza nueva tiene que estar en el mismo aparcamiento. Para cambiar de "
                            + "aparcamiento, finaliza este contrato y crea otro");
        }

        comprobarQueCabe(nueva, contrato.getVehiculo());

        contrato.setPlaza(nueva);

        return aRespuesta(contrato);
    }

    // ---- Usuario autenticado ----

    // RF-27: los contratos propios, de todos los aparcamientos
    @Transactional(readOnly = true)
    public List<ContratoRespuesta> listarPropios() {
        return contratoRepositorio.findByTitularOrderByFechaInicioDesc(usuarioActualServicio.obtener())
                .stream()
                .map(this::aRespuesta)
                .toList();
    }

    // Metodos internos

    // La regla de las medias plazas está en Plaza.admite(); aquí solo se aplica
    private void comprobarQueCabe(Plaza plaza, Vehiculo vehiculo) {
        int mediasOcupadas = plazaServicio.mediasOcupadas(plaza);

        if (!plaza.admite(vehiculo.getTipo(), mediasOcupadas)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "La plaza " + plaza.getNumero() + " no admite este vehículo. Capacidad: "
                            + plaza.getCapacidad() + " medias plazas, ocupadas: " + mediasOcupadas
                            + ", solicitadas: " + vehiculo.getTipo().getMediasPlazas());
        }
    }

    // Precio de la tarifa vigente en la fecha de inicio del contrato. El contrato
    // guarda una copia: si la tarifa sube después, el contrato mantiene su precio.
    private BigDecimal precioDeTarifa(Aparcamiento aparcamiento, TipoVehiculo tipo, LocalDate fecha) {
        return tarifaAlquilerRepositorio.buscarVigente(aparcamiento, tipo, fecha)
                .map(TarifaAlquiler::getPrecioMensual)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "No se ha indicado el precio mensual y no hay tarifa vigente para "
                                + tipo + " en el aparcamiento " + aparcamiento.getNombre()
                                + " el " + fecha + ". Indica el precio o crea antes la tarifa"));
    }

    // 404 si no existe; 403 si es de un aparcamiento de otro gestor
    private Contrato buscarPropio(Long id) {
        Contrato contrato = contratoRepositorio.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No existe el contrato con id " + id));

        aparcamientoServicio.buscarPropio(contrato.getPlaza().getPlanta().getAparcamiento().getId());

        return contrato;
    }

    // Solo se finaliza o se cambia de plaza un contrato ACTIVO: si no, 409
    private Contrato buscarActivo(Long id) {
        Contrato contrato = buscarPropio(id);

        if (contrato.getEstado() != EstadoContrato.ACTIVO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El contrato " + id + " no está activo: está " + contrato.getEstado());
        }

        return contrato;
    }

    private Usuario buscarTitular(Long id) {
        return usuarioRepositorio.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No existe el usuario con id " + id));
    }

    private Vehiculo buscarVehiculo(Long id) {
        return vehiculoRepositorio.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No existe el vehículo con id " + id));
    }

    private ContratoRespuesta aRespuesta(Contrato contrato) {
        Usuario titular = contrato.getTitular();
        Plaza plaza = contrato.getPlaza();

        return new ContratoRespuesta(
                contrato.getId(),
                contrato.getFechaInicio(),
                contrato.getFechaFin(),
                contrato.getPeriodicidad().name(),
                contrato.getPrecioMensual(),
                contrato.getEstado().name(),
                titular.getNombre() + " " + titular.getApellidos(),
                plaza.getPlanta().getAparcamiento().getNombre(),
                plaza.getPlanta().getIdentificador(),
                plaza.getNumero(),
                contrato.getVehiculo().getMatricula(),
                contrato.ocupacion());
    }
}
