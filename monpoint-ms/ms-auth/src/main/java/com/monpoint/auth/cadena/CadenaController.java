package com.monpoint.auth.cadena;

import com.monpoint.auth.cadena.dto.AgregarBloqueRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * API de la cadena de auditoría. Las tres rutas son solo para ADMIN (regla en
 * {@code SecurityConfig}), porque la bitácora contiene correos y acciones de los usuarios.
 */
@RestController
@RequestMapping("/auth/cadena")
@RequiredArgsConstructor
public class CadenaController {

    private final CadenaService cadenaService;

    /** Mostrar todos los registros en orden, con sus hashes (sección 4.2, punto 2). */
    @GetMapping
    public ResponseEntity<List<BloqueAuditoria>> listar() {
        return ResponseEntity.ok(cadenaService.listar());
    }

    /**
     * Verificar la integridad (sección 4.2, punto 3). Responde 200 tanto si la cadena es válida
     * como si no: el campo {@code valida} dice cuál es el caso y {@code problemas} dónde está el daño.
     */
    @GetMapping("/verificar")
    public ResponseEntity<ResultadoVerificacion> verificar() {
        return ResponseEntity.ok(cadenaService.verificar());
    }

    /** Agregar un registro manual al final de la cadena (sección 4.2, punto 1). */
    @PostMapping
    public ResponseEntity<BloqueAuditoria> agregar(@Valid @RequestBody AgregarBloqueRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cadenaService.agregar(request.datos()));
    }
}
