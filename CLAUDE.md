# GIAPP · Guía para asistentes de IA

Este archivo es el contexto que debe leer cualquier IA que trabaje en este
repositorio. Léelo entero antes de tocar nada.

## Qué es GIAPP

Aplicación de gestión de aparcamientos **en régimen de alquiler mensual**, no de
rotación. El gestor asigna una plaza fija a un abonado mediante un contrato, y
esa plaza es suya hasta que el contrato termina. Solo el gestor puede cambiar
una asignación.

Proyecto Intermodular del CFGS de Desarrollo de Aplicaciones Multiplataforma
(DAM), IES Juan Bosco, curso 2026/2027. Autor: Andrés Moreno.

## Cómo trabajar conmigo

Estas reglas vienen del enunciado del módulo y no son negociables:

1. **Antes de escribir código, analiza el problema y propón varias formas de
   resolverlo**, con sus ventajas e inconvenientes. Yo decido cuál se
   implementa.
2. **Explica antes de picar.** El objetivo del proyecto es que yo aprenda, no
   que el código aparezca. Si generas código, explica qué hace y por qué está
   así.
3. **No inventes requisitos ni entidades.** Si algo no está en
   `docs/requisitos.md` o en los diagramas de `docs/uml/`, pregúntame antes de
   añadirlo.
4. **Un cambio, un commit.** No mezcles cosas distintas en el mismo commit: el
   historial progresivo forma parte de la evaluación.
5. **Si mi petición ya trae la decisión tomada, impleméntala directamente**, sin
   volver a proponer alternativas. Las reglas 1 y 2 siguen valiendo para todo lo
   que la petición no resuelva: si te encuentras una decisión que no está
   tomada, para y pregúntame.

### Cierre de cada tarea

Al terminar cada tarea, y antes de pasar a la siguiente:

1. Compila y ejecuta las pruebas (`./mvnw test` desde `backend/`). Docker y
   MySQL tienen que estar levantados.
2. Resúmeme en tres líneas qué has hecho y por qué. Lo uso para preparar la
   defensa, así que tiene que poder entenderlo sin leer el código.
3. Haz `git add -A`, revisa con `git status` que solo entra lo de esta tarea, y
   haz un commit con un mensaje descriptivo en español.
4. **Pregúntame si hago push** y súbelo solo si te digo que sí, nunca por tu
   cuenta. Así cada tarea pasa por GitHub Actions por separado y, si algo sale
   en rojo, se sabe qué tarea lo ha roto.
5. Después del push, comprueba con `gh run list` / `gh run watch` que GitHub
   Actions sale en verde. Si sale en rojo, léeme el error y arréglalo antes de
   seguir.

## Reglas de negocio que no son obvias

Estas cuatro son lo que distingue al proyecto. Si las implementas mal, el
proyecto pierde su sentido.

### Cada aparcamiento es un espacio independiente

- Los usuarios son únicos en toda la aplicación (email único). Cualquiera puede
  registrarse como cliente (RF-54), o le da de alta un gestor (RF-01).
- Un cliente pertenece a un aparcamiento mediante `ClienteAparcamiento`, no a un
  gestor. Puede estar vinculado a varios aparcamientos, de gestores distintos.
- El gestor encuentra a un usuario solo por su **email exacto** y lo vincula a
  uno de sus aparcamientos (RF-55). Nunca puede listar usuarios que no sean
  clientes de sus aparcamientos.
- Cada gestor solo ve y modifica sus aparcamientos y los clientes vinculados a
  ellos (RNF-18). Sobre datos de otro gestor: 403.
- El gestor da de baja a un cliente **en un aparcamiento** (`activo` de
  `ClienteAparcamiento`), nunca desactiva su cuenta (`Usuario.activo`): eso le
  dejaría fuera de todos sus aparcamientos. Los datos personales los edita el
  propio usuario desde su perfil.
- Un vehículo pertenece a un usuario. La matrícula es única por propietario, no
  global: un coche vendido puede registrarlo su nuevo dueño.
- Un contrato en un aparcamiento exige que el cliente esté vinculado y activo en
  él. No se puede dar de baja a un cliente con contratos activos en ese
  aparcamiento.

### Medias plazas

La unidad de ocupación **no es la plaza, es la media plaza**.

