# Arquitectura De SecureAuth Desktop

Este documento describe la arquitectura real que se mantiene hoy y la arquitectura objetivo que se implementara por fases.

## Principios

- Mantener compatibilidad con Java Swing y Maven.
- Mover reglas de negocio fuera de la UI.
- Mantener el proyecto compilable en cada fase.
- Agregar capas sin romper los modulos existentes.
- Usar migraciones idempotentes y trazables.
- Favorecer DTO, Command, Mapper, Repository y Service.

## Arquitectura Actual

```text
secureauth.ui
  -> secureauth.controller
  -> secureauth.service
  -> secureauth.repository / secureauth.dao
  -> secureauth.config.DatabaseConnection
  -> MySQL
```

La separacion ya existe en autenticacion, usuarios, mascotas, inventario y algunos reportes. El modulo de ventas todavia concentra demasiada coordinacion en Swing.

## Problemas En Transicion

- Algunos paneles Swing crean servicios o DAOs directamente.
- `SalesServiceCatalog` vive en `ui.sales`, pero actua como cache/repositorio de catalogo.
- Hay dos modelos de agenda: `appointments` y `citas_servicio`.
- La creacion/migracion de tablas esta distribuida en DAOs.
- Las clases UI grandes dificultan pruebas y evolucion.

## Arquitectura Objetivo

```text
secureauth
  app
    config
    session
  domain
    appointment
    inventory
    pet
    sales
    serviceorder
  application
    command
    dto
    mapper
    usecase
  infrastructure
    migration
    persistence
      jdbc
      repository
  presentation
    swing
      appointment
      components
      inventory
      sales
  shared
    error
    event
    money
    validation
```

## Flujo Por Capas

```text
Swing View
 -> Controller
 -> Command/DTO
 -> Use Case Service
 -> Domain Model
 -> Repository
 -> DAO/JDBC
 -> MySQL
```

## Responsabilidades

| Capa | Responsabilidad |
| --- | --- |
| `presentation.swing` | Componentes Swing, eventos visuales, renderizado, validacion inmediata no persistente. |
| `controller` | Traducir eventos de UI a comandos de aplicacion. |
| `application` | Casos de uso, DTOs, commands, mappers y coordinacion transaccional. |
| `domain` | Entidades, value objects, estados y reglas puras. |
| `infrastructure` | Persistencia JDBC, migraciones, integraciones externas. |
| `shared` | Utilidades comunes sin dependencia de UI o BD. |

## Estado De Implementacion

- Fase 1 en curso: estabilizacion, documentacion y correccion de inconsistencias.
- Maven y documentacion quedan alineados a Java 21+.
- Los estados de agenda se centralizan en `secureauth.model.AppointmentStatus`.
- Las capas nuevas se agregaran en Fase 2 sin eliminar compatibilidad.

## Decisiones

- Se mantiene Swing como tecnologia UI.
- Se mantiene MySQL como base de datos.
- Se mantiene Maven como build.
- La compatibilidad objetivo se alinea a Java 21+.
- Las tablas legacy no se eliminan hasta tener migracion de datos validada.
- Los estados legacy de agenda se leen mediante aliases mientras se migran los datos.
