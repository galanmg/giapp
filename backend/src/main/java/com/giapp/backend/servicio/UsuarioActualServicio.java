package com.giapp.backend.servicio;

import com.giapp.backend.modelo.Usuario;
import com.giapp.backend.repositorio.UsuarioRepositorio;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Da acceso al usuario que hace la petición actual.
 *
 * JwtFiltro guarda el email como nombre de la autenticación; aquí se lee y
 * se busca la entidad. Al llamarlo desde un método @Transactional, la
 * entidad devuelta pertenece a esa transacción y se puede modificar.
 */
@Service
public class UsuarioActualServicio {

    private final UsuarioRepositorio usuarioRepositorio;

    public UsuarioActualServicio(UsuarioRepositorio usuarioRepositorio) {
        this.usuarioRepositorio = usuarioRepositorio;
    }

    public Usuario obtener() {
        Authentication autenticacion = SecurityContextHolder.getContext().getAuthentication();

        if (autenticacion == null
                || !autenticacion.isAuthenticated()
                || autenticacion instanceof AnonymousAuthenticationToken) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Hace falta autenticarse");
        }

        return usuarioRepositorio.findByEmail(autenticacion.getName())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Hace falta autenticarse"));
    }
}
