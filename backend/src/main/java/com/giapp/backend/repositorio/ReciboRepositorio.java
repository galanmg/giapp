package com.giapp.backend.repositorio;

import com.giapp.backend.modelo.Contrato;
import com.giapp.backend.modelo.Recibo;
import com.giapp.backend.modelo.Usuario;
import com.giapp.backend.modelo.enums.EstadoRecibo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface ReciboRepositorio extends JpaRepository<Recibo, Long> {

    // Un mismo periodo de un contrato no se cobra dos veces
    boolean existsByContratoAndPeriodoInicio(Contrato contrato, LocalDate periodoInicio);

    // Recibos de los aparcamientos de un gestor, con filtros opcionales (RF-34)
    @Query("""
            select r from Recibo r
            where r.contrato.plaza.planta.aparcamiento.gestor = :gestor
              and (:contratoId is null or r.contrato.id = :contratoId)
              and (:clienteId is null or r.contrato.titular.id = :clienteId)
              and (:estado is null or r.estado = :estado)
              and (:aparcamientoId is null or r.contrato.plaza.planta.aparcamiento.id = :aparcamientoId)
            order by r.fechaEmision desc, r.id desc
            """)
    List<Recibo> buscarDeGestor(@Param("gestor") Usuario gestor,
                                @Param("contratoId") Long contratoId,
                                @Param("clienteId") Long clienteId,
                                @Param("estado") EstadoRecibo estado,
                                @Param("aparcamientoId") Long aparcamientoId);

    // Histórico de un cliente (RF-36)
    List<Recibo> findByContratoTitularOrderByFechaEmisionDescIdDesc(Usuario titular);

    // Recibos vencidos sin pagar (RF-33): de todos, o solo los de un gestor
    List<Recibo> findByEstadoAndFechaVencimientoBefore(EstadoRecibo estado, LocalDate fecha);

    List<Recibo> findByEstadoAndFechaVencimientoBeforeAndContratoPlazaPlantaAparcamientoGestor(
            EstadoRecibo estado, LocalDate fecha, Usuario gestor);

    // Ingresos (RF-35): lo cobrado con fecha de pago en el rango, ambos días incluidos.
    // coalesce para que un rango sin pagos sume 0 y no null.
    @Query("""
            select coalesce(sum(r.importeTotal), 0) from Recibo r
            where r.contrato.plaza.planta.aparcamiento.gestor = :gestor
              and r.estado = com.giapp.backend.modelo.enums.EstadoRecibo.PAGADO
              and r.fechaPago between :desde and :hasta
            """)
    BigDecimal sumarIngresos(@Param("gestor") Usuario gestor,
                             @Param("desde") LocalDate desde,
                             @Param("hasta") LocalDate hasta);

    long countByContratoPlazaPlantaAparcamientoGestorAndEstadoAndFechaPagoBetween(
            Usuario gestor, EstadoRecibo estado, LocalDate desde, LocalDate hasta);
}
