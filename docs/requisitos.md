# GIAPP · Requisitos

Documento de la fase de análisis. Entrega 2 del Proyecto Intermodular.
Autor: Andrés Moreno.

La numeración es permanente: si se elimina un requisito se deja el hueco, porque
los diagramas de `docs/uml/` y los wireframes de `docs/wireframes/` los
referencian.

---

## Requisitos funcionales

### Acceso y usuarios

| Nº | Requisito |
|---|---|
| RF-01 | El gestor podrá dar de alta a un cliente registrando sus datos personales y de contacto. |
| RF-02 | El usuario podrá iniciar sesión con su correo y contraseña, y el sistema le devolverá un token de acceso. |
| RF-03 | El usuario podrá cerrar sesión, invalidando el token en el dispositivo. |
| RF-04 | El usuario podrá consultar y modificar sus datos de perfil y cambiar su contraseña. |
| RF-05 | El sistema asignará a cada usuario un rol (gestor o cliente) que determinará las funciones a las que tiene acceso. |
| RF-06 | El gestor podrá consultar, editar y desactivar las cuentas de sus clientes. |

### Vehículos

| Nº | Requisito |
|---|---|
| RF-07 | El gestor podrá asociar uno o varios vehículos a un cliente, indicando matrícula, marca, modelo y tipo (coche o moto). |
| RF-08 | El gestor podrá editar o eliminar los vehículos de un cliente. |
| RF-09 | El cliente podrá consultar los vehículos asociados a su cuenta. |

### Aparcamientos, plantas y plazas

| Nº | Requisito |
|---|---|
| RF-10 | El gestor podrá dar de alta un aparcamiento indicando nombre, dirección y número de plantas. |
| RF-11 | El gestor podrá modificar o eliminar los aparcamientos que administra. |
| RF-12 | El gestor podrá crear plantas dentro de un aparcamiento indicando su identificador y su nivel. |
| RF-13 | El gestor podrá crear plazas dentro de una planta indicando su número, su capacidad en medias plazas, si admite coches y cuántos enchufes tiene. |
| RF-14 | El gestor podrá generar plazas en lote indicando un rango de numeración, una capacidad y si admiten coches. |
| RF-15 | El gestor podrá marcar una plaza como fuera de servicio por mantenimiento. |
| RF-16 | El gestor podrá consultar el estado de todas las plazas de un aparcamiento (libre, parcialmente ocupada, completa o fuera de servicio). |

### Tarifas y promociones

| Nº | Requisito |
|---|---|
| RF-17 | El gestor podrá definir tarifas por aparcamiento y tipo de vehículo (coche o moto), indicando el precio mensual. |
| RF-18 | El gestor podrá crear promociones indicando código, porcentaje de descuento y fecha de caducidad. |
| RF-19 | El gestor podrá activar o desactivar una promoción sin eliminarla. |

### Contratos

| Nº | Requisito |
|---|---|
| RF-20 | El gestor podrá crear un contrato asignando una plaza a un cliente y a uno de sus vehículos. |
| RF-21 | El contrato registrará la fecha de inicio, la periodicidad de pago (mensual, trimestral, semestral o anual) y el precio mensual acordado. |
| RF-22 | El sistema impedirá crear un contrato si la plaza no admite el tipo de vehículo o si no le queda capacidad libre suficiente. |
| RF-23 | El gestor podrá aplicar una promoción vigente al crear el contrato. |
| RF-24 | El gestor podrá reasignar a un cliente a otra plaza con capacidad libre, conservando el histórico de la plaza anterior. |
| RF-25 | El gestor podrá finalizar o rescindir un contrato indicando la fecha efectiva de baja. |
| RF-26 | El gestor podrá consultar todos los contratos y filtrarlos por aparcamiento, cliente o estado. |
| RF-27 | El cliente podrá consultar los datos de su contrato: plaza asignada, vehículo, fecha de inicio y periodicidad. |

### Facturación y recibos

| Nº | Requisito |
|---|---|
| RF-28 | El sistema generará automáticamente los recibos de cada contrato activo según su periodicidad. |
| RF-29 | Los periodos de facturación se alinearán al calendario natural, de forma que todos los contratos con la misma periodicidad facturen en las mismas fechas. |
| RF-30 | El primer recibo de un contrato cubrirá únicamente los meses que resten hasta el cierre del periodo en curso. |
| RF-31 | Cada recibo registrará el periodo cubierto, el número de meses, el importe, la fecha de emisión, la fecha de vencimiento y su estado. |
| RF-32 | El gestor podrá marcar un recibo como pagado indicando la fecha y la forma de pago. |
| RF-33 | El sistema marcará automáticamente como impagado todo recibo que supere su fecha de vencimiento sin haberse abonado. |
| RF-34 | El gestor podrá consultar los recibos impagados de sus aparcamientos. |
| RF-35 | El gestor podrá consultar los ingresos de sus aparcamientos en un rango de fechas. |
| RF-36 | El cliente podrá consultar el histórico de sus recibos y el estado de cada uno. |

