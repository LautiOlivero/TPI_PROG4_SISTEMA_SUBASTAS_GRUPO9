# Notas Técnicas - Sistema de Subastas

## Estado Actual del Proyecto

**Completado:**

- Fase 1: Entidades JPA, relaciones, DTOs, repositorios
- Fase 2: Manejo global de excepciones, configuración PostgreSQL
- Fase 3: Seguridad completa (Spring Security, JWT, autenticación, RBAC)
- Fase 5: Tareas programadas, cierre automático, notificaciones, disputas

**Pendiente:**

- Fase 4: Motor de pujas, privacidad de ofertas, lógica de negocio avanzada

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
