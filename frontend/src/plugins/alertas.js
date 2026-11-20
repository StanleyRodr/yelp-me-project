import Swal from 'sweetalert2'

/**
 * Alertas del sistema con SweetAlert2 tematizado.
 * Botones con clases de Bootstrap; textos en español.
 * Regla: el botón de confirmar dice exactamente qué hace ("Enviar ayuda").
 */
const dialogo = Swal.mixin({
  buttonsStyling: false,
  reverseButtons: true,
  customClass: {
    confirmButton: 'btn btn-primary',
    cancelButton: 'btn btn-outline-secondary',
  },
  cancelButtonText: 'Cancelar',
})

const toast = Swal.mixin({
  toast: true,
  position: 'top-end',
  showConfirmButton: false,
  timer: 3500,
})

/**
 * Confirmación simple. Si se pasa `accion`, se ejecuta con indicador de
 * carga dentro del diálogo (Swal.showLoading) antes de cerrar.
 * @returns {Promise<boolean>} true si se confirmó (y la acción terminó bien)
 */
export async function confirmar({ titulo, texto = '', confirmar = 'Confirmar', peligro = false, accion = null }) {
  const resultado = await dialogo.fire({
    title: titulo,
    text: texto,
    showCancelButton: true,
    confirmButtonText: confirmar,
    focusCancel: peligro,
    customClass: {
      confirmButton: peligro ? 'btn btn-danger' : 'btn btn-primary',
      cancelButton: 'btn btn-outline-secondary',
    },
    showLoaderOnConfirm: Boolean(accion),
    allowOutsideClick: () => !Swal.isLoading(),
    preConfirm: accion
      ? async () => {
          try {
            await accion()
            return true
          } catch (e) {
            Swal.showValidationMessage(e?.message || 'No se pudo completar la acción.')
            return false
          }
        }
      : undefined,
  })
  return resultado.isConfirmed
}

export function exito(mensaje) {
  return toast.fire({ icon: 'success', title: mensaje })
}

export function error(mensaje) {
  return toast.fire({ icon: 'error', title: mensaje, timer: 5000 })
}
