package com.monpoint.auth.cadena;

import com.monpoint.auth.cadena.ResultadoVerificacion.TipoProblema;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Prueba del núcleo criptográfico sin base de datos: cadena válida y cada tipo de ataque
 * (proyecto final, secciones 4.2.3 y 4.4).
 */
class VerificadorCadenaTest {

    /** Construye una cadena válida de {@code n} bloques enlazados. */
    private static List<BloqueAuditoria> cadenaValida(int n) {
        List<BloqueAuditoria> bloques = new ArrayList<>();
        String prev = BloqueAuditoria.HASH_GENESIS;
        for (int i = 1; i <= n; i++) {
            BloqueAuditoria bloque = BloqueAuditoria.crear(i, "evento=PRUEBA numero=" + i, prev);
            bloques.add(bloque);
            prev = bloque.hashActual();
        }
        return bloques;
    }

    @Test
    void hashEsSha256DeterministaDe64CaracteresHex() {
        Instant t = Instant.ofEpochMilli(1_700_000_000_000L);
        String h1 = CalculadorHash.calcular(1, t, "datos", BloqueAuditoria.HASH_GENESIS);
        String h2 = CalculadorHash.calcular(1, t, "datos", BloqueAuditoria.HASH_GENESIS);

        assertThat(h1).isEqualTo(h2).hasSize(64).matches("[0-9a-f]{64}");
    }

    @Test
    void cualquierCambioDeUnCampoCambiaElHash() {
        Instant t = Instant.ofEpochMilli(1_700_000_000_000L);
        String base = CalculadorHash.calcular(1, t, "datos", "x");

        assertThat(CalculadorHash.calcular(2, t, "datos", "x")).isNotEqualTo(base);
        assertThat(CalculadorHash.calcular(1, t.plusMillis(1), "datos", "x")).isNotEqualTo(base);
        assertThat(CalculadorHash.calcular(1, t, "datoz", "x")).isNotEqualTo(base);
        assertThat(CalculadorHash.calcular(1, t, "datos", "y")).isNotEqualTo(base);
    }

    @Test
    void primerBloqueEnlazaConElGenesis() {
        assertThat(cadenaValida(1).get(0).prevHash()).isEqualTo("0".repeat(64));
    }

    @Test
    void cadenaIntactaEsValida() {
        ResultadoVerificacion r = VerificadorCadena.verificar(cadenaValida(5));

        assertThat(r.valida()).isTrue();
        assertThat(r.totalBloques()).isEqualTo(5);
        assertThat(r.problemas()).isEmpty();
    }

    @Test
    void cadenaVaciaEsValida() {
        assertThat(VerificadorCadena.verificar(List.of()).valida()).isTrue();
    }

    @Test
    void datosAlteradosSeDetectanEnElBloqueModificado() {
        List<BloqueAuditoria> bloques = cadenaValida(5);
        BloqueAuditoria original = bloques.get(2);
        // El atacante cambia los datos del bloque 3 pero no puede recalcular su hash sin romper el siguiente.
        bloques.set(2, new BloqueAuditoria(original.id(), original.timestamp(),
                "evento=PRUEBA numero=999", original.prevHash(), original.hashActual()));

        ResultadoVerificacion r = VerificadorCadena.verificar(bloques);

        assertThat(r.valida()).isFalse();
        assertThat(r.problemas()).hasSize(1);
        assertThat(r.problemas().get(0).bloqueId()).isEqualTo(3L);
        assertThat(r.problemas().get(0).tipo()).isEqualTo(TipoProblema.HASH_ALTERADO);
    }

    @Test
    void atacanteQueRecalculaElHashRompeElEnlaceDelSiguiente() {
        List<BloqueAuditoria> bloques = cadenaValida(5);
        BloqueAuditoria original = bloques.get(2);
        // Ataque más hábil: cambia los datos Y recalcula el hash del bloque 3 para que sea coherente.
        String nuevosDatos = "evento=PRUEBA numero=999";
        String nuevoHash = CalculadorHash.calcular(original.id(), original.timestamp(), nuevosDatos, original.prevHash());
        bloques.set(2, new BloqueAuditoria(original.id(), original.timestamp(), nuevosDatos, original.prevHash(), nuevoHash));

        ResultadoVerificacion r = VerificadorCadena.verificar(bloques);

        // El bloque 3 se ve bien por sí solo, pero el bloque 4 ya no apunta a su nuevo hash.
        assertThat(r.valida()).isFalse();
        assertThat(r.problemas()).hasSize(1);
        assertThat(r.problemas().get(0).bloqueId()).isEqualTo(4L);
        assertThat(r.problemas().get(0).tipo()).isEqualTo(TipoProblema.ENLACE_ROTO);
    }

    @Test
    void bloqueEliminadoSeDetectaPorNumeroFaltanteYEnlaceRoto() {
        List<BloqueAuditoria> bloques = cadenaValida(5);
        bloques.remove(2); // desaparece el bloque 3

        ResultadoVerificacion r = VerificadorCadena.verificar(bloques);

        assertThat(r.valida()).isFalse();
        assertThat(r.problemas()).extracting(ResultadoVerificacion.Problema::tipo)
                .containsExactlyInAnyOrder(TipoProblema.BLOQUE_FALTANTE, TipoProblema.ENLACE_ROTO);
        assertThat(r.problemas()).allMatch(p -> p.bloqueId() == 4L);
    }

    @Test
    void genesisManipuladoSeDetecta() {
        List<BloqueAuditoria> bloques = cadenaValida(2);
        BloqueAuditoria primero = bloques.get(0);
        String prevFalso = "f".repeat(64);
        String hashCoherente = CalculadorHash.calcular(1, primero.timestamp(), primero.datos(), prevFalso);
        bloques.set(0, new BloqueAuditoria(1L, primero.timestamp(), primero.datos(), prevFalso, hashCoherente));

        ResultadoVerificacion r = VerificadorCadena.verificar(bloques);

        assertThat(r.valida()).isFalse();
        assertThat(r.problemas().get(0).bloqueId()).isEqualTo(1L);
        assertThat(r.problemas().get(0).tipo()).isEqualTo(TipoProblema.ENLACE_ROTO);
    }

    @Test
    void variosBloquesDanadosSeReportanTodos() {
        List<BloqueAuditoria> bloques = cadenaValida(6);
        for (int i : new int[]{1, 4}) {
            BloqueAuditoria b = bloques.get(i);
            bloques.set(i, new BloqueAuditoria(b.id(), b.timestamp(), "manipulado", b.prevHash(), b.hashActual()));
        }

        ResultadoVerificacion r = VerificadorCadena.verificar(bloques);

        assertThat(r.problemas()).extracting(ResultadoVerificacion.Problema::bloqueId)
                .containsExactly(2L, 5L);
    }
}
