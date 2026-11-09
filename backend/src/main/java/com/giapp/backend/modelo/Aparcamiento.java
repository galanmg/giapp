package com.giapp.backend.modelo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "aparcamiento")
public class Aparcamiento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 200)
    private String direccion;

    @Column(nullable = false, length = 100)
    private String ciudad;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "gestor_id", foreignKey = @ForeignKey(name = "fk_aparcamiento_gestor"))
    private Usuario gestor;

    // Constructor para poder añadir datos de prueba
    public Aparcamiento(String nombre, String direccion, String ciudad, Usuario gestor) {
        this.nombre = nombre;
        this.direccion = direccion;
        this.ciudad = ciudad;
        this.gestor = gestor;
    }
}