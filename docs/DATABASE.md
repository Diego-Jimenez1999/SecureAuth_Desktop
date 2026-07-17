# Base De Datos

SecureAuth Desktop utiliza MySQL 8+.

## Conexion

La conexion se centraliza en `secureauth.config.DatabaseConnection` y usa HikariCP cuando esta disponible.

Variables:

- `SECUREAUTH_DB_URL`
- `SECUREAUTH_DB_USER`
- `SECUREAUTH_DB_PASSWORD`

## Modelo Actual

Tablas principales:

| Tabla | Responsabilidad |
| --- | --- |
| `users` | Usuarios y trabajadores. |
| `roles` | Roles base. |
| `owners` | Propietarios/clientes. |
| `pets` | Mascotas. |
| `inventory_items` | Productos de inventario por negocio/sucursal. |
| `sales_categories` | Categorias y subcategorias de ventas. |
| `sales_items` | Servicios/productos vendibles no inventariados. |
| `sales_item_sizes` | Precios por tamano. |
| `ventas` | Cabecera de venta. |
| `detalle_venta` | Detalle de venta. |
| `sales_tx` | Transacciones resumidas para dashboard/reportes. |
| `appointments` | Citas creadas desde ventas. |
| `citas_servicio` | Agenda legacy de servicios. |
| `actividad_reciente` | Eventos visibles en dashboard. |

## Inconsistencias Conocidas

- `appointments` y `citas_servicio` modelan agendas similares.
- `detalle_venta.id_producto` no declara una FK hacia `inventory_items`.
- `sales_tx` duplica datos resumidos de ventas.
- Las migraciones estan embebidas en DAOs.

## Estados De Agenda

Los estados operativos se centralizan en `AppointmentStatus`.

| Estado objetivo | Valor BD | Alias legacy |
| --- | --- | --- |
| Pendiente | `PENDIENTE` | - |
| Confirmada | `CONFIRMADA` | - |
| En proceso | `EN_PROCESO` | - |
| Finalizada | `FINALIZADA` | `REALIZADO` |
| Cancelada | `CANCELADA` | `CANCELADO` |

La aplicacion acepta aliases legacy para no romper citas existentes.

## Modelo Objetivo

| Tabla | Relacion | Justificacion |
| --- | --- | --- |
| `service_orders` | Cliente 1:N, mascota 1:N, servicio 1:N | Representa la orden clinica/operativa. |
| `service_order_products` | Orden 1:N productos usados | Permite saber que se uso en cada servicio. |
| `service_suggested_products` | Servicio N:M inventario | Precarga insumos frecuentes. |
| `inventory_consumptions` | Orden 1:N consumos | Trazabilidad de descuento de stock. |
| `service_status_history` | Orden 1:N cambios de estado | Auditoria operativa. |
| `service_status` | Catalogo 1:N ordenes | Estados normalizados. |
| `invoice` | Venta 1:0..1 factura | Separacion fiscal/comercial. |

## Cardinalidades Objetivo

```text
owner 1 -> N pet
pet 1 -> N service_order
sales_item/service 1 -> N service_order
service_order 1 -> N service_order_product
service_order 1 -> N inventory_consumption
service_order 1 -> N service_status_history
venta 1 -> N detalle_venta
venta 0..1 -> N service_order
```

## Migraciones

Las migraciones versionadas se agregaran en Fase 3. Hasta entonces se conserva `ensureSchema()` para compatibilidad.
