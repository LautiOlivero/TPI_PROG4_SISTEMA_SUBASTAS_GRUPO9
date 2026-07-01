# Notas Técnicas - Sistema de Subastas

## Estado Actual del Proyecto

**Completado:**

- Fase 1: Entidades JPA, relaciones, DTOs, repositorios
- Fase 2: Manejo global de excepciones, configuración PostgreSQL
- Fase 3: Seguridad completa (Spring Security, JWT, autenticación, RBAC)
- Fase 4: Motor de pujas, privacidad de ofertas, lógica de negocio avanzada
- Fase 5: Tareas programadas, cierre automático, notificaciones, disputas
- Carga de datos: 3 usuarios, 3 categorías, 65 productos, 65 subastas cargados en Render

## Bugs Corregidos

**ROLE_null en JWT** (`AuthService.java`): `register()` usaba `Rol.builder().id(1L).build()` creando objetos con `nombre=null`, lo que generaba `ROLE_null` en el token. Corregido con `rolRepository.findByNombre("USER")` y `rolRepository.findByNombre("SELLER")`.

**imagenUrl ausente en respuesta** (`DtoMapper.java`): `toProductoDTO()` no incluía `.imagenUrl(producto.getImagenUrl())`. Los datos se guardaban pero no se devolvían en el JSON. Corregido agregando el campo al builder.

**NPE en historialEstado** (`DtoMapper.java`): `toHistorialEstadoDTO()` llamaba a `toUsuarioDTO(historial.getUsuarioResponsable())` sin verificar null. El scheduler no setea `usuarioResponsable` en transiciones automáticas, lo que causaría NullPointerException. Corregido con null-check previo.

## Convenciones del Proyecto

**Inyección de dependencias:** Siempre por constructor con `private final`, nunca `@Autowired` en campos.

**DTOs:** Usan Lombok (`@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`). Los de request están en `dtos/request/`, los de response en `dtos/response/`.

**Servicios:** Anotados con `@Service`, métodos transaccionales con `@Transactional`. Los de solo lectura usan `@Transactional(readOnly = true)`.

**Estructura de paquetes:**
```
com.prog.tpi.sistema_subastas/
├── config/          → DataInitializer, configuración de arranque
├── controllers/     → Endpoints REST
├── dtos/
│   ├── request/     → DTOs de entrada
│   └── response/    → DTOs de salida
├── exceptions/      → Excepciones personalizadas y GlobalExceptionHandler
├── models/          → Entidades JPA
├── repositories/    → Interfaces JpaRepository
├── security/        → JWT, filtros, configuración de seguridad
├── services/        → Lógica de negocio
└── util/            → DtoMapper y helpers
```

**Mapeo:** Se usa `DtoMapper` (clase estática) para convertir entre entidades y DTOs.

## Endpoints Existentes

| Ruta | Método | Protección | Rol | Estado |
|------|--------|-----------|-----|--------|
| `/auth/register` | POST | Público | - | ✅ |
| `/auth/login` | POST | Público | - | ✅ |
| `/api/categorias` | GET | Público | - | ✅ |
| `/api/categorias` | POST | Auth | ADMIN | ✅ |
| `/api/productos` | GET | Público | - | ✅ |
| `/api/productos/{id}` | GET | Público | - | ✅ |
| `/api/productos` | POST | Auth | SELLER | ✅ |
| `/api/subastas` | GET | Público | - | ✅ |
| `/api/subastas/{id}` | GET | Público | - | ✅ |
| `/api/subastas` | POST | Auth | SELLER | ✅ |
| `/api/subastas/{id}/publicar` | PATCH | Auth | SELLER | ✅ |
| `/api/subastas/{id}/cancelar` | PATCH | Auth | SELLER, ADMIN | ✅ |
| `/api/subastas/{id}/pujas` | POST | Auth | USER | ✅ |
| `/api/subastas/{id}/pujas` | GET | Auth | - | ✅ |
| `/api/notificaciones` | GET | Auth | - | ✅ |
| `/api/disputas` | POST | Auth | USER | ✅ |
| `/api/disputas/{id}/resolver` | PATCH | Auth | ADMIN | ✅ |
| `/api/usuarios/perfil` | GET | Auth | - | ✅ |

## Seguridad

**Autenticación:** JWT con algoritmo HS256. El token se envía en header `Authorization: Bearer <token>`.

**Usuario autenticado:** Usar `SecurityUtils.getUsuarioActual()` para obtener el `Usuario` desde el contexto de seguridad. Ejemplo de uso en `ProductoService.crearProducto()`.

**Roles:**
- `USER`: Rol por defecto al registrarse
- `SELLER`: Puede crear productos y subastas
- `ADMIN`: Puede crear categorías y cancelar subastas activas

