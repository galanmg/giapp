package com.giapp.backend.controlador;

import com.giapp.backend.dto.RegistroPeticion;
import com.giapp.backend.dto.UsuarioRespuesta;
import com.giapp.backend.servicio.AutenticacionServicio;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AutenticacionControlador {

    private final AutenticacionServicio autenticacionServicio;

    public AutenticacionControlador(AutenticacionServicio autenticacionServicio) {
        this.autenticacionServicio = autenticacionServicio;
    }

    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioRespuesta registrar(@Valid @RequestBody RegistroPeticion peticion) {
        return autenticacionServicio.registrar(peticion);
    }
}