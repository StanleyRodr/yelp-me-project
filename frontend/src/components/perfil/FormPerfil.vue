<template>
  <form novalidate @submit.prevent="enviar">
    <div class="foto">
      <img v-if="datos.foto_perfil" :src="datos.foto_perfil" alt="Foto de perfil" class="avatar" />
      <span v-else class="avatar avatar-iniciales" aria-hidden="true">{{ iniciales }}</span>
      <div>
        <label :for="`${id}-foto`" class="btn btn-outline-secondary btn-sm">
          <i class="bi bi-camera me-1" aria-hidden="true"></i>Cambiar foto
        </label>
        <button v-if="datos.foto_perfil" type="button" class="btn btn-link btn-sm" :disabled="guardando" @click="quitarFoto">
          Quitar foto
        </button>
        <input
          :id="`${id}-foto`"
          type="file"
          class="visually-hidden"
          accept="image/jpeg,image/png,image/webp"
          :aria-describedby="`${id}-foto-ayuda`"
          :disabled="guardando"
          @change="elegirFoto"
        />
        <p :id="`${id}-foto-ayuda`" class="form-text mb-0" :class="{ 'text-error': errores.foto }">
          {{ errores.foto || 'JPG, PNG o WEBP de hasta 1 MB.' }}
        </p>
      </div>
    </div>

    <div class="campos">
      <div v-for="campo in CAMPOS" :key="campo.clave" :class="{ 'campo-ancho': campo.ancho }">
        <label :for="`${id}-${campo.clave}`" class="form-label">{{ campo.etiqueta }}</label>
        <input
          :id="`${id}-${campo.clave}`"
          v-model.trim="datos[campo.clave]"
          :type="campo.tipo"
          class="form-control"
          :class="{ 'is-invalid': errores[campo.clave] }"
          :autocomplete="campo.autocompletar"
          :inputmode="campo.modo"
          :aria-invalid="Boolean(errores[campo.clave])"
          :aria-describedby="errores[campo.clave] ? `${id}-${campo.clave}-error` : undefined"
          :disabled="guardando"
        />
        <div v-if="errores[campo.clave]" :id="`${id}-${campo.clave}-error`" class="invalid-feedback">
          {{ errores[campo.clave] }}
        </div>
      </div>
    </div>

    <p v-if="error" class="text-error" role="alert">{{ error }}</p>

    <div class="pie">
      <BotonCarga tipo="submit" icono="bi-check2" :cargando="guardando">Guardar cambios</BotonCarga>
    </div>
  </form>
</template>

<script setup>
import { computed, reactive, useId, watch } from 'vue'
import BotonCarga from '../comunes/BotonCarga.vue'

const props = defineProps({
  admin: { type: Object, required: true },
  guardando: { type: Boolean, default: false },
  error: { type: String, default: '' },
  /** Errores por campo de la API (ErrorApi.errores) */
  erroresApi: { type: Object, default: null },
})

const emit = defineEmits(['guardar'])

const CAMPOS = [
  { clave: 'nombre', etiqueta: 'Nombre', tipo: 'text', autocompletar: 'given-name' },
  { clave: 'apellidos', etiqueta: 'Apellidos', tipo: 'text', autocompletar: 'family-name' },
  { clave: 'telefono', etiqueta: 'Teléfono', tipo: 'tel', autocompletar: 'tel', modo: 'numeric' },
  { clave: 'correo', etiqueta: 'Correo', tipo: 'email', autocompletar: 'email' },
]

const TAMANO_MAXIMO = 1024 * 1024
const id = useId()
const datos = reactive({})
const errores = reactive({})
// Archivo elegido (se sube aparte, multipart) y si se pidió quitar la foto.
let archivoFoto = null
let quitar = false

// Copia editable del admin; se rehace si cambia desde fuera (p. ej. tras guardar).
watch(
  () => props.admin,
  (admin) => {
    Object.assign(datos, {
      nombre: admin.nombre,
      apellidos: admin.apellidos,
      telefono: admin.telefono,
      correo: admin.correo,
      foto_perfil: admin.foto_perfil,
    })
    archivoFoto = null
    quitar = false
  },
  { immediate: true },
)

// Mensajes de la API (p. ej. "El teléfono ya está registrado.") bajo su campo.
watch(
  () => props.erroresApi,
  (lista) => {
    for (const [campo, mensajes] of Object.entries(lista ?? {})) errores[campo] = mensajes[0]
  },
)

const iniciales = computed(() => [datos.nombre, datos.apellidos].map((p) => p?.[0] ?? '').join('').toUpperCase())

function elegirFoto(evento) {
  const archivo = evento.target.files?.[0]
  evento.target.value = ''
  errores.foto = ''
  if (!archivo) return
  if (!['image/jpeg', 'image/png', 'image/webp'].includes(archivo.type)) {
    errores.foto = 'Elige una imagen JPG, PNG o WEBP.'
    return
  }
  if (archivo.size > TAMANO_MAXIMO) {
    errores.foto = 'La imagen pesa más de 1 MB. Elige una más ligera.'
    return
  }
  // Vista previa local; el archivo se sube al guardar.
  archivoFoto = archivo
  quitar = false
  const lector = new FileReader()
  lector.onload = () => (datos.foto_perfil = lector.result)
  lector.readAsDataURL(archivo)
}

function validar() {
  errores.nombre = datos.nombre ? '' : 'Escribe tu nombre.'
  errores.apellidos = datos.apellidos ? '' : 'Escribe tus apellidos.'
  errores.telefono = /^\d{10}$/.test(datos.telefono ?? '') ? '' : 'Escribe un teléfono de 10 dígitos, sin espacios.'
  errores.correo = /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(datos.correo ?? '') ? '' : 'Escribe un correo válido, por ejemplo nombre@dominio.com.'
  return CAMPOS.every((c) => !errores[c.clave])
}

function quitarFoto() {
  datos.foto_perfil = null
  archivoFoto = null
  quitar = true
}

function enviar() {
  if (!validar()) return
  const { nombre, apellidos, telefono, correo } = datos
  emit('guardar', { datos: { nombre, apellidos, telefono, correo }, foto: archivoFoto, quitarFoto: quitar })
}
</script>

<style scoped>
.foto {
  display: flex;
  align-items: center;
  gap: var(--ym-espacio-4);
  margin-bottom: var(--ym-espacio-5);
}

.avatar {
  width: 4.5rem;
  height: 4.5rem;
  border-radius: 50%;
  object-fit: cover;
  flex-shrink: 0;
}

.avatar-iniciales {
  display: grid;
  place-items: center;
  background: var(--ym-marca);
  color: var(--ym-sobre-marca);
  font-size: var(--ym-texto-lg);
  font-weight: var(--ym-peso-fuerte);
}

/* El input de archivo está oculto: el foco se muestra en su etiqueta */
input[type='file']:focus-visible + .form-text,
.foto label:has(+ input:focus-visible) {
  outline: 2px solid var(--ym-foco);
  outline-offset: 2px;
}

.campos {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--ym-espacio-4);
}

.text-error {
  color: var(--ym-estado-pendiente-texto);
  margin: var(--ym-espacio-3) 0 0;
}

.pie {
  display: flex;
  justify-content: flex-end;
  margin-top: var(--ym-espacio-5);
}

@media (max-width: 575.98px) {
  .campos {
    grid-template-columns: minmax(0, 1fr);
  }
}
</style>
