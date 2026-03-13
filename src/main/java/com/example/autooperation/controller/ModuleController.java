package com.example.autooperation.controller;

import com.example.autooperation.dto.ModuleDTO;
import com.example.autooperation.service.ModuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

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

    @PostMapping
    public ResponseEntity<ModuleDTO> createModule(@RequestBody ModuleDTO dto) {
        return ResponseEntity.ok(moduleService.createModule(dto));
    }

    @PutMapping("/{moduleId}")
    public ResponseEntity<ModuleDTO> updateModule(@PathVariable Long moduleId, @RequestBody ModuleDTO dto) {
        return ResponseEntity.ok(moduleService.updateModule(moduleId, dto));
    }

    @DeleteMapping("/{moduleId}")
    public ResponseEntity<Void> deleteModule(@PathVariable Long moduleId) {
        moduleService.deleteModule(moduleId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{moduleId}/test")
    public ResponseEntity<Map<String, String>> testTemplate(
            @PathVariable String moduleId,
            @RequestBody Map<String, String> request) throws Exception {
        String rendered = moduleService.testTemplate(moduleId, request.get("params"));
        return ResponseEntity.ok(Map.of("code", rendered));
    }
}
