package com.example.autooperation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModuleDTO {
    private Long id;
    private String moduleId;
    private String name;
    private String category;
    private String description;
    private String inputSchema;
    private String outputSchema;
}
