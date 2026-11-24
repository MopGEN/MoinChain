package com.monpoint.auth.controller;

import com.monpoint.auth.cadena.BloqueAuditoria;
import com.monpoint.auth.cadena.CadenaController;
import com.monpoint.auth.cadena.CadenaService;
import com.monpoint.auth.cadena.VerificadorCadena;
import com.monpoint.auth.model.Rol;
import com.monpoint.auth.security.JwtService;
import com.monpoint.auth.security.UserPrincipal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Rutas de la cadena: solo ADMIN, y la forma de las respuestas. */
@WebMvcTest(CadenaController.class)
@Import(SeguridadDePruebaConfig.class)
class CadenaControllerTest {

    private static final UserPrincipal ADMIN = new UserPrincipal("admin-1", "admin@monpoint.com", Rol.ADMIN);
    private static final UserPrincipal VENDEDOR = new UserPrincipal("vend-1", "vale@monpoint.com", Rol.VENDEDOR);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private CadenaService cadenaService;

    private String bearer(UserPrincipal principal) {
        return "Bearer " + jwtService.generarToken(principal);
    }

    @Test
    void listar_ComoAdmin_DevuelveLosBloquesConSusHashes() throws Exception {
        BloqueAuditoria b = BloqueAuditoria.crear(1, "evento=PRUEBA", BloqueAuditoria.HASH_GENESIS);
        when(cadenaService.listar()).thenReturn(List.of(b));

        mockMvc.perform(get("/auth/cadena").header(HttpHeaders.AUTHORIZATION, bearer(ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].datos").value("evento=PRUEBA"))
                .andExpect(jsonPath("$[0].prevHash").value(BloqueAuditoria.HASH_GENESIS))
                .andExpect(jsonPath("$[0].hashActual").value(b.hashActual()));
    }

    @Test
    void verificar_CadenaValida_ReportaValidaTrue() throws Exception {
        when(cadenaService.verificar()).thenReturn(VerificadorCadena.verificar(List.of()));

        mockMvc.perform(get("/auth/cadena/verificar").header(HttpHeaders.AUTHORIZATION, bearer(ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valida").value(true));
    }

    @Test
    void agregar_ComoAdmin_Retorna201ConElBloque() throws Exception {
        BloqueAuditoria b = BloqueAuditoria.crear(1, "nota manual", BloqueAuditoria.HASH_GENESIS);
        when(cadenaService.agregar("nota manual")).thenReturn(b);

        mockMvc.perform(post("/auth/cadena")
                        .header(HttpHeaders.AUTHORIZATION, bearer(ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"datos\": \"nota manual\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.hashActual").value(b.hashActual()));
    }

    @Test
    void agregar_ConDatosVacios_Retorna400() throws Exception {
        mockMvc.perform(post("/auth/cadena")
                        .header(HttpHeaders.AUTHORIZATION, bearer(ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"datos\": \"  \"}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(cadenaService);
    }

    @Test
    void ComoVendedor_Retorna403EnLasTresRutas() throws Exception {
        mockMvc.perform(get("/auth/cadena").header(HttpHeaders.AUTHORIZATION, bearer(VENDEDOR)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/auth/cadena/verificar").header(HttpHeaders.AUTHORIZATION, bearer(VENDEDOR)))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/auth/cadena")
                        .header(HttpHeaders.AUTHORIZATION, bearer(VENDEDOR))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"datos\": \"x\"}"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(cadenaService);
    }

    @Test
    void SinToken_Retorna401() throws Exception {
        mockMvc.perform(get("/auth/cadena")).andExpect(status().isUnauthorized());
    }
}
