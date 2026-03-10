package com.example.autooperation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FlowDTO {
    private Long id;
    private Long projectId;
    private String name;
    private String configuration;
    private String generatedScript;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
