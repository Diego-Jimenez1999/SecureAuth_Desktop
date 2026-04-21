# SecureAuth Desktop - Arquitectura y Guia de Defensa

## 1. Componentes del sistema

### UI (View)
- Responsabilidad: mostrar pantallas Swing y capturar eventos del usuario.
- Clases principales: `LoginFrame`, `IngresoFrame`, `EditUserFrame`.
- Regla clave: no ejecutar SQL ni reglas de negocio en esta capa.

### Controller
- Responsabilidad: coordinar acciones entre la UI y los servicios.
- Clases principales: `AuthController`, `IngresoController`.
- Regla clave: delegar validaciones de negocio al Service.

### Service
- Responsabilidad: aplicar reglas del dominio y casos de uso.
- Clases principales: `AuthService`, `UserService`.
- Ejemplos:
  - validacion de email y password
  - registro seguro con hash BCrypt
  - login seguro con verificacion de hash

### Repository
- Responsabilidad: definir contrato de persistencia desacoplado.
- Clases:
  - `UserRepository` (interfaz)
  - `UserRepositoryImpl` (implementacion)
- Beneficio: permite cambiar MySQL por otro motor sin romper la capa Service.

### DAO
- Responsabilidad: acceso SQL concreto y mapeo `ResultSet -> User`.
- Clase principal: `UserDAO`.
- Regla clave: no mezclar reglas de negocio en esta capa.

### Model
- Responsabilidad: representar el dominio del negocio.
- Clase principal: `User`.

## 2. Arquitectura aplicada

Flujo principal:

`UI -> Controller -> Service -> Repository -> DAO -> MySQL`

Beneficios:
- Menor acoplamiento.
- Mejor mantenibilidad.
- Facilita pruebas unitarias y refactorizacion.

## 3. Casos de uso implementados

### Registro de usuario
1. UI captura datos del formulario.
2. `AuthController` delega a `AuthService.register(user)`.
3. `AuthService`:
   - normaliza email
   - valida formato de email
   - valida fortaleza de password
   - verifica duplicados
   - hashea password con BCrypt
4. `Repository/DAO` inserta en MySQL.

### Login seguro
1. UI captura email y password.
2. `AuthController` delega a `AuthService.login(email, password)`.
3. `AuthService`:
   - valida entradas
   - busca usuario por email
   - verifica hash con BCrypt
   - retorna usuario autenticado o `null`
4. UI abre dashboard (`IngresoFrame`) si el login es valido.

## 4. Errores de diseno detectados y mejoras aplicadas

### Problema: UI acoplada al DAO
- Antes: `IngresoFrame` usaba `UserDAO` directamente.
- Mejora aplicada: la gestion paso a `IngresoController -> UserService -> UserRepository`.

### Problema: inconsistencia de columnas en busqueda
- Antes: en `buscarUsuarios` se agregaban 6 valores para una tabla de 5 columnas.
- Mejora aplicada: se unifico a 5 columnas (`ID`, `NOMBRE COMPLETO`, `EMAIL`, `GENERO`, `ACCION`).

### Problema: validacion de password en edicion era contradictoria
- Antes: el tooltip decia "opcional", pero la validacion la exigia.
- Mejora aplicada: password ahora es opcional y solo valida longitud minima cuando se informa.

## 5. Librerias y herramientas recomendadas

### Seguridad
- `org.mindrot:jbcrypt` para hashing robusto de contrasenas.

### Persistencia
- `com.mysql:mysql-connector-j` para conexion MySQL.

### Calidad de codigo
- `JUnit 5` para pruebas unitarias.
- Recomendado siguiente paso: `Mockito` para mocks en pruebas de servicios.

### Operacion
- Maven para build estandarizado.
- Git para control de versiones.

## 6. Conceptos tecnicos para defender el proyecto

- Hashing != cifrado reversible.
- BCrypt usa sal y costo computacional, por eso es mas seguro ante fuerza bruta.
- Separacion de capas reduce deuda tecnica.
- Inyeccion de dependencias mejora testabilidad.
- `PreparedStatement` reduce riesgo de SQL Injection.

## 7. Ruta paso a paso para evolucionar el proyecto

### Paso 1 (base actual)
- Login y registro seguros funcionando.
- Arquitectura MVC + Service + Repository establecida.

### Paso 2 (siguiente iteracion sugerida)
- Agregar pruebas unitarias en `AuthService` y `UserService`.
- Validar politicas de password mas fuertes.
- Unificar manejo de errores con excepciones de dominio.

### Paso 3 (produccion)
- Configurar pool de conexiones (`HikariCP`).
- Auditoria de login (fecha, usuario, resultado).
- Politicas de bloqueo temporal por intentos fallidos.
