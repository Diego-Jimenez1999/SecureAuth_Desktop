# SecureAuth Desktop

Aplicacion de escritorio Java Swing + Maven para un ERP/POS veterinario con MySQL.

## Requisitos

- JDK 21
- Maven 3.9+
- MySQL 8+

## Configuracion de base de datos

La aplicacion lee la configuracion en este orden:

1. Propiedades JVM: `-DSECUREAUTH_DB_PASSWORD=...`
2. Variables de entorno:
   - `SECUREAUTH_DB_URL`
   - `SECUREAUTH_DB_USER`
   - `SECUREAUTH_DB_PASSWORD`
3. Archivo local `.env` en la raiz del proyecto

Ejemplo `.env` local:

```properties
SECUREAUTH_DB_URL=jdbc:mysql://localhost:3306/secureauth
SECUREAUTH_DB_USER=root
SECUREAUTH_DB_PASSWORD=1234
```

El archivo `.env` esta ignorado por Git para evitar publicar credenciales locales.

## Ejecutar con Maven

```bash
mvn clean compile
mvn exec:java
```

## Arquitectura

Consulta la guia completa en:

- `docs/ARQUITECTURA_SECUREAUTH.md`
- `docs/STABILIZATION_AUDIT.md`
