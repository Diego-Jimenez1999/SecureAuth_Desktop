# SecureAuth Desktop

SecureAuth Desktop es un ERP/POS veterinario de escritorio construido con Java Swing, Maven y MySQL. El proyecto comenzo como una aplicacion de autenticacion segura y esta evolucionando hacia una plataforma operativa para veterinarias, guarderias caninas y negocios de servicios para mascotas.

## Estado Del Proyecto

- Estado actual: Fase 1 de estabilizacion y migracion incremental hacia arquitectura limpia.
- UI: Java Swing.
- Build: Maven.
- Persistencia: MySQL con HikariCP.
- Seguridad: BCrypt para contrasenas.
- Pruebas actuales: pruebas unitarias de `PasswordHasher`.
- Compatibilidad objetivo: Java 21 o superior.
- Estados de agenda centralizados con compatibilidad para valores legacy.

## Modulos

- Autenticacion y usuarios.
- Clientes o propietarios.
- Mascotas.
- Ventas POS.
- Venta de servicios veterinarios.
- Agenda de servicios.
- Inventario.
- Reportes y dashboard.
- Configuracion visual y operativa.

## Arquitectura Actualizada

La arquitectura objetivo se implementa por fases. El sistema mantiene compatibilidad con la estructura actual mientras se incorporan capas de dominio y aplicacion.

```text
presentation.swing
  -> controller
  -> application.usecase / service
  -> domain
  -> infrastructure.persistence.repository / dao
  -> MySQL
```

Durante la transicion todavia existen DAOs JDBC y algunos servicios legacy. Las nuevas mejoras deben aislar reglas de negocio fuera de Swing, crear DTOs/Commands para casos de uso y centralizar migraciones de base de datos.

Consulta:

- `docs/ARCHITECTURE.md`
- `docs/SERVICE_FLOW.md`
- `docs/DATABASE.md`

## Estructura Del Proyecto

```text
src/main/java/secureauth
  ai/                 Integracion local con IA/Ollama
  config/             Conexion, contexto y utilidades de esquema
  controller/         Controladores Swing existentes
  dao/                DAOs JDBC legacy y enterprise
  model/              Modelos actuales
  repository/         Repositorios existentes para usuarios
  service/            Servicios de negocio actuales
  ui/                 Frames, paneles, dialogs y utilidades Swing

src/main/resources
  icon/               Iconografia
  *.png               Imagenes de marca y UI

docs
  ARCHITECTURE.md     Arquitectura real y objetivo
  ROADMAP.md          Plan de implementacion por fases
  CHANGELOG.md        Historial de cambios
  MIGRATION.md        Migraciones y compatibilidad
  SERVICE_FLOW.md     Flujo de venta de servicios
  DATABASE.md         Modelo de datos actual y propuesto
```

## Tecnologias

- Java 21+
- Swing
- Maven
- MySQL 8+
- HikariCP
- BCrypt
- JUnit 5
- Mockito
- Apache POI
- OkHttp
- Jackson

## Capturas

Las capturas se agregaran conforme avance la implementacion del nuevo flujo de venta y agenda.

Ubicacion sugerida:

```text
docs/screenshots/
```

## Instalacion

Requisitos:

- JDK 21 o superior.
- Maven 3.9 o superior.
- MySQL 8 o superior.

Compilar:

```bash
mvn clean compile
```

Ejecutar:

```bash
mvn exec:java
```

Ejecutar pruebas:

```bash
mvn test
```

## Configuracion

La aplicacion lee la configuracion de base de datos en este orden:

1. Propiedades JVM.
2. Variables de entorno.
3. Archivo local `.env` en la raiz del proyecto.

Variables soportadas:

```properties
SECUREAUTH_DB_URL=jdbc:mysql://localhost:3306/secureauth
SECUREAUTH_DB_USER=root
SECUREAUTH_DB_PASSWORD=secret
```

El archivo `.env` esta ignorado por Git.

## Base De Datos

El sistema usa MySQL. Actualmente varios DAOs crean o ajustan tablas con metodos `ensureSchema()`. La migracion objetivo consiste en mover esos cambios a scripts versionados e idempotentes.

Tablas actuales relevantes:

- `users`
- `owners`
- `pets`
- `inventory_items`
- `sales_categories`
- `sales_items`
- `sales_item_sizes`
- `ventas`
- `detalle_venta`
- `sales_tx`
- `appointments`
- `citas_servicio`
- `actividad_reciente`

Modelo objetivo:

- Orden de servicio.
- Productos sugeridos por servicio.
- Productos consumidos por servicio.
- Consumo de inventario trazable.
- Historial de estados.
- Agenda unificada.

Consulta `docs/DATABASE.md`.

## Flujo De Venta

Flujo actual estabilizado:

```text
Seleccionar item
 -> agregar al carrito
 -> agendar servicios requeridos
 -> seleccionar metodo de pago
 -> registrar venta transaccional
 -> descontar inventario
 -> guardar citas
 -> actualizar actividad reciente
```

Flujo objetivo:

```text
Cliente
 -> Mascota
 -> Servicio
 -> Veterinario y horario
 -> Productos sugeridos/usados
 -> Resumen economico
 -> Orden de servicio
 -> Venta/factura
 -> Consumo de inventario
 -> Historial y agenda
```

Consulta `docs/SERVICE_FLOW.md`.

## Flujo De Agenda

La agenda actual usa `appointments` para citas creadas desde ventas y conserva compatibilidad con `citas_servicio`. La agenda objetivo sera una vista operativa de ordenes de servicio con estados:

- Pendiente
- Confirmada
- En proceso
- Finalizada
- Cancelada

## Flujo De Inventario

Inventario actual:

- Importacion CSV/XLSX.
- Busqueda por SKU, nombre o categoria.
- Stock por negocio y sucursal.
- Descuento transaccional desde ventas.

Inventario objetivo:

- Historial de movimientos.
- Consumos asociados a ordenes de servicio.
- Responsable del consumo.
- Fecha, costo y trazabilidad.

## Roadmap

Resumen:

1. Estabilizacion del proyecto.
2. Nueva arquitectura del dominio.
3. Nuevo modelo de base de datos.
4. Nuevo flujo de venta de servicios.
5. Nuevo JDialog profesional.
6. Agenda avanzada.
7. UX y componentes reutilizables.

Consulta `docs/ROADMAP.md`.

## Cambios Recientes

Consulta `docs/CHANGELOG.md`.

Resumen reciente:

- Documentacion tecnica nueva.
- Maven alineado a Java 21+.
- Estados de agenda normalizados mediante `AppointmentStatus`.
- Pruebas unitarias para compatibilidad de estados legacy.

## Buenas Practicas

- Mantener el proyecto compilable despues de cada fase.
- Ejecutar `mvn test` antes de cerrar cambios.
- Evitar reglas de negocio dentro de Swing.
- Usar `PreparedStatement` para SQL.
- Mantener migraciones idempotentes.
- Documentar decisiones importantes.
- No eliminar funcionalidades existentes sin migracion o justificacion.
- Preferir cambios incrementales y reversibles.

## Contribuir

1. Revisa `docs/ROADMAP.md`.
2. Trabaja por fases pequenas.
3. Actualiza documentacion junto con el codigo.
4. Ejecuta `mvn test`.
5. Describe riesgos y compatibilidad en `docs/CHANGELOG.md`.
