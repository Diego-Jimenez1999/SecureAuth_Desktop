# Changelog

Todos los cambios relevantes del proyecto se documentan aqui.

## 2026-07-17

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
