package com.giapp.backend.modelo;

import com.giapp.backend.modelo.enums.EstadoPlaza;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(
        name = "plaza",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_plaza_planta_numero",
                columnNames = {"planta_id", "numero"}
        )
)

public class Plaza {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 10)
    private String numero;

    @Column(nullable = false)
    private int capacidad;

    @Column(nullable = false)
    private boolean admiteCoche;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPlaza estado = EstadoPlaza.DISPONIBLE;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "planta_id", foreignKey = @ForeignKey(name = "fk_plaza_planta"))
    private Planta planta;

    // Constructor para poder añadir datos de prueba
    public Plaza(String numero, int capacidad, boolean admiteCoche, Planta planta) {
        this.numero = numero;
        this.capacidad = capacidad;
        this.admiteCoche = admiteCoche;
        this.estado = EstadoPlaza.DISPONIBLE;
        this.planta = planta;
    }
}
