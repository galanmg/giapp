package com.giapp.backend.config;

import com.giapp.backend.dto.ErrorRespuesta;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Convierte las excepciones que salen de los controladores en una respuesta
 * JSON con el formato común ErrorRespuesta.
 *
 * Los 401 y 403 de Spring Security no pasan por aquí: se producen en la
 * cadena de filtros, antes de llegar a ningún controlador.
 */
@RestControllerAdvice
public class ManejadorErrores {

    private static final Logger log = LoggerFactory.getLogger(ManejadorErrores.class);

    // Errores que lanzan nuestros servicios: se respeta su código y su mensaje
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorRespuesta> manejarResponseStatus(ResponseStatusException ex,
                                                               HttpServletRequest peticion) {
        return construir(ex.getStatusCode(), ex.getReason(), peticion, List.of());
    }

    // @Valid en el controlador: un error por cada campo no válido
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorRespuesta> manejarValidacion(MethodArgumentNotValidException ex,
                                                           HttpServletRequest peticion) {
        List<ErrorRespuesta.ErrorCampo> errores = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> new ErrorRespuesta.ErrorCampo(error.getField(), error.getDefaultMessage()))
                .toList();

        return construir(HttpStatus.BAD_REQUEST, "Hay datos no válidos", peticion, errores);
    }

    // JSON mal formado o un valor de enumerado que no existe
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorRespuesta> manejarJsonIlegible(HttpMessageNotReadableException ex,
                                                             HttpServletRequest peticion) {
        return construir(HttpStatus.BAD_REQUEST,
                "El cuerpo de la petición no es un JSON válido o contiene un valor no permitido",
                peticion, List.of());
    }

    // Restricciones de la base de datos (claves únicas, claves ajenas)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorRespuesta> manejarIntegridad(DataIntegrityViolationException ex,
                                                           HttpServletRequest peticion) {
        log.warn("Violación de integridad en {}: {}", peticion.getRequestURI(), ex.getMessage());

        return construir(HttpStatus.CONFLICT,
                "La operación choca con datos existentes: un valor repetido o un registro que está en uso",
                peticion, List.of());
    }

    // Un parámetro de la URL con un tipo que no corresponde, como /api/plazas/abc
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorRespuesta> manejarTipoIncorrecto(MethodArgumentTypeMismatchException ex,
                                                               HttpServletRequest peticion) {
        return construir(HttpStatus.BAD_REQUEST,
                "El parámetro " + ex.getName() + " tiene un valor no válido: " + ex.getValue(),
                peticion, List.of());
    }

    // Cualquier otra excepción
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorRespuesta> manejarOtras(Exception ex, HttpServletRequest peticion) {

        // Las excepciones propias de Spring MVC (ruta inexistente, método no
        // permitido, parámetro que falta...) ya traen su código: se respeta
        if (ex instanceof ErrorResponse errorSpring) {
            return construir(errorSpring.getStatusCode(), mensajeSpring(ex), peticion, List.of());
        }

        // Error inesperado: el detalle solo va al log, nunca al cliente
        log.error("Error no controlado en {}", peticion.getRequestURI(), ex);

        return construir(HttpStatus.INTERNAL_SERVER_ERROR,
                "Se ha producido un error interno en el servidor",
                peticion, List.of());
    }

    // Métodos internos

    private String mensajeSpring(Exception ex) {
        if (ex instanceof NoResourceFoundException) {
            return "No existe la ruta solicitada";
        }
        if (ex instanceof HttpRequestMethodNotSupportedException metodo) {
            return "El método " + metodo.getMethod() + " no está permitido en esta ruta";
        }
        if (ex instanceof MissingServletRequestParameterException parametro) {
            return "Falta el parámetro obligatorio " + parametro.getParameterName();
        }
        if (ex instanceof HttpMediaTypeNotSupportedException) {
            return "El tipo de contenido de la petición no está admitido";
        }
        return "La petición no se ha podido procesar";
    }

    private ResponseEntity<ErrorRespuesta> construir(HttpStatusCode codigo,
                                                     String mensaje,
                                                     HttpServletRequest peticion,
                                                     List<ErrorRespuesta.ErrorCampo> errores) {
        HttpStatus estado = HttpStatus.resolve(codigo.value());
        String nombre = estado != null ? estado.getReasonPhrase() : String.valueOf(codigo.value());

        ErrorRespuesta cuerpo = new ErrorRespuesta(
                LocalDateTime.now(),
                codigo.value(),
                nombre,
                mensaje,
                peticion.getRequestURI(),
                errores);

        return ResponseEntity.status(codigo).body(cuerpo);
    }
}
