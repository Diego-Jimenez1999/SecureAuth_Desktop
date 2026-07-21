# Changelog

Todos los cambios relevantes del proyecto se documentan aqui.

## 2026-07-17

### Fase 5: Integracion de Orden de Servicio con Inventario

- Se agrego `ServiceOrderRepository` como frontera de persistencia de ordenes de servicio.
- Se agrego `JdbcServiceOrderRepository` como adaptador temporal hacia inventario y actividad reciente.
- `JdbcSalesRepository` extrae `ServiceOrderDTO` desde los items de venta y los entrega al servicio transaccional.
- `SalesTransactionService` ahora tiene una ruta transaccional que registra venta, detalles, citas, ordenes adaptadas y consumo de inventario en un unico commit.
- `InventoryDAO` agrega lectura transaccional de producto para validar existencia, estado activo, costo/precio y stock antes de descontar.
- Se agrego `ValidateServiceOrderInventoryUseCase` para validar productos consumibles de orden.
- Se agregaron eventos `ServiceOrderRegisteredEvent` e `InventoryConsumptionEvent`.
- Se preparo `docs/migrations/V005_service_order_inventory.sql` con tablas futuras sin ejecutarlas desde la app.
- Se agregaron pruebas para adaptacion de persistencia, consumo de inventario, stock insuficiente y productos inactivos.

Restricciones respetadas:

- No se crearon tablas fisicas nuevas en runtime.
- No se modifico el flujo Producto/Servicio ni el comportamiento del carrito.
- La persistencia definitiva queda preparada para una fase posterior.

### Fase 4: Orden de servicio

- Se agrego el dominio independiente `secureauth.domain.services` con `ServiceOrder`, `ServiceOrderItem`, `ServiceProduct`, `ServiceSummary` y `ServiceOrderStatus`.
- Se agregaron DTOs propios para ordenes de servicio: `ServiceOrderDTO`, `ServiceOrderItemDTO` y `ServiceProductDTO`.
- Se agregaron `CreateServiceOrderUseCase`, `UpdateServiceOrderUseCase` y `ValidateServiceOrderUseCase`.
- `ServiceAppointmentDialog` ahora permite gestionar productos utilizados desde inventario como selector, sin modificar stock ni generar movimientos.
- El resumen economico separa valor del servicio, productos, subtotal, IVA, descuento y total.
- Se prepararon productos sugeridos desde el catalogo/inventario actual sin crear persistencia definitiva.
- Se agregaron pruebas unitarias para creacion, actualizacion, validaciones, resumen, productos utilizados y sugeridos.
- No se modificaron DAOs, transacciones JDBC, tablas, migraciones ni consumo de inventario.

### Fase 2: Arquitectura de aplicacion

- Se crearon las primeras capas `application`, `infrastructure` y `shared`.
- Se agregaron DTOs: `SaleDTO`, `SaleItemDTO`, `CustomerDTO`, `PetDTO` y `AppointmentDTO`.
- Se agregaron mappers para ventas, citas, clientes y mascotas.
- Se agregaron validadores independientes para ventas, agenda e inventario.
- Se agregaron `SalesCartUseCase` y `RegisterSaleUseCase`.
- Se introdujeron interfaces Repository base: ventas, inventario, agenda, clientes y mascotas.
- Se agrego `JdbcSalesRepository` como adaptador hacia el servicio transaccional legacy.
- Se preparo la base de eventos internos con `ApplicationEvent`, `EventPublisher` y `SaleRegisteredEvent`.
- Se refactorizo `SalesPanel` para delegar calculos, validacion y registro de ventas fuera de Swing.
- Se adapto `SalesController` como puente compatible hacia el nuevo caso de uso de carrito.
- Se agregaron pruebas unitarias para carrito, validacion de ventas y registro de ventas.

### Fase 3: Flujo de venta de servicios

- Se agrego `SaleItemType` para diferenciar `PRODUCT` y `SERVICE` sin reglas basadas en texto dentro del flujo.
- El POS abre automaticamente el dialogo de agendamiento cuando el item seleccionado es `SERVICE`.
- Los productos mantienen el comportamiento anterior: se agregan directamente al carrito.
- El carrito ahora puede mezclar productos y servicios; los servicios guardan internamente su `AppointmentDTO`.
- `RegisterSaleUseCase` consolida las citas asociadas al carrito y registra venta+citas en la misma transaccion existente.
- `ServiceAppointmentDialog` fue reorganizado en secciones de Cliente, Mascota, Servicio y Resumen, con autocompletado de cliente y carga de mascotas.
- `GestionVentasServiciosDialog` fue ajustado para seleccionar tipos desde `SaleItemType`.
- No se agregaron tablas ni consumo automatico de inventario.

Archivos principales cambiados:

- `src/main/java/secureauth/ui/components/SalesPanel.java`
- `src/main/java/secureauth/controller/SalesController.java`
- `src/main/java/secureauth/application/**`
- `src/main/java/secureauth/infrastructure/**`
- `src/main/java/secureauth/shared/**`
- `src/test/java/secureauth/application/**`

### Documentacion

- Se creo la documentacion viva de arquitectura, roadmap, migracion, flujo de servicios y base de datos.
- Se actualizo `README.md` para describir el ERP veterinario, arquitectura, modulos, instalacion, configuracion y roadmap.

### Fase 1: Estabilizacion

- Se alineo Maven a Java 21+ para corregir la inconsistencia entre documentacion y build.
- Se creo `AppointmentStatus` para centralizar estados de agenda.
- Se normalizaron estados objetivo `CONFIRMADA`, `FINALIZADA` y `CANCELADA`, conservando compatibilidad con datos legacy `REALIZADO` y `CANCELADO`.

Archivos cambiados:

- `pom.xml`
- `src/main/java/secureauth/model/AppointmentStatus.java`
- `src/main/java/secureauth/dao/enterprise/AppointmentDAO.java`
- `src/main/java/secureauth/service/enterprise/AppointmentService.java`
- `src/main/java/secureauth/ui/dialogs/ServiceAppointmentDialog.java`
- `src/main/java/secureauth/ui/components/HomeDashboardPanel.java`
- `src/test/java/secureauth/model/AppointmentStatusTest.java`

### Estado

- Inicio de Fase 1: estabilizacion.

### Riesgos

- Existen tablas legacy paralelas para agenda: `appointments` y `citas_servicio`.
- La UI de ventas todavia coordina reglas de negocio.
- La migracion de esquema sigue distribuida en DAOs hasta implementar scripts versionados.
- Debe verificarse que reportes historicos interpreten correctamente estados legacy y estados nuevos.
