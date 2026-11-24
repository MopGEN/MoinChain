package com.monpoint.auth.cadena;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;

/**
 * Calcula el hash de un bloque: {@code hash_actual = SHA-256(id | timestamp | datos | prev_hash)}.
 * <p>
 * Decisiones de diseño (se explican en el informe):
 * <ul>
 *   <li><b>SHA-256</b>: función hash estándar, resistente a colisiones; da 32 bytes = 64 caracteres hex.</li>
 *   <li><b>Timestamp en milisegundos desde 1970</b> (un número): evita depender del formato de fecha.</li>
 *   <li><b>Separador {@code |}</b> entre campos. No hay ambigüedad aunque {@code datos} contenga
 *       barras: {@code id} y {@code timestamp} son números (no llevan barras) y {@code prev_hash}
 *       siempre mide 64 caracteres hex, así que cada campo se puede separar sin dudas.</li>
 *   <li><b>UTF-8</b> explícito para que el resultado no cambie según el sistema operativo.</li>
 * </ul>
 */
public final class CalculadorHash {

    private static final String ALGORITMO = "SHA-256";

    private CalculadorHash() {
    }

    public static String calcular(long id, Instant timestamp, String datos, String prevHash) {
        String entrada = id + "|" + timestamp.toEpochMilli() + "|" + datos + "|" + prevHash;
        try {
            MessageDigest digest = MessageDigest.getInstance(ALGORITMO);
            byte[] resumen = digest.digest(entrada.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(resumen);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 es obligatorio en toda JVM; si faltara, no hay forma segura de continuar.
            throw new IllegalStateException("La JVM no ofrece " + ALGORITMO, e);
        }
    }
}
