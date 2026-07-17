# Flujo De Venta De Servicios

Este documento describe el flujo actual y el flujo objetivo para venta de servicios veterinarios.

## Flujo Actual

```text
Usuario selecciona producto o servicio en SalesPanel
 -> SalesController agrega al carrito
 -> SalesPanel calcula totales
 -> si hay servicio, abre ServiceAppointmentDialog
 -> usuario selecciona cliente, mascota, fecha y hora
 -> SalesPanel solicita metodo de pago
 -> SalesTransactionService registra venta, detalle, inventario, cita y actividad
```

## Fortalezas Actuales

- La venta con citas se registra dentro de una transaccion JDBC.
- El inventario se valida con bloqueo `FOR UPDATE`.
- Las citas se notifican al dashboard.

## Problemas A Resolver

- La UI decide reglas de negocio.
- No existe orden de servicio clinica.
- Los productos usados por un servicio no quedan vinculados a la cita o servicio.
- No hay historial de estados por servicio.
- No hay trazabilidad detallada de consumo de inventario.

## Flujo Objetivo

```text
Cliente
 -> Mascota
 -> Servicio
 -> Veterinario
 -> Fecha, hora y duracion
 -> Productos sugeridos
 -> Productos utilizados ajustables
 -> Resumen economico
 -> Orden de servicio
 -> Registro de venta/factura
 -> Consumo de inventario
 -> Historial de estado
 -> Agenda y dashboard
```

## Estados Objetivo

- Pendiente
- Confirmada
- En proceso
- Finalizada
- Cancelada

En Fase 1 los estados se centralizaron en `AppointmentStatus`. La aplicacion conserva compatibilidad de lectura con `REALIZADO` y `CANCELADO`, usados por datos anteriores.

## Datos Minimos De La Orden

- Cliente.
- Mascota.
- Servicio.
- Veterinario.
- Fecha y hora.
- Duracion.
- Precio.
- Productos utilizados.
- Observaciones.
- Usuario responsable.
- Estado actual.

## Transaccion Objetivo

Una operacion de registro debe persistir:

- Cabecera de orden de servicio.
- Venta o factura.
- Detalle economico.
- Productos consumidos.
- Descuento de inventario.
- Historial de estado.
- Actividad reciente.

## Implementacion Actual En Fase 1

- La creacion de citas desde ventas usa el estado centralizado `PENDIENTE`.
- Las acciones del dashboard actualizan a estados normalizados.
- El render de estados reconoce estados nuevos y aliases legacy.
