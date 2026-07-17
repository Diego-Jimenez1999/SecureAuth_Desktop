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

Estado: pendiente.

## Fase 3: Nuevo Modelo De Base De Datos

Objetivo: agregar tablas para ordenes de servicio, productos sugeridos, consumos e historial.

Tareas:

- Crear scripts SQL idempotentes.
- Agregar migraciones no destructivas.
- Documentar claves, relaciones y cardinalidad.
- Mantener tablas legacy durante la transicion.

Estado: pendiente.

## Fase 4: Nuevo Flujo De Venta De Servicios

Objetivo: implementar orden de servicio con productos utilizados, consumo de inventario, estados e historial.

Tareas:

- Crear caso de uso `RegistrarVentaServicio`.
- Asociar productos usados a la orden.
- Descontar inventario con trazabilidad.
- Registrar historial de estado.

Estado: pendiente.

## Fase 5: Nuevo JDialog

Objetivo: reemplazar el flujo disperso por un dialogo profesional dividido en paneles.

Tareas:

- Panel Cliente.
- Panel Mascota.
- Panel Servicio.
- Panel Productos utilizados.
- Panel Resumen economico.
- Botones Guardar, Agendar, Registrar venta y Cancelar.

Estado: pendiente.

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
