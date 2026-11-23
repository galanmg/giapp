package com.giapp.backend.servicio;

import com.giapp.backend.dto.PlantaPeticion;
import com.giapp.backend.dto.PlantaRespuesta;
import com.giapp.backend.modelo.Aparcamiento;
import com.giapp.backend.modelo.Planta;
import com.giapp.backend.repositorio.PlantaRepositorio;
import com.giapp.backend.repositorio.PlazaRepositorio;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

// Plantas de los aparcamientos del gestor autenticado (RF-12, RNF-18)
@Service
public class PlantaServicio {

    private final PlantaRepositorio plantaRepositorio;
    private final PlazaRepositorio plazaRepositorio;
    private final AparcamientoServicio aparcamientoServicio;
    private final UsuarioActualServicio usuarioActualServicio;

    public PlantaServicio(PlantaRepositorio plantaRepositorio,
                          PlazaRepositorio plazaRepositorio,
                          AparcamientoServicio aparcamientoServicio,
                          UsuarioActualServicio usuarioActualServicio) {
        this.plantaRepositorio = plantaRepositorio;
        this.plazaRepositorio = plazaRepositorio;
        this.aparcamientoServicio = aparcamientoServicio;
        this.usuarioActualServicio = usuarioActualServicio;
    }

    // Sin aparcamientoId, las de todos mis aparcamientos
    @Transactional(readOnly = true)
    public List<PlantaRespuesta> listar(Long aparcamientoId) {
        List<Planta> plantas;

        if (aparcamientoId == null) {
            plantas = plantaRepositorio.findByAparcamientoGestorOrderByAparcamientoIdAscNivelAsc(
                    usuarioActualServicio.obtener());
        } else {
            plantas = plantaRepositorio.findByAparcamientoOrderByNivelAsc(
                    aparcamientoServicio.buscarPropio(aparcamientoId));
        }

        return plantas.stream()
                .map(this::aRespuesta)
                .toList();
    }

    @Transactional(readOnly = true)
    public PlantaRespuesta buscarPorId(Long id) {
        return aRespuesta(buscarPropia(id));
    }

    @Transactional
    public PlantaRespuesta crear(PlantaPeticion peticion) {
        Aparcamiento aparcamiento = aparcamientoServicio.buscarPropio(peticion.aparcamientoId());

        if (plantaRepositorio.existsByAparcamientoAndIdentificador(aparcamiento, peticion.identificador())) {
            throw identificadorRepetido(peticion.identificador(), aparcamiento);
        }

        Planta planta = new Planta(peticion.identificador(), peticion.nivel(), aparcamiento);

        return aRespuesta(plantaRepositorio.save(planta));
    }

    // Se puede mover la planta a otro aparcamiento, siempre que también sea mío
    @Transactional
    public PlantaRespuesta actualizar(Long id, PlantaPeticion peticion) {
        Planta planta = buscarPropia(id);
        Aparcamiento aparcamiento = aparcamientoServicio.buscarPropio(peticion.aparcamientoId());

        if (plantaRepositorio.existsByAparcamientoAndIdentificadorAndIdNot(
                aparcamiento, peticion.identificador(), id)) {
            throw identificadorRepetido(peticion.identificador(), aparcamiento);
        }

        planta.setIdentificador(peticion.identificador());
        planta.setNivel(peticion.nivel());
        planta.setAparcamiento(aparcamiento);

        return aRespuesta(planta);
    }

    @Transactional
    public void eliminar(Long id) {
        Planta planta = buscarPropia(id);

        if (plazaRepositorio.existsByPlanta(planta)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No se puede eliminar la planta " + planta.getIdentificador()
                            + " porque tiene plazas. Elimina antes sus plazas");
        }

        plantaRepositorio.delete(planta);
    }

    // 404 si no existe; 403 si es de un aparcamiento de otro gestor.
    // Público para que lo use el servicio de plazas.
    public Planta buscarPropia(Long id) {
        Planta planta = plantaRepositorio.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No existe la planta con id " + id));

        aparcamientoServicio.buscarPropio(planta.getAparcamiento().getId());

        return planta;
    }

    // Métodos internos

    private ResponseStatusException identificadorRepetido(String identificador, Aparcamiento aparcamiento) {
        return new ResponseStatusException(HttpStatus.CONFLICT,
                "Ya existe una planta " + identificador + " en el aparcamiento " + aparcamiento.getNombre());
    }

    private PlantaRespuesta aRespuesta(Planta planta) {
        Aparcamiento aparcamiento = planta.getAparcamiento();

        return new PlantaRespuesta(
                planta.getId(),
                planta.getIdentificador(),
                planta.getNivel(),
                aparcamiento.getId(),
                aparcamiento.getNombre());
    }
}
