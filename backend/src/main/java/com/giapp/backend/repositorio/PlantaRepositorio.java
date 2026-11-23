package com.giapp.backend.repositorio;

import com.giapp.backend.modelo.Aparcamiento;
import com.giapp.backend.modelo.Planta;
import com.giapp.backend.modelo.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlantaRepositorio extends JpaRepository<Planta, Long> {

    List<Planta> findByAparcamientoOrderByNivelAsc(Aparcamiento aparcamiento);

    // Todas las plantas de los aparcamientos de un gestor, agrupadas por aparcamiento
    List<Planta> findByAparcamientoGestorOrderByAparcamientoIdAscNivelAsc(Usuario gestor);

    // El identificador es único dentro de cada aparcamiento
    boolean existsByAparcamientoAndIdentificador(Aparcamiento aparcamiento, String identificador);

    boolean existsByAparcamientoAndIdentificadorAndIdNot(Aparcamiento aparcamiento,
                                                        String identificador, Long id);
}
