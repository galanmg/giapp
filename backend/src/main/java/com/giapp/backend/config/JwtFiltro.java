package com.giapp.backend.config;

import com.giapp.backend.modelo.Usuario;
import com.giapp.backend.repositorio.UsuarioRepositorio;
import com.giapp.backend.servicio.JwtServicio;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Component
public class JwtFiltro extends OncePerRequestFilter {

    private final JwtServicio jwtServicio;
    private final UsuarioRepositorio usuarioRepositorio;

    public JwtFiltro(JwtServicio jwtServicio, UsuarioRepositorio usuarioRepositorio) {
        this.jwtServicio = jwtServicio;
        this.usuarioRepositorio = usuarioRepositorio;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest peticion,
                                    HttpServletResponse respuesta,
                                    FilterChain cadena) throws ServletException, IOException {

        String cabecera = peticion.getHeader("Authorization");

        if (cabecera != null && cabecera.startsWith("Bearer ")) {
            String token = cabecera.substring(7);

            boolean valido = jwtServicio.esValido(token);

            if (valido) {
                String email = jwtServicio.extraerEmail(token);
                Optional<Usuario> encontrado = usuarioRepositorio.findByEmail(email);

                if (encontrado.isPresent() && encontrado.get().isActivo()) {
                    Usuario usuario = encontrado.get();

                    List<SimpleGrantedAuthority> autoridades = List.of(
                            new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name()));

                    UsernamePasswordAuthenticationToken autenticacion =
                            new UsernamePasswordAuthenticationToken(
                                    usuario.getEmail(), null, autoridades);

                    SecurityContext contexto = SecurityContextHolder.createEmptyContext();
                    contexto.setAuthentication(autenticacion);
                    SecurityContextHolder.setContext(contexto);
                }
            }
        }

        cadena.doFilter(peticion, respuesta);
    }
}