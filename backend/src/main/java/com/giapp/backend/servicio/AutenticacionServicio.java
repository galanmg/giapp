package com.giapp.backend.servicio;

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

@Service
public class AutenticacionServicio {

    private final UsuarioRepositorio usuarioRepositorio;
    private final PasswordEncoder passwordEncoder;

    public AutenticacionServicio(UsuarioRepositorio usuarioRepositorio,
                                 PasswordEncoder passwordEncoder) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UsuarioRespuesta registrar(RegistroPeticion peticion) {

        if (usuarioRepositorio.existsByEmail(peticion.email())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Ya existe un usuario con el email " + peticion.email());
        }

        Usuario usuario = new Usuario(
                peticion.nombre(),
                peticion.apellidos(),
                peticion.email(),
                passwordEncoder.encode(peticion.password()),
                peticion.telefono(),
                Rol.CLIENTE);

        Usuario guardado = usuarioRepositorio.save(usuario);

        return new UsuarioRespuesta(
                guardado.getId(),
                guardado.getNombre(),
                guardado.getApellidos(),
                guardado.getEmail(),
                guardado.getRol().name());
    }
}