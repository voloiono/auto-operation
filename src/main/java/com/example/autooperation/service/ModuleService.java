package com.example.autooperation.service;

import com.example.autooperation.dto.ModuleDTO;
import com.example.autooperation.model.Module;
import com.example.autooperation.repository.ModuleRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ModuleService {
    private final ModuleRepository moduleRepository;
    private final ScriptGeneratorService scriptGeneratorService;
    private final ObjectMapper objectMapper;

    public ModuleDTO createModule(ModuleDTO dto) {
        Module module = new Module();
        module.setModuleId(dto.getModuleId());
        module.setName(dto.getName());
        module.setCategory(dto.getCategory());
        module.setDescription(dto.getDescription());
        module.setInputSchema(dto.getInputSchema());
        module.setOutputSchema(dto.getOutputSchema() != null ? dto.getOutputSchema() : "{}");
        module.setPythonTemplate(dto.getPythonTemplate() != null ? dto.getPythonTemplate() : "");
        module.setBuiltIn(false);
        module.setIcon(dto.getIcon());
        module.setAuthor(dto.getAuthor());
        module.setImports(dto.getImports());
        module.setVersion(dto.getVersion() != null ? dto.getVersion() : "1.0.0");
        Module saved = moduleRepository.save(module);
        return mapToDTO(saved);
    }

    public ModuleDTO updateModule(Long moduleId, ModuleDTO dto) {
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new RuntimeException("Module not found"));
        if (Boolean.TRUE.equals(module.getBuiltIn())) {
            throw new RuntimeException("Cannot modify built-in module");
        }
        if (dto.getName() != null) module.setName(dto.getName());
        if (dto.getCategory() != null) module.setCategory(dto.getCategory());
        if (dto.getDescription() != null) module.setDescription(dto.getDescription());
        if (dto.getInputSchema() != null) module.setInputSchema(dto.getInputSchema());
        if (dto.getOutputSchema() != null) module.setOutputSchema(dto.getOutputSchema());
        if (dto.getPythonTemplate() != null) module.setPythonTemplate(dto.getPythonTemplate());
        if (dto.getIcon() != null) module.setIcon(dto.getIcon());
        if (dto.getAuthor() != null) module.setAuthor(dto.getAuthor());
        if (dto.getImports() != null) module.setImports(dto.getImports());
        if (dto.getVersion() != null) module.setVersion(dto.getVersion());
        Module saved = moduleRepository.save(module);
        return mapToDTO(saved);
    }

    public void deleteModule(Long moduleId) {
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new RuntimeException("Module not found"));
        if (Boolean.TRUE.equals(module.getBuiltIn())) {
            throw new RuntimeException("Cannot delete built-in module");
        }
        moduleRepository.delete(module);
    }

    public ModuleDTO getModule(String moduleId) {
        return moduleRepository.findByModuleId(moduleId)
                .map(this::mapToDTO)
                .orElseThrow(() -> new RuntimeException("Module not found"));
    }

    public List<ModuleDTO> getAllModules() {
        return moduleRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<ModuleDTO> getModulesByCategory(String category) {
        return moduleRepository.findAll().stream()
                .filter(m -> m.getCategory().equals(category))
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Test template rendering with sample parameters
     */
    public String testTemplate(String moduleId, String testParams) throws Exception {
        Module module = moduleRepository.findByModuleId(moduleId)
                .orElseThrow(() -> new RuntimeException("Module not found"));
        JsonNode params = objectMapper.readTree(testParams);
        return scriptGeneratorService.renderTemplate(module.getPythonTemplate(), params);
    }

    private ModuleDTO mapToDTO(Module module) {
        return new ModuleDTO(
                module.getId(),
                module.getModuleId(),
                module.getName(),
                module.getCategory(),
                module.getDescription(),
                module.getInputSchema(),
                module.getOutputSchema(),
                module.getPythonTemplate(),
                module.getBuiltIn(),
                module.getIcon(),
                module.getAuthor(),
                module.getImports(),
                module.getVersion()
        );
    }
}
