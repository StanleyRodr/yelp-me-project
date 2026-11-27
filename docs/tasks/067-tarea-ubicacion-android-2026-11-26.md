# 067 — Tarea: Ubicación con respaldo (app)

> Plan: `011-plan-mobile-solicitudes-2026-11-26.md` · Rama: `feature/mobile-solicitudes` · Estado: ⬜ Pendiente

## Objetivo
Obtener la ubicación actual; si no se puede, ofrecer domicilio, reintentar o marcar en el mapa (plan principal 7.5).

## Pasos

### 1. `util/Ubicacion`
```java
public interface Resultado { void alObtener(double lat, double lng); void alFallar(Motivo motivo); }
public enum Motivo { SIN_PERMISO, GPS_APAGADO, TIEMPO_AGOTADO, ERROR }
public Ubicacion(ComponentActivity activity)   // registra el launcher de permisos en el constructor
public void obtener(Resultado r)
```
- Permisos `ACCESS_FINE_LOCATION` + `ACCESS_COARSE_LOCATION` con `ActivityResultContracts.RequestMultiplePermissions` (registrar antes de `onStart`).
- Si `shouldShowRequestPermissionRationale` → diálogo explicando "Usamos tu ubicación solo al enviar una solicitud."; si se negó para siempre → `alFallar(SIN_PERMISO)`.
- GPS apagado: `LocationServices.getSettingsClient().checkLocationSettings` → `ResolvableApiException` con `IntentSenderRequest` para pedir encenderlo; si no → `GPS_APAGADO`.
- `FusedLocationProviderClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, CancellationTokenSource)`; `Handler` con 10 s cancela el token → `TIEMPO_AGOTADO`. Si llega `null`, intentar `getLastLocation()` antes de fallar.

### 2. `ui/inicio/DialogoSinUbicacion`
- `MaterialAlertDialogBuilder` "No pudimos obtener tu ubicación" + texto según motivo.
- Opciones: "Usar mi domicilio" (solo si `GET /api/perfil/domicilio` trae uno), "Reintentar", "Marcar en el mapa" (→ `ElegirUbicacionActivity`, tarea 056), "Abrir ajustes" si `SIN_PERMISO` permanente (`Settings.ACTION_APPLICATION_DETAILS_SETTINGS`).
- Devuelve `OrigenUbicacion` (`GPS`, `DOMICILIO`, `MAPA`) + lat/lng.

### 3. Domicilio como ubicación
- Con domicilio: lat/lng del domicilio y `ubicacion_nombre` = "Domicilio: " + `domicilio.texto` (si el usuario no escribió "Lugar").
- Botón "Usar mi domicilio" también en el formulario (tarea 068), visible solo si tiene domicilio.

### 4. Emulador
Documentar en la tarea: Extended controls → Location para fijar coordenadas; para probar fallo, quitar el permiso en Ajustes de la app o apagar ubicación.

## Verificación
- Permiso concedido + ubicación del emulador → coordenadas correctas.
- Permiso negado → diálogo con 3 opciones; sin domicilio → solo Reintentar y Mapa.
- Ubicación apagada → pide encenderla.

## Resultado
Commit: `feat(mobile): obtiene la ubicación con respaldo del domicilio`.
