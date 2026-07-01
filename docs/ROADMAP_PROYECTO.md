# Roadmap del Proyecto: Sistema de Subastas Online

Este documento establece el plan de ejecución estratégico para el equipo de desarrollo, dividiendo los requerimientos del Trabajo Práctico Integrador (TPI) en fases lógicas y manejables. 

Este enfoque asegura que primero se construya una base sólida de datos antes de avanzar hacia lógicas de negocio complejas o seguridad.

## Fase 1: Capa de Persistencia y Modelado de Datos - COMPLETADA
El objetivo de esta fase es tener la base de datos generada automáticamente por JPA con todas las restricciones exactas pedidas en el documento.

1. **Entidades JPA (Models):**
   - Crear las entidades base: `Usuario`, `Rol`, `Categoria`, `Producto`, `Subasta`, `Puja`, `HistorialEstado`, `Notificacion`, `Disputa`.
   - Mapeo estricto de tipos: Uso exclusivo de `BigDecimal` para importes y configuración de fechas en UTC.
   - Implementación del mecanismo de Bloqueo Optimista: Añadir campo `version` (anotado con `@Version`) en la entidad `Subasta`.
2. **Relaciones JPA:**
   - Configurar relaciones OneToMany, ManyToOne, ManyToMany y OneToOne entre las entidades.
3. **DTOs y Validaciones:**
   - Crear clases DTO para transferencia de datos.
   - Aplicar anotaciones de validación (ej. `@NotNull`, `@Min`, `@NotBlank`).
4. **Repositorios:**
   - Creación de interfaces extendiendo `JpaRepository` para cada entidad.
5. **Mapeo:**
   - Crear clases o utilidades para transformar de Entidad a DTO y viceversa.

## Fase 2: Arquitectura Base y Manejo de Errores - COMPLETADA
Antes de programar la lógica del negocio, se debe preparar la estructura para manejar los flujos excepcionales de manera limpia.

1. **Manejo Global de Excepciones:**
   - Implementar un `@RestControllerAdvice` (`GlobalExceptionHandler`).
   - Definir excepciones personalizadas de negocio (`ReglaNegocioException`, `MontoInvalidoException`, `SubastaNoActivaException`).
   - Estandarizar la respuesta de error (`ApiErrorDTO` con código de estado, mensaje, timestamp).
2. **Configuración de Entorno y Base de Datos:**
   - Setup de **PostgreSQL** mediante variables de entorno para tener un entorno idéntico al de presentación/producción desde el día 1.

## Fase 3: Seguridad y Autenticacion - COMPLETADA
Implementar el escudo del sistema antes de exponer los endpoints de negocio.

1. **Configuracion de Spring Security:**
   - Cifrado de contrasenas con `BCryptPasswordEncoder`.
   - Implementacion de `UserDetails` en la entidad `Usuario` con `getAuthorities()` mapeando roles con prefijo `ROLE_`.
   - `UserDetailsServiceImpl` para cargar usuario desde BD.
   - Sesiones stateless (`SessionCreationPolicy.STATELESS`), CSRF deshabilitado.
2. **Implementacion de JWT:**
   - `JwtUtil`: generacion y validacion de tokens con algoritmo HS256, claims `sub`, `iat`, `exp`, `roles`.
   - `JwtAuthenticationFilter`: filtro que intercepta peticiones, extrae JWT del header `Authorization: Bearer`, valida y carga `SecurityContext`.
   - Secretos externalizados via `@Value("${jwt.secret}")` (configurable por variable de entorno `JWT_SECRET`).
3. **Endpoints de Auth:**
   - `POST /auth/register`: valida email unico, hashea password, asigna rol `USER` por defecto, retorna JWT.
   - `POST /auth/login`: autentica via `AuthenticationManager`, retorna JWT con roles. Mensaje generico ante credenciales invalidas (OWASP).
4. **Control de Acceso por Roles (RBAC):**
   - Rutas publicas: `GET /api/categorias`, `GET /api/productos`, `GET /api/subastas`.
   - `POST /api/productos` -> `SELLER`.
   - `POST /api/subastas`, `PATCH /api/subastas/{id}/publicar` -> `SELLER`.
   - `PATCH /api/subastas/{id}/cancelar` -> `SELLER` o `ADMIN`.
   - `POST /api/categorias` -> `ADMIN`.
   - Proteccion por metodo con `@PreAuthorize` y por URL con `requestMatchers`.
