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
    private String pythonTemplate;
    private Boolean builtIn;
    private String icon;
    private String author;
    private String imports;
    private String version;
}
