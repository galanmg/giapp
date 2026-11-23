package com.giapp.backend.controlador;

import com.giapp.backend.dto.ClienteAparcamientoPeticion;
import com.giapp.backend.dto.ClienteAparcamientoRespuesta;
import com.giapp.backend.servicio.ClienteServicio;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

// Clientes de un aparcamiento: alta (RF-55) y baja (RF-06)
@RestController
@RequestMapping("/api/aparcamientos/{aparcamientoId}/clientes")
public class ClienteAparcamientoControlador {

    private final ClienteServicio clienteServicio;

    public ClienteAparcamientoControlador(ClienteServicio clienteServicio) {
        this.clienteServicio = clienteServicio;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClienteAparcamientoRespuesta vincular(@PathVariable Long aparcamientoId,
                                                 @Valid @RequestBody ClienteAparcamientoPeticion peticion) {
        return clienteServicio.vincular(aparcamientoId, peticion.clienteId());
    }

    @PatchMapping("/{clienteId}/baja")
    public ClienteAparcamientoRespuesta darDeBaja(@PathVariable Long aparcamientoId,
                                                  @PathVariable Long clienteId) {
        return clienteServicio.darDeBaja(aparcamientoId, clienteId);
    }
}
