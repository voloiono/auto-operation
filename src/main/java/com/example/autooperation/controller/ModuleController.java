package com.example.autooperation.controller;

import com.example.autooperation.dto.ModuleDTO;
import com.example.autooperation.service.ModuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/modules")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ModuleController {
    private final ModuleService moduleService;

    @GetMapping
    public ResponseEntity<List<ModuleDTO>> getAllModules() {
        return ResponseEntity.ok(moduleService.getAllModules());
    }

    @GetMapping("/{moduleId}")
    public ResponseEntity<ModuleDTO> getModule(@PathVariable String moduleId) {
        return ResponseEntity.ok(moduleService.getModule(moduleId));
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<ModuleDTO>> getModulesByCategory(@PathVariable String category) {
        return ResponseEntity.ok(moduleService.getModulesByCategory(category));
    }
}
