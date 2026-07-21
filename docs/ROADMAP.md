# Roadmap

El roadmap sigue el plan aprobado en la auditoria tecnica. Cada fase debe terminar con compilacion, pruebas y documentacion actualizada.

## Fase 1: Estabilizacion

Objetivo: corregir inconsistencias, reducir duplicacion inicial y preparar el proyecto para cambios mayores sin romper compatibilidad.

Tareas:

- Alinear Java objetivo entre README, Maven y documentacion.
- Crear documentacion tecnica viva.
- Identificar y aislar constantes/estados repetidos.
- Mantener compatibilidad con DAOs y UI existentes.
- Ejecutar `mvn test`.

Estado: en curso.

Avance:

- Documentacion base creada.
- Build alineado a Java 21+.
- Estados de agenda centralizados con compatibilidad legacy.
- Pruebas unitarias agregadas para normalizacion de estados.

Pendiente:

- Extraer mas reglas fuera de Swing.
- Preparar capas de dominio en Fase 2.
- Separar migraciones runtime en scripts en Fase 3.

## Fase 2: Nueva Arquitectura Del Dominio

Objetivo: crear capas nuevas para dominio, aplicacion e infraestructura sin romper el flujo actual.

Tareas:

- Crear paquetes `domain`, `application`, `infrastructure` y `shared`.
- Crear DTOs y Commands para venta de servicios.
- Crear mappers entre UI/modelos legacy y dominio nuevo.
- Introducir repositories como frontera hacia JDBC.

Estado: en curso.

Avance:

- Se crearon DTOs iniciales para ventas, clientes, mascotas y citas.
- Se agregaron mappers entre DTOs y modelos legacy.
- Se introdujeron validadores de venta, agenda e inventario.
- Se agrego `SalesCartUseCase` para sacar calculos del carrito fuera de Swing.
- Se agrego `RegisterSaleUseCase` para registrar ventas desde DTOs sin construir `Venta` en la UI.
- Se introdujeron interfaces Repository y una implementacion JDBC inicial para ventas.
- Se preparo una base de eventos internos con `SaleRegisteredEvent`.

Pendiente:

- Migrar `SalesServiceCatalog`, `GestionVentasServiciosDialog`, `HomeDashboardPanel`, `PanelConfig`, `RegistroTrabajadores` y `UiTheme`.
- Mover progresivamente implementaciones JDBC fuera de servicios legacy.
- Reemplazar el puente temporal `SalesController` por controladores de aplicacion mas pequenos.
- Mantener Fase 3 bloqueada hasta terminar la extraccion de responsabilidades UI criticas.

## Fase 3: Nuevo Modelo De Base De Datos

Objetivo: agregar tablas para ordenes de servicio, productos sugeridos, consumos e historial.

Tareas:

- Crear scripts SQL idempotentes.
- Agregar migraciones no destructivas.
- Documentar claves, relaciones y cardinalidad.
- Mantener tablas legacy durante la transicion.

Estado: pendiente.

Nota: antes de modificar base de datos se implemento el flujo funcional de venta
de servicios sobre el modelo actual. Esta iteracion no agrega tablas nuevas ni
consume inventario automaticamente.

Avance funcional previo a base de datos:

- Se introdujo `SaleItemType` para separar `PRODUCT` y `SERVICE`.
- La apertura del dialogo de agendamiento ocurre automaticamente al seleccionar un servicio.
- El carrito acepta productos y servicios juntos; cada servicio conserva su `AppointmentDTO`.
- `RegisterSaleUseCase` consolida las citas desde el carrito y persiste venta+citas en una unica transaccion legacy.
- `ServiceAppointmentDialog` se reorganizo en Cliente, Mascota, Servicio y Resumen.
- `GestionVentasServiciosDialog` usa el enum de dominio para evitar tipos ambiguos en el catalogo.

## Fase 4: Orden De Servicio Y Preparacion De Inventario

Objetivo: implementar orden de servicio con productos utilizados y preparar el
dominio para futuro consumo de inventario sin descontar stock todavia.

Tareas:

- Crear dominio `ServiceOrder`, `ServiceOrderItem`, `ServiceProduct`, `ServiceSummary` y `ServiceOrderStatus`.
- Crear DTOs propios de orden de servicio.
- Crear casos de uso `CreateServiceOrderUseCase`, `UpdateServiceOrderUseCase` y `ValidateServiceOrderUseCase`.
- Asociar productos usados y sugeridos a la orden.
- Ampliar `ServiceAppointmentDialog` con productos utilizados y resumen economico separado.
- Mantener inventario solo como selector, sin stock, movimientos ni migraciones.

Estado: completada funcionalmente sobre modelo en memoria.

Avance:

- Orden de servicio creada como dominio independiente de Swing/JDBC/DAOs.
- Productos utilizados y sugeridos se mantienen en la orden sin persistencia definitiva.
- El dialogo permite agregar, editar y eliminar productos utilizados.
- Resumen economico recalcula servicio, productos, subtotal, IVA, descuento y total.
- Pruebas unitarias agregadas para casos de uso, validaciones, resumen, productos utilizados y sugeridos.

Pendiente para Fase 5:

- Definir persistencia definitiva de ordenes de servicio.
- Implementar consumo de inventario, movimientos y trazabilidad.
- Integrar historial clinico y estados operativos avanzados.

## Fase 5: Integracion De Orden De Servicio Con Inventario

Objetivo: conectar la orden de servicio con persistencia adaptada e inventario
sin activar todavia las tablas nuevas definitivas.

Tareas:

- Extraer `ServiceOrderDTO` desde los items de venta.
- Registrar ordenes de servicio dentro de la transaccion actual.
- Validar productos existentes, activos y con stock suficiente.
- Descontar inventario de productos utilizados.
- Registrar actividad temporal de orden y consumo.
- Preparar scripts SQL para `service_order`, `service_order_item`,
  `inventory_consumption`, `service_history` y `service_product_template`.

Estado: implementada sobre adaptadores legacy.

Pendiente:

- Ejecutar migracion fisica cuando reportes/dashboard lean el nuevo modelo.
- Reemplazar trazabilidad temporal en `actividad_reciente` por
  `inventory_consumption` y `service_history`.
- Agregar integracion visual de eventos para refresco de inventario fuera del dashboard.

## Fase 6: Agenda

Objetivo: implementar agenda avanzada basada en ordenes de servicio.

Tareas:

- Tabla con estado, mascota, propietario, veterinario, servicio, fecha, hora, duracion, precio, productos y acciones.
- Filtros y busqueda.
- Colores por estado.
- Ordenamiento.

Estado: pendiente.

## Fase 7: UX

Objetivo: mejorar ergonomia y consistencia visual.

Tareas:

- Autocompletado.
- Validaciones inmediatas.
- Mensajes claros.
- Atajos de teclado.
- Componentes reutilizables.
- Indicadores de carga.

Estado: pendiente.
