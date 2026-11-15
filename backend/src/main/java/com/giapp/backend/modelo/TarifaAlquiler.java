package com.giapp.backend.modelo;

import com.giapp.backend.modelo.enums.TipoVehiculo;
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
@Table(
        name = "tarifa_alquiler",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_tarifa_aparcamiento_tipo_desde",
                columnNames = {"aparcamiento_id", "tipo_vehiculo", "vigente_desde"}
        )
)

public class TarifaAlquiler {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoVehiculo tipoVehiculo;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal precioMensual;

    @Column(nullable = false)
    private LocalDate vigenteDesde;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "aparcamiento_id", foreignKey = @ForeignKey(name = "fk_tarifa_aparcamiento"))
    private Aparcamiento aparcamiento;

    // Constructor para poder añadir datos de prueba
    public TarifaAlquiler(TipoVehiculo tipoVehiculo, BigDecimal precioMensual, LocalDate vigenteDesde, Aparcamiento aparcamiento) {
        this.tipoVehiculo = tipoVehiculo;
        this.precioMensual = precioMensual;
        this.vigenteDesde = vigenteDesde;
        this.aparcamiento = aparcamiento;
    }
}
