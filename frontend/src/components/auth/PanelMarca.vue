<template>
  <div class="panel">
    <p class="panel-marca">
      <i class="bi bi-bell-fill" aria-hidden="true"></i>
      YelpMe
    </p>
    <div class="panel-texto">
      <h1 class="panel-titulo">Panel de atención de emergencias</h1>
      <p class="panel-descripcion">
        Recibe las solicitudes de ayuda, ubícalas en el mapa y despacha los servicios que cada caso necesita.
      </p>
    </div>
    <p class="panel-nota">Prototipo académico. No se conecta con servicios reales de emergencia.</p>
  </div>
</template>

<style scoped>
.panel {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  gap: var(--ym-espacio-6);
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

.panel-texto {
  max-width: 32ch;
}

.panel-titulo {
  color: var(--ym-sobre-marca);
  font-size: 2.25rem;
  line-height: 1.15;
  margin-bottom: var(--ym-espacio-4);
}

.panel-descripcion {
  margin: 0;
  color: var(--ym-sobre-marca-tenue);
  font-size: var(--ym-texto-lg);
}

.panel-nota {
  margin: 0;
  color: var(--ym-sobre-marca-tenue);
  font-size: var(--ym-texto-sm);
}

@media (max-width: 767.98px) {
  .panel {
    padding: var(--ym-espacio-5);
    gap: var(--ym-espacio-4);
  }

  .panel-titulo {
    font-size: var(--ym-texto-xl);
  }

  .panel-descripcion {
    font-size: var(--ym-texto-base);
  }
}
</style>
