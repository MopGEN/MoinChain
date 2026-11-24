# MoinChain — Checklist del proyecto final de Criptografía (IH063)

Cada paso indica **qué** se hace, **por qué** y **en qué parte de los lineamientos** del profesor se basa
(números de sección del documento "Proyecto final", entrega 1 dic).

| # | Qué se hace | Por qué | Base en los lineamientos |
|---|---|---|---|
| 1 | Elegir el servicio `ms-auth` y registrar en la cadena sus eventos de seguridad (login, alta, modificación, baja). | Los lineamientos piden "un registro de transacciones, movimientos o eventos"; la bitácora de auditoría ya existía y solo se imprimía en un log, que es modificable sin dejar rastro. | §2 Descripción general |
| 2 | Preparar entorno: JDK, Docker con MongoDB, rama `cripto/cadena-auditoria`. | Sin base de datos corriendo no hay persistencia ni capturas para el informe. | §4.3 Persistencia, §5 Entregables |
| 3 | Modelar el bloque: `id`, `timestamp`, `datos`, `prevHash`, `hashActual`. | Son exactamente los campos mínimos exigidos. | §4.1 Estructura de los registros |
| 4 | Calcular `hash_actual = SHA-256(id \| timestamp \| datos \| prev_hash)` en una sola clase. | Es la fórmula que se pide; aislarla en una clase pequeña facilita probarla y explicarla. | §4.1, §3 objetivo 1 |
| 5 | Crear un bloque génesis con `prev_hash` de 64 ceros. | El primer registro no tiene anterior; se necesita un valor fijo y conocido. | §4.1, §3 objetivo 2 |
| 6 | Función de verificación: recorrer, recalcular, comparar y revisar que `prev_hash` = `hash_actual` del anterior; reportar **qué bloque** falla y **por qué**. | Es lo que más puntos vale (25) y el 4.2.3 describe el algoritmo paso a paso. | §4.2 punto 3, rúbrica "Verificación" |
| 7 | Pruebas unitarias de: cadena válida, dato alterado, enlace roto, bloque borrado. | Demuestran con evidencia que la detección funciona en todos los casos, no solo en uno. | §4.4, rúbrica "casos no cubiertos" |
| 8 | Guardar los bloques en MongoDB con id numérico como clave única. | Persistencia entre ejecuciones; la clave única impide que dos bloques compitan por el mismo número. | §4.3 |
| 9 | Conectar `AuthAuditService` para que cada evento cree un bloque, sin que un fallo de la cadena tumbe el login. | "Agregar un nuevo registro al final de la cadena"; el login es más importante que su bitácora. | §4.2 punto 1 |
| 10 | Endpoints solo ADMIN: `GET /auth/cadena`, `GET /auth/cadena/verificar`, `POST /auth/cadena`. | Mostrar registros con hashes, verificar y agregar manualmente (demo). Solo ADMIN porque la bitácora es información sensible. | §4.2 puntos 1–3, rúbrica "Usabilidad" |
| 11 | Simular el ataque editando un bloque directo en MongoDB y verificar. | Es la prueba de alteración obligatoria. | §4.4 |
| 12 | README con instrucciones de ejecución, estructura de datos y guion de demo. | Entregable explícito y 20 pts de "diseño documentado". | §5.1, rúbrica "Diseño" |
| 13 | Informe técnico 4–6 páginas con capturas de los 4 momentos, limitaciones y mejoras. | Entregable explícito (15 pts). | §5.2 |
| 14 | Comentarios en español y nombres claros en todo el código nuevo. | 5 pts de buenas prácticas. | Rúbrica "Buenas prácticas" |
| 15 | Subir todo a GitHub y revisar el código línea por línea con su autor. | Entrega en repositorio y regla de autoría: el estudiante debe poder explicarlo. | §5.1, §7 Lineamientos de IA |
