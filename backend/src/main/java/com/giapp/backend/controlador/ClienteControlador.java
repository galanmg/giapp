package com.giapp.backend.controlador;

import com.giapp.backend.dto.ClientePeticion;
import com.giapp.backend.dto.RegistroPeticion;
import com.giapp.backend.dto.UsuarioRespuesta;
import com.giapp.backend.servicio.ClienteServicio;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
public class ClienteControlador {

    private final ClienteServicio clienteServicio;

    public ClienteControlador(ClienteServicio clienteServicio) {
        this.clienteServicio = clienteServicio;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioRespuesta crear(@Valid @RequestBody RegistroPeticion peticion) {
        return clienteServicio.crear(peticion);
    }

    @GetMapping
    public List<UsuarioRespuesta> listar(@RequestParam(required = false) String texto) {
        return clienteServicio.listar(texto);
    }

    @GetMapping("/{id}")
    public UsuarioRespuesta buscarPorId(@PathVariable Long id) {
        return clienteServicio.buscarPorId(id);
    }

    @PutMapping("/{id}")
    public UsuarioRespuesta actualizar(@PathVariable Long id,
                                       @Valid @RequestBody ClientePeticion peticion) {
        return clienteServicio.actualizar(id, peticion);
    }

    @PatchMapping("/{id}/desactivar")
    public UsuarioRespuesta desactivar(@PathVariable Long id) {
        return clienteServicio.cambiarActivo(id, false);
    }

    @PatchMapping("/{id}/activar")
    public UsuarioRespuesta activar(@PathVariable Long id) {
        return clienteServicio.cambiarActivo(id, true);
    }
}
