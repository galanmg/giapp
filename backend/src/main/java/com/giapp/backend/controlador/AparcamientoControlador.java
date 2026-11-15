package com.giapp.backend.controlador;

import com.giapp.backend.dto.AparcamientoPeticion;
import com.giapp.backend.dto.AparcamientoRespuesta;
import com.giapp.backend.servicio.AparcamientoServicio;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/aparcamientos")
public class AparcamientoControlador {

    private final AparcamientoServicio aparcamientoServicio;

    public AparcamientoControlador(AparcamientoServicio aparcamientoServicio) {
        this.aparcamientoServicio = aparcamientoServicio;
    }

    @GetMapping
    public List<AparcamientoRespuesta> listar() {
        return aparcamientoServicio.listar();
    }

    @GetMapping("/{id}")
    public AparcamientoRespuesta buscarPorId(@PathVariable Long id) {
        return aparcamientoServicio.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AparcamientoRespuesta crear(@Valid @RequestBody AparcamientoPeticion peticion) {
        return aparcamientoServicio.crear(peticion);
    }

    @PutMapping("/{id}")
    public AparcamientoRespuesta actualizar(@PathVariable Long id,
                                            @Valid @RequestBody AparcamientoPeticion peticion) {
        return aparcamientoServicio.actualizar(id, peticion);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        aparcamientoServicio.eliminar(id);
    }
}