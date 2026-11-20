# 015 — Tarea: Perfil del admin

> Plan: `002-plan-frontend-estandarizacion-2026-11-19.md` · Rama: `feature/frontend-base` · Estado: ✅ Completada

## Objetivo
Perfil propio solo con datos que existen en la BD.

## Pasos

### 1. Service y store
- `usuariosService.actividadAdmin(adminId)`: solicitudes con `admin_id` = admin + conteo de `strikes.admin_id`. Endpoint futuro: `GET /api/perfil/actividad`.
- `stores/perfil.js`: `actividad`, `cargarActividad()`, `guardarDatos()` (actualiza también la sesión: navbar muestra el nombre nuevo), `cambiarPassword()`.

### 2. `components/perfil/FormPerfil`
- Campos con `v-for` sobre `CAMPOS` (nombre, apellidos, teléfono, correo) con `autocomplete` correcto e `inputmode` numérico en teléfono.
- Validación con mensajes que dicen cómo corregir: teléfono de 10 dígitos, correo con formato.
- Foto: input de archivo oculto con `label` como botón. Solo JPG/PNG/WEBP de hasta 1 MB. Vista previa con `FileReader` (con API se enviará multipart al disco `perfiles`). Foco visible en la etiqueta.
- Copia editable del admin; se rehace si el admin cambia desde fuera.

### 3. `components/perfil/FormPassword`
- Actual, nueva (mín. 8), confirmar (coincide). Ayuda debajo de cada campo; se reemplaza por el error.
- `defineExpose({ reiniciar })`: la vista limpia el formulario tras éxito.
- Simulación: la contraseña de demo no cambia en memoria; la siguiente sesión sigue con `Admin123*`.

### 4. `components/perfil/SolicitudesAtendidas`
- Resumen (`dl`, `v-for`): Atendidas, En curso (Enviado), Terminadas, Rechazadas, Strikes dados.
- Lista con `HistorialSolicitudes` (de `usuarios/`), ahora con `vacioTitulo` y `vacioMensaje` configurables.
- Quitadas métricas sin respaldo en la BD del mock anterior (historial genérico de acciones).

### 5. `views/PerfilView.vue`
- Rejilla 5/7: formularios (datos, contraseña) | actividad.
- Errores de cada formulario por separado; toasts "Datos actualizados" y "Contraseña actualizada".
- Actividad: skeleton → error con "Reintentar" → resumen y lista.

## Verificación
- `npm run build` sin errores. Sin hex, sin `v-if` + `v-for`, vistas sin `<table>` ni `<form>`.
- Script con `vite.ssrLoadModule` (fuera del repo): 7/7 OK.
  - Admin 1 atendió #3, #7, #9, #11 y dio 6 strikes; admin 2: 3 atendidas, 2 strikes.
  - Teléfono duplicado rechazado; perfil actualizado.
  - Contraseña actual incorrecta rechazada; cambio aceptado.
- Reglas generales: 23/23 OK (re-ejecutado).
- **Pendiente en navegador:** extensión sin conexión. Tarea 016.

## Resultado
Commit: `feat(frontend): agrega perfil del administrador`.
