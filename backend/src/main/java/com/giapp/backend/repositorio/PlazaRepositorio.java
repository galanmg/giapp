package com.giapp.backend.repositorio;

import com.giapp.backend.modelo.Aparcamiento;
import com.giapp.backend.modelo.Planta;
import com.giapp.backend.modelo.Plaza;
import com.giapp.backend.modelo.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlazaRepositorio extends JpaRepository<Plaza, Long> {

    List<Plaza> findByPlantaOrderByNumeroAsc(Planta planta);

    List<Plaza> findByPlantaAparcamientoOrderByPlantaNivelAscNumeroAsc(Aparcamiento aparcamiento);

    // Todas las plazas de los aparcamientos de un gestor
    List<Plaza> findByPlantaAparcamientoGestorOrderByPlantaAparcamientoIdAscPlantaNivelAscNumeroAsc(
            Usuario gestor);

    boolean existsByPlanta(Planta planta);
}
