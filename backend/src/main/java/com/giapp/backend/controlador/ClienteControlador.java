package com.giapp.backend.controlador;

import com.giapp.backend.dto.ClienteAltaPeticion;
import com.giapp.backend.dto.UsuarioRespuesta;
import com.giapp.backend.servicio.ClienteServicio;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
    public UsuarioRespuesta crear(@Valid @RequestBody ClienteAltaPeticion peticion) {
        return clienteServicio.crear(peticion);
    }

    @GetMapping
    public List<UsuarioRespuesta> listar(@RequestParam(required = false) String texto,
                                         @RequestParam(required = false) Long aparcamientoId) {
        return clienteServicio.listar(texto, aparcamientoId);
    }

    @GetMapping("/buscar")
    public UsuarioRespuesta buscarPorEmail(@RequestParam String email) {
        return clienteServicio.buscarPorEmail(email);
    }

    @GetMapping("/{id}")
    public UsuarioRespuesta buscarPorId(@PathVariable Long id) {
        return clienteServicio.buscarPorId(id);
    }
}
