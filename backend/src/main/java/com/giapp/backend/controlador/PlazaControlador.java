package com.giapp.backend.controlador;

import com.giapp.backend.dto.PlazaPeticion;
import com.giapp.backend.dto.PlazaRespuesta;
import com.giapp.backend.servicio.PlazaServicio;
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
@RequestMapping("/api/plazas")
public class PlazaControlador {

    private final PlazaServicio plazaServicio;

    public PlazaControlador(PlazaServicio plazaServicio) {
        this.plazaServicio = plazaServicio;
    }

    @GetMapping
    public List<PlazaRespuesta> listar(@RequestParam(required = false) Long plantaId) {
        return plazaServicio.listar(plantaId);
    }

    @GetMapping("/{id}")
    public PlazaRespuesta buscarPorId(@PathVariable Long id) {
        return plazaServicio.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PlazaRespuesta crear(@Valid @RequestBody PlazaPeticion peticion) {
        return plazaServicio.crear(peticion);
    }

    @PutMapping("/{id}")
    public PlazaRespuesta actualizar(@PathVariable Long id,
                                     @Valid @RequestBody PlazaPeticion peticion) {
        return plazaServicio.actualizar(id, peticion);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        plazaServicio.eliminar(id);
    }
}