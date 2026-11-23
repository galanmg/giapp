package com.giapp.backend.repositorio;

import com.giapp.backend.modelo.Aparcamiento;
import com.giapp.backend.modelo.TarifaAlquiler;
import com.giapp.backend.modelo.enums.TipoVehiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TarifaAlquilerRepositorio extends JpaRepository<TarifaAlquiler, Long> {

    List<TarifaAlquiler> findByAparcamientoOrderByTipoVehiculoAscVigenteDesdeDesc(Aparcamiento aparcamiento);

    boolean existsByAparcamientoAndTipoVehiculoAndVigenteDesde(Aparcamiento aparcamiento,
                                                              TipoVehiculo tipoVehiculo,
                                                              LocalDate vigenteDesde);

    // La tarifa vigente en una fecha es la más reciente que no empieza después de esa fecha
    @Query("""
            select t from TarifaAlquiler t
            where t.aparcamiento = :aparcamiento
              and t.tipoVehiculo = :tipoVehiculo
              and t.vigenteDesde <= :fecha
            order by t.vigenteDesde desc
            limit 1
            """)
    Optional<TarifaAlquiler> buscarVigente(@Param("aparcamiento") Aparcamiento aparcamiento,
                                           @Param("tipoVehiculo") TipoVehiculo tipoVehiculo,
                                           @Param("fecha") LocalDate fecha);
}
