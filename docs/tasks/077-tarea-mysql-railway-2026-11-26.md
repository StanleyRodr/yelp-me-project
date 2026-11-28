# 077 — Tarea: MySQL en Railway

> Plan: `013-plan-despliegue-2026-11-26.md` · Rama: `feature/despliegue` · Estado: ✅ Hecha

## Objetivo
Base MySQL en la nube con el esquema oficial y datos de demo.

## Pasos

### 1. (Tú) Crear proyecto y servicio
- Railway → New Project → "YelpMe".
- Add → Database → MySQL (versión 8). Nombre del servicio: `MySQL`.

### 2. CLI
```bash
npm i -g @railway/cli
railway login          # (Tú) abre el navegador
railway link           # elegir el proyecto YelpMe
```

### 3. Aplicar el esquema (una vez)
```bash
railway connect MySQL < database/script-bd.sql
```
(Si `connect` no acepta stdin, usar las variables públicas `MYSQL_PUBLIC_URL` del servicio con el cliente `mysql` local: `mysql -h … -P … -u root -p < database/script-bd.sql`.)
El script crea la base `YelpMe`; la API usará `DB_DATABASE=YelpMe`.

### 4. Comprobar
```sql
USE YelpMe; SHOW TABLES;   -- 12 tablas (9 + codigos_recuperacion + domicilios; contactos con favorito)
SELECT correo, rol FROM usuarios;   -- admin inicial
```

### 5. Datos de demo
Se cargan en la tarea 078, desde el servicio `api` (necesita Laravel):
```bash
railway run --service api php artisan db:seed
```

### 6. Respaldo
Anotar en `docs/` (tarea 081) cómo exportar: `mysqldump` con la URL pública. Railway no sustituye un respaldo propio antes de la demo.

## Cómo se hizo (2026-11-27)
- Cuenta en plan **Free** (1 proyecto, 3 servicios, 1 volumen de 0.5 GB): se reutilizó el proyecto vacío `feisty-surprise` (renombrable a YelpMe en el panel). `railway link --project feisty-surprise` + `railway add --database mysql` → servicio `MySQL` (8.4.13), solo red privada.
- Acceso sin proxy público: túnel SSH del CLI. Llave `~/.ssh/id_ed25519` registrada con `railway ssh keys add --name yelpme-pc`.
  ```bash
  railway connect MySQL --tunnel-only -P 3407     # deja el túnel abierto en 127.0.0.1:3407
  ```
- El cliente `mysql` de XAMPP (MariaDB) no soporta `caching_sha2_password` de MySQL 8 → el script se aplicó con PHP/PDO (`PDO::MYSQL_ATTR_MULTI_STATEMENTS`, usuario y contraseña de `railway variables --service MySQL --json`, sin imprimirlos).
- Datos de demo: en la tarea 078, desde el servicio `api` (`ADMIN_PASSWORD` y `DEMO_PASSWORD` de sus variables).
- Respaldo: con el túnel abierto, `mysqldump` de MySQL 8 (o Workbench) contra `127.0.0.1:3407`.

## Verificación
`SHOW TABLES`: 11 tablas (las mismas que en local; el "12" del paso 4 era un error de cuenta). Admin inicial `admin@yelpme.com` presente.

## Resultado
Commit: `docs(despliegue): documenta la base MySQL en Railway` (solo documentación de la tarea; sin secretos). Ver tabla del plan.
