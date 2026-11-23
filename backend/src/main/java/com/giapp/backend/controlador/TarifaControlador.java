package com.giapp.backend.controlador;

import com.giapp.backend.dto.TarifaPeticion;
import com.giapp.backend.dto.TarifaRespuesta;
import com.giapp.backend.modelo.enums.TipoVehiculo;
import com.giapp.backend.servicio.TarifaServicio;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

// Sin PUT ni DELETE: las tarifas no se modifican, se crea otra con fecha posterior
@RestController
@RequestMapping("/api/tarifas")
public class TarifaControlador {

    private final TarifaServicio tarifaServicio;

    public TarifaControlador(TarifaServicio tarifaServicio) {
        this.tarifaServicio = tarifaServicio;
    }

    @GetMapping
    public List<TarifaRespuesta> listar(@RequestParam Long aparcamientoId) {
        return tarifaServicio.listar(aparcamientoId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TarifaRespuesta crear(@Valid @RequestBody TarifaPeticion peticion) {
        return tarifaServicio.crear(peticion);
    }

    // La fecha llega como 2026-11-23 (ISO)
    @GetMapping("/vigente")
    public TarifaRespuesta vigente(@RequestParam Long aparcamientoId,
                                   @RequestParam TipoVehiculo tipoVehiculo,
                                   @RequestParam(required = false)
                                   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return tarifaServicio.vigente(aparcamientoId, tipoVehiculo, fecha);
    }
}
