package com.monpoint.auth.cadena;

import java.util.List;

/**
 * Resultado de verificar la cadena completa (sección 4.2, punto 3 del proyecto).
 *
 * @param valida        {@code true} si no se encontró ningún problema
 * @param totalBloques  cuántos bloques se revisaron
 * @param problemas     lista de bloques corruptos y el motivo; vacía si la cadena es válida
 * @param mensaje       resumen legible para quien opera el sistema
 */
public record ResultadoVerificacion(
        boolean valida,
        int totalBloques,
        List<Problema> problemas,
        String mensaje) {

    /** Tipos de alteración que la verificación sabe distinguir. */
    public enum TipoProblema {
        /** El hash guardado no coincide con el recalculado: alguien cambió id, timestamp o datos. */
        HASH_ALTERADO,
        /** El {@code prev_hash} no coincide con el {@code hash_actual} del bloque anterior. */
        ENLACE_ROTO,
        /** Falta un número de bloque: se borró un bloque de la cadena. */
        BLOQUE_FALTANTE
    }

    /** Un bloque con problema y por qué. */
    public record Problema(Long bloqueId, TipoProblema tipo, String detalle) {
    }
}
