package com.giapp.backend.controlador;

import com.giapp.backend.dto.ReciboRespuesta;
import com.giapp.backend.servicio.ReciboServicio;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Histórico de recibos del usuario autenticado (RF-36)
@RestController
@RequestMapping("/api/perfil/recibos")
public class PerfilReciboControlador {

    private final ReciboServicio reciboServicio;

    public PerfilReciboControlador(ReciboServicio reciboServicio) {
        this.reciboServicio = reciboServicio;
    }

    @GetMapping
    public List<ReciboRespuesta> listar() {
        return reciboServicio.listarPropios();
    }
}
