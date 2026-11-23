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

// Vehículos del usuario autenticado (RF-09)
@RestController
@RequestMapping("/api/perfil/vehiculos")
public class PerfilVehiculoControlador {

    private final VehiculoServicio vehiculoServicio;

    public PerfilVehiculoControlador(VehiculoServicio vehiculoServicio) {
        this.vehiculoServicio = vehiculoServicio;
    }

    @GetMapping
    public List<VehiculoRespuesta> listar() {
        return vehiculoServicio.listarPropios();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VehiculoRespuesta crear(@Valid @RequestBody VehiculoPeticion peticion) {
        return vehiculoServicio.crearPropio(peticion);
    }

    @PutMapping("/{id}")
    public VehiculoRespuesta actualizar(@PathVariable Long id,
                                        @Valid @RequestBody VehiculoPeticion peticion) {
        return vehiculoServicio.actualizarPropio(id, peticion);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        vehiculoServicio.eliminarPropio(id);
    }
}
