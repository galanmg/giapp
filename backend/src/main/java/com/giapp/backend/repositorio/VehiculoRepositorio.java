package com.giapp.backend.repositorio;

import com.giapp.backend.modelo.Usuario;
import com.giapp.backend.modelo.Vehiculo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VehiculoRepositorio extends JpaRepository<Vehiculo, Long> {

    List<Vehiculo> findByPropietarioOrderByMatriculaAsc(Usuario propietario);

    // La matrícula es única por propietario
    boolean existsByPropietarioAndMatricula(Usuario propietario, String matricula);

    boolean existsByPropietarioAndMatriculaAndIdNot(Usuario propietario, String matricula, Long id);
}
