package com.giapp.backend.config;

import com.giapp.backend.dto.ErrorRespuesta;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Respuestas 401 y 403 de Spring Security con el formato común ErrorRespuesta.
 *
 * Estos errores se producen en la cadena de filtros, antes de llegar a los
 * controladores, así que ManejadorErrores no los ve: hay que escribir aquí
 * el JSON a mano.
 */
@Component
public class ManejadorErroresSeguridad implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final JsonMapper jsonMapper;

    public ManejadorErroresSeguridad(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    // Sin token, o con un token no válido o de un usuario desactivado
    @Override
    public void commence(HttpServletRequest peticion,
                         HttpServletResponse respuesta,
                         AuthenticationException ex) throws IOException {
        escribir(respuesta, peticion, HttpStatus.UNAUTHORIZED, "Hace falta autenticarse");
    }

    // Autenticado, pero su rol no tiene permiso para esa ruta
    @Override
    public void handle(HttpServletRequest peticion,
                       HttpServletResponse respuesta,
                       AccessDeniedException ex) throws IOException {
        escribir(respuesta, peticion, HttpStatus.FORBIDDEN,
                "No tienes permiso para realizar esta operación");
    }

    private void escribir(HttpServletResponse respuesta,
                          HttpServletRequest peticion,
                          HttpStatus estado,
                          String mensaje) throws IOException {
        ErrorRespuesta cuerpo = new ErrorRespuesta(
                LocalDateTime.now(),
                estado.value(),
                estado.getReasonPhrase(),
                mensaje,
                peticion.getRequestURI(),
                List.of());

        respuesta.setStatus(estado.value());
        respuesta.setContentType(MediaType.APPLICATION_JSON_VALUE);
        respuesta.setCharacterEncoding(StandardCharsets.UTF_8.name());
        jsonMapper.writeValue(respuesta.getOutputStream(), cuerpo);
    }
}
