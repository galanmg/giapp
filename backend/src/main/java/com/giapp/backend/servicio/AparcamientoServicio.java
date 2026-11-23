package com.giapp.backend.servicio;

import com.giapp.backend.dto.AparcamientoPeticion;
import com.giapp.backend.dto.AparcamientoRespuesta;
import com.giapp.backend.modelo.Aparcamiento;
import com.giapp.backend.modelo.Usuario;
import com.giapp.backend.repositorio.AparcamientoRepositorio;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class AparcamientoServicio {

    private final AparcamientoRepositorio aparcamientoRepositorio;
    private final UsuarioActualServicio usuarioActualServicio;

    public AparcamientoServicio(AparcamientoRepositorio aparcamientoRepositorio,
                                UsuarioActualServicio usuarioActualServicio) {
        this.aparcamientoRepositorio = aparcamientoRepositorio;
        this.usuarioActualServicio = usuarioActualServicio;
    }

    // Solo los aparcamientos del gestor autenticado (RNF-16)
    @Transactional(readOnly = true)
    public List<AparcamientoRespuesta> listar() {
        return aparcamientoRepositorio.findByGestor(usuarioActualServicio.obtener())
                .stream()
                .map(this::aRespuesta)
                .toList();
    }

    @Transactional(readOnly = true)
    public AparcamientoRespuesta buscarPorId(Long id) {
        return aRespuesta(buscarPropio(id));
    }

    // El gestor del aparcamiento es siempre quien lo crea
    @Transactional
    public AparcamientoRespuesta crear(AparcamientoPeticion peticion) {
        Aparcamiento aparcamiento = new Aparcamiento(
                peticion.nombre(), peticion.direccion(), peticion.ciudad(),
                usuarioActualServicio.obtener());

        return aRespuesta(aparcamientoRepositorio.save(aparcamiento));
    }

    @Transactional
    public AparcamientoRespuesta actualizar(Long id, AparcamientoPeticion peticion) {
        Aparcamiento aparcamiento = buscarPropio(id);

        aparcamiento.setNombre(peticion.nombre());
        aparcamiento.setDireccion(peticion.direccion());
        aparcamiento.setCiudad(peticion.ciudad());

        return aRespuesta(aparcamiento);
    }

    @Transactional
    public void eliminar(Long id) {
        aparcamientoRepositorio.delete(buscarPropio(id));
    }

    // Métodos internos

    // 404 si no existe; 403 si existe pero lo administra otro gestor
    private Aparcamiento buscarPropio(Long id) {
        Aparcamiento aparcamiento = aparcamientoRepositorio.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No existe el aparcamiento con id " + id));

        Usuario gestor = usuarioActualServicio.obtener();

        if (!aparcamiento.getGestor().getId().equals(gestor.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "No administras el aparcamiento con id " + id);
        }

        return aparcamiento;
    }

    private AparcamientoRespuesta aRespuesta(Aparcamiento aparcamiento) {
        Usuario gestor = aparcamiento.getGestor();

        return new AparcamientoRespuesta(
                aparcamiento.getId(),
                aparcamiento.getNombre(),
                aparcamiento.getDireccion(),
                aparcamiento.getCiudad(),
                gestor.getNombre() + " " + gestor.getApellidos());
    }
}
