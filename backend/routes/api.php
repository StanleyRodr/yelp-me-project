<?php

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
});

// Con token y cuenta activa (app y panel)
Route::middleware(['auth:sanctum', 'cuenta.activa'])->group(function () {
    Route::get('/sesion', [SesionController::class, 'show']);
    Route::delete('/sesion', [SesionController::class, 'destroy']);
});
