# 062 — Tarea: Lista de contactos (app)

> Plan: `010-plan-mobile-contactos-2026-11-26.md` · Rama: `feature/mobile-contactos` · Estado: ⬜ Pendiente

## Objetivo
**Consultar** del CRUD: lista de la agenda desde la API con RecyclerView.

## Pasos

### 1. `api/ServicioContactos`
```java
@GET("contactos")               Call<Respuesta<List<Contacto>>> listar();
@GET("contactos/{id}")          Call<Respuesta<Contacto>> obtener(@Path("id") long id);
@POST("contactos")              Call<Respuesta<Contacto>> crear(@Body Contacto c);
@PUT("contactos/{id}")          Call<Respuesta<Contacto>> actualizar(@Path("id") long id, @Body Contacto c);
@DELETE("contactos/{id}")       Call<Respuesta<Void>> eliminar(@Path("id") long id);
@PUT("contactos/{id}/favorito") Call<Respuesta<Contacto>> marcarFavorito(@Path("id") long id);
@DELETE("contactos/{id}/favorito") Call<Respuesta<Contacto>> quitarFavorito(@Path("id") long id);
@GET("telefonos/{telefono}")    Call<Respuesta<VerificacionTelefono>> verificar(@Path("telefono") String t);
@GET("vinculos")                Call<Respuesta<List<Vinculo>>> vinculos();
@DELETE("vinculos/{id}")        Call<Respuesta<Void>> desvincular(@Path("id") long id);
@GET("bloqueos")                Call<Respuesta<List<Bloqueo>>> bloqueos();
@POST("bloqueos")               Call<Respuesta<Bloqueo>> bloquear(@Body Map<String, Long> cuerpo); // contacto_id
@DELETE("bloqueos/{id}")        Call<Respuesta<Void>> desbloquear(@Path("id") long id);
```
Modelos: `Contacto` (id, nombre, telefono, parentesco, vinculado, favorito), `VerificacionTelefono` (telefono, vinculable, enAgenda), `Vinculo` (id, nombre, apellidoPaterno, apellidoMaterno, telefono, desde, domicilio), `Bloqueo`.

### 2. Layout `activity_contactos.xml` (estructura del diseño)
- Tarjeta "Mis contactos": buscador (`TextInputLayout` con `ic_search`), `RecyclerView rvContactos`, estado vacío ("Aún no tienes contactos. Agrega a alguien de confianza.").
- `ExtendedFloatingActionButton` "Agregar contacto" (reemplaza `btnAgregarContacto` de 40dp).
- `SwipeRefreshLayout`.
- Quitar `cardDetalleContacto` y `cardNuevoContacto` (pasan a sus activities, tareas 063–064) y `tvContactoEmergenciaActual`/`btnCambiarEmergencia`.

### 3. `ui/contactos/AdaptadorContactos extends ListAdapter<Contacto, …>`
- `item_contacto.xml` (ViewBinding): avatar con inicial, nombre, parentesco, teléfono, chip "Vinculado" (`ic_link`) si `vinculado`, estrella si `favorito`. Fila entera tocable (≥ 72dp) → `DetalleContactoActivity` con `id`.
- `DiffUtil.ItemCallback` por `id` + contenido.
- `contentDescription` de la fila: "Carlos, Mamá, vinculado, favorito".

### 4. `ContactosActivity extends BaseActivity`
- `onResume` → `listar()` (orden de la API: favorito primero).
- Búsqueda local por nombre/teléfono/parentesco (sin acentos, `Normalizer`).
- Extra `elegirFavorito` (desde la barra): Snackbar "Toca un contacto y márcalo como favorito.".

## Verificación
Carlos: lista con favorito primero y chips de vinculado. Búsqueda "mam" filtra. Usuario nuevo: estado vacío.

## Resultado
Commit: `feat(mobile): lista los contactos desde la API`.
