# INFORME TÉCNICO DE AUDITORÍA INICIAL (FASE 0)
## ERP SECUREAUTH DESKTOP

**Fecha:** 2026-07-22
**Autor:** Arquitecto de Software Senior
**Objetivo:** Analizar en profundidad la arquitectura actual, el modelo físico de la base de datos MySQL, las relaciones de dependencias entre módulos y las áreas de optimización de código para el plan maestro de desarrollo de SecureAuth Desktop.

---

## 1. ANÁLISIS DE ARQUITECTURA GENERAL

SecureAuth Desktop es un sistema ERP/POS para clínicas veterinarias y negocios multi-rubro desarrollado sobre **Java 21**, usando **Swing** para la capa de presentación, **Maven** para la gestión del ciclo de vida del proyecto, y **MySQL** como motor de persistencia relacional con el pool de conexiones **HikariCP**.

### 1.1 Cumplimiento de Principios Arquitectónicos
*   **Clean Architecture:** El proyecto está en un proceso de migración progresiva hacia Clean Architecture. Se aprecia una separación clara de responsabilidades en módulos clave como Ventas (`SalesCartUseCase`, `RegisterSaleUseCase`), pero aún existen acoplamientos fuertes en pantallas legacy que instancian DAOs y servicios directamente desde la UI de Swing.
*   **SOLID:**
    *   *Single Responsibility Principle (SRP):* Algunas clases de Swing (como `SalesPanel`, `PanelConfig` y `LoginFrame`) acumulan responsabilidades de presentación, validación y flujos de coordinación de negocio. La reciente extracción del programador multi-día a la utilidad `ServiceScheduleHelper` es un excelente ejemplo del camino a seguir.
    *   *Open/Closed Principle (OCP):* El catálogo de ítems de venta utiliza enums flexibles como `SaleItemType` lo que permite extender tipos de venta sin modificar la lógica interna del carrito.
    *   *Dependency Inversion Principle (DIP):* Existen capas desacopladas mediante interfaces como `SalesRepository` y su implementación JDBC (`JdbcSalesRepository`), lo que facilita la sustitución de la persistencia y la realización de pruebas unitarias aisladas.
*   **MVC & Repository Pattern:** Se emplean controladores compatibles (`SalesController`) que actúan como puentes entre las vistas y los casos de uso. El patrón Repository unifica el acceso a la persistencia evitando la dispersión de transacciones a bajo nivel de JDBC.

---

## 2. AUDITORÍA DEL MODELO FÍSICO DE BASE DE DATOS (MySQL)

A continuación, se detalla el análisis estructural de las tablas físicas administradas por el sistema.

### 2.1 Tabla: `business_type`
*   **Propósito:** Define los rubros comerciales soportados por el ERP multi-negocio (ej. Veterinaria, Hospitalización, Peluquería).
*   **Campos Clave:** `id` (PK, Auto-incremental), `name` (VARCHAR 120, Unique).
*   **Índices & Constraints:** Restricción Unique sobre `name` para evitar tipos duplicados.
*   **Oportunidades de Mejora:** Ninguna crítica detected. Cumple con la normalización básica.

### 2.2 Tabla: `business`
*   **Propósito:** Almacena la información de las empresas/negocios registrados.
*   **Campos Clave:** `id` (PK), `business_type_id` (FK).
*   **Índices & Constraints:** `fk_business_type` apuntando a `business_type(id)`.
*   **Oportunidades de Mejora:** Se requiere agregar un índice explícito sobre `business_type_id` si el volumen de registros crece para optimizar la carga de paneles multi-empresa.

### 2.3 Tabla: `branches`
*   **Propósito:** Sucursales asociadas a cada empresa.
*   **Campos Clave:** `id` (PK), `business_id` (FK).
*   **Índices & Constraints:** `fk_branch_business` apuntando a `business(id)`.
*   **Oportunidades de Mejora:** Falta un índice compuesto en `(business_id, status)` para agilizar el listado de sucursales activas en la UI de configuración.

### 2.4 Tabla: `roles`
*   **Propósito:** Catálogo de roles del sistema (Administrador, Supervisor, Recepcionista, Médico).
*   **Campos Clave:** `id` (PK), `nombre_rol` (VARCHAR 80, Unique).
*   **Oportunidades de Mejora:** Estructura estable y normalizada.

### 2.5 Tabla: `users`
*   **Propósito:** Usuarios, trabajadores y credenciales del sistema.
*   **Campos Clave:** `id` (PK), `email` (VARCHAR 160, Unique), `rol_id` (FK).
*   **Índices & Constraints:** `fk_users_roles` apuntando a `roles(id)`.
*   **Oportunidades de Mejora:**
    *   No tiene campos para control de seguridad como `intentos_fallidos`, `bloqueado_hasta` o `ultimo_login`.
    *   Falta un índice en `email` (creado implícitamente por la restricción `UNIQUE`).

### 2.6 Tabla: `owners`
*   **Propósito:** Propietarios o clientes del establecimiento veterinario.
*   **Campos Clave:** `id` (PK).
*   **Oportunidades de Mejora:** Falta un índice sobre `nombre_completo` para optimizar búsquedas dinámicas tipo autocompletado en el flujo de citas y ventas.

