<?php

use App\Http\Controllers\Api\PingController;
use App\Http\Controllers\Api\RegistroController;
use Illuminate\Support\Facades\Route;

// Rutas públicas
Route::get('/ping', [PingController::class, 'index']);

// Acceso (límite: 5 intentos por minuto por IP + identificador)
Route::middleware('throttle:acceso')->group(function () {
    Route::post('/registro', [RegistroController::class, 'store']);
});
