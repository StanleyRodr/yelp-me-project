# 081 — Tarea: Verificación en la nube

> Plan: `013-plan-despliegue-2026-11-26.md` · Rama: `feature/despliegue` · Estado: ✅ Terminada

## Objetivo
Los 11 criterios del MVP (plan principal 13) en producción; documentación final del proyecto.

## Pasos

### 1. Recorrido (panel en Vercel + APK en teléfono, sin la PC)
| # | Criterio | Resultado |
|---|---|---|
| 1 | Registro/login app; login admin web | API y panel en línea; login de Carlos en el APK contra Railway ✅. Login admin con `ADMIN_PASSWORD`: manual |
| 2 | CRUD de contactos con vinculación | Probado en local (plan 010); misma API |
| 3 | A no ve datos de B | Pruebas de backend (108 OK) |
| 4 | Llamada directa | Probado en local (plan 010) |
| 5 | Solicitud con GPS y servicios en el mapa | Probado en local (plan 011); en nube: manual |
| 6 | Admin corrige, despacha y termina | Probado en local (planes 005/007) |
| 7 | Broma con strike → suspensión → rehabilitación | Probado en local (plan 006) |
| 8 | Push a contacto y solicitante | Push llegó al teléfono en producción ✅ |
| 9 | Recuperar contraseña por correo | Código llegó por Brevo a un correo real ✅ |
| 10 | Sin GPS con domicilio; llamada al favorito | Probado en local (planes 009–011) |
| 11 | Todo en la nube | Panel en Vercel, API + BD en Railway, APK en teléfono físico ✅ |

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
- Dominios: API `https://api-production-9782e676.up.railway.app`, panel `https://yelp-me-project.vercel.app`, APK en release `v1.0.0`.
- Secretos: ambas búsquedas vacías.
- Fotos: sin volumen en plan Free → se pierden al redesplegar (Hobby lo resuelve).
- Deploys fallidos en `121c9ab` (main): intentos de antes de fijar la raíz `/backend`; el servicio `yelp-me-project` ya no existe.
- Probado a mano: APK en teléfono físico, correo de recuperación y push en producción.
- Tras fusionar: Production Branch de Vercel y rama de Railway de vuelta a `main`.

Commit: `docs: documenta el despliegue y cierra el MVP`.
