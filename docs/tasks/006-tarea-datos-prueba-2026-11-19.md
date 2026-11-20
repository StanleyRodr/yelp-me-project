# 006 — Tarea: Datos de prueba

> Plan: `001-plan-backend-base-2026-11-19.md` · Rama: `feature/backend-base` · Estado: Pendiente

## Objetivo
Cargar datos de demo realistas que cubran todos los casos del plan principal, sin modelos ni factories.

## Subtareas
- [ ] `DatabaseSeeder` con `DB::table(...)` y `Hash::make`, dentro de una transacción.
- [ ] No duplicar los datos iniciales del script (admin y servicios).
- [ ] Datos:
  - [ ] 1 administrador adicional.
  - [ ] 5 usuarios con contraseña conocida.
  - [ ] Contactos vinculados (teléfono de un usuario registrado) y no vinculados.
  - [ ] 1 bloqueo.
  - [ ] Solicitudes en los 5 estados, con y sin servicios, con coordenadas reales de la zona (Misantla, Ver.).
  - [ ] Un usuario con 2 strikes activos.
  - [ ] Un usuario suspendido (3 strikes) con una suspensión previa rehabilitada (historial).
- [ ] Idempotente: si los datos de demo ya existen (por correo), no se vuelven a insertar.
- [ ] Documentar las credenciales de prueba en el README del backend.

## Criterios de aceptación
- `php artisan db:seed` corre sin errores; una segunda ejecución no duplica datos.
- Las consultas de verificación muestran solicitudes en los 5 estados, el usuario suspendido y los strikes.
