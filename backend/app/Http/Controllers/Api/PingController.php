<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use Illuminate\Http\JsonResponse;

class PingController extends Controller
{
    /**
     * Verifica que la API responde.
     */
    public function index(): JsonResponse
    {
        return $this->respuesta([
            'app' => config('app.name'),
            'hora' => now()->toIso8601String(),
        ], 'API Yelp Me funcionando');
    }
}
