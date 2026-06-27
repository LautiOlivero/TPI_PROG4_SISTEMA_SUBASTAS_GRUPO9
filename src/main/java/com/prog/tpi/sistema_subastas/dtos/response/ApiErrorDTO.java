package com.prog.tpi.sistema_subastas.dtos.response;

import java.time.Instant;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiErrorDTO {
    private Integer status;
    private String message;
    private Map<String, String> errors;
    private Instant timestamp;
}
