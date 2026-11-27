# 077 — Tarea: MySQL en Railway

> Plan: `013-plan-despliegue-2026-11-26.md` · Rama: `feature/despliegue` · Estado: ⬜ Pendiente

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

## Verificación
`SHOW TABLES` correcto; admin presente.

## Resultado
Commit: `docs(despliegue): documenta la base MySQL en Railway` (solo documentación de la tarea; sin secretos).
