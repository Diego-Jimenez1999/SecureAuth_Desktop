# Fase 1 — Seguridad y saneamiento del repositorio

Fecha de cierre: 2026-08-11

## Prerrequisito: Fase 0

Se restauró la limpieza de regresiones legado autorizada antes de esta fase. Se eliminaron 21 fuentes: los 19 archivos en español/legado presentes (incluido `TestUserDAO` en su ubicación real `model/`) y los dos duplicados de `secureauth.ui.sales`.

Antes de cada eliminación se comprobó que no hubiera referencias Java externas al conjunto de archivos retirados. Los únicos imports de `secureauth.ui.sales` pertenecían a los diálogos legado que también se retiraron. `mvn compile` terminó en verde.

## Acciones de saneamiento

1. **`.env`**: se eliminó la copia local que contenía credenciales. Git confirmó que no estaba versionada; `.gitignore` la excluye.
2. **`.env.example`**: se creó en la raíz con URL local sin credenciales y valores vacíos para usuario y contraseña. Debe incluirse en el próximo commit.
3. **`.gitignore`**: conserva las exclusiones existentes de Maven, `.env`, VS Code y sistema operativo; se añadieron metadatos de Eclipse/Fleet, `.agents/` y las dos rutas de tooling generado bajo `.github/`.
4. **`target/`**: no está versionado y se eliminó después de las validaciones con `mvn clean`.
5. **Configuración y tooling**: `.vscode/` era configuración local ignorada y se retiró de la distribución. `.agents/` estaba vacío y se excluye. `.github/java-upgrade/` y `.github/modernize/` no contenían workflows ni referencias del proyecto; contenían planes, logs, reportes y hooks generados. Se retiraron sus archivos y se excluyeron explícitamente. La decisión es no versionarlos; los workflows legítimos futuros pueden vivir en otras rutas de `.github/`.
6. **Historial Git**: la revisión completa encontró el valor débil histórico en commits anteriores, entre ellos `c8087bf` y `7533195`, además de sus referencias documentales posteriores. La contraseña de MySQL debe rotarse si aún existe o se reutilizó. No se reescribió historial: requiere coordinación explícita del equipo mediante `git filter-repo` u operación equivalente antes de publicar un historial compartido.
7. **Búsqueda de secretos actual**: se revisaron `src/`, `docs/`, `README.md`, `pom.xml` y la configuración versionable para contraseñas, tokens, claves y claves privadas. No quedaron credenciales reales ni valores `root`/`1234`; los valores de pruebas son fixtures y la documentación usa placeholders o describe el saneamiento sin repetir la credencial.
8. **Configuración externa de base de datos**: no se modificó `DatabaseConnection`; se conserva la cadena system property → variable de entorno → `.env` → fallo rápido.

## Validación

- `mvn compile` — correcto tras Fase 0.
- `mvn clean test` — correcto: 46 pruebas, 0 fallos, 0 errores.
- `mvn clean package` — correcto.
- `mvn clean` final — correcto; no queda `target/` en el árbol de trabajo.

## Checklist final

| Elemento | Estado |
| --- | --- |
| `.env` | No presente ni versionado |
| `.env.example` | Preparado para versionar, sin secretos |
| `target/` | No presente ni versionado |
| Secretos actuales | 0 credenciales reales detectadas |
