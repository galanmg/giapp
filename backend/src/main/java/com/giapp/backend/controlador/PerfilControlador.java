package com.giapp.backend.controlador;

import com.giapp.backend.dto.CambioContrasenaPeticion;
import com.giapp.backend.dto.PerfilPeticion;
import com.giapp.backend.dto.UsuarioRespuesta;
import com.giapp.backend.servicio.PerfilServicio;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/perfil")
public class PerfilControlador {

    private final PerfilServicio perfilServicio;

    public PerfilControlador(PerfilServicio perfilServicio) {
        this.perfilServicio = perfilServicio;
    }

    @GetMapping
    public UsuarioRespuesta obtener() {
        return perfilServicio.obtener();
    }

    @PutMapping
    public UsuarioRespuesta actualizar(@Valid @RequestBody PerfilPeticion peticion) {
        return perfilServicio.actualizar(peticion);
    }

    @PutMapping("/contrasena")
    public UsuarioRespuesta cambiarContrasena(@Valid @RequestBody CambioContrasenaPeticion peticion) {
        return perfilServicio.cambiarContrasena(peticion);
    }
}
