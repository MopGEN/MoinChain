# MoinChain — Documento de contexto del proyecto

> **Para quién es:** para un compañero (y su asistente de IA) que necesita ponerse al día sin haber visto la
> conversación original. Pega este archivo completo al inicio de la sesión de la IA.
> **Fecha del estado descrito:** 28 de noviembre de 2026. **Entrega del proyecto:** 1 de diciembre de 2026.
> **Idioma del código, comentarios, commits y documentación:** español.

---

## 1. Resumen en 30 segundos

- **MoinChain** es una copia de **MonPoint** (punto de venta con microservicios, proyecto de otra materia) que se usa
  como base para el **proyecto final de la materia IH063 – Criptografía** (profesor: César Alejandro García García).
- El proyecto pide una **"base de datos encadenada con hash (mini-blockchain)"**: registros cuyo hash depende del
  registro anterior, de modo que modificar uno pasado rompa la cadena y la aplicación lo detecte.
- Se implementó dentro del microservicio **`ms-auth`**: cada evento de seguridad (login, alta, modificación o baja de
  usuarios) se guarda como un bloque de la cadena. Hay API para listar, verificar y agregar bloques (solo rol ADMIN).
- **Estado: terminado y subido a GitHub** (`https://github.com/MopGEN/MoinChain`, rama `main`). Código, 21 pruebas
  nuevas, demostración con evidencia, README, informe técnico en PDF y este documento.
- **Lo que depende de una persona:** revisar el informe (nombre, formato), ejecutar la demo en su máquina y poder explicar
  el código en una revisión oral (regla de autoría, sección 7 de los lineamientos).

---

## 2. Dos repositorios que no hay que mezclar

| | **MoinChain** (este proyecto) | **MonPoint** (proyecto de microservicios) |
|---|---|---|
| Carpeta local | `C:\Users\mopge\MoinChain` | `C:\Users\mopge\MonPoint` |
| GitHub | `github.com/MopGEN/MoinChain` | `github.com/MopGEN/MonPoint` |
| Materia | Criptografía | Microservicios |
| Remotos | `origin` = MoinChain; `upstream` = MonPoint (solo para traer cambios de la base) | `origin` = MonPoint |

