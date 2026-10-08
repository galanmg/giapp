# GIAPP · Gestión Integral de Aparcamiento

Aplicación multiplataforma para la gestión de aparcamientos en régimen de
alquiler mensual. Proyecto Intermodular del CFGS de Desarrollo de Aplicaciones
Multiplataforma (DAM), IES Juan Bosco, curso 2026/2027.

**Autor:** Andrés Moreno

---

## Qué resuelve

Los aparcamientos pequeños y medianos de pueblos y ciudades alquilan sus plazas
por meses, y los siguen gestionando con hojas de cálculo y recibos en papel. El
gestor no sabe qué plazas tiene libres, los cobros no tienen trazabilidad y el
control de accesos depende de una llave o un mando que no caduca cuando alguien
deja de pagar.

GIAPP reúne en un solo sistema el inventario de plazas, los contratos de
alquiler, la facturación periódica y el control de accesos.

## Quién lo usa

- **El gestor** administra sus aparcamientos, da de alta a los abonados, asigna
  plazas y lleva el control de los cobros.
- **El abonado** consulta su contrato y sus recibos, y entra al aparcamiento con
  un código QR que solo funciona si está al corriente de pago.

## Lo que tiene de particular

**Las plazas se miden en medias plazas.** Un coche ocupa dos, una moto ocupa
una. Así una plaza puede alojar un coche o dos motos, y el gestor configura
plaza por plaza qué cabe de verdad en cada hueco de su aparcamiento.

**La facturación se alinea al calendario.** Cada abonado elige si paga por
meses, trimestres, semestres o años, pero los periodos son los naturales: todos
los contratos semestrales facturan en enero y en julio. El primer recibo de un
contrato se prorratea hasta el cierre del periodo en curso.

**La luz se cobra por provisión y se regulariza.** Las plazas pueden tener
enchufe con contador. Cada recibo cobra una estimación por adelantado y ajusta
en el siguiente la diferencia con el consumo real, como hacen las compañías
eléctricas.

## Stack

| Capa | Tecnología |
|---|---|
| Backend | Spring Boot 3 · Java 21 · Maven |
| Persistencia | Spring Data JPA / Hibernate · MySQL 8 |
| Seguridad | Spring Security · JWT |
| API | REST documentada con OpenAPI / Swagger UI |
| Cliente | Flutter 3 (Dart) → Android y web/PWA |
| Entorno | Docker |

## Estructura

```
giapp/
├── CLAUDE.md          contexto y reglas para los asistentes de IA
├── backend/           API REST en Spring Boot
├── frontend/          cliente Flutter
└── docs/
    ├── requisitos.md  requisitos funcionales y no funcionales
    ├── uml/           casos de uso, clases y flujo de navegación
    └── wireframes/    prototipo de interfaz navegable
```

## Documentación

| Documento | Qué contiene |
|---|---|
| [`docs/requisitos.md`](docs/requisitos.md) | 51 requisitos funcionales y 17 no funcionales |
| [`docs/uml/`](docs/uml) | Diagramas de casos de uso, de clases y de navegación |
| [`docs/wireframes/index.html`](docs/wireframes/index.html) | Prototipo de interfaz, navegable en el navegador |

Los diagramas están escritos en PlantUML. Cada `.puml` tiene su `.png` al lado
para poder verlos sin instalar nada.

## Cómo ver el prototipo

Abre `docs/wireframes/index.html` en cualquier navegador. Las pantallas están
enlazadas entre sí, así que se puede recorrer el flujo completo haciendo clic.

## Herramientas de desarrollo

IntelliJ IDEA Community para el backend, VS Code con la extensión de Flutter
para el cliente, y Android Studio para el emulador. La IA utilizada es Claude
(Claude Code en el terminal y Claude en interfaz de chat para el diseño de la
arquitectura), con el contexto y las reglas de trabajo recogidos en
[`CLAUDE.md`](CLAUDE.md).
