package com.example.autooperation.service;

import com.example.autooperation.dto.ModuleDTO;
import com.example.autooperation.model.Module;
import com.example.autooperation.repository.ModuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ModuleService {
    private final ModuleRepository moduleRepository;

    public ModuleDTO createModule(ModuleDTO dto) {
        Module module = new Module();
        module.setModuleId(dto.getModuleId());
        module.setName(dto.getName());
        module.setCategory(dto.getCategory());
        module.setDescription(dto.getDescription());
        module.setInputSchema(dto.getInputSchema());
        module.setOutputSchema(dto.getOutputSchema());
        Module saved = moduleRepository.save(module);
        return mapToDTO(saved);
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

    private ModuleDTO mapToDTO(Module module) {
        return new ModuleDTO(
                module.getId(),
                module.getModuleId(),
                module.getName(),
                module.getCategory(),
                module.getDescription(),
                module.getInputSchema(),
                module.getOutputSchema()
        );
    }
}
