# INFORME TÉCNICO DE ANÁLISIS Y DISEÑO — FASE 1
## CONTROL DE VENTANAS, CONTROL DE SESIÓN, RBAC Y TIPOS DE NEGOCIO DINÁMICOS

**Fecha:** 2026-07-22
**Autor:** Arquitecto de Software Senior
**Estado:** Propuesta de Diseño (Listo para Revisión)

---

## 1. INTRODUCCIÓN Y OBJETIVOS

El propósito de la **Fase 1** es robustecer la arquitectura global del sistema mediante un control centralizado de la navegación (evitando la creación dispersa de JFrames), unificar el estado de autenticación (SessionManager), implementar un control estricto de acceso basado en roles (RBAC) y habilitar la modularidad dinámica del sistema dependiendo del rubro comercial (veterinaria, peluquería, tienda).

---

## 2. PAQUETES Y CLASES INVOLUCRADAS

### 2.1 Clases Nuevas a Crear
1.  `secureauth.ui.config.WindowManager`
    *   **Propósito:** Controlador centralizado de ventanas (patrón Singleton/Mediator). Encargado de abrir, cerrar, maximizar y reutilizar instancias de JFrames, JDialogs y JPanels sin duplicidad ni fugas de memoria.
2.  `secureauth.service.enterprise.SessionManager`
    *   **Propósito:** Gestor único en memoria de la sesión activa, empresa activa, sucursal activa, rol del usuario, permisos y parámetros de configuración actuales.
3.  `secureauth.service.enterprise.AuthorizationService`
    *   **Propósito:** Servicio que valida si el rol cargado en el `SessionManager` posee el permiso específico para ejecutar operaciones de negocio.
4.  `secureauth.service.enterprise.ModuleConfigurationService`
    *   **Propósito:** Servicio encargado de consultar los módulos habilitados/deshabilitados según el tipo de negocio registrado en MySQL.
5.  `secureauth.shared.util.SoundManager`
    *   **Propósito:** Generador asíncrono y multiplataforma de tonos sintetizados (Error, Advertencia, Confirmación, Login, etc.) usando la API estándar de audio de Java.
6.  `secureauth.ui.dialogs.NotificationCenterDialog`
    *   **Propósito:** Interfaz de usuario para visualizar, marcar como leídas y limpiar notificaciones dinámicas del sistema.
7.  `secureauth.shared.error.AccessDeniedException`
    *   **Propósito:** Excepción personalizada de negocio para denegar transacciones no autorizadas.

### 2.2 Clases Existentes a Modificar
1.  `secureauth.ui.frames.LoginFrame`
    *   **Cambio:** Delegar la transición post-login al `WindowManager`. Almacenar el usuario autenticado en `SessionManager`. Eliminar el botón "Registrarse" o adaptarlo a "Solicitar acceso".
2.  `secureauth.ui.frames.IngresoFrame`
    *   **Cambio:** Utilizar `setExtendedState(JFrame.MAXIMIZED_BOTH)` para arranque completamente maximizado. Integrar el indicador de conexión MySQL en línea/fuera de línea y el centro de notificaciones con badge.
3.  `secureauth.ui.components.SidebarPanel`
    *   **Cambio:** Cargar las opciones del menú de forma dinámica consultando a `ModuleConfigurationService` y `AuthorizationService` (evitando condiciones harcodeadas en la UI).
4.  `secureauth.config.DatabaseConnection`
    *   **Cambio:** Agregar un hilo monitor en segundo plano para alertar desconexión de MySQL y activar reconexión automática sin bloquear Swing.
5.  `secureauth.service.UserService` y `secureauth.service.enterprise.InventoryService`
    *   **Cambio:** Validar permisos de forma asertiva al inicio de cada método sensible (ej. `insert()`, `delete()`) consultando a `AuthorizationService`.

---

## 3. DIAGRAMA DE FLUJO: AUTENTICACIÓN Y AUTORIZACIÓN (RBAC)

