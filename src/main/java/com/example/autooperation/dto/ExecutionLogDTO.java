package com.example.autooperation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExecutionLogDTO {
    private Long id;
    private Long flowId;
    private String status;
    private String output;
    private String errorMessage;
    private Long executionTimeMs;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
}
