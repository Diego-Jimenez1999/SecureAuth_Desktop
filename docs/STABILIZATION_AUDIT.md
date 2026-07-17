# SecureAuth Desktop - Auditoria de estabilizacion

Fecha: 2026-05-23

Actualizacion: 2026-07-17. La ejecucion del roadmap se documenta ahora en `docs/ROADMAP.md`, `docs/ARCHITECTURE.md`, `docs/DATABASE.md`, `docs/MIGRATION.md` y `docs/SERVICE_FLOW.md`.

## Estado tecnico observado

- El proyecto compila con Java 21 y Maven.
- La arquitectura por capas ya existe parcialmente: UI, Controller, Service, Repository, DAO y Database.
- El SQL esta concentrado mayormente en DAOs. No se detecto SQL operativo embebido en eventos Swing.
- La deuda principal esta en clases Swing grandes, control de flujo dentro de paneles/dialogos y migraciones runtime dispersas.
- `PasswordHasher` ya usa BCrypt y conserva compatibilidad controlada con hashes legacy SHA-256 y texto plano temporal.
- La conexion a BD leia variables de entorno, pero conservaba un fallback inseguro de password `1234`.

## Riesgos prioritarios

1. Clases UI demasiado grandes:
   - `RegistroTrabajadores.java`
   - `PanelConfig.java`
   - `LoginFrame.java`
   - `RegMascotaPanel.java`
   - `SalesPanel.java`

2. Migraciones de esquema dentro de DAOs:
   - `EnterpriseBootstrapDAO`
   - `SalesCatalogDAO`
   - `SalesTransactionDAO`
   - `InventoryDAO`

3. Modelo ERP aun mixto:
   - usuarios/trabajadores ya estan en `users`
   - clientes/dueños usan `owners`
   - falta consolidar permisos, actividad, ventas normalizadas e inventario por movimientos

4. Persistencia de configuracion:
   - existen pantallas visuales/configuracion, pero se requiere separar estado UI de persistencia real.

## Cambios aplicados en esta intervencion

- Se elimino el fallback hardcodeado `SECUREAUTH_DB_PASSWORD=1234`.
- Se tipifico HikariCP directamente en `DatabaseConnection`.
- Se configuro `initializationFailTimeout=-1` para no bloquear el arranque Swing cuando MySQL no este disponible al inicio.
- Se creo `SchemaInspector` para validar tablas/columnas reales antes de aplicar migraciones progresivas.
- Se reemplazo logica duplicada `columnExists` en DAOs enterprise.

## Proxima migracion segura recomendada

1. Crear `MigrationService` y mover alli los `ensureSchema()`.
2. Crear migraciones idempotentes para:
   - `permissions`
   - `role_permissions`
   - columnas de seguridad en `users`: `last_login`, `failed_attempts`, `locked_until`
   - `user_activity_log`
3. Sacar DTOs de UI:
   - mover `WorkerRow` fuera de `UserDAO`
   - mover entradas de ventas fuera de `ui.sales`
4. Extraer de `LoginFrame`:
   - `RecentUsersManager`
   - `LoginViewModel`
   - navegacion post-login hacia un coordinador de ventanas
5. Normalizar ventas:
   - migrar progresivamente de `sales_tx` a `sales` y `sale_details`
   - mantener compatibilidad temporal leyendo ambas fuentes

## Regla de continuidad

Cada cambio debe mantener:

- compilacion Maven verde
- pruebas existentes verdes
- consultas con `PreparedStatement`
- migraciones idempotentes
- ningun cambio destructivo sobre tablas existentes

## Avance 2026-07-17

- Build alineado a Java 21+.
- Estados de agenda centralizados en `AppointmentStatus`.
- Documentacion tecnica viva creada para la migracion ERP.
