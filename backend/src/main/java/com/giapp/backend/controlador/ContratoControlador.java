package com.giapp.backend.controlador;

import com.giapp.backend.dto.ContratoPeticion;
import com.giapp.backend.dto.ContratoRespuesta;
import com.giapp.backend.servicio.ContratoServicio;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/contratos")
public class ContratoControlador {

    private final ContratoServicio contratoServicio;

    public ContratoControlador(ContratoServicio contratoServicio) {
        this.contratoServicio = contratoServicio;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ContratoRespuesta crear(@Valid @RequestBody ContratoPeticion peticion) {
        return contratoServicio.crear(peticion);
    }
}