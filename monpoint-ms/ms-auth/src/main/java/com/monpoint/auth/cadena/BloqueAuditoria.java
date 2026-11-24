package com.monpoint.auth.cadena;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Un bloque (registro) de la cadena de auditoría, guardado en la colección {@code cadena_auditoria}.
 * <p>
 * Campos mínimos exigidos por el proyecto final de Criptografía (sección 4.1):
 * <ul>
 *   <li>{@code id}: número de bloque, consecutivo desde 1. Es también la clave primaria, así que
 *       MongoDB impide que dos bloques usen el mismo número.</li>
 *   <li>{@code timestamp}: momento en que se creó el bloque (precisión de milisegundos).</li>
 *   <li>{@code datos}: el evento registrado, por ejemplo {@code evento=LOGIN_EXITOSO ...}.</li>
 *   <li>{@code prevHash}: {@code hashActual} del bloque anterior (64 ceros en el primero).</li>
 *   <li>{@code hashActual}: SHA-256 de {@code id|timestamp|datos|prevHash}.</li>
 * </ul>
 * Es un {@code record}: una vez creado no se puede modificar en memoria. Que alguien lo cambie
 * directamente en la base de datos es justo el ataque que la verificación debe detectar.
 */
@Document(collection = "cadena_auditoria")
public record BloqueAuditoria(
        @Id Long id,
        Instant timestamp,
        String datos,
        @Field("prev_hash") String prevHash,
        @Field("hash_actual") String hashActual) {

    /** Valor de {@code prevHash} del primer bloque (génesis): 64 ceros, el largo de un SHA-256 en hex. */
    public static final String HASH_GENESIS = "0".repeat(64);

    /**
     * Crea un bloque nuevo calculando su hash.
     * <p>
     * El timestamp se trunca a milisegundos porque MongoDB no guarda más precisión: si se hasheara
     * con nanosegundos, al releer el bloque el hash recalculado no coincidiría y la cadena se
     * declararía corrupta sin que nadie la hubiera tocado.
     */
    public static BloqueAuditoria crear(long id, String datos, String prevHash) {
        Instant ahora = Instant.now().truncatedTo(ChronoUnit.MILLIS);
        String hash = CalculadorHash.calcular(id, ahora, datos, prevHash);
        return new BloqueAuditoria(id, ahora, datos, prevHash, hash);
    }
}
