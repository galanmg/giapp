package com.giapp.backend.servicio;

import com.giapp.backend.dto.PlazaLotePeticion;
import com.giapp.backend.dto.PlazaPeticion;
import com.giapp.backend.dto.PlazaRespuesta;
import com.giapp.backend.modelo.Contrato;
import com.giapp.backend.modelo.Planta;
import com.giapp.backend.modelo.Plaza;
import com.giapp.backend.modelo.enums.EstadoContrato;
import com.giapp.backend.modelo.enums.EstadoPlaza;
import com.giapp.backend.modelo.enums.TipoVehiculo;
import com.giapp.backend.repositorio.ContratoRepositorio;
import com.giapp.backend.repositorio.PlazaRepositorio;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

// Plazas de los aparcamientos del gestor autenticado (RF-13, RF-15, RF-16, RNF-18)
@Service
public class PlazaServicio {

    // Límite de seguridad del alta en lote, contra errores al teclear el rango
    private static final int MAXIMO_POR_LOTE = 100;

    // Longitud de la columna numero de la tabla plaza
    private static final int LONGITUD_NUMERO = 10;

    private final PlazaRepositorio plazaRepositorio;
    private final ContratoRepositorio contratoRepositorio;
    private final PlantaServicio plantaServicio;
    private final AparcamientoServicio aparcamientoServicio;
    private final UsuarioActualServicio usuarioActualServicio;

    public PlazaServicio(PlazaRepositorio plazaRepositorio,
                         ContratoRepositorio contratoRepositorio,
                         PlantaServicio plantaServicio,
                         AparcamientoServicio aparcamientoServicio,
                         UsuarioActualServicio usuarioActualServicio) {
        this.plazaRepositorio = plazaRepositorio;
        this.contratoRepositorio = contratoRepositorio;
        this.plantaServicio = plantaServicio;
        this.aparcamientoServicio = aparcamientoServicio;
        this.usuarioActualServicio = usuarioActualServicio;
    }

    // Filtros opcionales: si llegan los dos, la planta tiene que ser de ese aparcamiento
    @Transactional(readOnly = true)
    public List<PlazaRespuesta> listar(Long plantaId, Long aparcamientoId) {
        List<Plaza> plazas;

        if (plantaId != null) {
            Planta planta = plantaServicio.buscarPropia(plantaId);

            if (aparcamientoId != null && !planta.getAparcamiento().getId().equals(aparcamientoId)) {
                return List.of();
            }

            plazas = plazaRepositorio.findByPlantaOrderByNumeroAsc(planta);
        } else if (aparcamientoId != null) {
            plazas = plazaRepositorio.findByPlantaAparcamientoOrderByPlantaNivelAscNumeroAsc(
                    aparcamientoServicio.buscarPropio(aparcamientoId));
        } else {
            plazas = plazaRepositorio
                    .findByPlantaAparcamientoGestorOrderByPlantaAparcamientoIdAscPlantaNivelAscNumeroAsc(
                            usuarioActualServicio.obtener());
        }

        Map<Long, Integer> ocupacion = ocupacionPorPlaza(plazas);

        return plazas.stream()
                .map(plaza -> aRespuesta(plaza, ocupacion.getOrDefault(plaza.getId(), 0)))
                .toList();
    }

    @Transactional(readOnly = true)
    public PlazaRespuesta buscarPorId(Long id) {
        Plaza plaza = buscarPropia(id);

        return aRespuesta(plaza, mediasOcupadas(contratosActivos(plaza)));
    }

    @Transactional
    public PlazaRespuesta crear(PlazaPeticion peticion) {
        Planta planta = plantaServicio.buscarPropia(peticion.plantaId());

        Plaza plaza = new Plaza(
                peticion.numero(), peticion.capacidad(), peticion.admiteCoche(), planta);

        return aRespuesta(plazaRepositorio.save(plaza), 0);
    }

    // RF-14. Todo o nada: si algún número ya existe en la planta, no se crea ninguna
    // plaza y el 409 dice cuáles chocan. @Transactional deshace todo si algo falla.
    @Transactional
    public List<PlazaRespuesta> crearLote(PlazaLotePeticion peticion) {
        if (peticion.numeroInicial() > peticion.numeroFinal()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El número inicial no puede ser mayor que el final");
        }

        int cantidad = peticion.numeroFinal() - peticion.numeroInicial() + 1;

        if (cantidad > MAXIMO_POR_LOTE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Como mucho se crean " + MAXIMO_POR_LOTE + " plazas por lote; se han pedido " + cantidad);
        }

        Planta planta = plantaServicio.buscarPropia(peticion.plantaId());

        // Número con al menos dos cifras: S1-07, S1-08... S1-100
        List<String> numeros = IntStream.rangeClosed(peticion.numeroInicial(), peticion.numeroFinal())
                .mapToObj(n -> peticion.prefijo() + String.format("%02d", n))
                .toList();

        // El último es el más largo: si cabe en la columna, caben todos
        String masLargo = numeros.get(numeros.size() - 1);

        if (masLargo.length() > LONGITUD_NUMERO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El número " + masLargo + " supera los " + LONGITUD_NUMERO + " caracteres");
        }

        List<String> repetidos = plazaRepositorio.findByPlantaAndNumeroIn(planta, numeros)
                .stream()
                .map(Plaza::getNumero)
                .sorted()
                .toList();

        if (!repetidos.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No se ha creado ninguna plaza: ya existen en la planta " + planta.getIdentificador()
                            + " las plazas " + String.join(", ", repetidos));
        }

