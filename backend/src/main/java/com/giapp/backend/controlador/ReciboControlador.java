package com.giapp.backend.controlador;

import com.giapp.backend.dto.GeneracionRespuesta;
import com.giapp.backend.servicio.ReciboServicio;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/recibos")
public class ReciboControlador {

    private final ReciboServicio reciboServicio;

    public ReciboControlador(ReciboServicio reciboServicio) {
        this.reciboServicio = reciboServicio;
    }

    // Lanza a mano la generación de la tarea de las 6:00, solo para mis aparcamientos.
    // 200 y no 201: puede no crear nada si los recibos ya existen.
    @PostMapping("/generar")
    public GeneracionRespuesta generar() {
        return new GeneracionRespuesta(reciboServicio.generarPendientesDeGestor(LocalDate.now()));
    }
}