- Nunca hacer `push` de trabajo de criptografía a MonPoint ni al revés. En MoinChain, `git push` va a `origin` (MoinChain).
- MonPoint tiene trabajo sin guardar de otra persona/otra tarea (inventario, clientes, diagramas); **no tocarlo**.
- La base de MoinChain es el estado de MonPoint en el commit `301ea19` (18 de noviembre de 2026). MonPoint no ha avanzado desde entonces.
- Estado de Git de MoinChain: todo está en `main` (= `origin/main`). El commit `440596a` ("Agrega cadena de auditoría con hash
  (mini-blockchain) en ms-auth") contiene el código, las pruebas, el README, las evidencias, el checklist y el informe; un commit
  posterior agrega este documento y la presentación. La rama `cripto/cadena-auditoria` se mantiene igual que `main`.
- `CLAUDE.md` en MoinChain existe **solo en local** (está en `.gitignore`); describe el proyecto y no se sube.

---

## 3. Qué es MonPoint (contexto mínimo de la base)

POS para micro y pequeñas empresas migrado de un monolito JavaFX (`MonPointV2/`) a microservicios Spring Boot
(`monpoint-ms/`): `ms-auth`, `ms-inventario`, `ms-ventas`, `ms-clientes`, `ms-reportes`, `ms-notificaciones`, más
`api-gateway` (puerto 8080), `eureka-server` (8761), RabbitMQ y MongoDB (una base por servicio) y un cliente web Vue 3.

Reglas de arquitectura heredadas (siguen vigentes): ningún servicio llama a otro por HTTP (se comunican por eventos en
RabbitMQ); todo consumidor de eventos es idempotente; el catálogo de eventos es un contrato. **La cadena de MoinChain vive
completa dentro de `ms-auth` y no agrega eventos ni llamadas entre servicios, así que no rompe ninguna regla.**

El Gateway enruta `Path=/auth/**` hacia `ms-auth`, por lo que las rutas nuevas `/auth/cadena*` quedarían accesibles por
`:8080` con token. **Eso no se probó**: todas las pruebas reales fueron directo contra `ms-auth` en `:8081`.

---

## 4. Los lineamientos del profesor (resumen fiel)

Título: *Proyecto final IH063 – Criptografía: "Base de datos encadenada con hash (mini-blockchain)"*. Valor: 100 puntos.

- **§2** App sencilla de registros (transacciones, movimientos o eventos) con hash que depende del anterior. Si se modifica un
  registro pasado, la cadena se rompe y la app lo detecta. Sin consenso distribuido, sin minería, sin P2P.
- **§3** Objetivos: (1) hash criptográfico (p. ej. SHA-256) sobre registros; (2) estructura que encadene registros con el hash
  del anterior; (3) detectar modificaciones verificando la cadena; (4) documentar funcionamiento, supuestos, limitaciones y mejoras.
- **§4.1** Cada registro: ID/número de bloque, timestamp, datos, `prev_hash`, `hash_actual = H(ID ‖ timestamp ‖ datos ‖ prev_hash)`.
- **§4.2** Funciones obligatorias: (1) agregar un registro al final; (2) mostrar todos en orden con sus hashes; (3) verificar
  integridad: recorrer, recalcular, comparar, comprobar que `prev_hash` = `hash_actual` del anterior, y reportar si es válida o hubo alteración.
- **§4.3** Persistencia (archivo, binario, BD ligera, JSON/CSV): al cerrar y abrir, los registros siguen y la cadena es verificable.
- **§4.4** Mecanismo para simular un ataque (modificar un registro pasado y verificar que la cadena queda corrupta).
- **§5** Entregables: (1) código fuente completo con README de compilación/ejecución; (2) informe técnico PDF de 4–6 páginas
  (introducción, estructura y encadenamiento, capturas de 4 momentos, limitaciones, mejoras); (3) demostración breve (opcional).
- **§6** Rúbrica (100 pts): diseño y estructura de datos 20; hash y encadenamiento 25; verificación y detección 25;
  persistencia y usabilidad 10; informe 15; buenas prácticas y comentarios 5.
- **§7** IA permitida solo como apoyo; no se permite entregar código que el estudiante no pueda explicar. Si no puede explicarlo,
  se considera falta de autoría.
- El profesor adjuntó tres PDF: `Plantilla.pdf` y `Ejemplo.pdf` (artículos en formato LNCS, de dos columnas) y
  `Ejemplo Hash - Hoja 1.pdf` (tabla de usuarios con una columna de hash **sin encadenar**).

---

## 5. Qué se construyó y dónde está cada cosa

Rutas desde la raíz de `C:\Users\mopge\MoinChain`.

### 5.1 Código (`monpoint-ms/ms-auth/src/main/java/com/monpoint/auth/`)

| Archivo | Qué hace |
|---|---|
| `cadena/CalculadorHash.java` | La fórmula: `SHA-256(id \| timestamp_ms \| datos \| prev_hash)` en UTF-8, salida en 64 caracteres hex. |
| `cadena/BloqueAuditoria.java` | `record` (inmutable) = documento de la colección `cadena_auditoria`. Constante `HASH_GENESIS` (64 ceros). `crear(id, datos, prevHash)` trunca el timestamp a milisegundos y calcula el hash. |
| `cadena/VerificadorCadena.java` | Verificación **pura** (sin base de datos): recibe la lista ordenada y devuelve el resultado con todos los problemas. |
| `cadena/ResultadoVerificacion.java` | `valida`, `totalBloques`, `problemas[]` (`bloqueId`, `tipo`, `detalle`), `mensaje`. Tipos: `HASH_ALTERADO`, `ENLACE_ROTO`, `BLOQUE_FALTANTE`. |
| `cadena/CadenaRepository.java` | Repositorio Mongo: último bloque y lista ordenada por id. |
| `cadena/CadenaService.java` | `agregar` (con reintento ante concurrencia), `agregarSinFallar` (no propaga errores), `listar`, `verificar`. |
| `cadena/CadenaController.java` + `cadena/dto/AgregarBloqueRequest.java` | API REST. Validación: `datos` obligatorio, máximo 500 caracteres. |
| `audit/AuthAuditService.java` (modificado) | Además de escribir el log, cada evento de seguridad crea un bloque. El mismo texto va al log y a la cadena. |
| `config/SecurityConfig.java` (modificado) | `/auth/cadena` y `/auth/cadena/**` exigen rol ADMIN (cualquier método). |

### 5.2 API (solo ADMIN, con `Authorization: Bearer <token>`)

| Método y ruta | Qué hace | Respuesta |
|---|---|---|
| `POST /auth/cadena` con `{"datos":"…"}` | Agrega un bloque manual al final | `201` + bloque |
| `GET /auth/cadena` | Lista todos los bloques en orden | `200` + arreglo |
| `GET /auth/cadena/verificar` | Verifica la cadena | `200` siempre; el campo `valida` dice si está íntegra |

Sin token → 401. Con token de VENDEDOR → 403. Datos vacíos → 400.
Ojo con los nombres: **en MongoDB** los campos son `_id`, `timestamp`, `datos`, `prev_hash`, `hash_actual`; **en el JSON de la API**
son `id`, `timestamp`, `datos`, `prevHash`, `hashActual`.

### 5.3 Eventos que generan bloques

`LOGIN_EXITOSO`, `LOGIN_FALLIDO`, `USUARIO_CREADO`, `USUARIO_MODIFICADO`, `USUARIO_DESACTIVADO`. Texto tipo
`evento=LOGIN_EXITOSO usuarioId=… correo=… rol=ADMIN`. **Nunca** se registran contraseñas ni tokens. Sí se registra el correo
(por eso la API es solo ADMIN). La siembra inicial de usuarios (`DataInitializer`) **no** crea bloques.

### 5.4 Pruebas (`monpoint-ms/ms-auth/src/test/java/com/monpoint/auth/`)

- `cadena/VerificadorCadenaTest` (10): hash determinista y de 64 hex; cualquier campo cambia el hash; génesis; cadena válida; cadena
  vacía válida; datos alterados; atacante que recalcula el hash (`ENLACE_ROTO` en el siguiente); bloque eliminado; génesis
  manipulado; varios bloques dañados a la vez.
- `cadena/CadenaServiceTest` (5): primer bloque = génesis; enlace con el último; reintento ante `DuplicateKeyException`; error tras 5 intentos; `agregarSinFallar` no propaga.
- `controller/CadenaControllerTest` (6): ADMIN lista/verifica/agrega; datos vacíos → 400; VENDEDOR → 403 en las 3 rutas; sin token → 401.
- Total de `ms-auth`: **97 pruebas** (76 previas + 21 nuevas), 0 fallos. Las 4 pruebas de integración previas necesitan MongoDB corriendo.

### 5.5 Documentación y evidencias

| Qué | Dónde |
|---|---|
| Checklist (qué, por qué, base en el enunciado) | `docs/CRIPTO_CHECKLIST.md` |
| README con estructura de datos, ejecución y alteración | `monpoint-ms/ms-auth/docs/cadena/README.md` |
| Script de demostración (agrega, muestra, verifica, ataca, verifica) | `monpoint-ms/ms-auth/docs/cadena/demo.sh` |
| Salida real de la demo | `monpoint-ms/ms-auth/docs/cadena/salida-demo.txt` |
| Verificación con otro programa (Node.js) del hash del bloque 1 | `monpoint-ms/ms-auth/docs/cadena/verificacion-independiente.txt` |
| Persistencia tras reiniciar el servicio | `monpoint-ms/ms-auth/docs/cadena/persistencia.txt` |
| Informe técnico (5 páginas) | `docs/informe/informe.pdf` (fuente: `informe.html`) |
| Presentación interactiva con la parte de criptografía | `docs/presentacion/index.html` |
| Este documento | `docs/CONTEXTO_PROYECTO.md` |

**Presentación:** es una copia del `index.html` de la feria de MonPoint (`MonPoint/monpoint-ms/docs/feria/index.html`, que no se tocó),
con todo su contenido más 4 secciones nuevas (hash y cadena, bloque y código, alterar y detectar —interactiva, recalcula SHA-256 en
el navegador con los bloques reales—, alcance y límites), un menú "Criptografía" y pequeños ajustes de texto. Se probó con Edge sin
ventana; no en otros navegadores ni en pantallas pequeñas.

---

## 6. Cómo funciona (lo esencial para explicarlo)

**Bloque:** `id` (1, 2, 3…, clave primaria), `timestamp` (ms), `datos`, `prev_hash`, `hash_actual`.
**Hash:** `hash_actual = SHA-256(id|timestamp_ms|datos|prev_hash)`. El primer bloque usa `prev_hash` = 64 ceros.

**Decisiones de diseño y por qué:**
1. **Timestamp en milisegundos y truncado:** MongoDB solo guarda milisegundos; con nanosegundos, al releer, el hash recalculado no coincidiría y la cadena parecería corrupta sin que nadie la tocara.
2. **Separador `|` sin ambigüedad:** `id` y `timestamp` son numéricos y `prev_hash` mide siempre 64 caracteres, así que aunque `datos` contenga barras cada campo se separa de forma única.
3. **UTF-8 explícito:** el mismo texto da el mismo hash en cualquier máquina.
4. **Clave primaria = número de bloque:** dos eventos simultáneos no pueden crear una bifurcación; el segundo `insert` falla con `DuplicateKeyException` y reintenta con el número siguiente (hasta 5 veces).
5. **Registro tolerante a fallos:** si la cadena no se puede escribir, se registra el error en el log pero el login continúa.
6. **Verificación sin base de datos:** `VerificadorCadena` recibe una lista, así que se prueba en memoria.

**Verificación, por cada bloque:** (1) ¿su número es el esperado? (si no: `BLOQUE_FALTANTE`); (2) ¿el hash recalculado coincide con el guardado? (si no: `HASH_ALTERADO`); (3) ¿su `prev_hash` es el `hash_actual` guardado del anterior? (si no: `ENLACE_ROTO`). No se detiene en el primer error.

**Comportamientos que conviene saber (no son errores):**
- Si se **borra un bloque intermedio**, el bloque siguiente reporta `BLOQUE_FALTANTE` **y** `ENLACE_ROTO`.
- Si se altera un bloque **y se recalcula su hash**, el error aparece en el **siguiente** (`ENLACE_ROTO`).
- Si se altera y recalcula el **último** bloque, la cadena sale **válida**: es la limitación de "truncado final / sin ancla externa".
- Una cadena vacía se considera válida.

---

## 7. Cómo ejecutarlo (Windows, Git Bash)

Requisitos: JDK (se probó con la 26; el proyecto declara Java 21), Docker Desktop, Node.js (solo para `demo.sh`).

```bash
# 1. MongoDB propio de esta práctica (replica set de un nodo, sin autenticación, puerto 27018)
docker run -d --name moinchain-mongo -p 27018:27017 mongo:7 --replSet rs0 --bind_ip_all
docker exec moinchain-mongo mongosh --quiet --eval 'rs.initiate({_id:"rs0",members:[{_id:0,host:"localhost:27017"}]})'
#    (si el contenedor ya existe: docker start moinchain-mongo)

# 2. Variables de entorno (misma ventana donde se arranca la app)
export JAVA_HOME="/c/Program Files/Java/jdk-26.0.2.1"
export MONGO_URI="mongodb://localhost:27018/auth_db?replicaSet=rs0&directConnection=true"
export EUREKA_CLIENT_ENABLED=false
export SEMILLA_NOMINAL_HABILITADA=true
export AUTOTACHI_VICTOR_PASSWORD='<contraseña válida>'
export AUTOTACHI_ALEJANDRO_PASSWORD='<contraseña válida>'
export AUTOTACHI_HUGO_PASSWORD='<contraseña válida>'
export AUTOTACHI_VENDEDOR_PASSWORD='<contraseña válida>'

# 3. Pruebas y arranque (puerto 8081)
cd monpoint-ms/ms-auth
./mvnw clean verify
./mvnw spring-boot:run

# 4. En OTRA ventana (con AUTOTACHI_VICTOR_PASSWORD definida): la demostración completa
bash monpoint-ms/ms-auth/docs/cadena/demo.sh
```

Datos útiles:
- **Política de contraseñas:** mínimo 8, mayúscula, minúscula, número y un símbolo de `@$!%*?&` (el `#` **no** vale; máximo 72 bytes).
- **Admin para la demo:** `victor.ortiz@autotachi.mx` (rol ADMIN, sembrado). También hay `alejandro.bermudez@…`, `hugo.trejo@…` (ADMIN) y `vendedor@autotachi.mx` (VENDEDOR), todos `@autotachi.mx`.
- **Repetir la demo desde cero:** `docker exec moinchain-mongo mongosh --quiet auth_db --eval 'db.cadena_auditoria.drop()'`.
- **Ataque manual:** `docker exec moinchain-mongo mongosh --quiet auth_db --eval 'db.cadena_auditoria.updateOne({_id: 2}, {$set: {datos: "otra cosa"}})'` y luego `GET /auth/cadena/verificar`.
- Se usa un MongoDB **separado** (puerto 27018) para no tocar el de MonPoint (puerto 27017, con usuarios y claves propios).
- Las contraseñas de prueba **no** están en el repositorio y no deben subirse.

Trampas conocidas en esta máquina: con `mongosh`, `_id` numérico sale como objeto `Long` (usar `Number(b._id)`); `curl` desde Git Bash
manda mal los acentos en JSON (usar texto sin acentos); `python` en Git Bash puede colgarse (usar `node`); no matar procesos con
`pkill -f`/`Stop-Process` por coincidencia de texto (puede matar la propia terminal).

---

## 8. Por qué el proyecto está terminado: cada requisito del profesor

Cada fila cita el apartado del enunciado, qué pide, dónde se cumple y cómo se comprueba.

### §2 Descripción general
| Pide | Cumplimiento | Comprobación |
|---|---|---|
| Registros tipo transacciones, movimientos o **eventos** con hash dependiente del anterior | Eventos de seguridad de `ms-auth`, cada bloque guarda el hash del anterior (`prev_hash`) | `salida-demo.txt`: el `prevHash` de cada bloque es el `hashActual` del anterior |
| Si se modifica un registro pasado, la cadena se rompe y la app lo detecta | `VerificadorCadena` + `GET /auth/cadena/verificar` | `salida-demo.txt` sección 5: `valida:false`, bloque 2, `HASH_ALTERADO` |
| Sin consenso, minería ni P2P | Un solo nodo, sin ninguno de los tres; documentado como limitación | Informe, sección 5 |
| Demostrar: cálculo de hashes, encadenamiento, detección de alteraciones | Las tres cosas implementadas y probadas | `VerificadorCadenaTest` (10 pruebas) y demo |

### §3 Objetivos de aprendizaje
| # | Objetivo | Cumplimiento |
|---|---|---|
| 1 | Hash criptográfico (SHA-256) aplicado a registros | `CalculadorHash.java` con `MessageDigest` SHA-256. Verificado con una implementación independiente en Node.js: `verificacion-independiente.txt` (hashes idénticos) |
| 2 | Estructura que encadene registros con el hash del anterior | `BloqueAuditoria.java` (`prev_hash`, `hash_actual`, génesis) y `CadenaService.agregar` |
| 3 | Detectar modificaciones verificando la cadena | `VerificadorCadena.java`, con tres tipos de problema distinguidos |
| 4 | Documentar funcionamiento, supuestos, limitaciones y mejoras | `informe.pdf` secciones 2, 5 y 6; README de la cadena; este documento |

### §4.1 Estructura de los registros
| Campo exigido | Dónde |
|---|---|
| ID / número de bloque | `id` (`_id` en Mongo), consecutivo desde 1 |
| Timestamp | `timestamp` (milisegundos) |
| Datos | `datos` |
| `prev_hash` | `prev_hash` / `prevHash` |
| `hash_actual = H(ID ‖ timestamp ‖ datos ‖ prev_hash)` | `CalculadorHash.calcular`: `id + "|" + timestamp.toEpochMilli() + "|" + datos + "|" + prevHash`, SHA-256 |

### §4.2 Funcionalidades obligatorias
| # | Pide | Cumplimiento |
|---|---|---|
| 1 | Agregar un registro al final | Automático desde `AuthAuditService` por cada evento y manual con `POST /auth/cadena`; `CadenaService.agregar` |
| 2 | Mostrar todos en orden con sus hashes | `GET /auth/cadena` (orden por `id` ascendente) |
| 3 | Verificar: recorrer, recalcular, comparar, comprobar `prev_hash` = `hash_actual` del anterior, reportar válida o alterada | `VerificadorCadena.verificar` hace exactamente esos pasos y devuelve `valida`, `problemas[]` con bloque y motivo, y `mensaje` |

### §4.3 Persistencia
MongoDB (colección `cadena_auditoria`). **Comprobación:** se detuvo y se volvió a arrancar `ms-auth`; los 4 bloques seguían y la alteración
del bloque 2 seguía detectándose (`persistencia.txt`).

### §4.4 Prueba de alteración
Se modifica un bloque **directo en la base de datos** (sin pasar por la app) y se ejecuta la verificación: `demo.sh` pasos 4 y 5 y la sección
"Prueba de alteración" del README. Resultado real en `salida-demo.txt`. Además hay pruebas automáticas de 4 variantes de ataque.

### §5 Entregables
| Entregable | Cumplimiento |
|---|---|
| 1. Código fuente completo en carpeta organizada o repositorio, con README de compilación/ejecución | Repositorio `github.com/MopGEN/MoinChain`; paquete `cadena/`; `monpoint-ms/ms-auth/docs/cadena/README.md` |
| 2. Informe técnico 4–6 páginas en PDF | `docs/informe/informe.pdf`: **5 páginas** |
| 2a. Introducción: qué es un hash y qué idea de blockchain se simula | Informe, sección 1 |
| 2b. Estructura de datos y lógica de encadenamiento | Informe, sección 2 |
| 2c. Capturas: agregando, mostrando, verificando, detectando alteración | Informe, sección 4, figuras 1–5 (más figura 6 de verificación independiente). **Son salida real de la consola en formato de texto, no pantallazos** |
| 2d. Limitaciones (por qué NO es blockchain completa) | Informe, sección 5 |
| 2e. Posibles mejoras | Informe, sección 6 (firma digital, anclaje externo, múltiples nodos, árbol de Merkle, verificación periódica) |
| 3. Demostración breve (opcional) | `demo.sh` y la sección interactiva "Alterar y detectar" de la presentación |

### §6 Rúbrica (autoevaluación honesta; la nota la pone el profesor)
| Criterio (pts) | Evidencia | Riesgo |
|---|---|---|
| Diseño y estructura de datos (20) | Tabla de campos documentada en README e informe; `record` inmutable; decisiones justificadas | Bajo |
| Hash y encadenamiento (25) | SHA-256, génesis, concurrencia resuelta, timestamp truncado, verificación cruzada con Node.js | Bajo |
| Verificación y detección (25) | Reporta **qué bloque y por qué**, no se detiene en el primer error, cubre alteración, enlace roto, borrado y atacante hábil | Bajo; la limitación del último bloque está explicada |
| Persistencia y usabilidad (10) | MongoDB, reinicio comprobado, mensajes claros, `demo.sh`. **No hay interfaz gráfica**: se usa API REST | Medio si el profesor espera una pantalla |
| Informe técnico (15) | 5 páginas con todas las secciones pedidas | Medio: figuras en texto y formato de una columna (ver §9) |
| Buenas prácticas y comentarios (5) | Javadoc y comentarios en español; nombres claros; clases pequeñas | Bajo |

### §7 Lineamientos de IA y ética
- Se usó IA (Claude) para construir el proyecto, lo cual el enunciado permite **solo como apoyo**.
- **Condición para cumplir §7:** la persona que entrega debe **poder explicar el código y las decisiones** en una revisión oral. Si no puede, se
  considera falta de autoría. Por eso este documento incluye la sección 6 (cómo funciona) y la 10 (preparación para la defensa).
- No se debe afirmar que el código se escribió sin ayuda de IA.

**Conclusión:** los 4 objetivos, las 3 funciones obligatorias, la persistencia, la prueba de alteración y los 3 entregables del enunciado tienen
implementación y evidencia verificable. Lo que queda no es desarrollo sino revisión humana (sección 9).

---

## 9. Pendientes y puntos a revisar por una persona

1. **Formato del informe:** el profesor adjuntó plantillas LNCS (dos columnas); el informe está en **una columna**. Confirmar con el profesor si lo exige.
2. **Capturas:** son salida de consola renderizada como texto. Si se quieren pantallazos reales, tomarlos ejecutando `demo.sh`.
3. **Datos de autoría en el informe:** aparece "Víctor Ortiz · Universidad de Guadalajara". Revisar que sea correcto.
4. **Ejecutar la demo en la máquina de quien entrega** al menos una vez antes de presentar.
5. **No verificado:** acceso a `/auth/cadena*` a través del API Gateway (`:8080`); la presentación en otros navegadores o móvil; ejecución con JDK 21 (se usó la 26).
6. **Limitación a conocer para la defensa:** alterar y recalcular el hash del **último** bloque no se detecta (se resolvería con firma o con anclar el último hash fuera de la base).
7. **Privacidad:** los bloques contienen correos; la API es solo ADMIN.
8. **Nombre del contenedor/puerto:** `moinchain-mongo` en `27018`. Si el compañero ya usa ese puerto, cambiar `-p 27018:27017` y `MONGO_URI`.

---

## 10. Preparación para la defensa oral (§7)

**Discurso de 1 minuto:** "Tomé la bitácora de seguridad de mi servicio de usuarios, que solo era un log editable, y la convertí en una cadena.
Cada bloque guarda el hash del anterior y su propio hash se calcula con id, tiempo, datos y ese hash previo. Si alguien cambia un registro viejo,
su hash ya no coincide y el siguiente bloque apunta a un hash que ya no existe. La verificación recorre todo y dice qué bloque se rompió y por qué."

**Preguntas probables:**
- *¿Por qué SHA-256?* Estándar (FIPS 180-4), sin colisiones prácticas conocidas, viene en Java sin dependencias.
- *¿Qué detecta si cambio los datos del bloque 3?* `HASH_ALTERADO` en el bloque 3.
- *¿Y si recalculo también su hash?* El bloque 4 deja de apuntar a él: `ENLACE_ROTO` en el 4 (hay prueba automática).
- *¿Qué no detecta?* Reescritura total de la cadena por quien controle toda la base, y borrado o alteración del último bloque sin referencia externa.
- *¿Por qué truncar el timestamp?* Mongo guarda milisegundos; sin truncar, la cadena parecería corrupta sola.
- *¿Y dos usuarios a la vez?* Clave primaria + reintento.
- *¿Por qué no es blockchain completa?* Un solo nodo, sin consenso, sin minería, sin firma.
- *¿Qué mejorarías?* Firmar bloques (ECDSA/Ed25519), anclar el último hash fuera, replicar en varios nodos, árbol de Merkle.

**Qué dominar línea por línea (unas 90 líneas):** `CalculadorHash.java` y `VerificadorCadena.java`. Practicar: ejecutar `demo.sh` prediciendo la salida
antes de verla; alterar `prev_hash` del bloque 3 y adivinar el tipo de problema (`ENLACE_ROTO`).

---

## 11. Si un asistente de IA va a continuar este trabajo

- Responder y comentar el código **en español**; seguir el estilo existente (Javadoc en español, nombres descriptivos).
- Cambios pequeños y verificables; no refactorizar lo que ya funciona (el proyecto está terminado).
- Antes de cambiar algo, correr las pruebas: `cd monpoint-ms/ms-auth && ./mvnw -o test` (con `JAVA_HOME` y las variables de la sección 7).
- Nunca versionar credenciales, llaves, `.env` reales ni `target/`; `CLAUDE.md` está ignorado a propósito.
- No hacer `push` a `MonPoint`. En MoinChain, `main` es la rama principal; preguntar antes de subir o reescribir historia.
- Cualquier cambio que altere la fórmula del hash invalida todas las cadenas existentes y los hashes de `salida-demo.txt` y de la presentación.
- La presentación (`docs/presentacion/index.html`) duplica los 3 bloques reales de `salida-demo.txt`; si se regenera la demo, actualizar ahí también.
- El profesor evalúa la **autoría**: ayudar a la persona a entender, no solo a entregar.
