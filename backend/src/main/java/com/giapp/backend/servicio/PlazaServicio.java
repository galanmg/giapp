package com.giapp.backend.servicio;

import com.giapp.backend.dto.PlazaPeticion;
import com.giapp.backend.dto.PlazaRespuesta;
import com.giapp.backend.modelo.Planta;
import com.giapp.backend.modelo.Plaza;
import com.giapp.backend.repositorio.PlantaRepositorio;
import com.giapp.backend.repositorio.PlazaRepositorio;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class PlazaServicio {

    private final PlazaRepositorio plazaRepositorio;
    private final PlantaRepositorio plantaRepositorio;

    public PlazaServicio(PlazaRepositorio plazaRepositorio,
                         PlantaRepositorio plantaRepositorio) {
        this.plazaRepositorio = plazaRepositorio;
        this.plantaRepositorio = plantaRepositorio;
    }

    @Transactional(readOnly = true)
    public List<PlazaRespuesta> listar(Long plantaId) {
        List<Plaza> plazas;

        if (plantaId == null) {
            plazas = plazaRepositorio.findAll();
        } else {
            plazas = plazaRepositorio.findByPlanta(buscarPlanta(plantaId));
        }

        return plazas.stream()
                .map(this::aRespuesta)
                .toList();
    }

    @Transactional(readOnly = true)
    public PlazaRespuesta buscarPorId(Long id) {
        return aRespuesta(buscarEntidad(id));
    }

    @Transactional
    public PlazaRespuesta crear(PlazaPeticion peticion) {
        Planta planta = buscarPlanta(peticion.plantaId());

        Plaza plaza = new Plaza(
                peticion.numero(), peticion.capacidad(), peticion.admiteCoche(),planta);

        return aRespuesta(plazaRepositorio.save(plaza));
    }

    @Transactional
    public PlazaRespuesta actualizar(Long id, PlazaPeticion peticion) {
        Plaza plaza = buscarEntidad(id);

        plaza.setNumero(peticion.numero());
        plaza.setCapacidad(peticion.capacidad());
        plaza.setAdmiteCoche(peticion.admiteCoche());
        plaza.setPlanta(buscarPlanta(peticion.plantaId()));

        return aRespuesta(plaza);
    }

    @Transactional
    public void eliminar(Long id) {
        plazaRepositorio.delete(buscarEntidad(id));
    }

    // Métodos internos

    private Plaza buscarEntidad(Long id) {
        return plazaRepositorio.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No existe la plaza con id " + id));
    }

    private Planta buscarPlanta(Long id) {
        return plantaRepositorio.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No existe la planta con id " + id));
    }

    private PlazaRespuesta aRespuesta(Plaza plaza) {
        return new PlazaRespuesta(
                plaza.getId(),
                plaza.getNumero(),
                plaza.getCapacidad(),
                plaza.isAdmiteCoche(),
                plaza.getEstado().name(),
                plaza.getPlanta().getIdentificador());
    }
}
