package com.prog.tpi.sistema_subastas.exceptions;

import org.springframework.security.access.AccessDeniedException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.security.core.AuthenticationException;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.prog.tpi.sistema_subastas.dtos.response.ApiErrorDTO;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Excepciones de Validación
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorDTO handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        return ApiErrorDTO.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message("Error de validación en los datos enviados")
                .errors(errors)
                .timestamp(Instant.now())
                .build();
    }

    // Errores de JSON o URL
    @ExceptionHandler({ HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
            ConstraintViolationException.class })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorDTO handleBadRequest(Exception ex) {
        return ApiErrorDTO.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message("La solicitud está mal formada o contiene datos inválidos.")
                .timestamp(Instant.now())
                .build();
    }

    // Excepciones de Negocio
    @ExceptionHandler(ReglaNegocioException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorDTO handleReglaNegocioException(ReglaNegocioException ex) {
        return ApiErrorDTO.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message(ex.getMessage())
                .timestamp(Instant.now())
                .build();
    }

    // Recursos no Encontrados
    @ExceptionHandler({ EntityNotFoundException.class, NoSuchElementException.class })
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiErrorDTO handleNotFoundExceptions(Exception ex) {
        return ApiErrorDTO.builder()
                .status(HttpStatus.NOT_FOUND.value())
                .message(ex.getMessage())
                .timestamp(Instant.now())
                .build();
    }

    // Concurrencia de Pujas Simultáneas (Bloqueo Optimista)
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiErrorDTO handleOptimisticLocking(ObjectOptimisticLockingFailureException ex) {
        return ApiErrorDTO.builder()
                .status(HttpStatus.CONFLICT.value())
                .message(
                        "La subasta ha recibido otra oferta simultáneamente. Por favor, actualiza el monto actual y vuelve a intentarlo.")
                .timestamp(Instant.now())
                .build();
    }

    // Problemas de Integridad de Base de Datos
    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiErrorDTO handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        return ApiErrorDTO.builder()
                .status(HttpStatus.CONFLICT.value())
                .message(
                        "No se puede completar la operación debido a una restricción de integridad de la base de datos.")
                .timestamp(Instant.now())
                .build();
    }

    // Seguridad: Acceso Denegado o Credenciales Inváidas
    @ExceptionHandler({ AuthenticationException.class, AccessDeniedException.class })
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiErrorDTO handleSecurityExceptions(Exception ex) {
        return ApiErrorDTO.builder()
                .status(HttpStatus.UNAUTHORIZED.value())
                .message("Error de autenticación o no tienes permisos para realizar esta acción.")
                .timestamp(Instant.now())
                .build();
    }

    // Errores Generales del Servidor
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiErrorDTO handleGeneralExceptions(Exception ex) {
        return ApiErrorDTO.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message("Ocurrió un error inesperado en el servidor.")
                .timestamp(Instant.now())
                .build();
    }

}
