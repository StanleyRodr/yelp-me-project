# 065 — Tarea: Vínculos y bloqueos (app)

> Plan: `010-plan-mobile-contactos-2026-11-26.md` · Rama: `feature/mobile-contactos` · Estado: ⬜ Pendiente

## Objetivo
"Quién me tiene como contacto" con el domicilio de cada persona, desvincularse, bloquear y desbloquear.

## Pasos

### 1. `ui/vinculos/VinculosActivity extends BaseActivity` (ítem de menú "Quién me tiene")
- Explicación arriba: "Estas personas te eligieron como contacto de confianza. Recibirás sus alertas.".
- `RecyclerView` con `item_vinculo.xml`: nombre completo, teléfono, "Desde <fecha>", domicilio (texto) si tiene, botón "Ver en el mapa" (abre `ElegirUbicacionActivity` en modo solo lectura o un diálogo con `MapaOsm.soloLectura`), botón "Llamar" (`Llamadas`), menú ⋮ con "Desvincularme" y "Bloquear".
- Desvincularme → confirmación "Ana ya no te tendrá en su agenda." → `DELETE /api/vinculos/{id}`.
- Bloquear → confirmación "Ana ya no podrá volver a agregarte." → `POST /api/bloqueos` `{contacto_id: id}`.
- Vacío: "Nadie te tiene como contacto todavía.".
- Enlace al pie: "Personas bloqueadas" → `BloqueosActivity`.

### 2. `ui/vinculos/BloqueosActivity`
- Lista `GET /api/bloqueos` (nombre, teléfono, desde) con botón "Desbloquear" → confirmación → `DELETE /api/bloqueos/{id}`.
- Aviso: "Al desbloquear, no vuelve a tu agenda de forma automática.".

### 3. Fechas
`util/Formato.fecha(String iso)` → "26 nov 2026" (`DateTimeFormatter` con `Locale("es", "MX")`).

## Verificación (seeder)
- José: ve a quien lo tiene; Ana aparece en sus bloqueos (José la bloqueó).
- Entrar como un usuario que tiene a Carlos: Carlos ve su domicilio (si quien lo tiene registró domicilio).
- Desvincularse → el contacto desaparece de la agenda del otro (comprobar entrando como el otro).
- Desbloquear a Ana → sale de la lista.

## Resultado
Commit: `feat(mobile): agrega vínculos con domicilio y bloqueos`.
