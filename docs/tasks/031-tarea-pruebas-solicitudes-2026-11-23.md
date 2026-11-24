# 031 — Tarea: Pruebas y documentación de solicitudes

> Plan: `005-plan-backend-solicitudes-2026-11-23.md` · Rama: `feature/backend-solicitudes` · Estado: ✅ Completada

## Objetivo
Pruebas automáticas del plan 005 y documentación de los endpoints.

## Pasos

### 1. `tests/Unit/TransicionesSolicitudTest.php` (3)
- **Matriz completa:** 2 roles × con/sin servicios × 5 × 5 = **100 combinaciones**. Lista explícita de las 9 permitidas (plan 7.2); todo lo demás debe ser prohibido. Mensaje de fallo dice qué combinación.
- Estados finales sin salida.
- `adminPuedeEditarServicios`.
- Unitaria pura (`PHPUnit\Framework\TestCase`): no toca BD.

### 2. `tests/Feature/SolicitudesUsuarioTest.php` (10)
Solicitudes del demo buscadas por **título** (no por id: los ids pueden variar).

| Prueba | Comprueba |
|---|---|
| Catálogo | 6 servicios, `icono`, cualquier rol |
| Listar | 3 de Carlos, filtro, estado inválido 422 |
| Crear | forma del frontend, textos limpios, servicios ordenados, `admin: null` |
| Validación | obligatorios, coordenadas fuera de rango, servicios repetidos o inexistentes |
| Suspendido | 403 con servicios; sin servicios sí; no agrega al editar; sí edita título |
| Modificar | Pendiente OK, quitar todos los servicios, Enviado 409 |
| Ajenas | ver, modificar, cambiar estado → 404; DELETE → 405 |
| Cancelar | motivo obligatorio (mensaje exacto), cierre registrado, segunda vez 409 |
| Terminar | Pendiente con servicios 409, Enviado OK (admin conservado), Cancelado 409, sin servicios OK |
| Roles | admin 403 en rutas de la app |

### 3. `tests/Feature/SolicitudesPanelTest.php` (8)
| Prueba | Comprueba |
|---|---|
| Listado | 11, filtro 5, detalle con servicios, 404 |
| Roles | usuario 403 |
| Enviar + concurrencia | `admin` y `enviado_en`; Laura recibe 409 al enviar y al rechazar |
| Terminar | conserva `admin`, `cerrado_por` = Laura |
| Rechazar con strike | motivo obligatorio; Ana pasa a 3 strikes, suspendida, strike ligado a la solicitud |
| Rechazar sin strike | no suma |
| Sin servicios | no se envía (409) |
| Corregir servicios | vacío / inexistente 422; ordena; sin servicios 409; estado final 409 |

### 4. Documentación
- `backend/README.md`: sección "Solicitudes de ayuda" (forma, endpoints, transiciones, errores).
- `CLAUDE.md`: endpoints del plan 005, patrón de cambio de estado, aviso sobre `+` en arrays de reglas.
- `principal-plan.md`: plan 005 ✅ y estado actual.

## Verificación

```bash
php artisan test   # 58 passed (352 assertions)
```

## Resultado
Commit: `test(backend): agrega pruebas de solicitudes y matriz de transiciones`.
