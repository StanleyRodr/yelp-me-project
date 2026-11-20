<?php

namespace App\Console\Commands;

use Illuminate\Console\Command;
use Illuminate\Support\Facades\Artisan;
use Illuminate\Support\Facades\DB;

/**
 * Crea (o recrea) la BD de pruebas desde database/script-bd.sql y carga los
 * datos de demo. Solo acepta nombres terminados en "_pruebas": nunca puede
 * borrar la BD de desarrollo.
 */
class PrepararBdPruebas extends Command
{
    protected $signature = 'bd:pruebas {--nombre=YelpMe_pruebas : Nombre de la BD de pruebas}';

    protected $description = 'Recrea la BD de pruebas desde script-bd.sql y carga los datos de demo';

    public function handle(): int
    {
        $nombre = (string) $this->option('nombre');

        if (! preg_match('/^[A-Za-z0-9_]+_pruebas$/', $nombre)) {
            $this->error('El nombre debe terminar en "_pruebas" y usar solo letras, números y guion bajo.');

            return self::FAILURE;
        }

        $ruta = base_path('../database/script-bd.sql');
        if (! is_file($ruta)) {
            $this->error("No se encontró el script: {$ruta}");

            return self::FAILURE;
        }

        // Cambiar solo el nombre de la BD en CREATE DATABASE y USE.
        $script = file_get_contents($ruta);
        $script = preg_replace('/CREATE DATABASE IF NOT EXISTS\s+YelpMe\b/', "CREATE DATABASE `{$nombre}`", $script, 1, $creates);
        $script = preg_replace('/\bUSE\s+YelpMe\s*;/', "USE `{$nombre}`;", $script, 1, $uses);

        if ($creates !== 1 || $uses !== 1) {
            $this->error('El script no tiene la forma esperada (CREATE DATABASE / USE YelpMe).');

            return self::FAILURE;
        }

        $this->info("Recreando `{$nombre}`…");
        DB::unprepared("DROP DATABASE IF EXISTS `{$nombre}`;\n".$script);

        // Datos de demo en la BD de pruebas
        config(['database.connections.mysql.database' => $nombre]);
        DB::purge('mysql');
        Artisan::call('db:seed', ['--force' => true], $this->output);

        $this->info("Listo: `{$nombre}` con esquema y datos de demo.");

        return self::SUCCESS;
    }
}
