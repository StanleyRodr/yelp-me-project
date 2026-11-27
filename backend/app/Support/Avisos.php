<?php

namespace App\Support;

use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Log;
use Throwable;

/**
 * Qué avisar con push y a quién (plan 012). Se llama con DB::afterCommit: si la
 * transacción se revierte, no se avisa. Un envío que falla no frena a los
 * demás ni cambia la respuesta de la API.
 */
class Avisos
{
    /** Canales de Android (los crea la app con estos ids). */
    public const CANAL_ALERTAS = 'alertas';
    public const CANAL_ESTADO = 'estado';

    private const ESTADOS = [
        'Enviado' => ['Tu ayuda va en camino', null],
        'Rechazado' => ['Tu solicitud fue rechazada', 'motivo'],
        'Terminado' => ['Tu solicitud se marcó como terminada', null],
    ];

    /** A los contactos vinculados (cuenta activa y con token) de quien pidió ayuda. */
    public static function solicitudCreada(int $solicitudId): void
    {
        $s = DB::table('solicitudes as s')
            ->join('usuarios as u', 'u.id', '=', 's.usuario_id')
            ->where('s.id', $solicitudId)
            ->first(['s.usuario_id', 's.titulo', 's.latitud', 's.longitud', 's.ubicacion_nombre',
                'u.nombre', 'u.apellido_paterno', 'u.apellido_materno', 'u.telefono']);
        if (! $s) {
            return;
        }

        $tokens = DB::table('contactos as c')
            ->join('usuarios as u', 'u.id', '=', 'c.usuario_contacto_id')
            ->where('c.usuario_id', $s->usuario_id)
            ->where('u.activo', true)
            ->whereNotNull('u.fcm_token')
            ->pluck('u.fcm_token')
            ->unique();

        $nombre = PresentadorUsuario::nombreCompleto($s);
        $datos = [
            'tipo' => 'alerta_contacto',
            'nombre' => $nombre,
            'telefono' => (string) $s->telefono,
            'titulo' => (string) $s->titulo,
            'latitud' => (string) $s->latitud,
            'longitud' => (string) $s->longitud,
            'ubicacion_nombre' => (string) ($s->ubicacion_nombre ?? ''),
        ];

        foreach ($tokens as $token) {
            self::enviar($token, "{$s->nombre} pidió ayuda", (string) $s->titulo, $datos, self::CANAL_ALERTAS);
        }
    }

    /** Al solicitante, cuando un admin pasa la solicitud a Enviado, Rechazado o Terminado. */
    public static function estadoCambiado(int $solicitudId): void
    {
        $s = DB::table('solicitudes as s')
            ->join('usuarios as u', 'u.id', '=', 's.usuario_id')
            ->where('s.id', $solicitudId)
            ->first(['s.estado', 's.motivo', 's.titulo', 'u.activo', 'u.fcm_token']);
        if (! $s || ! isset(self::ESTADOS[$s->estado]) || ! $s->activo || ! $s->fcm_token) {
            return;
        }

        [$titulo, $campoCuerpo] = self::ESTADOS[$s->estado];
        $cuerpo = $campoCuerpo ? (string) ($s->{$campoCuerpo} ?? $s->titulo) : (string) $s->titulo;

        self::enviar($s->fcm_token, $titulo, $cuerpo, [
            'tipo' => 'estado',
            'solicitud_id' => (string) $solicitudId,
            'estado' => $s->estado,
        ], self::CANAL_ESTADO);
    }

    private static function enviar(string $token, string $titulo, string $cuerpo, array $datos, string $canal): void
    {
        try {
            app(Notificador::class)->enviar($token, $titulo, $cuerpo, $datos, $canal);
        } catch (Throwable $e) {
            Log::warning('Aviso push no enviado.', ['error' => $e->getMessage()]);
        }
    }
}
