# SecureAuth Desktop

Aplicacion de escritorio Java (Swing) para autenticacion segura con MySQL.

## Requisitos
- JDK 21
- Maven 3.9+
- MySQL 8+

## Variables de entorno
Configura estas variables antes de ejecutar:

- `SECUREAUTH_DB_URL` (ej: `jdbc:mysql://localhost:3306/secureauth`)
- `SECUREAUTH_DB_USER`
- `SECUREAUTH_DB_PASSWORD`

## Ejecutar con Maven
```bash
mvn clean compile
mvn exec:java
```

## Arquitectura
Consulta la guia completa en:

- `docs/ARQUITECTURA_SECUREAUTH.md`

## Git (repositorio local ya inicializado)
Para conectar a GitHub/GitLab:

```bash
git remote add origin <URL_DEL_REPOSITORIO>
git add .
git commit -m "feat: base secureauth con arquitectura mvc+service+repository"
git push -u origin main
```
