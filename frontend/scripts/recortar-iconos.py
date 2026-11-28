"""Recorta Material Symbols Outlined a los íconos que usa el panel.

Uso (desde frontend/): python scripts/recortar-iconos.py
Requiere: pip install fonttools brotli

Fija los ejes en el estilo del panel (FILL 0, wght 400, GRAD 0, opsz 24,
igual que styles/base.css) y deja solo los glifos de ICONOS, con sus
ligaduras. Al usar un ícono nuevo en el panel o en la tabla `servicios`,
agregarlo a ICONOS y volver a correr el script.
"""
from pathlib import Path

from fontTools.subset import Options, Subsetter
from fontTools.ttLib import TTFont
from fontTools.varLib.instancer import instantiateVariableFont

ICONOS = [
    # servicios (database/script-bd.sql)
    'local_police', 'ambulance', 'fire_truck', 'traffic', 'health_and_safety', 'support',
    # estados de solicitud (constantes/estados.js) y respaldo de EstadoBadge
    'emergency', 'local_shipping', 'check_circle', 'block', 'cancel', 'help',
    # estados de cuenta (constantes/cuentas.js)
    'pause_circle', 'person_off',
    # mapas y domicilio
    'place', 'home',
    # estados vacíos y errores (EstadoVacio)
    'inbox', 'verified_user', 'event_available', 'search_off', 'error', 'task_alt', 'person_search',
]

RAIZ = Path(__file__).resolve().parent.parent
ORIGEN = RAIZ / 'node_modules/material-symbols/material-symbols-outlined.woff2'
DESTINO = RAIZ / 'src/assets/fonts/material-symbols-panel.woff2'

fuente = TTFont(ORIGEN)
fuente = instantiateVariableFont(fuente, {'FILL': 0, 'wght': 400, 'GRAD': 0, 'opsz': 24})

glifos = set(fuente.getGlyphOrder())
faltan = [i for i in ICONOS if i not in glifos]
if faltan:
    raise SystemExit(f'Íconos que no existen en la fuente: {faltan}')

opciones = Options()
opciones.flavor = 'woff2'
opciones.layout_features = ['*']  # las ligaduras de esta fuente van en rlig/rclt
opciones.layout_closure = False  # solo las ligaduras de ICONOS, no todas las que forman las letras
opciones.name_IDs = ['*']
opciones.notdef_outline = True

letras = sorted(set(''.join(ICONOS)))
subconjunto = Subsetter(opciones)
subconjunto.populate(glyphs=ICONOS, text=''.join(letras))
subconjunto.subset(fuente)

DESTINO.parent.mkdir(parents=True, exist_ok=True)
fuente.flavor = 'woff2'
fuente.save(DESTINO)
print(f'{DESTINO.relative_to(RAIZ)}: {DESTINO.stat().st_size / 1024:.1f} KB, {len(ICONOS)} íconos')