- Un coche ocupa 2 medias plazas, una moto ocupa 1.
- Cada `Plaza` tiene `capacidad` (entero, en medias plazas) y `admiteCoche`
  (booleano), que el gestor configura una por una.
- Una plaza normal: capacidad 2, admite coche → un coche o dos motos.
- Una plaza amplia con mal acceso: capacidad 2, no admite coche → dos motos.
- Un recoveco: capacidad 1 → una moto.
- **Restricción**: la suma de la ocupación de los contratos activos de una
  plaza nunca puede superar su capacidad.

La regla está implementada en `Plaza.admite(TipoVehiculo, int mediasOcupadas)`,
y la ocupación de un contrato en `Contrato.ocupacion()`, que delega en
`TipoVehiculo.getMediasPlazas()`. Reutilízalos; no dupliques la regla.

### Periodos de facturación alineados al calendario

La periodicidad de pago (mensual, trimestral, semestral o anual) se elige por
contrato, pero **los periodos se alinean al calendario natural, no a la fecha de
alta**. Todos los contratos semestrales facturan en enero y en julio; los
trimestrales, en enero, abril, julio y octubre; los anuales, en enero.

**Se factura siempre por meses completos. Nunca se prorratea por días.**

- `Contrato.fechaInicio` es la **fecha real de entrada** del cliente, y es la
  que cuenta para la ocupación de la plaza.
- La facturación empieza el **día 1 del mes siguiente** a la entrada, o ese
  mismo mes si la entrada es un día 1. Esa fecha se calcula, no se guarda.
- Los días entre la entrada y el inicio de la facturación no se cobran: son de
  cortesía. Decisión del negocio: se prefiere afianzar clientes a cobrarles días
  sueltos.
- El primer recibo cubre los meses completos que faltan desde el inicio de la
  facturación hasta el cierre del periodo natural en curso.
- Si un cliente se va antes de que acabe el periodo pagado, no se le devuelve
  nada.

Ejemplos con pago semestral:

| Entrada | Inicio de facturación | Primer recibo | Siguiente |
|---|---|---|---|
| 1 de marzo | 1 de marzo | marzo a junio (4 meses) | julio a diciembre |
| 17 de marzo | 1 de abril | abril a junio (3 meses) | julio a diciembre |
| 1 de julio | 1 de julio | julio a diciembre (6 meses) | enero a junio |
| 31 de diciembre | 1 de enero del año siguiente | enero a junio (6 meses) | julio a diciembre |

El importe de un recibo es `precioMensual × número de meses`.

Este cálculo debe ser determinista y estar cubierto por pruebas unitarias
(RNF-14), incluido el cambio de año.

### Provisión y regularización eléctrica

Las plazas pueden tener enchufes, cada uno con su contador. El consumo se cobra
como lo hacen las eléctricas:

- Cada recibo incluye una **provisión**: una estimación cobrada por adelantado.
- El recibo siguiente incluye la **regularización** del periodo anterior: la
  diferencia entre lo que se provisionó y lo que de verdad se consumió, en
  positivo o en negativo.
- El consumo real se calcula restando dos lecturas del contador.
- El primer recibo de un contrato no lleva regularización.

**Este módulo es una ampliación opcional y no está implementado.** No añadas
campos eléctricos a ninguna entidad.

## Modelo de datos

Catorce entidades en el diseño. El diagrama está en
`docs/uml/diagrama-clases.puml`; los enumerados, en
`docs/uml/diagrama-clases-enums.puml`.

| Entidad | Estado | Notas |
|---|---|---|
| `Usuario` | Implementada | Una sola tabla con un campo `rol` (GESTOR o CLIENTE), no herencia |
| `Vehiculo` | Implementada | Tipo COCHE o MOTO. Matrícula única por propietario |
| `Aparcamiento` | Implementada | Un gestor puede tener varios |
| `ClienteAparcamiento` | Implementada | Vínculo cliente-aparcamiento, con `fechaAlta` y `activo` |
| `Planta` | Implementada | Compuesta en Aparcamiento |
| `Plaza` | Implementada | `capacidad` y `admiteCoche`; ver medias plazas |
| `TarifaAlquiler` | Implementada | Por tipo de vehículo, con `vigenteDesde` |
| `Contrato` | Implementada | El núcleo: cliente, plaza, vehículo, periodicidad |
| `Recibo` | Implementada | Sin los campos eléctricos del diagrama; con `formaPago` (RF-32) |
| `Promocion` | Pendiente | Código, descuento y caducidad |
| `RegistroAcceso` | Fuera de alcance | Ampliación opcional (QR) |
| `Enchufe` | Fuera de alcance | Ampliación opcional (eléctrico) |
| `LecturaContador` | Fuera de alcance | Ampliación opcional (eléctrico) |
| `TarifaElectrica` | Fuera de alcance | Ampliación opcional (eléctrico) |

