# Migracion

Este documento registra la estrategia de migracion tecnica y de datos.

## Objetivo

Evolucionar el ERP sin eliminar funcionalidades existentes ni romper instalaciones locales.

## Reglas

- Toda migracion debe ser idempotente.
- No se eliminan tablas legacy hasta tener respaldo y migracion validada.
- Las nuevas tablas se agregan de forma no destructiva.
- Cada fase debe compilar y pasar pruebas.

## Estado Actual

Los DAOs ejecutan `CREATE TABLE IF NOT EXISTS` y algunos `ALTER TABLE` en tiempo de ejecucion mediante `ensureSchema()`.

DAOs relevantes:

- `EnterpriseBootstrapDAO`
- `SalesCatalogDAO`
- `SalesTransactionDAO`
- `InventoryDAO`
- `AppointmentDAO`
- `AgendaServicioDAO`

## Estrategia

1. Mantener `ensureSchema()` mientras se agregan scripts SQL.
2. Crear carpeta de migraciones.
3. Registrar cambios versionados.
4. Mover gradualmente la inicializacion de esquema a un servicio de migraciones.
5. Migrar datos legacy solo cuando el nuevo flujo este probado.

## Compatibilidad

Tablas legacy conservadas:

- `sales_tx`
- `ventas`
- `detalle_venta`
- `appointments`
- `citas_servicio`

## Migracion De Estados De Agenda

Fase 1 no modifica datos existentes. La aplicacion normaliza nuevos cambios de estado hacia:

- `PENDIENTE`
- `CONFIRMADA`
- `EN_PROCESO`
- `FINALIZADA`
- `CANCELADA`

Aliases legacy aceptados:

- `REALIZADO` se interpreta como `FINALIZADA`.
- `CANCELADO` se interpreta como `CANCELADA`.

En Fase 3 se debe agregar un script opcional para actualizar datos historicos:

```sql
UPDATE appointments SET status = 'FINALIZADA' WHERE status = 'REALIZADO';
UPDATE appointments SET status = 'CANCELADA' WHERE status = 'CANCELADO';
```

Tablas objetivo a crear en fases posteriores:

- `service_orders`
- `service_order_items`
- `service_order_products`
- `service_status_history`
- `service_suggested_products`
- `inventory_consumptions`
- `invoice`
