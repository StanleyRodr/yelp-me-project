<?php

use App\Http\Controllers\Api\Admin\SesionController as AdminSesionController;
use App\Http\Controllers\Api\PingController;
use App\Http\Controllers\Api\RegistroController;
use App\Http\Controllers\Api\SesionController;
use Illuminate\Support\Facades\Route;

// Rutas públicas
Route::get('/ping', [PingController::class, 'index']);

// Acceso (límite: 5 intentos por minuto por IP + identificador)
Route::middleware('throttle:acceso')->group(function () {
    Route::post('/registro', [RegistroController::class, 'store']);
    Route::post('/login', [SesionController::class, 'store']);
    Route::post('/admin/login', [AdminSesionController::class, 'store']);
});

// Con token y cuenta activa: cualquier rol
Route::middleware(['auth:sanctum', 'cuenta.activa'])->group(function () {
    Route::get('/sesion', [SesionController::class, 'show']);
    Route::delete('/sesion', [SesionController::class, 'destroy']);

    // App Android (rol usuario): contactos y solicitudes propias (planes 004 y 005)
    Route::middleware('rol:usuario')->group(function () {
        //
    });

    // Panel web (rol admin): solicitudes y usuarios (planes 005 y 006)
    Route::prefix('admin')->middleware('rol:admin')->group(function () {
        //
    });
});
