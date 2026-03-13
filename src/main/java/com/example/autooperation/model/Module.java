package com.example.autooperation.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "modules")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Module {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String moduleId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String category;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String inputSchema;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String outputSchema;

    @Column(columnDefinition = "LONGTEXT", nullable = false)
    private String pythonTemplate;

    @Column(nullable = false)
    private Boolean builtIn = false;

    @Column(length = 10)
    private String icon;

    @Column(length = 100)
    private String author;

    @Column(columnDefinition = "TEXT")
    private String imports;

    @Column(length = 20)
    private String version = "1.0.0";
}
