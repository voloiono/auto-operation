package com.example.autooperation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExecutionResultDTO {
    private Boolean success;
    private String message;
    private String output;
    private String errorMessage;
    private Long executionTimeMs;
}
