package com.giapp.backend.modelo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(
        name = "planta",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_planta_aparcamiento_identificador",
                columnNames = {"aparcamiento_id", "identificador"}
        )
)
public class Planta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String identificador;

    @Column(nullable = false)
    private int nivel;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "aparcamiento_id", foreignKey = @ForeignKey(name = "fk_planta_aparcamiento"))
    private Aparcamiento aparcamiento;

    // Constructor para poder añadir datos de prueba
    public Planta(String identificador, int nivel, Aparcamiento aparcamiento) {
        this.identificador = identificador;
        this.nivel = nivel;
        this.aparcamiento = aparcamiento;
    }
}
