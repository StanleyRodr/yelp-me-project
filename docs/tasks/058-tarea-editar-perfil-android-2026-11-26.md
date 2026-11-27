# 058 — Tarea: Editar perfil y foto (app)

> Plan: `009-plan-perfil-2026-11-26.md` · Rama: `feature/perfil-domicilio` · Estado: ✅ Terminada

## Objetivo
Editar nombre, apellidos, teléfono y correo; subir y quitar foto.

## Pasos

### 1. `ui/perfil/EditarPerfilActivity` (`activity_editar_perfil.xml`)
- Campos `TextInputLayout`: nombre, apellido paterno, apellido materno (opcional), teléfono, correo. Precargados de `Sesion.usuario()`.
- Envía solo lo que cambió (`PUT /api/perfil`). Errores por campo (`ErrorApi.campo`), p. ej. "El teléfono ya está registrado.".
- OK → `Sesion.actualizarUsuario`, `setResult(RESULT_OK)`, `finish()`.

### 2. Foto
- Botón sobre la foto (en `PerfilActivity`): hoja inferior (`BottomSheetDialog`) con "Elegir de la galería", "Quitar foto" (solo si tiene).
- Galería: Photo Picker (`ActivityResultContracts.PickVisualMedia`, `ImageOnly`): no requiere permiso de almacenamiento.
- Comprimir antes de enviar (`util/Imagenes.comprimir(Context, Uri)`): reducir a lado máx. 1024 px, JPEG calidad 80; si aún > 1 MB, bajar calidad. Archivo en `cacheDir`.
- `MultipartBody.Part.createFormData("foto", "perfil.jpg", RequestBody.create(archivo, MediaType.get("image/jpeg")))`.
- Mientras sube: `CircularProgressIndicator` sobre la foto. Error 422 → Snackbar con el mensaje.
- Quitar → `DELETE /api/perfil/foto` con confirmación.

### 3. Caché de Glide
La URL cambia de nombre al subir (el backend genera nombre nuevo), así que no hace falta invalidar. Si se repite, `signature(new ObjectKey(usuario.updatedAt))`.

## Verificación
- Cambiar materno a vacío → perfil muestra sin espacio extra; panel igual.
- Teléfono de otro usuario → error bajo el campo.
- Subir foto de 4 MB de la cámara → se comprime y sube; aparece en el panel (detalle del usuario).
- Quitar foto → iniciales.

## Resultado
Commit: `feat(mobile): permite editar datos y foto de perfil`.

Notas: `EditarPerfilActivity` envía solo lo que cambió (materno vacío → `""`, la API lo guarda `null`); errores 422 bajo su campo. Barra común `layout_barra_secundaria` (también en `ElegirUbicacionActivity`). Foto: botón "Cambiar foto" → `BottomSheetDialog` (`hoja_foto`) con galería (Photo Picker, sin permisos) y "Quitar foto" (con confirmación, solo si hay foto). `util/Imagenes.comprimir` usa `ImageDecoder` (respeta EXIF), 1024 px, JPEG 80 bajando de 10 en 10 hasta ≤ 1 MB, en un hilo aparte. La URL cambia en cada subida: sin caché que invalidar. Probado en emulador: teléfono ajeno → "El teléfono ya está registrado." bajo el campo; materno vacío → "Luis García"; JPEG de 7.8 MB (4000×3000) subido como 275 KB; quitar foto → iniciales y archivo borrado. Íconos `ic_photo_camera` e `ic_image` de la fuente.
