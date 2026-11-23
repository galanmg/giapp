package com.giapp.backend.controlador;

import com.giapp.backend.dto.PromocionPeticion;
import com.giapp.backend.dto.PromocionRespuesta;
import com.giapp.backend.servicio.PromocionServicio;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/promociones")
public class PromocionControlador {

    private final PromocionServicio promocionServicio;

    public PromocionControlador(PromocionServicio promocionServicio) {
        this.promocionServicio = promocionServicio;
    }

    @GetMapping
    public List<PromocionRespuesta> listar(@RequestParam(required = false) Long aparcamientoId) {
        return promocionServicio.listar(aparcamientoId);
    }

    @GetMapping("/{id}")
    public PromocionRespuesta buscarPorId(@PathVariable Long id) {
        return promocionServicio.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PromocionRespuesta crear(@Valid @RequestBody PromocionPeticion peticion) {
        return promocionServicio.crear(peticion);
    }

    @PutMapping("/{id}")
    public PromocionRespuesta actualizar(@PathVariable Long id,
                                         @Valid @RequestBody PromocionPeticion peticion) {
        return promocionServicio.actualizar(id, peticion);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        promocionServicio.eliminar(id);
    }

    @PatchMapping("/{id}/activar")
    public PromocionRespuesta activar(@PathVariable Long id) {
        return promocionServicio.cambiarActiva(id, true);
    }

    @PatchMapping("/{id}/desactivar")
    public PromocionRespuesta desactivar(@PathVariable Long id) {
        return promocionServicio.cambiarActiva(id, false);
    }
}
