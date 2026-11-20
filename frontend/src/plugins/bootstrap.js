/**
 * Único punto de entrada al JavaScript de Bootstrap (build ESM).
 * Al importarse registra la data-API (collapse, dropdown...) y expone las
 * clases que los componentes Vue controlan por código, como Modal.
 * No importar también bootstrap.bundle: duplicaría instancias y eventos.
 */
export { Modal } from 'bootstrap'
