#!/usr/bin/env bash
# Demostración de la cadena de auditoría (proyecto final de Criptografía).
# Requisitos: ms-auth corriendo en localhost:8081, MongoDB accesible como contenedor Docker
# llamado "moinchain-mongo" (ver README), y la variable AUTOTACHI_VICTOR_PASSWORD definida.
# Nunca imprime contraseñas ni tokens.
set -u
API=${API:-http://localhost:8081}
MONGO_CONTENEDOR=${MONGO_CONTENEDOR:-moinchain-mongo}
bonito() { node -e 'let s="";process.stdin.on("data",d=>s+=d).on("end",()=>console.log(JSON.stringify(JSON.parse(s),null,2)))'; }
titulo() { printf '\n==================== %s ====================\n' "$1"; }

titulo "1. AGREGAR registros (cada evento de seguridad crea un bloque)"
echo "-> Login fallido (contraseña incorrecta)"
curl -s -o /dev/null -w "HTTP %{http_code}\n" -X POST "$API/auth/login" -H 'Content-Type: application/json' \
  -d '{"correo":"victor.ortiz@autotachi.mx","password":"incorrecta"}'
echo "-> Login exitoso del ADMIN"
TOKEN=$(curl -s -X POST "$API/auth/login" -H 'Content-Type: application/json' \
  -d "{\"correo\":\"victor.ortiz@autotachi.mx\",\"password\":\"$AUTOTACHI_VICTOR_PASSWORD\"}" \
  | node -e 'let s="";process.stdin.on("data",d=>s+=d).on("end",()=>console.log(JSON.parse(s).token||JSON.parse(s).accessToken||""))')
[ -n "$TOKEN" ] && echo "token obtenido (oculto)" || { echo "No se obtuvo token"; exit 1; }
echo "-> Registro manual (POST /auth/cadena)"
curl -s -X POST "$API/auth/cadena" -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"datos":"nota manual: inicio de la demostracion"}' | bonito

titulo "2. MOSTRAR la cadena (GET /auth/cadena)"
curl -s "$API/auth/cadena" -H "Authorization: Bearer $TOKEN" | bonito

titulo "3. VERIFICAR la cadena intacta (GET /auth/cadena/verificar)"
curl -s "$API/auth/cadena/verificar" -H "Authorization: Bearer $TOKEN" | bonito

titulo "4. ATAQUE: modificar el bloque 2 directamente en MongoDB (sin pasar por la aplicación)"
docker exec "$MONGO_CONTENEDOR" mongosh --quiet auth_db --eval \
  'printjson(db.cadena_auditoria.updateOne({_id: 2}, {$set: {datos: "evento=LOGIN_FALLIDO BORRADO POR EL ATACANTE"}}))'

titulo "5. VERIFICAR otra vez: la alteración debe detectarse"
curl -s "$API/auth/cadena/verificar" -H "Authorization: Bearer $TOKEN" | bonito
