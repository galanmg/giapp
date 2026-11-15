package com.giapp.backend.servicio;

import com.giapp.backend.modelo.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtServicio {

    private final SecretKey clave;
    private final long expiracionMs;

    public JwtServicio(@Value("${giapp.jwt.secreto}") String secreto,
                       @Value("${giapp.jwt.expiracion-ms}") long expiracionMs) {
        this.clave = Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
        this.expiracionMs = expiracionMs;
    }

    public String generarToken(Usuario usuario) {
        Date ahora = new Date();
        Date caducidad = new Date(ahora.getTime() + expiracionMs);

        return Jwts.builder()
                .subject(usuario.getEmail())
                .claim("rol", usuario.getRol().name())
                .claim("nombre", usuario.getNombre())
                .issuedAt(ahora)
                .expiration(caducidad)
                .signWith(clave)
                .compact();
    }

    public String extraerEmail(String token) {
        return leerContenido(token).getSubject();
    }

    public boolean esValido(String token) {
        try {
            leerContenido(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // Metodos internos

    private Claims leerContenido(String token) {
        return Jwts.parser()
                .verifyWith(clave)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}