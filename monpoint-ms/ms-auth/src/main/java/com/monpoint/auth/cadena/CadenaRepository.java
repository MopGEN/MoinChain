package com.monpoint.auth.cadena;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

/** Acceso a la colección {@code cadena_auditoria}. */
public interface CadenaRepository extends MongoRepository<BloqueAuditoria, Long> {

    /** Último bloque de la cadena (el de mayor número), si existe alguno. */
    Optional<BloqueAuditoria> findTopByOrderByIdDesc();

    /** Todos los bloques en orden de cadena. */
    List<BloqueAuditoria> findAllByOrderByIdAsc();
}
