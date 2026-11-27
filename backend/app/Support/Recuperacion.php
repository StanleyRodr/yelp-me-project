<?php

namespace App\Support;

use App\Mail\CodigoRecuperacion;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Hash;
use Illuminate\Support\Facades\Mail;
use Illuminate\Validation\ValidationException;
use Throwable;

/**
 * Recuperación de contraseña con código de 6 dígitos (plan principal 5.1).
 * Un solo código vigente por cuenta; se guarda su hash.
 */
class Recuperacion
{
    public const VIGENCIA_MIN = 15;

    public const MAX_INTENTOS = 5;

    public const MENSAJE_NEUTRO = 'Si el correo está registrado, te enviamos un código.';

    public const MENSAJE_INVALIDO = 'El código no es válido o ya venció.';

    /** Correo del cuerpo, normalizado y validado. */
    public static function correo(Request $request): string
    {
        $request->merge(['correo' => Normalizar::correo($request->input('correo'))]);

        return $request->validate(['correo' => ['required', 'email', 'max:150']])['correo'];
    }

    /** Anula los códigos vigentes, crea uno nuevo y lo manda por correo. */
    public static function emitir(object $usuario): void
    {
        $codigo = str_pad((string) random_int(0, 999999), 6, '0', STR_PAD_LEFT);
        $ahora = now();

        DB::transaction(function () use ($usuario, $codigo, $ahora) {
            DB::table('codigos_recuperacion')
                ->where('usuario_id', $usuario->id)
                ->whereNull('usado_en')
                ->update(['usado_en' => $ahora]);

            DB::table('codigos_recuperacion')->insert([
                'usuario_id' => $usuario->id,
                'codigo' => Hash::make($codigo),
                'expira_en' => $ahora->copy()->addMinutes(self::VIGENCIA_MIN),
                'created_at' => $ahora,
            ]);
        });

        // Si el correo falla no se avisa al cliente: la respuesta debe ser
        // la misma exista o no la cuenta.
        try {
            Mail::to($usuario->correo)->send(new CodigoRecuperacion($codigo, self::VIGENCIA_MIN));
        } catch (Throwable $e) {
            report($e);
        }
    }

    /** Fila del código si es correcto; si no, 422 en el campo `codigo`. */
    public static function verificar(string $correo, string $codigo): object
    {
        return DB::transaction(fn () => self::comprobar($correo, $codigo)) ?? self::rechazar();
    }

    /**
     * Dentro de una transacción: bloquea el código vigente de la cuenta y lo
     * compara. Si no coincide suma un intento y devuelve null (el 422 se
     * lanza fuera de la transacción para que el intento no se revierta).
     */
    public static function comprobar(string $correo, string $codigo): ?object
    {
        $fila = DB::table('codigos_recuperacion as c')
            ->join('usuarios as u', 'u.id', '=', 'c.usuario_id')
            ->where('u.correo', $correo)
            ->where('u.activo', true)
            ->whereNull('c.usado_en')
            ->where('c.expira_en', '>', now())
            ->where('c.intentos', '<', self::MAX_INTENTOS)
            ->orderByDesc('c.id')
            ->lockForUpdate()
            ->first(['c.id', 'c.usuario_id', 'c.codigo']);

        if (! $fila) {
            return null;
        }

        if (! preg_match('/^\d{6}$/', $codigo) || ! Hash::check($codigo, $fila->codigo)) {
            DB::table('codigos_recuperacion')->where('id', $fila->id)->increment('intentos');

            return null;
        }

        return $fila;
    }

    public static function rechazar(): never
    {
        throw ValidationException::withMessages(['codigo' => self::MENSAJE_INVALIDO]);
    }
}
