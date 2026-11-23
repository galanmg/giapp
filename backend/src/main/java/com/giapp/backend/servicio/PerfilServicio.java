package com.giapp.backend.servicio;

import com.giapp.backend.dto.CambioContrasenaPeticion;
import com.giapp.backend.dto.PerfilPeticion;
import com.giapp.backend.dto.UsuarioRespuesta;
import com.giapp.backend.modelo.Usuario;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PerfilServicio {

    private final UsuarioActualServicio usuarioActualServicio;
    private final PasswordEncoder passwordEncoder;

    public PerfilServicio(UsuarioActualServicio usuarioActualServicio,
                          PasswordEncoder passwordEncoder) {
        this.usuarioActualServicio = usuarioActualServicio;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public UsuarioRespuesta obtener() {
        return aRespuesta(usuarioActualServicio.obtener());
    }

    // El email y el rol no se cambian desde el perfil
    @Transactional
    public UsuarioRespuesta actualizar(PerfilPeticion peticion) {
        Usuario usuario = usuarioActualServicio.obtener();

        usuario.setNombre(peticion.nombre());
        usuario.setApellidos(peticion.apellidos());
        usuario.setTelefono(peticion.telefono());

        return aRespuesta(usuario);
    }

    @Transactional
    public UsuarioRespuesta cambiarContrasena(CambioContrasenaPeticion peticion) {
        Usuario usuario = usuarioActualServicio.obtener();

        if (!passwordEncoder.matches(peticion.contrasenaActual(), usuario.getPasswordHash())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "La contraseña actual no es correcta");
        }

        usuario.setPasswordHash(passwordEncoder.encode(peticion.contrasenaNueva()));

        return aRespuesta(usuario);
    }

    // Métodos internos

    private UsuarioRespuesta aRespuesta(Usuario usuario) {
        return new UsuarioRespuesta(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getApellidos(),
                usuario.getEmail(),
                usuario.getTelefono(),
                usuario.getRol().name());
    }
}
