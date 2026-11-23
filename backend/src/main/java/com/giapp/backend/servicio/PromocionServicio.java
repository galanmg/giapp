package com.giapp.backend.servicio;

import com.giapp.backend.dto.PromocionPeticion;
import com.giapp.backend.dto.PromocionRespuesta;
import com.giapp.backend.modelo.Aparcamiento;
import com.giapp.backend.modelo.Promocion;
import com.giapp.backend.repositorio.PromocionRepositorio;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

// Promociones de los aparcamientos del gestor autenticado (RF-18, RF-19, RF-23)
@Service
public class PromocionServicio {

    private final PromocionRepositorio promocionRepositorio;
    private final AparcamientoServicio aparcamientoServicio;
    private final UsuarioActualServicio usuarioActualServicio;

    public PromocionServicio(PromocionRepositorio promocionRepositorio,
                             AparcamientoServicio aparcamientoServicio,
                             UsuarioActualServicio usuarioActualServicio) {
        this.promocionRepositorio = promocionRepositorio;
        this.aparcamientoServicio = aparcamientoServicio;
        this.usuarioActualServicio = usuarioActualServicio;
    }

    // Sin aparcamientoId, las de todos mis aparcamientos
    @Transactional(readOnly = true)
    public List<PromocionRespuesta> listar(Long aparcamientoId) {
        List<Promocion> promociones;

        if (aparcamientoId == null) {
            promociones = promocionRepositorio.findByAparcamientoGestorOrderByAparcamientoIdAscCodigoAsc(
                    usuarioActualServicio.obtener());
        } else {
            promociones = promocionRepositorio.findByAparcamientoOrderByCodigoAsc(
                    aparcamientoServicio.buscarPropio(aparcamientoId));
        }

        return promociones.stream()
                .map(this::aRespuesta)
                .toList();
    }

    @Transactional(readOnly = true)
    public PromocionRespuesta buscarPorId(Long id) {
        return aRespuesta(buscarPropia(id));
    }

    @Transactional
    public PromocionRespuesta crear(PromocionPeticion peticion) {
        Aparcamiento aparcamiento = aparcamientoServicio.buscarPropio(peticion.aparcamientoId());
        String codigo = normalizarCodigo(peticion.codigo());

        if (promocionRepositorio.existsByAparcamientoAndCodigo(aparcamiento, codigo)) {
            throw codigoRepetido(codigo, aparcamiento);
        }

        Promocion promocion = new Promocion(
                codigo, peticion.porcentajeDescuento(), peticion.fechaCaducidad(), aparcamiento);

        return aRespuesta(promocionRepositorio.save(promocion));
    }

    // Se puede pasar a otro aparcamiento, siempre que también sea mío
    @Transactional
    public PromocionRespuesta actualizar(Long id, PromocionPeticion peticion) {
        Promocion promocion = buscarPropia(id);
        Aparcamiento aparcamiento = aparcamientoServicio.buscarPropio(peticion.aparcamientoId());
        String codigo = normalizarCodigo(peticion.codigo());

        if (promocionRepositorio.existsByAparcamientoAndCodigoAndIdNot(aparcamiento, codigo, id)) {
            throw codigoRepetido(codigo, aparcamiento);
        }

        promocion.setCodigo(codigo);
        promocion.setPorcentajeDescuento(peticion.porcentajeDescuento());
        promocion.setFechaCaducidad(peticion.fechaCaducidad());
        promocion.setAparcamiento(aparcamiento);

        return aRespuesta(promocion);
    }

    // Se puede borrar sin problema: los contratos guardan el precio, no la promoción
    @Transactional
    public void eliminar(Long id) {
        promocionRepositorio.delete(buscarPropia(id));
    }

    // RF-19: activar o desactivar sin eliminarla
    @Transactional
    public PromocionRespuesta cambiarActiva(Long id, boolean activa) {
        Promocion promocion = buscarPropia(id);

        promocion.setActiva(activa);

        return aRespuesta(promocion);
    }

    // RF-23. Precio con el descuento del código en ese aparcamiento y en esa fecha.
    // Si el código no vale, 400 diciendo por qué. Lo usa el servicio de contratos.
    public BigDecimal aplicar(String codigo, Aparcamiento aparcamiento, LocalDate fecha, BigDecimal precio) {
        String normalizado = normalizarCodigo(codigo);

        Promocion promocion = promocionRepositorio.findByAparcamientoAndCodigo(aparcamiento, normalizado)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "No existe la promoción " + normalizado + " en el aparcamiento " + aparcamiento.getNombre()));

        if (!promocion.isActiva()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La promoción " + normalizado + " no está activa");
        }

        if (!promocion.esValidaEn(fecha)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La promoción " + normalizado + " caducó el " + promocion.getFechaCaducidad()
                            + ", antes de la fecha de inicio del contrato (" + fecha + ")");
        }

        return promocion.aplicarA(precio);
    }

    // Métodos internos

    // 404 si no existe; 403 si es de un aparcamiento de otro gestor
    private Promocion buscarPropia(Long id) {
        Promocion promocion = promocionRepositorio.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No existe la promoción con id " + id));

        aparcamientoServicio.buscarPropio(promocion.getAparcamiento().getId());

        return promocion;
    }

    // En mayúsculas y sin espacios en los extremos: «verano10» y «VERANO10» son el mismo código
    private String normalizarCodigo(String codigo) {
        return codigo.trim().toUpperCase(Locale.ROOT);
    }

    private ResponseStatusException codigoRepetido(String codigo, Aparcamiento aparcamiento) {
        return new ResponseStatusException(HttpStatus.CONFLICT,
                "Ya existe la promoción " + codigo + " en el aparcamiento " + aparcamiento.getNombre());
    }

    private PromocionRespuesta aRespuesta(Promocion promocion) {
        Aparcamiento aparcamiento = promocion.getAparcamiento();

        return new PromocionRespuesta(
                promocion.getId(),
                promocion.getCodigo(),
                promocion.getPorcentajeDescuento(),
                promocion.getFechaCaducidad(),
                promocion.isActiva(),
                aparcamiento.getId(),
                aparcamiento.getNombre());
    }
}