El estado de ocupación de una plaza **se deduce** de los contratos activos, no
se guarda como campo. Nunca añadas un campo `estaOcupada` ni similar a `Plaza`.
`EstadoPlaza` solo dice si la plaza es alquilable (`DISPONIBLE`) o no
(`FUERA_DE_SERVICIO`).

Los usuarios no se borran: se desactivan con el campo `activo`, para conservar
su histórico de contratos y recibos.

## Stack

| Capa | Tecnología |
|---|---|
| Backend | Spring Boot 4.1, Java 21, Maven |
| Persistencia | Spring Data JPA / Hibernate 7 sobre MySQL 8.4 |
| Seguridad | Spring Security 7 con JWT (jjwt 0.13) y BCrypt |
| Documentación de la API | springdoc-openapi 3.2 / Swagger UI |
| Pruebas | JUnit 5 |
| Cliente | Flutter 3 (Dart), compilado a Android y web/PWA |
| Entorno | Docker para MySQL; desarrollo en Windows 11 |
| Integración continua | GitHub Actions con MySQL de servicio |

Ojo con la documentación que encuentres: la mayoría de ejemplos de Spring
Security y de springdoc que circulan son de Spring Boot 3 y **no compilan** en
este proyecto. Si dudas, ve a la documentación oficial de la versión correcta.

## Estructura del repositorio

```
giapp/
├── CLAUDE.md          este archivo
├── README.md
├── .github/workflows/ integración continua
├── backend/           Spring Boot
├── frontend/          Flutter
└── docs/
    ├── requisitos.md  los 53 RF y 17 RNF
    ├── uml/           diagramas PlantUML con sus PNG
    └── wireframes/    prototipo HTML navegable
```

**Ejecuta siempre la IA desde la raíz del proyecto**, no desde `backend/` ni
`frontend/`, para que vea los dos módulos y mantenga los endpoints sincronizados
entre la API y el cliente.

## Arquitectura del backend

Paquetes **por capas** bajo `com.giapp.backend`:

```
config/         seguridad, OpenAPI, datos de prueba
controlador/    endpoints REST
dto/            peticiones y respuestas de la API (records)
modelo/         entidades JPA
modelo/enums/   enumerados
repositorio/    interfaces de Spring Data JPA
servicio/       lógica de negocio y transacciones
```

- **El controlador no toca nunca el repositorio.** Recibe la petición, delega en
  el servicio y devuelve la respuesta.
- **Una entidad JPA no sale nunca por un endpoint.** Para eso están los DTO:
  `Usuario` contiene `passwordHash`, y las relaciones `LAZY` revientan al
  serializar fuera de la transacción.
- Los DTO son `record`. Uno para la petición (`XxxPeticion`) y otro para la
  respuesta (`XxxRespuesta`). En la entrada, los números y booleanos van con su
  tipo envoltorio (`Integer`, `Long`, `Boolean`) para que `@NotNull` funcione;
  en la salida, primitivos. En la salida, los enumerados van como `String`.
- La conversión de entidad a DTO se hace **dentro del servicio**, dentro de la
  transacción, en un método privado `aRespuesta(...)`.
- Los métodos de servicio que solo leen llevan
  `@Transactional(readOnly = true)`, de
  `org.springframework.transaction.annotation`, **nunca** el de
  `jakarta.transaction`.
- Dentro de una transacción, una entidad modificada se guarda sola al terminar
  (*dirty checking*): `actualizar` no necesita `save()`.
- Validación en el servidor siempre, con Bean Validation en los DTO y `@Valid`
  en el controlador (RNF-04). Mensajes en español (RNF-10).
