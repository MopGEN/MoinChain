package com.monpoint.auth.cadena;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Lógica de agregar bloques sobre un repositorio simulado (sin MongoDB). */
@ExtendWith(MockitoExtension.class)
class CadenaServiceTest {

    @Mock
    private CadenaRepository repositorio;

    @InjectMocks
    private CadenaService servicio;

    @Test
    void primerBloqueEsElGenesisNumero1() {
        when(repositorio.findTopByOrderByIdDesc()).thenReturn(Optional.empty());
        when(repositorio.insert(any(BloqueAuditoria.class))).thenAnswer(inv -> inv.getArgument(0));

        BloqueAuditoria bloque = servicio.agregar("evento=PRUEBA");

        assertThat(bloque.id()).isEqualTo(1L);
        assertThat(bloque.prevHash()).isEqualTo(BloqueAuditoria.HASH_GENESIS);
        assertThat(bloque.hashActual()).isEqualTo(
                CalculadorHash.calcular(1, bloque.timestamp(), "evento=PRUEBA", BloqueAuditoria.HASH_GENESIS));
    }

    @Test
    void bloqueNuevoEnlazaConElHashDelUltimo() {
        BloqueAuditoria ultimo = BloqueAuditoria.crear(7, "anterior", BloqueAuditoria.HASH_GENESIS);
        when(repositorio.findTopByOrderByIdDesc()).thenReturn(Optional.of(ultimo));
        when(repositorio.insert(any(BloqueAuditoria.class))).thenAnswer(inv -> inv.getArgument(0));

        BloqueAuditoria bloque = servicio.agregar("siguiente");

        assertThat(bloque.id()).isEqualTo(8L);
        assertThat(bloque.prevHash()).isEqualTo(ultimo.hashActual());
    }

    @Test
    void siOtroHiloAgregoPrimeroReintentaConElNumeroSiguiente() {
        BloqueAuditoria b1 = BloqueAuditoria.crear(1, "uno", BloqueAuditoria.HASH_GENESIS);
        BloqueAuditoria b2 = BloqueAuditoria.crear(2, "dos", b1.hashActual());
        // 1.ª lectura: último = 1 (pero otro hilo ya insertó el 2). 2.ª lectura: último = 2.
        when(repositorio.findTopByOrderByIdDesc()).thenReturn(Optional.of(b1), Optional.of(b2));
        when(repositorio.insert(any(BloqueAuditoria.class)))
                .thenThrow(new DuplicateKeyException("bloque 2 ya existe"))
                .thenAnswer(inv -> inv.getArgument(0));

        BloqueAuditoria bloque = servicio.agregar("tres");

        assertThat(bloque.id()).isEqualTo(3L);
        assertThat(bloque.prevHash()).isEqualTo(b2.hashActual());
        verify(repositorio, times(2)).insert(any(BloqueAuditoria.class));
    }

    @Test
    void tras5ChoquesSeRindeConError() {
        when(repositorio.findTopByOrderByIdDesc()).thenReturn(Optional.empty());
        when(repositorio.insert(any(BloqueAuditoria.class))).thenThrow(new DuplicateKeyException("siempre ocupado"));

        assertThatThrownBy(() -> servicio.agregar("x")).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void agregarSinFallarNuncaPropagaErrores() {
        when(repositorio.findTopByOrderByIdDesc()).thenThrow(new RuntimeException("MongoDB caído"));

        assertThatCode(() -> servicio.agregarSinFallar("evento=LOGIN_EXITOSO")).doesNotThrowAnyException();
    }
}
