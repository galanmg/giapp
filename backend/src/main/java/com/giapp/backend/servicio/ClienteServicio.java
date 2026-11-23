package com.giapp.backend.servicio;

import com.giapp.backend.dto.ClientePeticion;
import com.giapp.backend.dto.RegistroPeticion;
import com.giapp.backend.dto.UsuarioRespuesta;
import com.giapp.backend.modelo.Usuario;
import com.giapp.backend.modelo.enums.Rol;
import com.giapp.backend.repositorio.UsuarioRepositorio;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ClienteServicio {

    private final UsuarioRepositorio usuarioRepositorio;
    private final PasswordEncoder passwordEncoder;

    public ClienteServicio(UsuarioRepositorio usuarioRepositorio,
                           PasswordEncoder passwordEncoder) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.passwordEncoder = passwordEncoder;
    }

    // Alta de cliente (RF-01). También la usa el registro público:
    // en los dos casos el usuario se crea siempre con rol CLIENTE.
    @Transactional
    public UsuarioRespuesta crear(RegistroPeticion peticion) {
        if (usuarioRepositorio.existsByEmail(peticion.email())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Ya existe un usuario con el email " + peticion.email());
        }

        Usuario cliente = new Usuario(
                peticion.nombre(),
                peticion.apellidos(),
                peticion.email(),
                passwordEncoder.encode(peticion.password()),
                peticion.telefono(),
                Rol.CLIENTE);

        return aRespuesta(usuarioRepositorio.save(cliente));
    }

    // Sin texto, todos los clientes; con texto, filtra por nombre, apellidos o email
    @Transactional(readOnly = true)
    public List<UsuarioRespuesta> listar(String texto) {
        List<Usuario> clientes;

        if (texto == null || texto.isBlank()) {
            clientes = usuarioRepositorio.findByRolOrderByApellidosAscNombreAsc(Rol.CLIENTE);
        } else {
            clientes = usuarioRepositorio.buscarPorTexto(Rol.CLIENTE, texto.trim());
        }

        return clientes.stream()
                .map(this::aRespuesta)
                .toList();
    }

    @Transactional(readOnly = true)
    public UsuarioRespuesta buscarPorId(Long id) {
        return aRespuesta(buscarEntidad(id));
    }

    // RF-06
    @Transactional
    public UsuarioRespuesta actualizar(Long id, ClientePeticion peticion) {
        Usuario cliente = buscarEntidad(id);

        if (usuarioRepositorio.existsByEmailAndIdNot(peticion.email(), id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Ya existe un usuario con el email " + peticion.email());
        }

        cliente.setNombre(peticion.nombre());
        cliente.setApellidos(peticion.apellidos());
        cliente.setEmail(peticion.email());
        cliente.setTelefono(peticion.telefono());

        return aRespuesta(cliente);
    }

    // Los clientes no se borran: se desactivan para conservar su histórico.
    // JwtFiltro rechaza en cada petición los tokens de usuarios desactivados.
    @Transactional
    public UsuarioRespuesta cambiarActivo(Long id, boolean activo) {
        Usuario cliente = buscarEntidad(id);

        cliente.setActivo(activo);

        return aRespuesta(cliente);
    }

    // Métodos internos

    // Un id que existe pero es de un gestor se trata como inexistente
    private Usuario buscarEntidad(Long id) {
        return usuarioRepositorio.findByIdAndRol(id, Rol.CLIENTE)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No existe el cliente con id " + id));
    }

    private UsuarioRespuesta aRespuesta(Usuario usuario) {
        return new UsuarioRespuesta(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getApellidos(),
                usuario.getEmail(),
                usuario.getTelefono(),
                usuario.getRol().name(),
                usuario.isActivo());
    }
}
