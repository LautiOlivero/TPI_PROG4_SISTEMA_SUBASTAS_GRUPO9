# Roadmap del Proyecto: Sistema de Subastas Online

Este documento establece el plan de ejecución estratégico para el equipo de desarrollo, dividiendo los requerimientos del Trabajo Práctico Integrador (TPI) en fases lógicas y manejables. 

Este enfoque asegura que primero se construya una base sólida de datos antes de avanzar hacia lógicas de negocio complejas o seguridad.

## Fase 1: Capa de Persistencia y Modelado de Datos
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

## Fase 2: Arquitectura Base y Manejo de Errores
Antes de programar la lógica del negocio, se debe preparar la estructura para manejar los flujos excepcionales de manera limpia.

1. **Manejo Global de Excepciones:**
   - Implementar un `@ControllerAdvice` o `@RestControllerAdvice`.
   - Definir excepciones personalizadas de negocio (ej. `MontoInvalidoException`, `SubastaNoActivaException`, `VersionConflictException` para el Bloqueo Optimista).
   - Estandarizar la respuesta de error (código de estado, mensaje, timestamp).
2. **Configuración de Entorno y Base de Datos:**
   - Setup de **PostgreSQL** mediante variables de entorno para tener un entorno idéntico al de presentación/producción desde el día 1.

## Fase 3: Seguridad y Autenticación
Implementar el escudo del sistema antes de exponer los endpoints de negocio.

1. **Configuración de Spring Security:**
   - Cifrado de contraseñas con `BCryptPasswordEncoder`.
   - Implementación de `UserDetails` en la entidad `Usuario`.
2. **Implementación de JWT:**
   - Creación del proveedor de tokens (generación y validación).
   - Configuración de filtros (`JwtAuthenticationFilter`) para interceptar peticiones.
3. **Endpoints de Auth:**
   - `/auth/register` y `/auth/login`.
4. **Control de Acceso por Roles (RBAC):**
   - Proteger rutas según roles (`USER`, `SELLER`, `ADMIN`).

## Fase 4: Lógica de Negocio y Controladores 

1. **Gestión de Productos y Categorías:**
   - CRUD básico restringido a rol `SELLER`.
2. **Gestión de Subastas (SELLER y ADMIN):**
   - Creación (estado BORRADOR/PUBLICADA).
   - Reglas de cancelación (diferenciando permisos entre SELLER y ADMIN).
   - Transiciones de estado iniciales.
3. **Motor de Pujas (El núcleo transaccional):**
   - Endpoint estricto para recibir pujas.
   - Validación autónoma en backend (precio base o monto actual + incremento).
   - Verificación estricta de tiempo y estado.
   - Resolución de concurrencia mediante el rechazo limpio cuando falla la versión (Bloqueo Optimista).
4. **Privacidad de las Ofertas:**
   - Lógica en el backend para ocultar la identidad de los oferentes mientras la subasta está ACTIVA, revelándolos solo al final al SELLER y al ADMIN en todo momento.

## Fase 5: Tareas Programadas y Auditoría (Cierre Dinámico)
Implementar automatizaciones y trazabilidad.

1. **Cierre Automático y Temporizadores:**
   - Uso de `@Scheduled` en Spring Boot para barrer periódicamente las subastas.
   - Cambios de estado automáticos: PUBLICADA → ACTIVA; ACTIVA → ADJUDICADA / FINALIZADA.
   - Lógica del "Minuto de Ley": Extensión automática del tiempo de cierre ante pujas de último minuto.
2. **Auditoría e Historial:**
   - Registro automático en `historial_estados` ante cada cambio de fase.
3. **Notificaciones y Disputas:**
   - Generación de notificaciones internas en BD al ganador y vendedor.
   - Flujo de creación de disputas por el usuario y resolución por el ADMIN.
