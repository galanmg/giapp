package com.giapp.backend.controlador;

import com.giapp.backend.dto.PlantaPeticion;
import com.giapp.backend.dto.PlantaRespuesta;
import com.giapp.backend.servicio.PlantaServicio;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/plantas")
public class PlantaControlador {

    private final PlantaServicio plantaServicio;

    public PlantaControlador(PlantaServicio plantaServicio) {
        this.plantaServicio = plantaServicio;
    }

    @GetMapping
    public List<PlantaRespuesta> listar(@RequestParam(required = false) Long aparcamientoId) {
        return plantaServicio.listar(aparcamientoId);
    }

    @GetMapping("/{id}")
    public PlantaRespuesta buscarPorId(@PathVariable Long id) {
        return plantaServicio.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PlantaRespuesta crear(@Valid @RequestBody PlantaPeticion peticion) {
        return plantaServicio.crear(peticion);
    }

    @PutMapping("/{id}")
    public PlantaRespuesta actualizar(@PathVariable Long id,
                                      @Valid @RequestBody PlantaPeticion peticion) {
        return plantaServicio.actualizar(id, peticion);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        plantaServicio.eliminar(id);
    }
}