- Inyección de dependencias por constructor, con campos `final`. Sin
  `@Autowired`.
- Las reglas de negocio que se puedan expresar sin base de datos van en la
  entidad, para poder probarlas con JUnit sin Spring ni mocks (como
  `Plaza.admite()`).

Códigos HTTP: 200 al leer o modificar, 201 al crear, 204 al borrar, 400 si los
datos no son válidos, 401 sin autenticar, 403 sin permiso, 404 si no existe,
409 si choca con el estado actual (capacidad, duplicados, borrados con
dependencias).

## Convenciones de persistencia (JPA)

Estas reglas salieron de comprobar en la base de datos lo que Hibernate genera
de verdad. Ante la duda, `SHOW CREATE TABLE <tabla>;` manda.

- Toda entidad lleva `@Entity`, `@Getter`, `@Setter`, `@NoArgsConstructor` y
  `@Table(name = "...")` con el nombre en minúscula y `snake_case`. **Nunca
  `@Data`**: su `equals`/`hashCode` rompe la identidad de las entidades.
- Un constructor con parámetros solo para crear datos de prueba, marcado con un
  comentario.
- No se escribe `@Column(name = ...)` si la traducción automática de
  `camelCase` a `snake_case` ya da el nombre correcto.
- `nullable = false` solo donde el requisito lo exige. `length` pensado en los
  textos, no el 255 por defecto.
- Enumerados en `modelo/enums/`, con `@Enumerated(EnumType.STRING)` siempre.
  **Nunca `ORDINAL`**, y omitir la anotación equivale a ORDINAL. En un campo
  enumerado, `@Column` solo lleva `nullable`, **nunca `length`**: Hibernate
  genera el tipo `enum(...)` nativo de MySQL.
- Dinero con `BigDecimal` y `@Column(precision = 8, scale = 2)`. **Nunca
  `double`.** Al crear un `BigDecimal` a partir de un literal, desde `String`:
  `new BigDecimal("45.00")`.
- Fechas con `java.time.LocalDate`, sin `@Temporal`.
- Unicidad con `@UniqueConstraint` dentro de `@Table` y con nombre
  `uk_<tabla>_<columnas>`, **no** con `@Column(unique = true)`. En
  `columnNames` van los nombres de columna de la base de datos, en
  `snake_case`.
- Relaciones `@ManyToOne(fetch = FetchType.LAZY, optional = ...)` siempre
  `LAZY`, con `@JoinColumn(name = ..., foreignKey = @ForeignKey(name =
  "fk_<tabla>_<campo>"))`.
- **Sin `@OneToMany` en el lado inverso** salvo que algo lo necesite de verdad.
  Para obtener los hijos, un método en el repositorio (`findByPropietario`).
- Repositorios sin `@Repository`. Consultas derivadas del nombre del método;
  `@Query` solo si el nombre no puede expresarlo. Si una búsqueda puede no
  encontrar nada y devuelve un único resultado, `Optional`.
- `spring.jpa.hibernate.ddl-auto=update` **solo añade**: no renombra, no cambia
  tipos y no borra. Si un cambio de esquema necesita recrear la base de datos,
  **avísame y lo hago yo**; no la borres por tu cuenta.
- Si cambias el modelo, actualiza el `DataInitializer` para que los datos de
  prueba sigan siendo coherentes.

## Seguridad

- Spring Security 7: solo beans `SecurityFilterChain` con el DSL de lambdas.
  `authorizeHttpRequests` y `requestMatchers`, nada de `antMatchers`,
  `authorizeRequests` ni `.and()`.
- La autenticación es sin estado (`STATELESS`) con JWT. El token lleva el email
  como `subject`, el rol y el nombre. Nada sensible.
- `JwtFiltro` valida el token, comprueba en cada petición que el usuario existe
  y está **activo**, y carga la autenticación con
  `SecurityContextHolder.createEmptyContext()` + `setContext()`.
- Las autoridades llevan el prefijo `ROLE_`, para que `hasRole("GESTOR")`
  funcione.
- `/error` está permitido. Sin eso, un 403 se convierte en 401 al reenviarse
  internamente a `/error`.
