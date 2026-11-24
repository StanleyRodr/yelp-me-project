<?php

use Illuminate\Support\Facades\Route;

// El backend es solo API: la raíz indica dónde está.
Route::get('/', fn () => response()->json([
    'ok' => true,
    'mensaje' => 'YelpMe API. Usa las rutas bajo /api.',
    'datos' => ['ping' => url('/api/ping')],
]));
