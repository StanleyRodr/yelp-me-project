# 064 — Tarea: Detalle, llamada y favorito (app)

> Plan: `010-plan-mobile-contactos-2026-11-26.md` · Rama: `feature/mobile-contactos` · Estado: ✅ Terminada

## Objetivo
Detalle del contacto, **Eliminar** del CRUD, marcar favorito y llamada directa; activar el botón del favorito en la barra.

## Pasos

### 1. `util/Llamadas`
```java
public static void llamar(ComponentActivity a, ActivityResultLauncher<String> permiso, String telefono)
```
- Con `CALL_PHONE` concedido → `Intent.ACTION_CALL` `tel:`.
- Sin permiso → pedirlo con `permiso.launch(Manifest.permission.CALL_PHONE)`; en el callback: concedido → `ACTION_CALL`; negado → `ACTION_DIAL`.
- Sin app de teléfono (`resolveActivity == null`) → Snackbar "Este dispositivo no puede hacer llamadas.".

### 2. `ui/contactos/DetalleContactoActivity` (`activity_detalle_contacto.xml`, estilo de la tarjeta de detalle del diseño)
- Avatar con inicial, nombre, parentesco, teléfono, chip vinculado / "Sin cuenta: solo llamada".
- Botones: "Llamar" (principal, `ic_call`), "Favorito" (toggle `ic_star`/`ic_star_border`), "Editar" → `FormContactoActivity` con `id`, "Eliminar" (texto `ym_peligro`).
- Eliminar → `MaterialAlertDialogBuilder` "¿Eliminar a Carlos de tus contactos?" → `DELETE` → `finish()` con Snackbar "Contacto eliminado.".
- Favorito → `PUT`/`DELETE /favorito`; actualiza el ícono; Snackbar "Carlos es tu contacto favorito.".
- 404 (contacto ya no existe: B se desvinculó o bloqueó) → Snackbar y `finish()`.

### 3. Barra inferior (tarea 052)
- `BarraServicios` pide `GET /api/contactos` al crearse y toma el primero con `favorito` (caché en memoria; se invalida al volver de Contactos).
- Con favorito: botón con su inicial/nombre corto y `contentDescription` "Llamar a Carlos (favorito)" → `Llamadas.llamar`.
- Sin favorito: comportamiento actual (abre Contactos con `elegirFavorito`).

## Verificación
- Llamar con permiso concedido → llamada; negado → marcador con el número.
- Eliminar → desaparece de la lista; en la BD ya no está.
- Marcar favorito B tras A → solo B tiene estrella; barra llama a B.
- Emulador sin telefonía: mensaje.

## Resultado
Commit: `feat(mobile): agrega detalle, llamada directa y contacto favorito`.

Notas: `util/Llamadas` (se crea en `onCreate` porque registra el pedido de permiso): con `CALL_PHONE` → `ACTION_CALL`; si se niega → `ACTION_DIAL`; sin app de teléfono → aviso. `api/Favorito`: favorito en memoria por usuario (la lista de contactos lo actualiza sin otra petición; detalle y formulario lo invalidan). `BaseActivity` pone el primer nombre del favorito en el botón de la barra (`contentDescription` "Llamar a … (favorito)") y llama con `Llamadas`; sin favorito abre Contactos con el aviso. Detalle con llamar, favorito (alterna), editar (→ formulario) y eliminar con confirmación; 404 → vuelve a la lista con "Este contacto ya no está en tu agenda.". Probado en emulador con Carlos: marcar a Ana → estrella y barra "Ana"; barra con permiso negado → marcador con su número; con permiso → llamada `ACTIVE` (emulada); editar parentesco → "Hermana"; eliminar → fuera de la lista y de la BD, barra vuelve a "Favorito" y su botón abre Contactos con el aviso. María restaurada como favorito de demo.
