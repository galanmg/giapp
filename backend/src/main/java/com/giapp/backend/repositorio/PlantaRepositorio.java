package com.giapp.backend.repositorio;

import com.giapp.backend.modelo.Aparcamiento;
import com.giapp.backend.modelo.Planta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlantaRepositorio extends JpaRepository<Planta, Long> {

    List<Planta> findByAparcamientoOrderByNivelAsc(Aparcamiento aparcamiento);
}