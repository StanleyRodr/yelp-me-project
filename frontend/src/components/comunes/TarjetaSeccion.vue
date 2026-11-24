<template>
  <section class="seccion" :aria-labelledby="idTitulo">
    <header v-if="titulo || $slots.acciones" class="seccion-cabecera">
      <div>
        <h2 :id="idTitulo" class="seccion-titulo">{{ titulo }}</h2>
        <p v-if="descripcion" class="seccion-descripcion">{{ descripcion }}</p>
      </div>
      <div v-if="$slots.acciones" class="seccion-acciones">
        <slot name="acciones" />
      </div>
    </header>
    <div class="seccion-cuerpo" :class="{ 'sin-relleno': sinRelleno }">
      <slot />
    </div>
  </section>
</template>

<script setup>
import { useId } from 'vue'

defineProps({
  titulo: { type: String, default: '' },
  descripcion: { type: String, default: '' },
  sinRelleno: { type: Boolean, default: false },
})

const idTitulo = useId()
</script>

<style scoped>
.seccion {
  background: var(--ym-superficie);
  border: 1px solid var(--ym-borde);
  border-radius: var(--ym-radio);
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.seccion-cabecera {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: var(--ym-espacio-3);
  padding: var(--ym-espacio-4) var(--ym-espacio-4) 0;
}

.seccion-titulo {
  font-size: var(--ym-texto-lg);
  margin: 0;
}

.seccion-descripcion {
  margin: var(--ym-espacio-1) 0 0;
  color: var(--ym-texto-secundario);
  font-size: var(--ym-texto-sm);
}

.seccion-acciones {
  display: flex;
  gap: var(--ym-espacio-2);
  flex-shrink: 0;
}

.seccion-cuerpo {
  padding: var(--ym-espacio-4);
  flex: 1;
  min-height: 0;
}

.seccion-cuerpo.sin-relleno {
  padding: var(--ym-espacio-3) 0 0;
}
</style>
