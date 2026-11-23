package com.giapp.backend.servicio;

import com.giapp.backend.dto.ClienteAltaPeticion;
import com.giapp.backend.dto.ClienteAparcamientoRespuesta;
import com.giapp.backend.dto.RegistroPeticion;
import com.giapp.backend.dto.UsuarioRespuesta;
import com.giapp.backend.modelo.Aparcamiento;
import com.giapp.backend.modelo.ClienteAparcamiento;
import com.giapp.backend.modelo.Usuario;
import com.giapp.backend.modelo.enums.EstadoContrato;
import com.giapp.backend.modelo.enums.Rol;
import com.giapp.backend.repositorio.ClienteAparcamientoRepositorio;
import com.giapp.backend.repositorio.ContratoRepositorio;
import com.giapp.backend.repositorio.UsuarioRepositorio;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

/**
 * Clientes y su vínculo con los aparcamientos.
 *
 * Las cuentas son únicas en toda la aplicación, pero cada gestor solo ve a los
 * clientes vinculados a sus aparcamientos (RNF-18). Al resto solo puede
 * encontrarlos por su email exacto, para vincularlos (RF-55).
 */
@Service
public class ClienteServicio {

    private final UsuarioRepositorio usuarioRepositorio;
    private final ClienteAparcamientoRepositorio clienteAparcamientoRepositorio;
    private final ContratoRepositorio contratoRepositorio;
    private final AparcamientoServicio aparcamientoServicio;
    private final UsuarioActualServicio usuarioActualServicio;
    private final PasswordEncoder passwordEncoder;

