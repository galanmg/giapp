package com.giapp.backend.controlador;

import com.giapp.backend.dto.VehiculoPeticion;
import com.giapp.backend.dto.VehiculoRespuesta;
import com.giapp.backend.servicio.VehiculoServicio;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Vehículos de los clientes, gestionados por el gestor (RF-07, RF-08)
@RestController
@RequestMapping("/api")
public class VehiculoControlador {

    private final VehiculoServicio vehiculoServicio;

    public VehiculoControlador(VehiculoServicio vehiculoServicio) {
        this.vehiculoServicio = vehiculoServicio;
    }

    @GetMapping("/clientes/{clienteId}/vehiculos")
    public List<VehiculoRespuesta> listarDeCliente(@PathVariable Long clienteId) {
        return vehiculoServicio.listarDeCliente(clienteId);
    }

    @PostMapping("/clientes/{clienteId}/vehiculos")
    @ResponseStatus(HttpStatus.CREATED)
    public VehiculoRespuesta crearParaCliente(@PathVariable Long clienteId,
                                              @Valid @RequestBody VehiculoPeticion peticion) {
        return vehiculoServicio.crearParaCliente(clienteId, peticion);
    }

    @PutMapping("/vehiculos/{id}")
    public VehiculoRespuesta actualizar(@PathVariable Long id,
                                        @Valid @RequestBody VehiculoPeticion peticion) {
        return vehiculoServicio.actualizar(id, peticion);
    }

    @DeleteMapping("/vehiculos/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        vehiculoServicio.eliminar(id);
    }
}
