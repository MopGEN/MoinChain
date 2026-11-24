package com.monpoint.auth.cadena;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Operaciones de la cadena: agregar un bloque, listar y verificar (proyecto final, sección 4.2).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CadenaService {

    /** Reintentos si otro hilo agregó un bloque al mismo tiempo (ver {@link #agregar}). */
    private static final int MAX_INTENTOS = 5;

    private final CadenaRepository repositorio;

    /**
     * Agrega un bloque al final de la cadena.
     * <p>
     * Concurrencia: dos peticiones simultáneas podrían leer el mismo último bloque y crear dos
     * bloques con el mismo número (una bifurcación). Se evita porque el número de bloque es la
     * clave primaria: {@code insert} falla con {@link DuplicateKeyException} para el segundo, que
     * simplemente vuelve a leer el último bloque y lo intenta de nuevo.
     */
    public BloqueAuditoria agregar(String datos) {
        for (int intento = 1; intento <= MAX_INTENTOS; intento++) {
            BloqueAuditoria ultimo = repositorio.findTopByOrderByIdDesc().orElse(null);
            long id = ultimo == null ? 1 : ultimo.id() + 1;
            String prevHash = ultimo == null ? BloqueAuditoria.HASH_GENESIS : ultimo.hashActual();
            try {
                return repositorio.insert(BloqueAuditoria.crear(id, datos, prevHash));
            } catch (DuplicateKeyException e) {
                log.debug("Bloque {} ya existía (concurrencia), reintento {}/{}", id, intento, MAX_INTENTOS);
            }
        }
        throw new IllegalStateException("No se pudo agregar el bloque tras " + MAX_INTENTOS + " intentos");
    }

    /**
     * Agrega un bloque sin propagar errores: la bitácora nunca debe impedir que un usuario inicie
     * sesión. Si falla, queda registrado en el log y la operación principal sigue.
     */
    public void agregarSinFallar(String datos) {
        try {
            agregar(datos);
        } catch (RuntimeException e) {
            log.error("No se pudo registrar el evento en la cadena de auditoría: {}", e.getMessage());
        }
    }

    /** Todos los bloques, del primero al último. */
    public List<BloqueAuditoria> listar() {
        return repositorio.findAllByOrderByIdAsc();
    }

    /** Recorre la cadena completa y reporta si es válida o qué bloques están alterados. */
    public ResultadoVerificacion verificar() {
        return VerificadorCadena.verificar(listar());
    }
}
