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

## Reglas de negocio que no son obvias

Estas tres son lo que distingue al proyecto. Si las implementas mal, el proyecto
pierde su sentido.

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

### Periodos de facturación alineados al calendario

La periodicidad de pago (mensual, trimestral, semestral o anual) se elige por
contrato, pero **los periodos se alinean al calendario natural, no a la fecha de
alta**. Todos los contratos semestrales facturan en enero y en julio.

El primer recibo de un contrato cubre **solo los meses que restan hasta el
cierre del periodo en curso**.

Ejemplo: alta el 1 de marzo con pago semestral. Los semestres son enero–junio y
julio–diciembre, así que el primer recibo cubre marzo, abril, mayo y junio
(4 meses). El 1 de julio ya se cobra el semestre completo.

Este cálculo debe ser determinista y estar cubierto por pruebas unitarias.

### Provisión y regularización eléctrica

Las plazas pueden tener enchufes, cada uno con su contador. El consumo se cobra
como lo hacen las eléctricas:

- Cada recibo incluye una **provisión**: una estimación cobrada por adelantado.
- El recibo siguiente incluye la **regularización** del periodo anterior: la
  diferencia entre lo que se provisionó y lo que de verdad se consumió, en
  positivo o en negativo.
- El consumo real se calcula restando dos lecturas del contador.
- El primer recibo de un contrato no lleva regularización.

## Modelo de datos

Trece entidades. El diagrama está en `docs/uml/diagrama-clases.puml`; los
enumerados, en `docs/uml/diagrama-clases-enums.puml`.

| Entidad | Notas |
|---|---|
| `Usuario` | Una sola tabla con un campo `rol` (GESTOR o CLIENTE), no herencia |
| `Vehiculo` | Tipo COCHE o MOTO, nada más |
| `Aparcamiento` | Un gestor puede tener varios |
| `Planta` | Compuesta en Aparcamiento |
| `Plaza` | `capacidad` y `admiteCoche`; ver medias plazas |
| `Enchufe` | Entidad propia porque cada uno tiene su contador |
| `LecturaContador` | Lecturas acumuladas de un enchufe |
| `TarifaAlquiler` | Por tipo de vehículo, con `vigenteDesde` |
| `TarifaElectrica` | Precio del kWh, con `vigenteDesde` |
| `Promocion` | Código, descuento y caducidad |
| `Contrato` | El núcleo: cliente, plaza, vehículo, periodicidad |
| `Recibo` | Desglosado en alquiler, provisión y regularización |
| `RegistroAcceso` | Entradas y salidas |

El estado de una plaza **se deduce** de los contratos activos, no se guarda como
campo. Nunca añadas un campo `estaOcupada` a `Plaza`.

## Stack

| Capa | Tecnología |
|---|---|
| Backend | Spring Boot 3, Java 21, Maven |
| Persistencia | Spring Data JPA / Hibernate sobre MySQL 8 |
| Seguridad | Spring Security con JWT (jjwt) |
| Documentación de la API | springdoc-openapi / Swagger UI |
| Cliente | Flutter 3 (Dart), compilado a Android y web/PWA |
| Entorno | Docker para MySQL; desarrollo en Windows 11 |

## Estructura del repositorio

```
giapp/
├── CLAUDE.md          este archivo
├── README.md
├── backend/           Spring Boot
├── frontend/          Flutter
└── docs/
    ├── requisitos.md  los 51 RF y 17 RNF
    ├── uml/           diagramas PlantUML con sus PNG
    └── wireframes/    prototipo HTML navegable
```

**Ejecuta siempre la IA desde la raíz del proyecto**, no desde `backend/` ni
`frontend/`, para que vea los dos módulos y mantenga los endpoints sincronizados
entre la API y el cliente.

## Convenciones de código

- Nombres de clases, variables, métodos y comentarios **en español**.
- `camelCase` para variables y métodos, `PascalCase` para clases.
- Indentación con tabuladores.
- Los enumerados se persisten con `@Enumerated(EnumType.STRING)`.
  **Nunca `ORDINAL`**: guarda la posición, y al insertar un valor nuevo en medio
  del enum todos los registros antiguos pasan a significar otra cosa.
- Importes monetarios y lecturas de contador con `BigDecimal`, nunca `double`.

## Lo que no se hace

- **Credenciales en el repositorio.** Las contraseñas de base de datos y las
  claves de firma van en `application-local.properties`, que está en
  `.gitignore`. Si se cuela una en un commit, no basta con borrarla después:
  queda en el historial.
- **Renumerar requisitos.** Si se elimina uno, se deja el hueco. Los diagramas y
  los wireframes referencian los números.
- **Ampliar el alcance sin preguntar.** Ver la sección siguiente.

## Alcance comprometido

Lo que hay que entregar antes de diciembre:

1. Autenticación con JWT y dos roles, con los endpoints protegidos.
2. Inventario: aparcamientos, plantas y plazas con capacidad en medias plazas,
   más clientes y vehículos.
3. Contratos: asignación con control de capacidad, cambio de plaza,
   finalización y consulta.
4. Facturación periódica: generación automática de recibos alineados al
   calendario, prorrateo del primer periodo, y control de pagos e impagados.

Ampliaciones, por este orden y solo si da tiempo:

1. Control de accesos por QR, validando que el contrato está activo y al
   corriente de pago.
2. Suministro eléctrico: enchufes, lecturas y regularización.
3. Cuadro de mando con gráficas.

Todo lo demás está documentado pero **fuera de alcance**. No empieces a
construirlo por iniciativa propia.
