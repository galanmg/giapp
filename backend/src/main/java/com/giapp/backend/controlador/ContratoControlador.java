package com.giapp.backend.controlador;

import com.giapp.backend.dto.ContratoFinalizarPeticion;
import com.giapp.backend.dto.ContratoPeticion;
import com.giapp.backend.dto.ContratoPlazaPeticion;
import com.giapp.backend.dto.ContratoRespuesta;
import com.giapp.backend.modelo.enums.EstadoContrato;
import com.giapp.backend.servicio.ContratoServicio;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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

    @GetMapping
    public List<ContratoRespuesta> listar(@RequestParam(required = false) Long aparcamientoId,
                                          @RequestParam(required = false) Long clienteId,
                                          @RequestParam(required = false) EstadoContrato estado) {
        return contratoServicio.listar(aparcamientoId, clienteId, estado);
    }

    @GetMapping("/{id}")
    public ContratoRespuesta buscarPorId(@PathVariable Long id) {
        return contratoServicio.buscarPorId(id);
    }

    @PatchMapping("/{id}/finalizar")
    public ContratoRespuesta finalizar(@PathVariable Long id,
                                       @Valid @RequestBody ContratoFinalizarPeticion peticion) {
        return contratoServicio.finalizar(id, peticion);
    }

    @PatchMapping("/{id}/plaza")
    public ContratoRespuesta cambiarPlaza(@PathVariable Long id,
                                          @Valid @RequestBody ContratoPlazaPeticion peticion) {
        return contratoServicio.cambiarPlaza(id, peticion.plazaId());
    }
}