- Hay un punto de entrada que devuelve 401; sin él, Spring respondería 403.
- La última regla es `.anyRequest().authenticated()`: todo endpoint nuevo queda
  protegido por defecto. **Cada endpoint nuevo necesita su regla por rol**
  (RNF-03).
- En los mensajes de login no se distingue entre email inexistente y contraseña
  incorrecta.
- Por el registro público solo se crean usuarios `CLIENTE`.

Credenciales de prueba (las crea `DataInitializer`): `gestor@giapp.com`,
`laura@giapp.com` y `miguel@giapp.com`, los tres con contraseña `giapp1234`.

## Frontend (Flutter)

- Estructura en `frontend/lib/`: `modelos/`, `pantallas/`, `servicios/`.
  Nombres de ficheros en `snake_case` y en español.
- Todas las llamadas a la API pasan por `servicios/api_servicio.dart`. Hay una
  única instancia global, `api`, que guarda el token en memoria.
- Las peticiones autenticadas llevan la cabecera
  `Authorization: Bearer <token>`.
- Las respuestas se decodifican con `utf8.decode(respuesta.bodyBytes)`, nunca
  con `respuesta.body`: si no, las tildes salen mal.
- Cada modelo tiene un constructor `factory Xxx.desdeJson(...)`. Los nombres de
  los campos coinciden letra a letra con los del `record` de Java.
- Un 401 lleva de vuelta a la pantalla de acceso; un 403 muestra un mensaje sin
  cerrar la sesión.
- Los formularios validan en el cliente antes de enviar, además de la
  validación del servidor.
- Cuando uses `context` después de un `await`, comprueba antes `mounted`.
- Sin gestores de estado externos (Provider, Riverpod): `StatefulWidget`,
  `setState` y `FutureBuilder`.

## Git e integración continua

- El trabajo se hace en la rama `desarrollo`. `main` solo recibe cambios por
  pull request antes de cada entrega.
- Cada push lanza GitHub Actions, que compila el backend y ejecuta las pruebas
  contra un MySQL de servicio. Un commit en rojo se arregla antes de seguir.
- Mensajes de commit en español, descriptivos, un cambio por commit.

## Convenciones de código

- Nombres de clases, variables, métodos y comentarios **en español** (RNF-12).
- `camelCase` para variables y métodos, `PascalCase` para clases.
- Indentación con 4 espacios en Java.
- Importes explícitos, uno por clase. Nada de `import paquete.*`.
- Importes monetarios y lecturas de contador con `BigDecimal`, nunca `double`.

## Lo que no se hace

- **Credenciales en el repositorio.** Las contraseñas de base de datos y las
  claves de firma van en `application-local.properties`, que está en
  `.gitignore`. Si se cuela una en un commit, no basta con borrarla después:
  queda en el historial.
- **Renumerar requisitos.** Si se elimina uno, se deja el hueco. Los diagramas y
  los wireframes referencian los números.
- **Ampliar el alcance sin preguntar.** Ver la sección siguiente.
- **Tocar `.env` ni `application-local.properties`.** Son locales y no están
  versionados. Si hace falta una credencial nueva, se añade a las plantillas
  `.ejemplo` y se documenta en el README.
- **Prorratear por días.** La facturación es por meses completos.
- **Guardar la ocupación de una plaza.** Se calcula.
- **Devolver entidades JPA por la API.** Siempre DTO.
- **Borrar la base de datos o reescribir el historial de Git** por tu cuenta.

## Alcance comprometido

Lo que hay que entregar antes de diciembre:

1. Autenticación con JWT y dos roles, con los endpoints protegidos.
2. Inventario: aparcamientos, plantas y plazas con capacidad en medias plazas,
   más clientes y vehículos.
3. Contratos: asignación con control de capacidad, cambio de plaza,
   finalización y consulta.
4. Facturación periódica: generación automática de recibos alineados al
   calendario, primer periodo por meses completos, y control de pagos e
   impagados.

Ampliaciones, por este orden y solo si da tiempo:

1. Control de accesos por QR, validando que el contrato está activo y al
   corriente de pago.
2. Suministro eléctrico: enchufes, lecturas y regularización.
3. Cuadro de mando con gráficas.

Todo lo demás está documentado pero **fuera de alcance**. No empieces a
construirlo por iniciativa propia.
