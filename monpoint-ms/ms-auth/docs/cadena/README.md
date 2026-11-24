# MoinChain — Cadena de auditoría con hash (mini-blockchain)

Proyecto final de **IH063 – Criptografía**. Base de datos encadenada con hash dentro de `ms-auth`:
cada evento de seguridad (login, alta, modificación o baja de usuarios) queda como un bloque cuyo hash
depende del bloque anterior. Si alguien modifica un registro pasado, la verificación lo detecta.

> Checklist de decisiones y su base en los lineamientos: [`/docs/CRIPTO_CHECKLIST.md`](../../../../docs/CRIPTO_CHECKLIST.md).
> Informe técnico: [`/docs/informe/informe.pdf`](../../../../docs/informe/informe.pdf).

## Estructura de datos (sección 4.1)

Colección MongoDB `cadena_auditoria` (clase [`BloqueAuditoria`](../../src/main/java/com/monpoint/auth/cadena/BloqueAuditoria.java)):

| Campo | Tipo | Descripción |
|---|---|---|
| `_id` (`id`) | número | Número de bloque, consecutivo desde 1. Es clave primaria: no pueden existir dos bloques con el mismo número. |
| `timestamp` | fecha (ms) | Momento de creación, truncado a milisegundos. |
| `datos` | texto | El evento, p. ej. `evento=LOGIN_EXITOSO usuarioId=… correo=… rol=ADMIN`. |
| `prev_hash` | 64 hex | `hash_actual` del bloque anterior; 64 ceros en el primero (génesis). |
| `hash_actual` | 64 hex | `SHA-256( id \| timestamp_ms \| datos \| prev_hash )`. |

## Código

| Archivo | Responsabilidad |
|---|---|
| `CalculadorHash` | La fórmula del hash (SHA-256, UTF-8). |
| `BloqueAuditoria` | El registro y su creación (trunca el timestamp a ms). |
| `VerificadorCadena` | Recorre, recalcula, compara y reporta qué bloque falla y por qué. Sin base de datos. |
| `CadenaService` | Agregar (con reintento ante concurrencia), listar y verificar. |
| `CadenaController` | `GET /auth/cadena`, `GET /auth/cadena/verificar`, `POST /auth/cadena` (solo ADMIN). |
| `AuthAuditService` | Cada evento de seguridad, además de ir al log, se agrega a la cadena. |

Tipos de alteración que distingue la verificación:

- `HASH_ALTERADO`: el contenido del bloque no coincide con su hash.
- `ENLACE_ROTO`: el `prev_hash` no coincide con el `hash_actual` del bloque anterior.
- `BLOQUE_FALTANTE`: falta un número de bloque (se borró uno).

## Cómo ejecutarlo

Requisitos: JDK 21 o superior, Docker, Node.js (solo para el script de demo).

```bash
# 1. MongoDB propio de esta práctica (replica set de un nodo, sin autenticación, puerto 27018)
docker run -d --name moinchain-mongo -p 27018:27017 mongo:7 --replSet rs0 --bind_ip_all
docker exec moinchain-mongo mongosh --quiet --eval 'rs.initiate({_id:"rs0",members:[{_id:0,host:"localhost:27017"}]})'

# 2. Variables de entorno (valores de prueba; la política exige mayúscula, minúscula, número y uno de @$!%*?&)
export MONGO_URI="mongodb://localhost:27018/auth_db?replicaSet=rs0&directConnection=true"
export EUREKA_CLIENT_ENABLED=false
export SEMILLA_NOMINAL_HABILITADA=true
export AUTOTACHI_VICTOR_PASSWORD='<elige una contraseña válida>'
export AUTOTACHI_ALEJANDRO_PASSWORD='<elige una contraseña válida>'
export AUTOTACHI_HUGO_PASSWORD='<elige una contraseña válida>'
export AUTOTACHI_VENDEDOR_PASSWORD='<elige una contraseña válida>'

# 3. Pruebas (97, no necesitan Docker salvo las de integración) y arranque
cd monpoint-ms/ms-auth
./mvnw clean verify
./mvnw spring-boot:run

# 4. Demostración completa: agrega, muestra, verifica, ataca y vuelve a verificar
bash docs/cadena/demo.sh
```

## Prueba de alteración (sección 4.4)

Se modifica un bloque **directamente en la base de datos**, sin pasar por la aplicación:

```bash
docker exec moinchain-mongo mongosh --quiet auth_db --eval \
  'db.cadena_auditoria.updateOne({_id: 2}, {$set: {datos: "evento=LOGIN_FALLIDO BORRADO POR EL ATACANTE"}})'
```

Luego `GET /auth/cadena/verificar` responde `"valida": false` y señala `bloqueId: 2, tipo: HASH_ALTERADO`.
La salida real de la demostración está en [`salida-demo.txt`](salida-demo.txt); la comprobación de que otro
programa (Node.js) obtiene el mismo hash, en [`verificacion-independiente.txt`](verificacion-independiente.txt);
y la de persistencia tras reiniciar, en [`persistencia.txt`](persistencia.txt).

## Limitaciones

No es una blockchain completa: un solo nodo (sin consenso ni P2P), sin minería, y sin firma digital, así que
quien controle toda la base de datos y recalcule **todos** los hashes siguientes puede reescribir la cadena sin ser
detectado. Tampoco se detecta que se borren los **últimos** bloques. Mejoras posibles: firmar cada bloque,
anclar periódicamente el último hash en un lugar externo, y replicar la cadena en varios nodos.
