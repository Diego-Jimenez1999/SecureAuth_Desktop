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
- Fase 2 iniciada: se crearon paquetes `application`, `infrastructure` y `shared` para ventas.
- `SalesPanel` ya delega el carrito y el registro de venta en casos de uso; conserva Swing solo para captura, renderizado y dialogos.
- `SalesController` queda como puente compatible hacia `SalesCartUseCase` mientras se migran los demas paneles.

## Decisiones

- Se mantiene Swing como tecnologia UI.
- Se mantiene MySQL como base de datos.
- Se mantiene Maven como build.
- La compatibilidad objetivo se alinea a Java 21+.
- Las tablas legacy no se eliminan hasta tener migracion de datos validada.
- Los estados legacy de agenda se leen mediante aliases mientras se migran los datos.

## Dependencias Fase 2

```text
secureauth.ui.components.SalesPanel
  -> secureauth.controller.SalesController
  -> secureauth.application.usecase.SalesCartUseCase

secureauth.ui.components.SalesPanel
  -> secureauth.application.command.RegisterSaleCommand
  -> secureauth.application.usecase.RegisterSaleUseCase
  -> secureauth.infrastructure.repository.SalesRepository
  -> secureauth.infrastructure.persistence.JdbcSalesRepository
  -> secureauth.service.enterprise.SalesTransactionService
  -> secureauth.dao.enterprise.*
```

Base de eventos preparada:

```text
RegisterSaleUseCase
  -> shared.events.EventPublisher
  -> shared.events.SaleRegisteredEvent
```

## Flujo De Venta De Servicios

El POS diferencia articulos vendibles con `domain.sales.SaleItemType`:

```text
PRODUCT -> se agrega directamente al carrito
SERVICE -> abre ServiceAppointmentDialog antes de agregarse al carrito
```

El panel de ventas ya no decide por nombres, prefijos ni comparaciones de
categoria. La unica conversion desde texto vive en `SaleItemType.fromCatalogValue`
como adaptador de compatibilidad porque la tabla `sales_items.item_type` sigue
existiendo como columna textual.

El carrito puede contener productos y servicios simultaneamente. Los servicios
se guardan como `SaleItemDTO` con su `AppointmentDTO` interno; al confirmar el
pago, `RegisterSaleUseCase` persiste venta y citas en una unica transaccion a
traves de `SalesRepository`.

## Orden De Servicio

Fase 4 introduce `secureauth.domain.services` como modelo independiente para
procedimientos veterinarios:

```text
ServiceOrder
  -> ServiceOrderItem
  -> ServiceProduct[]
  -> suggested ServiceProduct[]
  -> ServiceSummary
```

La orden no depende de Swing, JDBC, DAOs ni DTOs. Los DTOs viven en
`secureauth.application.dto` y los casos de uso en `secureauth.application.usecase`.
Durante esta fase no existe persistencia propia para ordenes: el dialogo prepara
la orden y mantiene productos utilizados/sugeridos en memoria, usando inventario
solo como selector.

Restriccion vigente: Fase 4 no descuenta stock, no registra consumos, no crea
movimientos de inventario y no agrega tablas nuevas.

## Integracion Orden-Inventario

Fase 5 conecta la orden de servicio preparada por el dialogo con el flujo
transaccional actual sin cambiar el contrato de la UI ni crear tablas nuevas en
runtime:

```text
RegisterSaleUseCase
  -> SalesRepository
  -> JdbcSalesRepository
  -> SalesTransactionService.registrarVentaConCitas(..., serviceOrders)
  -> JdbcServiceOrderRepository
  -> InventoryDAO
  -> ActividadRecienteDAO
```

La unidad transaccional incluye:

```text
ventas
detalle_venta
appointments
serviceOrders adaptadas
consumo de inventory_items
actividad_reciente
```

Si falla la validacion de stock, producto activo o consumo, se ejecuta rollback
de toda la venta. La trazabilidad de consumo queda temporalmente adaptada a
`actividad_reciente`; la migracion a tablas propias esta preparada en
`docs/migrations/V005_service_order_inventory.sql`.

Eventos preparados:

```text
ServiceOrderRegisteredEvent
InventoryConsumptionEvent
```
