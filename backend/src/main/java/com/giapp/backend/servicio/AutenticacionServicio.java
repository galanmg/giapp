package com.giapp.backend.servicio;

import com.giapp.backend.dto.LoginPeticion;
import com.giapp.backend.dto.LoginRespuesta;
import com.giapp.backend.dto.RegistroPeticion;
import com.giapp.backend.dto.UsuarioRespuesta;
import com.giapp.backend.modelo.Usuario;
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
    private final ClienteServicio clienteServicio;

    public AutenticacionServicio(UsuarioRepositorio usuarioRepositorio,
                                 PasswordEncoder passwordEncoder,
                                 JwtServicio jwtServicio,
                                 ClienteServicio clienteServicio) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.passwordEncoder = passwordEncoder;
        this.jwtServicio = jwtServicio;
        this.clienteServicio = clienteServicio;
    }

    // El registro público es un alta de cliente: siempre con rol CLIENTE
    public UsuarioRespuesta registrar(RegistroPeticion peticion) {
        return clienteServicio.crear(peticion);
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