### 2.7 Tabla: `pets`
*   **Propósito:** Mascotas registradas en el sistema.
*   **Campos Clave:** `id` (PK), `owner_id` (FK), `business_id` (FK/int).
*   **Índices & Constraints:** `fk_pets_owner_bootstrap` apuntando a `owners(id)`.
*   **Oportunidades de Mejora:**
    *   Falta índice compuesto sobre `(owner_id, nombre_mascota)` para consultas rápidas de mascotas por dueño.
    *   El campo `peso` es un decimal de precisión fija que cumple con los requerimientos clínicos.

### 2.8 Tabla: `inventory_items`
*   **Propósito:** Productos y bienes físicos en stock por negocio/sucursal.
*   **Campos Clave:** `id` (PK).
*   **Oportunidades de Mejora:**
    *   La columna `stock` y `stock_minimo` no tienen constraints de no-negatividad a nivel de base de datos (`CHECK (stock >= 0)`).
    *   Falta un índice en `sku` o `codigo_barras` si se integra escáner en el módulo de ventas.

### 2.9 Tabla: `appointments`
*   **Propósito:** Citas registradas en el sistema, adaptadas para rangos multi-día.
*   **Campos Clave:** `id` (PK), `service_id` (FK/int), `owner_id` (FK/int), `pet_id` (FK/int).
*   **Índices & Constraints:** No tiene llaves foráneas explícitas hacia `owners` ni `pets` declaradas formalmente en algunas instalaciones de BD debido a la naturaleza dinámica del bootstrap del esquema.
*   **Oportunidades de Mejora:**
    *   Normalizar agregando llaves foráneas (`CONSTRAINT fk_appointments_owner FOREIGN KEY...`).
    *   Añadir índices compuestos en `(appointment_date, endDate)` para acelerar las búsquedas por rangos del calendario.

### 2.10 Tabla: `actividad_reciente`
*   **Propósito:** Registro central de auditoría del sistema (recientemente optimizado con categorías y marcas de tiempo directas de base de datos).
*   **Campos Clave:** `id` (PK).
*   **Oportunidades de Mejora:**
    *   Agregar un índice en `timestamp_real` o `fecha_real` debido a que el dashboard ordena constantemente por este campo.

---

## 3. IDENTIFICACIÓN DE CÓDIGO DUPLICADO Y OBSOLETO

### 3.1 Agendas Duplicadas
*   **Inconsistencia:** Coexistencia de las tablas `appointments` (usada en el nuevo agendamiento multi-día integrado en ventas) y `citas_servicio` (agenda legacy).
*   **Acción Recomendada:** Despreciar gradualmente el uso de `citas_servicio` y redirigir toda la lógica visual hacia `appointments` a través de `AppointmentService`.

### 3.2 Lógica de Migraciones Embebida en DAOs
*   **Inconsistencia:** Los DAOs ejecutan bloques `ensureSchema()` de manera distribuida durante su arranque.
*   **Acción Recomendada (Fase 1/10):** Unificar y centralizar las inicializaciones y migraciones en un único servicio consolidado (`MigrationService`) que valide la versión actual del esquema mediante una tabla `schema_version`.

### 3.3 Persistencia de Configuración Mixta
*   **Inconsistencia:** El ERP mezcla persistencia en archivo de texto plano (`secureauth.config.txt`) a través de `ApplicationVisualSettings` y configuraciones que deberían vivir en base de datos.
*   **Acción Recomendada:** Migrar los parámetros generales y de negocio a una tabla física `app_settings` en MySQL para evitar inconsistencias de lectura/escritura concurrentes.

---

## 4. CONSULTAS SQL INEFICIENTES Y RIESGOS DE RENDIMIENTO

1.  **Búsqueda Completa de Textos sin Índices:**
    *   *Riesgo:* Consultas dinámicas sobre `owners` buscando coincidencias en `nombre_completo` usan cláusulas `LIKE '%texto%'` que fuerzan escaneos completos de tabla (*Full Table Scan*).
    *   *Solución:* Crear un índice sobre la columna `nombre_completo` y, opcionalmente, migrar a índices Full-Text si la base de clientes es superior a 10,000 registros.
2.  **Operaciones Fuera de Transacciones:**
    *   *Riesgo:* Creaciones de ventas y citas en paralelo que no estén enmarcadas bajo `Connection.setAutoCommit(false)` pueden resultar en datos huérfanos si se interrumpe la conexión a mitad de la operación.
    *   *Solución:* Validar que todas las llamadas mutacionales múltiples en el repositorio JDBC usen la misma conexión y apliquen `commit()` u `rollback()` de forma explícita.

---

## 5. RECOMENDACIONES DE IMPLEMENTACIÓN PARA LAS SIGUIENTES FASES

1.  **Fase 1 (Mejoras Globales):** Implementar la tabla `app_settings` y centralizar la autenticación basada en roles tanto en Swing como a nivel de capa de negocio.
2.  **Fase 3 (Dashboard):** Optimizar la consulta de actividad reciente añadiendo el índice propuesto en `timestamp_real` para evitar ralentizaciones en el panel inicial.
3.  **Fase 4 (Módulo de Ventas):** Consolidar la exclusión de control de stock sobre servicios de forma limpia en `SalesCartUseCase`.
4.  **Fase 10 & 11 (Optimización y Validaciones):** Asegurar que toda sentencia SQL del sistema utilice parámetros preparados (`PreparedStatement`) y que las llamadas bloqueantes a la base de datos se ejecuten asíncronamente mediante `SwingWorker` para no congelar la interfaz de usuario.
