package com.giapp.backend.modelo;


import com.giapp.backend.modelo.enums.TipoVehiculo;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(
        name = "vehiculo",
        uniqueConstraints = @UniqueConstraint(name = "uk_vehiculo_matricula", columnNames = "matricula")
)
public class Vehiculo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 15)
    private String matricula;

    @Column(nullable = false, length = 50)
    private String marca;

    @Column(nullable = false, length = 50)
    private String modelo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoVehiculo tipo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "propietario_id", foreignKey = @ForeignKey(name = "fk_vehiculo_propietario"))
    private Usuario propietario;

    // Constructor para poder añadir datos de prueba
    public Vehiculo(String matricula, String marca, String modelo, TipoVehiculo tipo, Usuario propietario) {
        this.matricula = matricula;
        this.marca = marca;
        this.modelo = modelo;
        this.tipo = tipo;
        this.propietario = propietario;
    }
}