5. **Infraestructura adicional:**
   - `DataInitializer`: crea roles `USER`, `SELLER`, `ADMIN` al iniciar la app.
   - `SecurityUtils`: helper para extraer usuario autenticado desde `SecurityContextHolder`.
   - `GlobalExceptionHandler`: maneja `AuthenticationException -> 401` y `AccessDeniedException -> 403`.
   - CORS global configurado para desarrollo (`*`).

### Pruebas de Seguridad (verificadas en terminal)

```bash
# 1. Registrar usuario (retorna JWT con ROLE_USER)
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"usernameEmail":"user@mail.com","password":"123456"}'
# -> 201 { accessToken, tokenType: "Bearer", expiresIn: 3600000, user: { id, usernameEmail, roles: ["ROLE_USER"] } }

# 2. Login (retorna JWT)
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"usernameEmail":"user@mail.com","password":"123456"}'
# -> 200 { accessToken, ... }

# 3. Login con credenciales invalidas
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"usernameEmail":"user@mail.com","password":"wrong"}'
# -> 401

# 4. GET publico (sin token)
curl http://localhost:8080/api/subastas
# -> 200 []

# 5. POST protegido sin token
curl -X POST http://localhost:8080/api/productos \
  -H "Content-Type: application/json" \
  -d '{"nombre":"Producto","categoriaId":1}'
# -> 403 (Spring Security bloquea antes de llegar al controller)

# 6. POST protegido con token de USER (sin rol SELLER)
curl -X POST http://localhost:8080/api/productos \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <TOKEN>" \
  -d '{"nombre":"Producto","categoriaId":1}'
# -> 403

# 7. POST categorias con token de USER (sin rol ADMIN)
curl -X POST http://localhost:8080/api/categorias \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <TOKEN>" \
  -d '{"nombre":"Electronica"}'
# -> 403
```

## Fase 4: Lógica de Negocio y Controladores 

1. **Gestion de Productos y Categorias:** (parcial - CRUD basico existente)
   - CRUD basico restringido a rol `SELLER`. GET y POST implementados.
2. **Gestion de Subastas (SELLER y ADMIN):** (parcial)
   - Creacion (estado BORRADOR/PUBLICADA). POST y PATCH publicar implementados.
   - Reglas de cancelacion (diferenciando permisos entre SELLER y ADMIN). PATCH cancelar implementado.
3. **Motor de Pujas (El nucleo transaccional):**
    - Endpoint estricto para recibir pujas.
    - Validacion autonoma en backend (precio base o monto actual + incremento).
    - Verificacion estricta de tiempo y estado.
    - Resolucion de concurrencia mediante el rechazo limpio cuando falla la version (Bloqueo Optimista).
    > NOTA: El bloqueo optimista ya esta configurado en Subasta (@Version). Cuando dos pujas lleguen simultaneamente, la segunda recibe 409 CONFLICT automaticamente. Ver docs/NOTAS_TECNICAS.md para detalles.
4. **Privacidad de las Ofertas:**
    - Logica en el backend para ocultar la identidad de los oferentes mientras la subasta esta ACTIVA, revelandolos solo al final al SELLER y al ADMIN en todo momento.
    > NOTA: Los services usan SecurityUtils.getUsuarioActual() para obtener el usuario autenticado. Ver ejemplo en ProductoService.crearProducto().
5. **Integracion Frontend-Backend (CORS):** COMPLETO
   - Configuracion global de CORS en `SecurityConfig` para permitir peticiones HTTP desde el frontend en desarrollo local.

## Fase 5: Tareas Programadas y Auditoría (Cierre Dinámico)
Implementar automatizaciones y trazabilidad.

1. **Cierre Automatico y Temporizadores:**
    - Uso de `@Scheduled` en Spring Boot para barrer periodicamente las subastas.
    - Cambios de estado automaticos: PUBLICADA -> ACTIVA; ACTIVA -> ADJUDICADA / FINALIZADA.
    - Logica del "Minuto de Ley": Extension automatica del tiempo de cierre ante pujas de ultimo minuto.
    > NOTA: El enum EstadoSubasta ya existe con: BORRADOR, PUBLICADA, ACTIVA, ADJUDICADA, FINALIZADA, CANCELADA, CERRADA. Las transiciones deben registrar HistorialEstado (entidad ya creada).
2. **Auditoría e Historial:**
   - Registro automático en `historial_estados` ante cada cambio de fase.
3. **Notificaciones y Disputas:**
   - Generación de notificaciones internas en BD al ganador y vendedor.
   - Flujo de creación de disputas por el usuario y resolución por el ADMIN.
