package com.giapp.backend.repositorio;

import com.giapp.backend.modelo.Planta;
import com.giapp.backend.modelo.Plaza;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlazaRepositorio extends JpaRepository<Plaza, Long> {

    List<Plaza> findByPlanta(Planta planta);
}