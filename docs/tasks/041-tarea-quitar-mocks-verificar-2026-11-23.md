# 041 — Tarea: Quitar mocks y verificar

> Plan: `007-plan-frontend-api-2026-11-23.md` · Rama: `feature/frontend-api` · Estado: ✅ Completada

## Objetivo
El panel solo usa la API. Verificar todos los services contra el backend real.

## Pasos

### 1. Borrar datos de ejemplo
- `git rm -r src/mocks src/services/simulacion.js src/services/reglasUsuarios.js`.
- Antes: `grep` confirma que nadie los importa.
- Reglas de strikes y transiciones de los mocks ya viven en el backend; no se duplican.

### 2. Comentarios de `constantes/`
- `transiciones.js` y `cuentas.js`: solo deciden qué botones mostrar; la validación es de la API.
- Indican su espejo en el backend (`TransicionesSolicitud`, `ReglasCuenta::ACCIONES`): cambiar ambos lados.

### 3. Verificación integral contra la API
Servidor: `DB_DATABASE=YelpMe_pruebas APP_URL=http://127.0.0.1:8010 php artisan serve --port=8010` (escribe en `YelpMe_pruebas`; comprobado en la tarea 038).

Scripts fuera del repo con `vite.ssrLoadModule`, `localStorage` simulado y `VITE_API_URL` al 8010. Antes de cada uno: `php artisan cache:clear` + `php artisan bd:pruebas`.

| Script | Resultado |
|---|---|
| Sesión (tarea 038) | 9/9 |
| Solicitudes y servicios (tarea 039) | 16/16 |
| Usuarios y perfil, foto incluida (tarea 040) | 19/19 |

**Total 44/44** sin mocks.

### 4. Límite de intentos en desarrollo
- Correr los scripts varias veces seguidas agotó el límite de 5 logins por minuto (plan 003): el frontend recibió 429 "Demasiados intentos. Intenta más tarde.".
- Es el comportamiento esperado. `php artisan cache:clear` reinicia el contador.

### 5. Cierre
- Servidor de prueba detenido; `YelpMe_pruebas` recreada limpia.
- `YelpMe` intacta: 0 tokens, admin "General", Ana con 2 strikes, 2 pendientes.
- `CLAUDE.md`: flujo de datos sin mocks, sesión, recarga periódica, cómo levantar el panel y cómo probar services contra la API.

## Verificación

```bash
npm run build           # sin errores
php artisan test        # 73 passed (backend sin cambios)
```

## Resultado
Commit: `refactor(frontend): elimina datos de ejemplo y verifica el panel contra la API`.