    public ClienteServicio(UsuarioRepositorio usuarioRepositorio,
                           ClienteAparcamientoRepositorio clienteAparcamientoRepositorio,
                           ContratoRepositorio contratoRepositorio,
                           AparcamientoServicio aparcamientoServicio,
                           UsuarioActualServicio usuarioActualServicio,
                           PasswordEncoder passwordEncoder) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.clienteAparcamientoRepositorio = clienteAparcamientoRepositorio;
        this.contratoRepositorio = contratoRepositorio;
        this.aparcamientoServicio = aparcamientoServicio;
        this.usuarioActualServicio = usuarioActualServicio;
        this.passwordEncoder = passwordEncoder;
    }

    // Registro público (RF-54): la cuenta se crea sin vincular a ningún aparcamiento
    @Transactional
    public UsuarioRespuesta registrar(RegistroPeticion peticion) {
        return aRespuesta(crearUsuario(peticion.nombre(), peticion.apellidos(),
                peticion.email(), peticion.password(), peticion.telefono()));
    }

    // Alta por el gestor de un cliente sin cuenta (RF-01), vinculado a su aparcamiento
    @Transactional
    public UsuarioRespuesta crear(ClienteAltaPeticion peticion) {
        Aparcamiento aparcamiento = aparcamientoServicio.buscarPropio(peticion.aparcamientoId());

        Usuario cliente = crearUsuario(peticion.nombre(), peticion.apellidos(),
                peticion.email(), peticion.password(), peticion.telefono());

        clienteAparcamientoRepositorio.save(
                new ClienteAparcamiento(cliente, aparcamiento, LocalDate.now()));

        return aRespuesta(cliente);
    }

    // Clientes activos de mis aparcamientos (RF-06). Filtros opcionales.
    @Transactional(readOnly = true)
    public List<UsuarioRespuesta> listar(String texto, Long aparcamientoId) {
        if (aparcamientoId != null) {
            aparcamientoServicio.buscarPropio(aparcamientoId);
        }

        String filtro = texto == null ? "" : texto.trim();

        return clienteAparcamientoRepositorio
                .buscarClientesDeGestor(usuarioActualServicio.obtener(), aparcamientoId, filtro)
                .stream()
                .map(this::aRespuesta)
                .toList();
    }

    // Búsqueda por email exacto de cualquier cliente registrado (RF-55)
    @Transactional(readOnly = true)
    public UsuarioRespuesta buscarPorEmail(String email) {
        return usuarioRepositorio.findByEmailAndRol(email.trim(), Rol.CLIENTE)
                .map(this::aRespuesta)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No hay ningún cliente registrado con el email " + email));
    }

    @Transactional(readOnly = true)
    public UsuarioRespuesta buscarPorId(Long id) {
        return aRespuesta(buscarClienteDeGestor(id));
    }

    // Vincular a un usuario registrado con uno de mis aparcamientos (RF-55).
    // Si ya estuvo y se dio de baja, se reactiva el mismo vínculo.
    @Transactional
    public ClienteAparcamientoRespuesta vincular(Long aparcamientoId, Long clienteId) {
        Aparcamiento aparcamiento = aparcamientoServicio.buscarPropio(aparcamientoId);
        Usuario cliente = buscarCliente(clienteId);

        ClienteAparcamiento vinculo = clienteAparcamientoRepositorio
                .findByClienteAndAparcamiento(cliente, aparcamiento)
                .orElse(null);

        if (vinculo == null) {
            vinculo = clienteAparcamientoRepositorio.save(
                    new ClienteAparcamiento(cliente, aparcamiento, LocalDate.now()));
        } else if (vinculo.isActivo()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El cliente ya está dado de alta en el aparcamiento " + aparcamiento.getNombre());
        } else {
            vinculo.setActivo(true);
            vinculo.setFechaAlta(LocalDate.now());
        }

        return aRespuesta(vinculo);
    }

    // Baja del cliente en uno de mis aparcamientos (RF-06). No toca su cuenta.
    @Transactional
    public ClienteAparcamientoRespuesta darDeBaja(Long aparcamientoId, Long clienteId) {
        Aparcamiento aparcamiento = aparcamientoServicio.buscarPropio(aparcamientoId);
        Usuario cliente = buscarCliente(clienteId);

        ClienteAparcamiento vinculo = clienteAparcamientoRepositorio
                .findByClienteAndAparcamiento(cliente, aparcamiento)
                .filter(ClienteAparcamiento::isActivo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "El cliente no está dado de alta en el aparcamiento " + aparcamiento.getNombre()));

        if (contratoRepositorio.existsByTitularAndPlazaPlantaAparcamientoAndEstado(
                cliente, aparcamiento, EstadoContrato.ACTIVO)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No se puede dar de baja a un cliente con contratos activos en este aparcamiento");
        }

        vinculo.setActivo(false);

        return aRespuesta(vinculo);
    }

    // Cliente vinculado a alguno de mis aparcamientos: 404 si no existe, 403 si no es mío.
    // Público para que lo usen otros servicios (vehículos, contratos).
    public Usuario buscarClienteDeGestor(Long id) {
        Usuario cliente = buscarCliente(id);

        if (!clienteAparcamientoRepositorio.existsByClienteAndAparcamientoGestorAndActivoTrue(
                cliente, usuarioActualServicio.obtener())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "El usuario con id " + id + " no es cliente de tus aparcamientos");
        }

        return cliente;
    }

    // Métodos internos

    // Las cuentas nuevas son siempre de rol CLIENTE, se creen como se creen
    private Usuario crearUsuario(String nombre, String apellidos, String email,
                                 String password, String telefono) {
        if (usuarioRepositorio.existsByEmail(email)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Ya existe un usuario con el email " + email);
        }

        return usuarioRepositorio.save(new Usuario(
                nombre, apellidos, email, passwordEncoder.encode(password), telefono, Rol.CLIENTE));
    }

    // Un id que existe pero es de un gestor se trata como inexistente
    private Usuario buscarCliente(Long id) {
        return usuarioRepositorio.findByIdAndRol(id, Rol.CLIENTE)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No existe el cliente con id " + id));
    }

    private UsuarioRespuesta aRespuesta(Usuario usuario) {
        return new UsuarioRespuesta(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getApellidos(),
                usuario.getEmail(),
                usuario.getTelefono(),
                usuario.getRol().name(),
                usuario.isActivo());
    }

    private ClienteAparcamientoRespuesta aRespuesta(ClienteAparcamiento vinculo) {
        Usuario cliente = vinculo.getCliente();
        Aparcamiento aparcamiento = vinculo.getAparcamiento();

        return new ClienteAparcamientoRespuesta(
                vinculo.getId(),
                cliente.getId(),
                cliente.getNombre() + " " + cliente.getApellidos(),
                cliente.getEmail(),
                aparcamiento.getId(),
                aparcamiento.getNombre(),
                vinculo.getFechaAlta(),
                vinculo.isActivo());
    }
}
