<?php

use App\Http\Controllers\Api\PingController;
use Illuminate\Support\Facades\Route;

// Rutas públicas
Route::get('/ping', [PingController::class, 'index']);
