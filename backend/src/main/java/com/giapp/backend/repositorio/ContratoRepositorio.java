package com.giapp.backend.repositorio;

import com.giapp.backend.modelo.Aparcamiento;
import com.giapp.backend.modelo.Contrato;
import com.giapp.backend.modelo.Plaza;
import com.giapp.backend.modelo.Usuario;
import com.giapp.backend.modelo.Vehiculo;
import com.giapp.backend.modelo.enums.EstadoContrato;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface ContratoRepositorio extends JpaRepository<Contrato, Long> {

    List<Contrato> findByPlazaAndEstado(Plaza plaza, EstadoContrato estado);

    // Contratos de varias plazas a la vez, para calcular la ocupación de un listado en una consulta
    List<Contrato> findByPlazaInAndEstado(Collection<Plaza> plazas, EstadoContrato estado);

    // Cualquier contrato, también finalizado: la plaza conserva su histórico
    boolean existsByPlaza(Plaza plaza);

    // Contratos de los aparcamientos de un gestor, con filtros opcionales (RF-26)
    @Query("""
            select c from Contrato c
            where c.plaza.planta.aparcamiento.gestor = :gestor
              and (:aparcamientoId is null or c.plaza.planta.aparcamiento.id = :aparcamientoId)
              and (:clienteId is null or c.titular.id = :clienteId)
              and (:estado is null or c.estado = :estado)
            order by c.fechaInicio desc, c.id desc
            """)
    List<Contrato> buscarDeGestor(@Param("gestor") Usuario gestor,
                                  @Param("aparcamientoId") Long aparcamientoId,
                                  @Param("clienteId") Long clienteId,
                                  @Param("estado") EstadoContrato estado);

    List<Contrato> findByTitularOrderByFechaInicioDesc(Usuario titular);

    // ¿Tiene el vehículo contratos en ese estado, en cualquier aparcamiento?
    boolean existsByVehiculoAndEstado(Vehiculo vehiculo, EstadoContrato estado);

    // ¿Tiene el titular contratos en ese estado en alguna plaza de ese aparcamiento?
    boolean existsByTitularAndPlazaPlantaAparcamientoAndEstado(
            Usuario titular, Aparcamiento aparcamiento, EstadoContrato estado);
}
