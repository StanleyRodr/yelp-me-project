<?php

namespace Database\Seeders;

use Illuminate\Database\Seeder;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Hash;

/**
 * Datos de demo de YelpMe (sin modelos: solo DB::table).
 *
 * El admin inicial (id 1) y el catálogo de servicios vienen de
 * database/script-bd.sql; aquí solo se agregan datos de prueba.
 * Es idempotente: si ya existen los datos de demo, no hace nada.
 */
class DatabaseSeeder extends Seeder
{
    private const CORREO_MARCA = 'laura.admin@yelpme.com';

    public function run(): void
    {
        // Producción: ADMIN_PASSWORD reemplaza la contraseña del admin inicial del script
        // (y de la admin de demo), aunque la demo ya esté cargada.
        if ($admin = config('app.admin_password')) {
            DB::table('usuarios')->whereIn('correo', ['admin@yelpme.com', self::CORREO_MARCA])
                ->update(['password' => Hash::make($admin), 'updated_at' => now()]);
        }

        if (DB::table('usuarios')->where('correo', self::CORREO_MARCA)->exists()) {
            $this->command?->info('Datos de demo ya cargados; no se hace nada.');
            return;
        }

        DB::transaction(function () {
            $ahora = now();
            $passAdmin = Hash::make(config('app.admin_password') ?: 'Admin123*');
            $passUsuario = Hash::make(config('app.demo_password') ?: 'Usuario123*');

            // ---------- Usuarios ----------
            $admin1 = DB::table('usuarios')->where('correo', 'admin@yelpme.com')->value('id');

            $usuario = fn (string $nombre, string $paterno, ?string $materno, string $telefono, string $correo, string $rol, string $pass) =>
                DB::table('usuarios')->insertGetId([
                    'nombre' => $nombre,
                    'apellido_paterno' => $paterno,
                    'apellido_materno' => $materno,
                    'telefono' => $telefono,
                    'correo' => $correo,
                    'password' => $pass,
                    'rol' => $rol,
                    'created_at' => $ahora,
                    'updated_at' => $ahora,
                ]);

            $admin2 = $usuario('Laura', 'Méndez', 'Ruiz', '2350000001', self::CORREO_MARCA, 'admin', $passAdmin);
            $carlos = $usuario('Carlos', 'Ramírez', 'Soto', '2351000001', 'carlos@correo.com', 'usuario', $passUsuario);
            $maria  = $usuario('María', 'López', 'Díaz', '2351000002', 'maria@correo.com', 'usuario', $passUsuario);
            $jose   = $usuario('José', 'Hernández', 'Cruz', '2351000003', 'jose@correo.com', 'usuario', $passUsuario);
            $ana    = $usuario('Ana', 'Torres', 'Vega', '2351000004', 'ana@correo.com', 'usuario', $passUsuario);
            $luis   = $usuario('Luis', 'García', 'Mora', '2351000005', 'luis@correo.com', 'usuario', $passUsuario);

            // ---------- Contactos (vinculados y no vinculados) ----------
            $contacto = fn (int $dueno, string $nombre, string $telefono, string $parentesco, ?int $vinculado) =>
                DB::table('contactos')->insert([
                    'usuario_id' => $dueno,
                    'nombre' => $nombre,
                    'telefono' => $telefono,
                    'parentesco' => $parentesco,
                    'usuario_contacto_id' => $vinculado,
                    'created_at' => $ahora,
                    'updated_at' => $ahora,
                ]);

            $contacto($carlos, 'María López', '2351000002', 'Pareja', $maria);
            $contacto($carlos, 'José Hernández', '2351000003', 'Amistad', $jose);
            $contacto($carlos, 'Mamá', '2359990001', 'Familia', null);
            $contacto($maria, 'Carlos Ramírez', '2351000001', 'Pareja', $carlos);
            $contacto($maria, 'Hermano', '2359990002', 'Familia', null);
            $contacto($jose, 'Carlos Ramírez', '2351000001', 'Amistad', $carlos);
            $contacto($ana, 'Papá', '2359990003', 'Familia', null);
            // Carlos: María es su favorito (botón de la barra en la app).
            DB::table('contactos')->where('usuario_id', $carlos)->where('telefono', '2351000002')->update(['favorito' => true]);

            // José bloqueó a Ana: Ana no puede vincularlo.
            DB::table('bloqueos')->insert([
                'usuario_id' => $jose,
                'bloqueado_id' => $ana,
                'created_at' => $ahora,
            ]);

            // ---------- Solicitudes ----------
            $servicio = DB::table('servicios')->pluck('id', 'nombre');

            $solicitud = function (array $datos, array $servicios = []) use ($servicio) {
                $id = DB::table('solicitudes')->insertGetId($datos + ['updated_at' => $datos['created_at']]);
                foreach ($servicios as $nombre) {
                    DB::table('solicitud_servicio')->insert([
                        'solicitud_id' => $id,
                        'servicio_id' => $servicio[$nombre],
                    ]);
                }
                return $id;
            };

            $hace = fn (int $dias, int $horas = 0) => $ahora->copy()->subDays($dias)->subHours($horas);

            // Pendiente con servicios
            $solicitud([
                'usuario_id' => $carlos,
                'titulo' => 'Incendio en pastizal cerca de viviendas',
                'descripcion' => 'El fuego avanza hacia las casas de la periferia.',
                'latitud' => 19.93250000, 'longitud' => -96.85300000,
                'ubicacion_nombre' => 'Col. Buenos Aires, Misantla',
                'estado' => 'Pendiente',
                'created_at' => $hace(0, 1),
            ], ['Bomberos', 'Protección Civil']);

            // Pendiente sin servicios (solo contactos)
            $solicitud([
                'usuario_id' => $maria,
                'titulo' => 'Me siento insegura, necesito que alguien venga por mí',
                'latitud' => 19.92980000, 'longitud' => -96.85250000,
                'ubicacion_nombre' => 'Parque Central, Misantla',
                'estado' => 'Pendiente',
                'created_at' => $hace(0, 2),
            ]);

            // Enviado
            $solicitud([
                'usuario_id' => $carlos,
                'titulo' => 'Persona lesionada por caída',
                'descripcion' => 'Posible fractura de pierna, no puede moverse.',
                'latitud' => 19.92650000, 'longitud' => -96.85050000,
                'ubicacion_nombre' => 'Av. Ignacio Zaragoza, Misantla',
                'estado' => 'Enviado',
                'admin_id' => $admin1,
                'enviado_en' => $hace(1, 3),
                'created_at' => $hace(1, 4),
            ], ['Ambulancia', 'Policía']);

            // Terminado por admin (con servicios)
            $solicitud([
                'usuario_id' => $jose,
                'titulo' => 'Choque entre motocicleta y automóvil',
                'latitud' => 19.93400000, 'longitud' => -96.84900000,
                'ubicacion_nombre' => 'Calle 5 de Mayo, Centro',
                'estado' => 'Terminado',
                'admin_id' => $admin2,
                'cerrado_por_id' => $admin2,
                'enviado_en' => $hace(3, 5),
                'cerrado_en' => $hace(3, 3),
                'created_at' => $hace(3, 6),
            ], ['Tránsito', 'Ambulancia']);

            // Terminado por el usuario (sin servicios)
            $solicitud([
                'usuario_id' => $maria,
                'titulo' => 'Se descompuso mi auto en carretera',
                'latitud' => 19.92400000, 'longitud' => -96.85400000,
                'ubicacion_nombre' => 'Entronque carretera estatal',
                'estado' => 'Terminado',
                'cerrado_por_id' => $maria,
                'cerrado_en' => $hace(4, 1),
                'created_at' => $hace(4, 3),
            ]);

            // Cancelado por el usuario
            $solicitud([
                'usuario_id' => $carlos,
                'titulo' => 'Perro agresivo en la calle',
                'latitud' => 19.93550000, 'longitud' => -96.85500000,
                'ubicacion_nombre' => 'Col. Manuel Villaraus',
                'estado' => 'Cancelado',
                'cerrado_por_id' => $carlos,
                'motivo' => 'El dueño ya recogió al perro.',
                'cerrado_en' => $hace(5, 2),
                'created_at' => $hace(5, 3),
            ], ['Protección Civil']);

            // ---------- Rechazos por broma + strikes ----------
            $rechazo = function (int $usuarioId, int $adminId, string $titulo, int $dias, bool $strikeActivo = true) use ($solicitud, $hace) {
                $id = $solicitud([
                    'usuario_id' => $usuarioId,
                    'titulo' => $titulo,
                    'latitud' => 19.92150000, 'longitud' => -96.85200000,
                    'ubicacion_nombre' => 'Calle Boquerón, Misantla',
                    'estado' => 'Rechazado',
                    'admin_id' => $adminId,
                    'cerrado_por_id' => $adminId,
                    'motivo' => 'Falsa alarma: la patrulla no encontró ninguna emergencia.',
                    'cerrado_en' => $hace($dias, 1),
                    'created_at' => $hace($dias, 2),
                ], ['Policía']);

                DB::table('strikes')->insert([
                    'usuario_id' => $usuarioId,
                    'admin_id' => $adminId,
                    'solicitud_id' => $id,
                    'motivo' => 'Broma / mal uso del servicio.',
                    'activo' => $strikeActivo,
                    'created_at' => $hace($dias, 1),
                ]);
            };

            // Ana: 2 strikes activos
            $rechazo($ana, $admin1, 'Riña en la plaza', 6);
            $rechazo($ana, $admin2, 'Robo a casa habitación', 2);

            // Luis: suspensión anterior ya rehabilitada (strikes inactivos)
            foreach ([40, 38, 35] as $dias) {
                DB::table('strikes')->insert([
                    'usuario_id' => $luis,
                    'admin_id' => $admin1,
                    'solicitud_id' => null,
                    'motivo' => 'Reporte manual: llamadas de broma reiteradas.',
                    'activo' => false,
                    'created_at' => $hace($dias),
                ]);
            }
            DB::table('suspensiones')->insert([
                'usuario_id' => $luis,
                'suspendido_por_id' => null,
                'motivo' => 'Suspensión automática por 3 strikes.',
                'fecha_suspension' => $hace(35),
                'rehabilitado_por_id' => $admin1,
                'fecha_rehabilitacion' => $hace(30),
            ]);

            // Luis: 3 strikes nuevos → suspensión vigente
            $rechazo($luis, $admin1, 'Incendio en escuela', 9);
            $rechazo($luis, $admin2, 'Persona armada', 8);
            $rechazo($luis, $admin1, 'Explosión de gas', 7);
            DB::table('suspensiones')->insert([
                'usuario_id' => $luis,
                'suspendido_por_id' => null,
                'motivo' => 'Suspensión automática por 3 strikes.',
                'fecha_suspension' => $hace(7, 1),
            ]);

            // ---------- Domicilios (Misantla). María sin domicilio ----------
            DB::table('domicilios')->insert([
                [
                    'usuario_id' => $carlos, 'calle' => 'Av. Ignacio Zaragoza', 'numero' => '214', 'colonia' => 'Centro',
                    'codigo_postal' => '93820', 'referencia' => 'Portón café frente a la farmacia',
                    'latitud' => 19.93152, 'longitud' => -96.85087, 'created_at' => $ahora, 'updated_at' => $ahora,
                ],
                [
                    'usuario_id' => $ana, 'calle' => 'Calle Comonfort', 'numero' => '37-B', 'colonia' => 'Ojite',
                    'codigo_postal' => '93821', 'referencia' => null,
                    'latitud' => 19.92864, 'longitud' => -96.85402, 'created_at' => $ahora, 'updated_at' => $ahora,
                ],
            ]);
        });

        $this->command?->info('Datos de demo cargados.');
    }
}
