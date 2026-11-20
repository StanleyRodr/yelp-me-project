# 012 — Tarea: Dashboard

> Plan: `002-plan-frontend-estandarizacion-2026-11-19.md` · Rama: `feature/frontend-base` · Estado: Pendiente

## Objetivo
Inicio enfocado en lo que hay que atender: conteo por estado, cola de pendientes y mapa.

## Pasos
1. `components/dashboard/FranjaEstados`: `v-for` sobre los 5 estados; cada uno enlaza a `/solicitudes?estado=X`.
2. `components/dashboard/ColaPendientes`: pendientes de la más antigua a la más reciente; folio, título, usuario, servicios, tiempo de espera.
   - **Elemento memorable:** indicador de espera que se intensifica: < 10 min neutro, 10–30 min ámbar, > 30 min rojo. Etiqueta textual además del color.
   - Tiempo se actualiza cada 30 s. Solicitudes sin servicios marcadas "Solo contactos".
3. `components/dashboard/MapaIncidentes`: `MapaBase` con solicitudes activas (Pendiente y Enviado).
4. `views/InicioView.vue`: solo rejilla + secciones; skeletons mientras carga.

## Verificación
- Clic en pendiente o marcador → `/solicitudes/:id`.
- Skeleton visible en carga inicial.

## Resultado
Pendiente.
