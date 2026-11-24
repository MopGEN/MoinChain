package com.monpoint.auth.audit;

import com.monpoint.auth.cadena.CadenaService;
import com.monpoint.auth.model.Rol;
import com.monpoint.auth.model.Usuario;
import com.monpoint.auth.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Bitácora de eventos de seguridad (RNF-13) en formato {@code clave=valor}, fácil de filtrar
 * con grep o de indexar en un agregador de logs.
 * <p>
 * Además del log, cada evento se registra como un bloque de la cadena de auditoría
 * ({@link CadenaService}): el log se puede editar sin dejar rastro, la cadena no (proyecto final de
 * Criptografía, sección 2). El mismo texto se usa para ambos, así que lo que dice el log es lo que
 * queda protegido por el hash.
 * <p>
 * No lee el {@code X-Request-ID} por su cuenta: {@link RequestIdFilter} ya lo dejó en el MDC y
 * {@code logging.pattern.level} lo imprime en cada línea, así que un evento se puede
 * seguir desde el API Gateway hasta aquí. Nunca registra contraseñas ni tokens.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthAuditService {

    /** Solo para la bitácora interna: al cliente siempre se le responde el mismo 401 opaco. */
    public enum MotivoLoginFallido { USUARIO_NO_ENCONTRADO, PASSWORD_INCORRECTA, USUARIO_INACTIVO }

    private final CadenaService cadena;

    public void loginExitoso(Usuario usuario) {
        info("evento=LOGIN_EXITOSO usuarioId=%s correo=%s rol=%s",
                usuario.getId(), usuario.getCorreo(), usuario.getRol());
    }

    public void loginFallido(String correo, MotivoLoginFallido motivo) {
        String mensaje = "evento=LOGIN_FALLIDO correo=%s motivo=%s".formatted(correo, motivo);
        log.warn("{}", mensaje);
        cadena.agregarSinFallar(mensaje);
    }

    public void usuarioCreado(Usuario usuario, UserPrincipal ejecutor) {
        info("evento=USUARIO_CREADO usuarioId=%s correo=%s rol=%s ejecutorId=%s",
                usuario.getId(), usuario.getCorreo(), usuario.getRol(), ejecutor.id());
    }

    public void usuarioModificado(Usuario usuario, UserPrincipal ejecutor, Rol rolAnterior, boolean cambioPassword) {
        info("evento=USUARIO_MODIFICADO usuarioId=%s correo=%s rolAnterior=%s rol=%s cambioPassword=%s ejecutorId=%s",
                usuario.getId(), usuario.getCorreo(), rolAnterior, usuario.getRol(), cambioPassword, ejecutor.id());
    }

    public void usuarioDesactivado(Usuario usuario, UserPrincipal ejecutor) {
        info("evento=USUARIO_DESACTIVADO usuarioId=%s correo=%s ejecutorId=%s",
                usuario.getId(), usuario.getCorreo(), ejecutor.id());
    }

    /** Escribe el evento en el log (nivel INFO) y lo agrega a la cadena. */
    private void info(String formato, Object... argumentos) {
        String mensaje = formato.formatted(argumentos);
        log.info("{}", mensaje);
        cadena.agregarSinFallar(mensaje);
    }
}
