<?php

use App\Http\Controllers\Api\Admin\ActividadController as AdminActividadController;
use App\Http\Controllers\Api\Admin\AnulacionStrikeController as AdminAnulacionStrikeController;
use App\Http\Controllers\Api\Admin\CuentaController as AdminCuentaController;
use App\Http\Controllers\Api\Admin\EstadoSolicitudController as AdminEstadoSolicitudController;
use App\Http\Controllers\Api\Admin\ServiciosSolicitudController as AdminServiciosSolicitudController;
use App\Http\Controllers\Api\Admin\SesionController as AdminSesionController;
use App\Http\Controllers\Api\Admin\SolicitudController as AdminSolicitudController;
use App\Http\Controllers\Api\Admin\StrikeController as AdminStrikeController;
use App\Http\Controllers\Api\Admin\SuspensionController as AdminSuspensionController;
use App\Http\Controllers\Api\Admin\UsuarioController as AdminUsuarioController;
use App\Http\Controllers\Api\BloqueoController;
use App\Http\Controllers\Api\ContactoController;
use App\Http\Controllers\Api\DomicilioController;
use App\Http\Controllers\Api\EstadoSolicitudController;
use App\Http\Controllers\Api\FavoritoController;
use App\Http\Controllers\Api\FcmController;
use App\Http\Controllers\Api\FotoPerfilController;
use App\Http\Controllers\Api\PasswordController;
use App\Http\Controllers\Api\PerfilController;
use App\Http\Controllers\Api\PingController;
use App\Http\Controllers\Api\RecuperacionController;
use App\Http\Controllers\Api\RegistroController;
use App\Http\Controllers\Api\ServicioController;
use App\Http\Controllers\Api\SesionController;
use App\Http\Controllers\Api\SolicitudController;
use App\Http\Controllers\Api\TelefonoController;
use App\Http\Controllers\Api\VerificacionRecuperacionController;
use App\Http\Controllers\Api\VinculoController;
use Illuminate\Support\Facades\Route;

// Rutas públicas
Route::get('/ping', [PingController::class, 'index']);

// Acceso (límite: 5 intentos por minuto por IP + identificador)
Route::middleware('throttle:acceso')->group(function () {
    Route::post('/registro', [RegistroController::class, 'store']);
    Route::post('/login', [SesionController::class, 'store']);
    Route::post('/admin/login', [AdminSesionController::class, 'store']);

    // Recuperar contraseña (cualquier rol, plan principal 5.1)
    Route::post('/recuperacion', [RecuperacionController::class, 'store']);
    Route::post('/recuperacion/verificacion', [VerificacionRecuperacionController::class, 'store']);
    Route::put('/recuperacion', [RecuperacionController::class, 'update']);
});

// Con token y cuenta activa: cualquier rol
Route::middleware(['auth:sanctum', 'cuenta.activa'])->group(function () {
    Route::get('/sesion', [SesionController::class, 'show']);
    Route::delete('/sesion', [SesionController::class, 'destroy']);
    Route::get('/servicios', [ServicioController::class, 'index']);

    // Perfil propio (cualquier rol). Consultar = GET /sesion
    Route::match(['put', 'patch'], '/perfil', [PerfilController::class, 'update']);
    Route::post('/perfil/foto', [FotoPerfilController::class, 'store']);
    Route::delete('/perfil/foto', [FotoPerfilController::class, 'destroy']);
    Route::put('/perfil/password', [PasswordController::class, 'update']);

    // App Android (rol usuario): contactos y solicitudes propias (planes 004 y 005)
    Route::middleware('rol:usuario')->group(function () {
        // Token de notificaciones del dispositivo (plan 012)
        Route::put('/sesion/fcm', [FcmController::class, 'update']);

        Route::apiResource('contactos', ContactoController::class)
            ->parameters(['contactos' => 'id'])
            ->whereNumber('id');

        // Contacto favorito: máximo uno (plan 010)
        Route::put('/contactos/{id}/favorito', [FavoritoController::class, 'update'])->whereNumber('id');
        Route::delete('/contactos/{id}/favorito', [FavoritoController::class, 'destroy'])->whereNumber('id');

        Route::get('/telefonos/{telefono}', [TelefonoController::class, 'show'])
            ->middleware('throttle:telefonos');

        // Solicitudes de ayuda propias (sin destroy: se cancelan con /estado)
        Route::apiResource('solicitudes', SolicitudController::class)->except(['destroy'])
            ->parameters(['solicitudes' => 'id'])->whereNumber('id');
        Route::put('/solicitudes/{id}/estado', [EstadoSolicitudController::class, 'update'])->whereNumber('id');

        // Quién me tiene como contacto, desvincularme y bloqueos
        Route::apiResource('vinculos', VinculoController::class)->only(['index', 'destroy'])
            ->parameters(['vinculos' => 'id'])->whereNumber('id');
        Route::apiResource('bloqueos', BloqueoController::class)->only(['index', 'store', 'destroy'])
            ->parameters(['bloqueos' => 'id'])->whereNumber('id');

        // Domicilio único del usuario (plan 009)
        Route::get('/perfil/domicilio', [DomicilioController::class, 'show']);
        Route::put('/perfil/domicilio', [DomicilioController::class, 'update']);
        Route::delete('/perfil/domicilio', [DomicilioController::class, 'destroy']);
    });

    // Panel web (rol admin): solicitudes y usuarios (planes 005 y 006)
    Route::prefix('admin')->middleware('rol:admin')->group(function () {
        Route::apiResource('solicitudes', AdminSolicitudController::class)->only(['index', 'show'])
            ->parameters(['solicitudes' => 'id'])->whereNumber('id')->names('admin.solicitudes');
        Route::put('/solicitudes/{id}/estado', [AdminEstadoSolicitudController::class, 'update'])->whereNumber('id');
        Route::put('/solicitudes/{id}/servicios', [AdminServiciosSolicitudController::class, 'update'])->whereNumber('id');

        // Usuarios de la app: cuenta, strikes y suspensiones
        Route::apiResource('usuarios', AdminUsuarioController::class)->only(['index', 'show'])
            ->parameters(['usuarios' => 'id'])->whereNumber('id')->names('admin.usuarios');
        Route::put('/usuarios/{id}/cuenta', [AdminCuentaController::class, 'update'])->whereNumber('id');
        Route::post('/usuarios/{id}/strikes', [AdminStrikeController::class, 'store'])->whereNumber('id');
        Route::put('/strikes/{id}/anulacion', [AdminAnulacionStrikeController::class, 'update'])->whereNumber('id');
        Route::post('/usuarios/{id}/suspension', [AdminSuspensionController::class, 'store'])->whereNumber('id');
        Route::delete('/usuarios/{id}/suspension', [AdminSuspensionController::class, 'destroy'])->whereNumber('id');

        Route::get('/actividad', [AdminActividadController::class, 'index']);
    });
});