### Control de accesos

| Nº | Requisito |
|---|---|
| RF-37 | El sistema generará un código QR asociado a cada contrato activo. |
| RF-38 | El cliente podrá visualizar el código QR de su contrato desde la aplicación. |
| RF-39 | El sistema validará el código QR comprobando que el contrato está activo y que el cliente no tiene recibos impagados. |
| RF-40 | El sistema registrará cada entrada y cada salida con su fecha y hora. |
| RF-41 | El gestor podrá consultar el histórico de accesos de un aparcamiento. |

### Suministro eléctrico

| Nº | Requisito |
|---|---|
| RF-42 | El gestor podrá dar de alta enchufes en una plaza, cada uno con su identificador propio. |
| RF-43 | El gestor podrá asignar un enchufe libre a un contrato, o dejar el contrato sin enchufe. |
| RF-44 | El gestor podrá definir el precio del kWh de un aparcamiento indicando su fecha de entrada en vigor. |
| RF-45 | El gestor podrá registrar la lectura del contador de un enchufe en una fecha determinada. |
| RF-46 | El contrato con enchufe asignado registrará una provisión mensual estimada de consumo eléctrico. |
| RF-47 | Cada recibo desglosará el importe en alquiler, provisión eléctrica y regularización. |
| RF-48 | El sistema calculará el consumo real de un periodo restando las lecturas del contador al inicio y al final. |
| RF-49 | El sistema incluirá en cada recibo la regularización del periodo anterior, como diferencia entre la provisión cobrada y el consumo real. |
| RF-50 | El cliente podrá consultar el desglose completo de cada uno de sus recibos. |
| RF-51 | El cliente podrá consultar su histórico de consumo eléctrico. |

---

## Requisitos no funcionales

| Nº | Requisito |
|---|---|
| RNF-01 | Las contraseñas se almacenarán cifradas mediante BCrypt, nunca en texto plano. |
| RNF-02 | El acceso a la API se autenticará mediante token JWT con fecha de caducidad. |
| RNF-03 | Cada endpoint de la API verificará el rol del usuario antes de ejecutar la operación. |
| RNF-04 | Todos los datos recibidos por la API se validarán en el servidor, con independencia de la validación del cliente. |
| RNF-05 | Las credenciales de la base de datos y las claves de firma no se almacenarán en el repositorio. |
| RNF-06 | La aplicación se servirá bajo HTTPS en el entorno de producción. |
| RNF-07 | Las consultas habituales responderán en menos de dos segundos. |
| RNF-08 | La interfaz se adaptará a pantallas de móvil, tableta y escritorio. |
| RNF-09 | La versión web será una PWA instalable en el dispositivo del usuario. |
| RNF-10 | Los mensajes de error serán comprensibles y estarán redactados en español. |
| RNF-11 | La API estará documentada con OpenAPI y accesible mediante Swagger UI. |
| RNF-12 | El código fuente, los nombres de clases y variables y los comentarios estarán en español, siguiendo la notación camelCase. |
| RNF-13 | La base de datos garantizará que la suma de la ocupación de los contratos activos de una plaza no supere su capacidad. |
| RNF-14 | El cálculo de periodos de facturación será determinista y estará cubierto por pruebas unitarias. |
| RNF-15 | El entorno de desarrollo será reproducible mediante contenedores Docker. |
| RNF-16 | La arquitectura permitirá que un mismo gestor administre varios aparcamientos sin cambios en el código. |
| RNF-17 | El cálculo de la regularización eléctrica estará cubierto por pruebas unitarias, incluyendo los casos de consumo superior e inferior a la provisión. |

---

## Alcance comprometido

### Funcionalidades mínimas viables

1. Autenticación con JWT y dos roles, con los endpoints de la API protegidos
   según el rol.
2. Gestión completa del inventario: aparcamientos, plantas y plazas con
   capacidad en medias plazas, más el alta de clientes y sus vehículos.
3. Contratos de alquiler: asignación de plaza con control de capacidad, cambio
   de plaza, finalización y consulta.
4. Facturación periódica: generación automática de recibos alineados al
   calendario, prorrateo del primer periodo, y control de pagos e impagados.

### Funcionalidades opcionales

1. Control de accesos por QR, validando que el contrato está activo y al
   corriente de pago.
2. Gestión del suministro eléctrico: enchufes, lecturas de contador y
   regularización del consumo en el recibo siguiente.
3. Cuadro de mando con gráficas de ocupación, ingresos y morosidad.
