package com.giapp.backend.modelo;

import com.giapp.backend.modelo.enums.EstadoContrato;
import com.giapp.backend.modelo.enums.Periodicidad;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "contrato")

public class Contrato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate fechaInicio;

    @Column
    private LocalDate fechaFin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Periodicidad periodicidad;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal precioMensual;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoContrato estado = EstadoContrato.ACTIVO;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "titular_id", foreignKey = @ForeignKey(name = "fk_contrato_titular"))
    private Usuario titular;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plaza_id", foreignKey = @ForeignKey(name = "fk_contrato_plaza"))
    private Plaza plaza;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehiculo_id", foreignKey = @ForeignKey(name = "fk_contrato_vehiculo"))
    private Vehiculo vehiculo;

    // Constructor para poder añadir datos de prueba
    public Contrato(LocalDate fechaInicio, Periodicidad periodicidad, BigDecimal precioMensual,
                    Usuario titular, Plaza plaza, Vehiculo vehiculo) {
        this.fechaInicio = fechaInicio;
        this.periodicidad = periodicidad;
        this.precioMensual = precioMensual;
        this.titular = titular;
        this.plaza = plaza;
        this.vehiculo = vehiculo;
        this.estado = EstadoContrato.ACTIVO;
    }

    // La ocupacion se mide en medias plazas: un coche ocupa 2 y una moto 1
    public int ocupacion() {
        return vehiculo.getTipo().getMediasPlazas();
    }
}
