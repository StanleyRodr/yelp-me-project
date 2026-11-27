<?php

namespace App\Mail;

use Illuminate\Mail\Mailable;
use Illuminate\Mail\Mailables\Content;
use Illuminate\Mail\Mailables\Envelope;

/** Correo con el código para recuperar la contraseña. */
class CodigoRecuperacion extends Mailable
{
    public function __construct(public string $codigo, public int $vigencia) {}

    public function envelope(): Envelope
    {
        return new Envelope(subject: 'Tu código de YelpMe');
    }

    public function content(): Content
    {
        return new Content(text: 'correos.codigo');
    }
}
