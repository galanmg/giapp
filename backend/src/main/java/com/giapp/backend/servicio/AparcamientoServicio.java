package com.giapp.backend.servicio;

import com.giapp.backend.dto.AparcamientoPeticion;
import com.giapp.backend.dto.AparcamientoRespuesta;
import com.giapp.backend.modelo.Aparcamiento;
import com.giapp.backend.modelo.Usuario;
import com.giapp.backend.modelo.enums.Rol;
import com.giapp.backend.repositorio.AparcamientoRepositorio;
import com.giapp.backend.repositorio.UsuarioRepositorio;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class AparcamientoServicio {

    private final AparcamientoRepositorio aparcamientoRepositorio;
    private final UsuarioRepositorio usuarioRepositorio;

    public AparcamientoServicio(AparcamientoRepositorio aparcamientoRepositorio,
                                UsuarioRepositorio usuarioRepositorio) {
        this.aparcamientoRepositorio = aparcamientoRepositorio;
        this.usuarioRepositorio = usuarioRepositorio;
    }

    @Transactional(readOnly = true)
    public List<AparcamientoRespuesta> listar() {
        return aparcamientoRepositorio.findAll()
                .stream()
                .map(this::aRespuesta)
                .toList();
    }

    @Transactional(readOnly = true)
    public AparcamientoRespuesta buscarPorId(Long id) {
        return aRespuesta(buscarEntidad(id));
    }

    @Transactional
    public AparcamientoRespuesta crear(AparcamientoPeticion peticion) {
        Usuario gestor = buscarGestor(peticion.gestorId());

        Aparcamiento aparcamiento = new Aparcamiento(
                peticion.nombre(), peticion.direccion(), peticion.ciudad(), gestor);

        return aRespuesta(aparcamientoRepositorio.save(aparcamiento));
    }

    @Transactional
    public AparcamientoRespuesta actualizar(Long id, AparcamientoPeticion peticion) {
        Aparcamiento aparcamiento = buscarEntidad(id);

        aparcamiento.setNombre(peticion.nombre());
        aparcamiento.setDireccion(peticion.direccion());
        aparcamiento.setCiudad(peticion.ciudad());
        aparcamiento.setGestor(buscarGestor(peticion.gestorId()));

        return aRespuesta(aparcamiento);
    }

    @Transactional
    public void eliminar(Long id) {
        aparcamientoRepositorio.delete(buscarEntidad(id));
    }

    // Metodos internos

    private Aparcamiento buscarEntidad(Long id) {
        return aparcamientoRepositorio.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No existe el aparcamiento con id " + id));
    }

    private Usuario buscarGestor(Long id) {
        Usuario usuario = usuarioRepositorio.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No existe el usuario con id " + id));

        if (usuario.getRol() != Rol.GESTOR) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "El usuario con id " + id + " no es un gestor");
        }

        return usuario;
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