**Protección de endpoints:** Se usa `@PreAuthorize("hasRole('ROL')")` a nivel de método en los controllers.

## Bloqueo Optimista

**Configuración:** La entidad `Subasta` tiene el campo `@Version` (línea 63-65). Hibernate controla la versión automáticamente.

**Funcionamiento:**
1. Se carga la subasta con su versión actual
2. Al hacer `save()`, Hibernate verifica que la versión en BD sea la misma
3. Si otro proceso modificó la subasta, lanza `ObjectOptimisticLockingFailureException`
4. El `GlobalExceptionHandler` captura esto y devuelve **409 CONFLICT** con mensaje para reintentar

**Importante:** Los métodos que modifican subastas deben tener `@Transactional`. No hay que manejar la versión manualmente.

## Cómo Correr la App

**Variables de entorno necesarias:**
```env
DB_URL=jdbc:postgresql://<host>:<port>/<database>
DB_USER=<usuario>
DB_PASS=<password>
JWT_SECRET=<clave_secreta_opcional>  # Tiene valor por defecto
```

**Comando:**
```bash
./mvnw spring-boot:run
```

**Pruebas básicas con curl:**
```bash
# Registrar usuario
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"usernameEmail":"user@mail.com","password":"123456"}'

# Login
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"usernameEmail":"user@mail.com","password":"123456"}'

# GET público
curl http://localhost:8080/api/subastas

# POST protegido (requiere token de SELLER)
curl -X POST http://localhost:8080/api/productos \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <TOKEN>" \
  -d '{"nombre":"Producto","categoriaId":1}'
```

**Roles en BD:** El `DataInitializer` crea automáticamente los roles `USER`, `SELLER`, `ADMIN` al iniciar la app.

## Reglas de Negocio Importantes

**Regla de 48 horas para publicar:** `SubastaService.publicarSubasta()` valida que `fechaInicio >= Instant.now() + 48h`. Si no se cumple, lanza `ReglaNegocioException` → 400 Bad Request. Al cargar subastas de prueba, usar fechas futuras (mínimo 72h para margen de seguridad).

**Ciclo de vida de una subasta:**
```
BORRADOR → PUBLICADA (POST /publicar, manual, SELLER)
         → ACTIVA    (scheduler, cuando fechaInicio llega)
         → ADJUDICADA (scheduler, cuando fechaCierre pasa Y hay ganadorActual)
         → FINALIZADA (scheduler, cuando fechaCierre pasa Y no hay pujas)
         → CANCELADA  (POST /cancelar, manual, SELLER/ADMIN)
```

**Scheduler:** Corre cada 30 segundos (`@Scheduled(fixedDelay=30_000)`). Hace las transiciones PUBLICADA→ACTIVA y ACTIVA→ADJUDICADA/FINALIZADA automáticamente.

**Cancelación:** SELLER puede cancelar sus subastas en estado BORRADOR o PUBLICADA. Solo ADMIN puede cancelar una subasta ACTIVA.

## Scripts de Carga de Datos

Los scripts están en la raíz del proyecto. Ejecutar en orden:

```bash
# 1. Cargar categorías, usuarios y productos (si no están ya)
python cargar_productos.py   # carga categorías + 2 sellers + 65 productos

# 2. Cargar subastas
python cargar_subastas.py    # crea y publica 65 subastas (7 CANCELADA)

# 3. Crear variedad de estados para demo
python actualizar_estados.py # setea fechas al pasado; esperar 30s al scheduler
```

`cargar_subastas.py` usa `fechaInicio = "2026-07-04T10:00:00Z"` para las subastas sin fecha futura explícita, satisfaciendo la regla de 48h.

`actualizar_estados.py` usa psycopg2 con conexión directa a Render para evitar problemas de auto-commit de DBeaver:
- id <= 25 PUBLICADA → setea fechaInicio al pasado → scheduler las pone ACTIVA
- id 26-40 PUBLICADA → setea fechaInicio y fechaCierre al pasado → scheduler las pone FINALIZADA (si no tienen pujas)

## Performance: N+1 en GET /api/subastas

`GET /api/subastas` tarda ~10 segundos contra Render porque Hibernate hace lazy-loading: 65 subastas × ~7 queries cada una ≈ 455 queries individuales a la DB remota.

La solución es agregar `@EntityGraph` en `SubastaRepository.findAll()` para traer todo en un JOIN. No implementado aún.

## Demo: Cómo mostrar estado ADJUDICADA

El estado ADJUDICADA no se puede precargar (requiere puja real + cierre). Para demostrarlo en vivo:
1. Hacer una puja en una subasta ACTIVA
2. Ejecutar: `UPDATE subastas SET fecha_cierre = NOW() - INTERVAL '1 minute' WHERE id = X;`
3. Esperar ~30 segundos → el scheduler la cierra como ADJUDICADA
