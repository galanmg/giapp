package com.giapp.backend.servicio;

import com.giapp.backend.dto.LoginPeticion;
import com.giapp.backend.dto.LoginRespuesta;
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
    private final JwtServicio jwtServicio;

    public AutenticacionServicio(UsuarioRepositorio usuarioRepositorio,
                                 PasswordEncoder passwordEncoder,
                                 JwtServicio jwtServicio) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.passwordEncoder = passwordEncoder;
        this.jwtServicio = jwtServicio;
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
                guardado.getTelefono(),
                guardado.getRol().name());
    }

    @Transactional(readOnly = true)
    public LoginRespuesta login(LoginPeticion peticion) {

        Usuario usuario = usuarioRepositorio.findByEmail(peticion.email())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Email o contraseña incorrectos"));

        if (!passwordEncoder.matches(peticion.password(), usuario.getPasswordHash())) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Email o contraseña incorrectos");
        }

        if (!usuario.isActivo()) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "La cuenta está desactivada");
        }

        return new LoginRespuesta(
                jwtServicio.generarToken(usuario),
                usuario.getEmail(),
                usuario.getNombre(),
                usuario.getRol().name());
    }
}