# 003 — Tarea: Adaptar el modelo `User` a `usuarios`

> Plan: `001-plan-backend-base-2026-11-19.md` · Rama: `feature/backend-base` · Estado: Pendiente

## Objetivo
Que `App\Models\User` (único modelo del proyecto, solo para Sanctum) funcione sobre la tabla `usuarios` y pueda generar tokens.

## Subtareas
- [ ] `protected $table = 'usuarios'`.
- [ ] Usar `HasApiTokens` (Sanctum); quitar `HasFactory` y la referencia a `UserFactory`.
- [ ] `$fillable`: `nombre`, `apellidos`, `telefono`, `correo`, `password`, `rol`, `foto_perfil`, `fcm_token`, `activo`.
- [ ] `$hidden`: `password`, `fcm_token`.
- [ ] `casts`: `password => hashed`, `activo => boolean`, `deshabilitado_en => datetime`.
- [ ] Desactivar `remember_token` (la tabla no lo tiene).
- [ ] Comentario en la clase indicando que es la única excepción a "sin modelos".

## Criterios de aceptación
- En `tinker`: `User::where('correo','admin@yelpme.com')->first()` devuelve al admin.
- `createToken('prueba')` genera un token guardado en `personal_access_tokens`; luego se borra el token de prueba.