        List<Plaza> nuevas = numeros.stream()
                .map(numero -> new Plaza(numero, peticion.capacidad(), peticion.admiteCoche(), planta))
                .toList();

        return plazaRepositorio.saveAll(nuevas)
                .stream()
                .map(plaza -> aRespuesta(plaza, 0))
                .toList();
    }

    // Los cambios no pueden dejar fuera a los contratos activos que ya tiene la plaza
    @Transactional
    public PlazaRespuesta actualizar(Long id, PlazaPeticion peticion) {
        Plaza plaza = buscarPropia(id);
        List<Contrato> activos = contratosActivos(plaza);
        int ocupadas = mediasOcupadas(activos);

        if (peticion.capacidad() < ocupadas) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "La plaza " + plaza.getNumero() + " tiene " + ocupadas
                            + " medias plazas ocupadas: la capacidad no puede ser menor");
        }

        boolean tieneCoche = activos.stream()
                .anyMatch(contrato -> contrato.getVehiculo().getTipo() == TipoVehiculo.COCHE);

        if (!peticion.admiteCoche() && tieneCoche) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "La plaza " + plaza.getNumero() + " tiene un coche con contrato activo: "
                            + "no se le puede quitar que admita coches");
        }

        plaza.setNumero(peticion.numero());
        plaza.setCapacidad(peticion.capacidad());
        plaza.setAdmiteCoche(peticion.admiteCoche());
        plaza.setPlanta(plantaServicio.buscarPropia(peticion.plantaId()));

        return aRespuesta(plaza, ocupadas);
    }

    @Transactional
    public void eliminar(Long id) {
        Plaza plaza = buscarPropia(id);

        if (contratoRepositorio.existsByPlaza(plaza)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No se puede eliminar la plaza " + plaza.getNumero()
                            + " porque tiene contratos. Ponla fuera de servicio");
        }

        plazaRepositorio.delete(plaza);
    }

    // RF-15. No se puede sacar de servicio una plaza con contratos activos.
    @Transactional
    public PlazaRespuesta cambiarEstado(Long id, EstadoPlaza estado) {
        Plaza plaza = buscarPropia(id);
        int ocupadas = mediasOcupadas(contratosActivos(plaza));

        if (estado == EstadoPlaza.FUERA_DE_SERVICIO && ocupadas > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No se puede poner fuera de servicio la plaza " + plaza.getNumero()
                            + " porque tiene contratos activos");
        }

        plaza.setEstado(estado);

        return aRespuesta(plaza, ocupadas);
    }

    // 404 si no existe; 403 si es de un aparcamiento de otro gestor.
    // Público para que lo use el servicio de contratos.
    public Plaza buscarPropia(Long id) {
        Plaza plaza = plazaRepositorio.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No existe la plaza con id " + id));

        plantaServicio.buscarPropia(plaza.getPlanta().getId());

        return plaza;
    }

    // Medias plazas ocupadas por los contratos activos de una plaza.
    // Público para que el servicio de contratos no duplique el cálculo.
    public int mediasOcupadas(Plaza plaza) {
        return mediasOcupadas(contratosActivos(plaza));
    }

    // Métodos internos

    private List<Contrato> contratosActivos(Plaza plaza) {
        return contratoRepositorio.findByPlazaAndEstado(plaza, EstadoContrato.ACTIVO);
    }

    // La ocupación de cada contrato la da Contrato.ocupacion(): no se duplica la regla
    private int mediasOcupadas(List<Contrato> contratos) {
        return contratos.stream()
                .mapToInt(Contrato::ocupacion)
                .sum();
    }

    // Una sola consulta para todas las plazas del listado, en lugar de una por plaza
    private Map<Long, Integer> ocupacionPorPlaza(List<Plaza> plazas) {
        if (plazas.isEmpty()) {
            return Map.of();
        }

        return contratoRepositorio.findByPlazaInAndEstado(plazas, EstadoContrato.ACTIVO)
                .stream()
                .collect(Collectors.groupingBy(
                        contrato -> contrato.getPlaza().getId(),
                        Collectors.summingInt(Contrato::ocupacion)));
    }

    private PlazaRespuesta aRespuesta(Plaza plaza, int mediasOcupadas) {
        return new PlazaRespuesta(
                plaza.getId(),
                plaza.getNumero(),
                plaza.getCapacidad(),
                plaza.isAdmiteCoche(),
                plaza.getEstado().name(),
                plaza.getPlanta().getIdentificador(),
                mediasOcupadas,
                plaza.situacion(mediasOcupadas));
    }
}
