<template>
  <div class="panel">
    <p class="panel-marca">
      <i class="bi bi-bell-fill" aria-hidden="true"></i>
      YelpMe
    </p>

    <div class="panel-centro">
      <!-- Plano de guardia: calles de trazo fino y tres avisos con los colores reales de estado. Decorativo. -->
      <svg class="plano" viewBox="0 0 400 260" aria-hidden="true" focusable="false">
        <g class="calles">
          <path d="M-10 62 C 70 54, 150 70, 230 58 S 360 44, 410 52" />
          <path d="M-10 132 C 60 126, 120 140, 200 134 S 330 118, 410 128" />
          <path d="M-10 206 C 80 198, 170 214, 260 204 S 360 196, 410 202" />
          <path d="M48 -10 C 54 60, 40 140, 52 270" />
          <path d="M132 -10 C 126 70, 140 150, 130 270" />
          <path d="M214 -10 C 222 80, 206 170, 220 270" />
          <path d="M300 -10 C 292 60, 308 150, 296 270" />
          <path d="M372 -10 C 378 90, 366 180, 376 270" />
          <path class="avenida" d="M-10 248 L 410 18" />
        </g>
        <path class="rio" d="M-10 96 C 40 110, 70 84, 110 102 S 170 146, 150 178 S 120 240, 160 270" />

        <g class="aviso aviso-pendiente">
          <circle class="aviso-pulso" cx="172" cy="96" r="10" />
          <circle class="aviso-punto" cx="172" cy="96" r="6" />
        </g>
        <g class="aviso aviso-enviado">
          <circle class="aviso-pulso" cx="268" cy="166" r="10" />
          <circle class="aviso-punto" cx="268" cy="166" r="6" />
        </g>
        <g class="aviso aviso-terminado">
          <circle class="aviso-pulso" cx="84" cy="182" r="10" />
          <circle class="aviso-punto" cx="84" cy="182" r="6" />
        </g>
      </svg>

      <h1 class="panel-titulo">Solicitudes de ayuda de Misantla, en un solo mapa</h1>

      <ul class="leyenda">
        <li><span class="leyenda-punto leyenda-pendiente" aria-hidden="true"></span>Pendiente: espera que alguien la atienda</li>
        <li><span class="leyenda-punto leyenda-enviado" aria-hidden="true"></span>Enviado: la ayuda va en camino</li>
        <li><span class="leyenda-punto leyenda-terminado" aria-hidden="true"></span>Terminado: la atención concluyó</li>
      </ul>
    </div>

    <p class="panel-nota">Prototipo académico. No se conecta con servicios reales de emergencia.</p>
  </div>
</template>

<style scoped>
.panel {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  gap: var(--ym-espacio-5);
  height: 100%;
  padding: var(--ym-espacio-6);
  background: var(--ym-marca);
  color: var(--ym-sobre-marca);
}

.panel-marca {
  display: inline-flex;
  align-items: center;
  gap: var(--ym-espacio-2);
  margin: 0;
  font-size: var(--ym-texto-lg);
  font-weight: var(--ym-peso-fuerte);
}

.panel-centro {
  max-width: 30rem;
}

/* ---------- Plano ---------- */

.plano {
  display: block;
  width: 100%;
  height: auto;
  margin-bottom: var(--ym-espacio-5);
}

.calles path {
  fill: none;
  stroke: color-mix(in srgb, var(--ym-sobre-marca) 16%, transparent);
  stroke-width: 1.5;
  stroke-linecap: round;
}

.calles .avenida {
  stroke: color-mix(in srgb, var(--ym-sobre-marca) 28%, transparent);
  stroke-width: 3;
}

.rio {
  fill: none;
  stroke: color-mix(in srgb, var(--ym-sobre-marca-tenue) 35%, transparent);
  stroke-width: 6;
  stroke-linecap: round;
}

.aviso-pendiente { --c: var(--ym-estado-pendiente); }
.aviso-enviado { --c: var(--ym-estado-enviado); }
.aviso-terminado { --c: var(--ym-estado-terminado); }

.aviso-punto {
  fill: var(--c);
  stroke: var(--ym-marca);
  stroke-width: 2;
}

.aviso-pulso {
  fill: none;
  stroke: var(--c);
  stroke-width: 2;
  opacity: 0;
  transform-box: fill-box;
  transform-origin: center;
}

/* Único movimiento: los avisos llegan uno tras otro al cargar. */
@media (prefers-reduced-motion: no-preference) {
  .aviso {
    opacity: 0;
    transform-box: fill-box;
    transform-origin: center;
    animation: llega 500ms ease-out forwards;
  }

  .aviso-enviado { animation-delay: 450ms; }
  .aviso-terminado { animation-delay: 900ms; }

  .aviso-pulso {
    animation: pulso 1400ms ease-out 2;
  }

  .aviso-enviado .aviso-pulso { animation-delay: 450ms; }
  .aviso-terminado .aviso-pulso { animation-delay: 900ms; }
}

@keyframes llega {
  from { opacity: 0; transform: scale(0.4); }
  to { opacity: 1; transform: scale(1); }
}

@keyframes pulso {
  from { opacity: 0.9; transform: scale(1); }
  to { opacity: 0; transform: scale(2.6); }
}

/* ---------- Texto ---------- */

.panel-titulo {
  max-width: 18ch;
  margin-bottom: var(--ym-espacio-5);
  color: var(--ym-sobre-marca);
  font-size: clamp(1.75rem, 1.1rem + 1.9vw, 2.75rem);
  font-weight: var(--ym-peso-fuerte);
  line-height: 1.08;
  letter-spacing: -0.01em;
}

.leyenda {
  display: grid;
  gap: var(--ym-espacio-2);
  margin: 0;
  padding: 0;
  list-style: none;
  color: var(--ym-sobre-marca-tenue);
}

.leyenda li {
  display: flex;
  align-items: center;
  gap: var(--ym-espacio-3);
}

.leyenda-punto {
  flex: none;
  width: 0.75rem;
  height: 0.75rem;
  border-radius: 50%;
}

.leyenda-pendiente { background: var(--ym-estado-pendiente); }
.leyenda-enviado { background: var(--ym-estado-enviado); }
.leyenda-terminado { background: var(--ym-estado-terminado); }

.panel-nota {
  margin: 0;
  color: var(--ym-sobre-marca-tenue);
  font-size: var(--ym-texto-sm);
}

/* Móvil: franja compacta, sin plano ni leyenda. */
@media (max-width: 767.98px) {
  .panel {
    padding: var(--ym-espacio-5);
    gap: var(--ym-espacio-3);
  }

  .plano,
  .leyenda {
    display: none;
  }

  .panel-titulo {
    margin-bottom: 0;
    font-size: var(--ym-texto-lg);
    max-width: none;
  }
}
</style>
