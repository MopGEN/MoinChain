package com.monpoint.auth.cadena;

import com.monpoint.auth.cadena.ResultadoVerificacion.Problema;
import com.monpoint.auth.cadena.ResultadoVerificacion.TipoProblema;

import java.util.ArrayList;
import java.util.List;

/**
 * Verifica la integridad de la cadena (sección 4.2, punto 3 del proyecto).
 * <p>
 * Es una clase pura (no toca la base de datos): recibe los bloques ya ordenados por id. Así se puede
 * probar con listas en memoria, sin MongoDB.
 * <p>
 * Para cada bloque revisa tres cosas y <b>sigue de largo</b> aunque encuentre un error, para reportar
 * todos los bloques dañados y no solo el primero:
 * <ol>
 *   <li>Su número es el esperado (consecutivo, desde 1); si no, se borró uno.</li>
 *   <li>Su hash recalculado coincide con el guardado; si no, alguien cambió sus datos.</li>
 *   <li>Su {@code prev_hash} coincide con el {@code hash_actual} del bloque anterior; si no, el enlace se rompió.</li>
 * </ol>
 */
public final class VerificadorCadena {

    private VerificadorCadena() {
    }

    public static ResultadoVerificacion verificar(List<BloqueAuditoria> bloques) {
        List<Problema> problemas = new ArrayList<>();
        long idEsperado = 1;
        String hashAnterior = BloqueAuditoria.HASH_GENESIS;

        for (BloqueAuditoria bloque : bloques) {
            if (bloque.id() != idEsperado) {
                problemas.add(new Problema(bloque.id(), TipoProblema.BLOQUE_FALTANTE,
                        "Se esperaba el bloque " + idEsperado + " y el siguiente es el " + bloque.id()
                                + ": se eliminó al menos un bloque"));
            }

            String recalculado = CalculadorHash.calcular(
                    bloque.id(), bloque.timestamp(), bloque.datos(), bloque.prevHash());
            if (!recalculado.equals(bloque.hashActual())) {
                problemas.add(new Problema(bloque.id(), TipoProblema.HASH_ALTERADO,
                        "El hash guardado no coincide con el recalculado: el contenido del bloque fue modificado"));
            }

            if (!hashAnterior.equals(bloque.prevHash())) {
                problemas.add(new Problema(bloque.id(), TipoProblema.ENLACE_ROTO,
                        "prev_hash no coincide con el hash_actual del bloque anterior"));
            }

            // Se avanza con el hash GUARDADO (no el recalculado): así el bloque siguiente se compara
            // contra lo que realmente dice la base de datos, como pide el proyecto.
            hashAnterior = bloque.hashActual();
            idEsperado = bloque.id() + 1;
        }

        boolean valida = problemas.isEmpty();
        String mensaje = valida
                ? "Cadena válida: " + bloques.size() + " bloque(s) verificados, sin alteraciones"
                : "CADENA CORRUPTA: " + problemas.size() + " problema(s) detectado(s) en " + bloques.size() + " bloque(s)";
        return new ResultadoVerificacion(valida, bloques.size(), List.copyOf(problemas), mensaje);
    }
}
