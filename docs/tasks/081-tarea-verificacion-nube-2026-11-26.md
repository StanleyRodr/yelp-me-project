# 081 — Tarea: Verificación en la nube

> Plan: `013-plan-despliegue-2026-11-26.md` · Rama: `feature/despliegue` · Estado: ⬜ Pendiente

## Objetivo
Los 11 criterios del MVP (plan principal 13) en producción; documentación final del proyecto.

## Pasos

### 1. Recorrido (panel en Vercel + APK en teléfono, sin la PC)
| # | Criterio | Resultado |
|---|---|---|
| 1 | Registro/login app; login admin web | |
| 2 | CRUD de contactos con vinculación | |
| 3 | A no ve datos de B | |
| 4 | Llamada directa | |
| 5 | Solicitud con GPS y servicios en el mapa | |
| 6 | Admin corrige, despacha y termina | |
| 7 | Broma con strike → suspensión → rehabilitación | |
| 8 | Push a contacto y solicitante | |
| 9 | Recuperar contraseña por correo | |
| 10 | Sin GPS con domicilio; llamada al favorito | |
| 11 | Todo en la nube | |

### 2. Documentación
- `README.md` raíz: qué es YelpMe, enlaces (panel, API, release del APK), arquitectura (diagrama simple), cómo correr en local, cómo desplegar (resumen de tareas 076–080), **lista de variables sin valores**.
- `backend/README.md`: sección "Producción".
- `CLAUDE.md`: tabla de estado (todo "En producción"); comandos de Railway/Vercel.
- `principal-plan.md`: sección 11 y "✅ Plan 013".
- Plan 013: tareas ✅, criterios `[x]`; anotar dominios reales.
- Respaldo de BD: comando `mysqldump` (tarea 077).

### 3. Secretos (última revisión)
```bash
git ls-files | grep -iE "\.env$|credenciales|google-services|keystore\.properties|\.jks"   # vacío
git log -p | grep -iE "MAIL_PASSWORD=.+|ADMIN_PASSWORD=.+"                                # vacío
```

### 4. PR
"Plan 013: despliegue del MVP". Tras fusionar, Railway y Vercel despliegan `main`; repetir criterios 1 y 5 como humo.

## Resultado
Commit: `docs: documenta el despliegue y cierra el MVP`.