```text
+--------------+        +-------------------+        +----------------------+
|  LoginFrame  | ---->  |    AuthService    | ---->  |    SessionManager    |
| (Credenciales|        |  (Valida Hash y   |        | (Carga Usuario, Rol, |
|   Usuario)   |        |   Belonging BD)   |        |  Empresa y Permisos) |
+--------------+        +-------------------+        +----------------------+
                                                                |
                                                                v
+--------------+        +-------------------+        +----------------------+
|     CAPA     |        |   Authorization   | <----  |    WindowManager     |
| PRESENTACIÓN | <----> |      Service      |        |   (Abre Ventana      |
| (Swing View) |        | (Verifica Permiso)|        |  IngresoFrame Max)   |
+--------------+        +-------------------+        +----------------------+
       |                          |
       | (Operación)              | (Permitido / Denegado)
       v                          v
+--------------+        +-------------------+        +--------------+
|   BUSINESS   |        |   Si es Denegado  | ---->  | Log Auditoría|
|   SERVICES   | ---->  |   (Lanza Alerta   |        |  (Registro   |
| (Backend Ok) |        | AccessDeniedExc)  |        | Evento Error)|
+--------------+        +-------------------+        +--------------+
```

---

## 4. CAMBIOS REQUERIDOS EN LA BASE DE DATOS (MySQL)

Para soportar la configuración dinámica de módulos y permisos RBAC, se definen las siguientes estructuras complementarias de forma idempotente:

```sql
-- Tabla de configuraciones operativas de la empresa
CREATE TABLE IF NOT EXISTS app_settings (
    setting_key VARCHAR(100) PRIMARY KEY,
    setting_value TEXT NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Tabla para vincular permisos específicos a cada rol
CREATE TABLE IF NOT EXISTS role_permissions (
    role_id INT NOT NULL,
    permission_name VARCHAR(120) NOT NULL,
    PRIMARY KEY (role_id, permission_name),
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- Tabla para habilitar/deshabilitar módulos según tipo de negocio
CREATE TABLE IF NOT EXISTS business_modules (
    business_type_id INT NOT NULL,
    module_name VARCHAR(100) NOT NULL,
    PRIMARY KEY (business_type_id, module_name),
    FOREIGN KEY (business_type_id) REFERENCES business_type(id) ON DELETE CASCADE
);

-- Tabla de registro de eventos de seguridad (Intento no autorizado)
CREATE TABLE IF NOT EXISTS security_activity_log (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    user_email VARCHAR(160),
    role_name VARCHAR(80),
    business_id INT,
    branch_id INT,
    module_name VARCHAR(100),
    action_name VARCHAR(100),
    result_status VARCHAR(30) DEFAULT 'DENIED',
    event_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

---

## 5. RIESGOS DE COMPATIBILIDAD Y MITIGACIÓN

1.  **Riesgo: Bloqueo de hilos en Swing (EDT Freeze).**
    *   *Mitigación:* La verificación de pérdida de conexión y el monitoreo de MySQL se ejecutarán estrictamente mediante un demonio independiente en segundo plano (`DatabaseConnectionMonitor`).
2.  **Riesgo: Incompatibilidad de roles legacy.**
    *   *Mitigación:* Si un usuario no tiene `rol_id` o tiene un rol legacy, se le asignará por defecto el rol de `Invitado` (lectura únicamente), previniendo elevación accidental de privilegios.
3.  **Riesgo: Rendimiento al validar permisos recurrentemente.**
    *   *Mitigación:* Los permisos del rol autenticado se precargan en el `SessionManager` durante el arranque de sesión. Nunca se consulta repetidamente la base de datos para operaciones rutinarias en memoria.

---

## 6. PLAN DE IMPLEMENTACIÓN PASO A PASO

### Paso 1: Configuración Estructural y Base de Datos (Apartados 2, 4 y 5)
*   Crear los scripts e insertarlos de forma idempotente en el arranque mediante `ensureSchema()` de los DAOs correspondientes.
*   Inicializar los permisos básicos para cada rol (ej. Administrador completo; Recepcionista con `citas_crear`, `ventas_registrar`, etc.).

### Paso 2: Singleton Managers e Infraestructura (Apartados 2 y 3)
*   Codificar `SessionManager` y `AuthorizationService`.
*   Codificar `SoundManager` para contar con sonidos de confirmación/error sin dependencias de hardware bloqueantes.

### Paso 3: Controlador Centralizado de Ventanas (Apartado 1)
*   Implementar `WindowManager` como mediador principal. Redirigir el arranque en `MainApp` para inicializar el Login y coordinar la transición maximizada a `IngresoFrame`.

### Paso 4: Dinamicidad del Menú Lateral e Interfaz (Apartados 5, 6 y 7)
*   Integrar en `SidebarPanel` la carga selectiva de botones según los módulos activos para el rubro comercial actual.

### Paso 5: Protección de Negocio y Auditoría (Apartados 8 y 9)
*   Insertar aserciones de permisos en las firmas de operaciones de `UserService` e `InventoryService`.
*   Registrar intentos fallidos en la tabla `security_activity_log`.
