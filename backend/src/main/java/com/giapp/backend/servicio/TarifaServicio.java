package com.giapp.backend.servicio;

import com.giapp.backend.dto.TarifaPeticion;
import com.giapp.backend.dto.TarifaRespuesta;
import com.giapp.backend.modelo.Aparcamiento;
import com.giapp.backend.modelo.TarifaAlquiler;
import com.giapp.backend.modelo.enums.TipoVehiculo;
import com.giapp.backend.repositorio.TarifaAlquilerRepositorio;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

/**
 * Tarifas de alquiler de los aparcamientos del gestor autenticado (RF-17).
 *
 * Las tarifas no se modifican ni se borran: para cambiar un precio se crea otra
 * con una fecha de vigencia posterior. Así los contratos y los recibos antiguos
 * siguen apuntando al precio que se aplicó en su momento.
 */
@Service
public class TarifaServicio {

    private final TarifaAlquilerRepositorio tarifaAlquilerRepositorio;
    private final AparcamientoServicio aparcamientoServicio;

    public TarifaServicio(TarifaAlquilerRepositorio tarifaAlquilerRepositorio,
                          AparcamientoServicio aparcamientoServicio) {
        this.tarifaAlquilerRepositorio = tarifaAlquilerRepositorio;
        this.aparcamientoServicio = aparcamientoServicio;
    }

    @Transactional(readOnly = true)
    public List<TarifaRespuesta> listar(Long aparcamientoId) {
        Aparcamiento aparcamiento = aparcamientoServicio.buscarPropio(aparcamientoId);

        return tarifaAlquilerRepositorio.findByAparcamientoOrderByTipoVehiculoAscVigenteDesdeDesc(aparcamiento)
                .stream()
                .map(this::aRespuesta)
                .toList();
    }

    @Transactional
    public TarifaRespuesta crear(TarifaPeticion peticion) {
        Aparcamiento aparcamiento = aparcamientoServicio.buscarPropio(peticion.aparcamientoId());

        if (tarifaAlquilerRepositorio.existsByAparcamientoAndTipoVehiculoAndVigenteDesde(
                aparcamiento, peticion.tipoVehiculo(), peticion.vigenteDesde())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ya hay una tarifa para " + peticion.tipoVehiculo() + " en el aparcamiento "
                            + aparcamiento.getNombre() + " vigente desde " + peticion.vigenteDesde());
        }

        TarifaAlquiler tarifa = new TarifaAlquiler(
                peticion.tipoVehiculo(), peticion.precioMensual(), peticion.vigenteDesde(), aparcamiento);

        return aRespuesta(tarifaAlquilerRepositorio.save(tarifa));
    }

    // Sin fecha, la vigente hoy
    @Transactional(readOnly = true)
    public TarifaRespuesta vigente(Long aparcamientoId, TipoVehiculo tipoVehiculo, LocalDate fecha) {
        Aparcamiento aparcamiento = aparcamientoServicio.buscarPropio(aparcamientoId);
        LocalDate dia = fecha == null ? LocalDate.now() : fecha;

        return tarifaAlquilerRepositorio.buscarVigente(aparcamiento, tipoVehiculo, dia)
                .map(this::aRespuesta)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No hay tarifa vigente para " + tipoVehiculo + " en el aparcamiento "
                                + aparcamiento.getNombre() + " el " + dia));
    }

    // Métodos internos

    private TarifaRespuesta aRespuesta(TarifaAlquiler tarifa) {
        Aparcamiento aparcamiento = tarifa.getAparcamiento();

        return new TarifaRespuesta(
                tarifa.getId(),
                aparcamiento.getId(),
                aparcamiento.getNombre(),
                tarifa.getTipoVehiculo().name(),
                tarifa.getPrecioMensual(),
                tarifa.getVigenteDesde());
    }
}
