package com.giapp.backend.config;

import com.giapp.backend.modelo.*;
import com.giapp.backend.modelo.enums.Periodicidad;
import com.giapp.backend.modelo.enums.Rol;
import com.giapp.backend.modelo.enums.TipoVehiculo;
import com.giapp.backend.repositorio.AparcamientoRepositorio;
import com.giapp.backend.repositorio.ContratoRepositorio;
import com.giapp.backend.repositorio.PlantaRepositorio;
import com.giapp.backend.repositorio.PlazaRepositorio;
import com.giapp.backend.repositorio.TarifaAlquilerRepositorio;
import com.giapp.backend.repositorio.UsuarioRepositorio;
import com.giapp.backend.repositorio.VehiculoRepositorio;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepositorio usuarioRepositorio;
    private final AparcamientoRepositorio aparcamientoRepositorio;
    private final PlantaRepositorio plantaRepositorio;
    private final PlazaRepositorio plazaRepositorio;
    private final VehiculoRepositorio vehiculoRepositorio;
    private final TarifaAlquilerRepositorio tarifaAlquilerRepositorio;
    private final ContratoRepositorio contratoRepositorio;

    public DataInitializer(UsuarioRepositorio usuarioRepositorio,
                           AparcamientoRepositorio aparcamientoRepositorio,
                           PlantaRepositorio plantaRepositorio,
                           PlazaRepositorio plazaRepositorio,
                           VehiculoRepositorio vehiculoRepositorio,
                           TarifaAlquilerRepositorio tarifaAlquilerRepositorio,
                           ContratoRepositorio contratoRepositorio) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.aparcamientoRepositorio = aparcamientoRepositorio;
        this.plantaRepositorio = plantaRepositorio;
        this.plazaRepositorio = plazaRepositorio;
        this.vehiculoRepositorio = vehiculoRepositorio;
        this.tarifaAlquilerRepositorio = tarifaAlquilerRepositorio;
        this.contratoRepositorio = contratoRepositorio;
    }

    @Override
    public void run(String... args) {


        if (usuarioRepositorio.count() > 0) {
            System.out.println("La base de datos ya tiene datos. No se cargan los de prueba.");
            return;
        }

        System.out.println("Cargando datos de prueba...");

        // Usuarios. La contraseña se cifrará cuando esté Spring Security
        Usuario gestor = usuarioRepositorio.save(
                new Usuario("Andrés", "Moreno", "gestor@giapp.com", "pendiente-de-cifrar", "600111222", Rol.GESTOR));

        Usuario laura = usuarioRepositorio.save(
                new Usuario("Laura", "Ortega", "laura@giapp.com", "pendiente-de-cifrar", "600333444", Rol.CLIENTE));

        Usuario miguel = usuarioRepositorio.save(
                new Usuario("Miguel", "Ruiz", "miguel@giapp.com", "pendiente-de-cifrar", "600555666", Rol.CLIENTE));

        // Aparcamiento
        Aparcamiento juanBosco = aparcamientoRepositorio.save(
                new Aparcamiento("Juan Bosco", "Calle Mayor 10", "Ciudad Real", gestor));

        // Plantas
        Planta sotano = plantaRepositorio.save(new Planta("Sótano 1", -1, juanBosco));
        Planta baja = plantaRepositorio.save(new Planta("Planta baja", 0, juanBosco));

        // Plazas. La capacidad va en medias plazas: 2 es una plaza normal, 1 da hueco para una moto
        Plaza s101 = plazaRepositorio.save(new Plaza("S1-01", 2, true, sotano));
        Plaza s102 = plazaRepositorio.save(new Plaza("S1-02", 2, true, sotano));
        plazaRepositorio.save(new Plaza("S1-03", 2, false, sotano));
        plazaRepositorio.save(new Plaza("S1-04", 1, false, sotano));
        plazaRepositorio.save(new Plaza("PB-01", 2, true, baja));
        plazaRepositorio.save(new Plaza("PB-02", 2, true, baja));

        // Vehículos
        Vehiculo peugeot306 = vehiculoRepositorio.save(
                new Vehiculo("CR 3450 X", "Peugeot", "306", TipoVehiculo.COCHE, laura));

        Vehiculo scooter = vehiculoRepositorio.save(
                new Vehiculo("1234 ABC", "Honda", "PCX 125", TipoVehiculo.MOTO, miguel));

        // No lleva contrato porque es el coche con el que se probará la regla de capacidad
        vehiculoRepositorio.save(
                new Vehiculo("7565 JLM", "Peugeot", "208", TipoVehiculo.COCHE, miguel));

        // Tarifas vigentes desde el 1 de enero
        tarifaAlquilerRepositorio.save(new TarifaAlquiler(
                TipoVehiculo.COCHE, new BigDecimal("45.00"), LocalDate.of(2026, 1, 1), juanBosco));

        tarifaAlquilerRepositorio.save(new TarifaAlquiler(
                TipoVehiculo.MOTO, new BigDecimal("25.00"), LocalDate.of(2026, 1, 1), juanBosco));

        // Contratos activos
        // S1-01 queda llena: un coche ocupa las 2 medias plazas
        contratoRepositorio.save(new Contrato(
                LocalDate.of(2026, 3, 1), Periodicidad.MENSUAL, new BigDecimal("45.00"),
                laura, s101, peugeot306));

        // S1-02 queda a medias: una moto ocupa 1 de 2, sobra media plaza
        contratoRepositorio.save(new Contrato(
                LocalDate.of(2026, 4, 1), Periodicidad.MENSUAL, new BigDecimal("25.00"),
                miguel, s102, scooter));

        System.out.println("Datos de prueba cargados.");
    }
}
