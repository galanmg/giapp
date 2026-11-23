package com.giapp.backend.controlador;

import com.giapp.backend.dto.ContratoRespuesta;
import com.giapp.backend.servicio.ContratoServicio;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Contratos del usuario autenticado (RF-27)
@RestController
@RequestMapping("/api/perfil/contratos")
public class PerfilContratoControlador {

    private final ContratoServicio contratoServicio;

    public PerfilContratoControlador(ContratoServicio contratoServicio) {
        this.contratoServicio = contratoServicio;
    }

    @GetMapping
    public List<ContratoRespuesta> listar() {
        return contratoServicio.listarPropios();
    }
}
