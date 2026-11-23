package com.giapp.backend.controlador;

import com.giapp.backend.dto.GeneracionRespuesta;
import com.giapp.backend.dto.IngresosRespuesta;
import com.giapp.backend.dto.PagoPeticion;
import com.giapp.backend.dto.ReciboRespuesta;
import com.giapp.backend.modelo.enums.EstadoRecibo;
import com.giapp.backend.servicio.ReciboServicio;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

// Recibos de los aparcamientos del gestor (RF-32 a RF-35)
@RestController
@RequestMapping("/api/recibos")
public class ReciboControlador {

    private final ReciboServicio reciboServicio;

    public ReciboControlador(ReciboServicio reciboServicio) {
        this.reciboServicio = reciboServicio;
    }

    @GetMapping
    public List<ReciboRespuesta> listar(@RequestParam(required = false) Long contratoId,
                                        @RequestParam(required = false) Long clienteId,
                                        @RequestParam(required = false) EstadoRecibo estado,
                                        @RequestParam(required = false) Long aparcamientoId) {
        return reciboServicio.listar(contratoId, clienteId, estado, aparcamientoId);
    }

    @PatchMapping("/{id}/pagar")
    public ReciboRespuesta pagar(@PathVariable Long id,
                                 @Valid @RequestBody PagoPeticion peticion) {
        return reciboServicio.pagar(id, peticion);
    }

    // Las fechas llegan como 2026-11-01 (ISO)
    @GetMapping("/ingresos")
    public IngresosRespuesta ingresos(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return reciboServicio.ingresos(desde, hasta);
    }

    // Lanza a mano el proceso de las 6:00, solo para mis aparcamientos.
    // 200 y no 201: puede no crear nada si los recibos ya existen.
    @PostMapping("/generar")
    public GeneracionRespuesta generar() {
        return reciboServicio.procesarDeGestor(LocalDate.now());
    }
}
