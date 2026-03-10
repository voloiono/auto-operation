package com.example.autooperation.service;

import com.example.autooperation.dto.FlowDTO;
import com.example.autooperation.model.Flow;
import com.example.autooperation.model.Project;
import com.example.autooperation.repository.FlowRepository;
import com.example.autooperation.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FlowService {
    private final FlowRepository flowRepository;
    private final ProjectRepository projectRepository;

    /**
     * 获取或创建项目的唯一流程
     * 一个项目只有一个流程，如果不存在则创建
     */
    @Transactional
    public FlowDTO getOrCreateProjectFlow(Long projectId) {
        // 先尝试获取已有流程
        return flowRepository.findFirstByProjectIdOrderByIdAsc(projectId)
                .map(this::mapToDTO)
                .orElseGet(() -> {
                    // 不存在则创建新流程
                    Project project = projectRepository.findById(projectId)
                            .orElseThrow(() -> new RuntimeException("Project not found: " + projectId));
                    Flow flow = new Flow();
                    flow.setProject(project);
                    flow.setName("默认流程");
                    flow.setConfiguration("{\"modules\":[],\"connections\":[]}");
                    flow.setStatus("draft");
                    Flow saved = flowRepository.save(flow);
                    return mapToDTO(saved);
                });
    }

    public FlowDTO createFlow(FlowDTO dto) {
        // 检查项目是否已有流程
        if (flowRepository.existsByProjectId(dto.getProjectId())) {
            // 已有流程，返回现有的
            return getOrCreateProjectFlow(dto.getProjectId());
        }

        Project project = projectRepository.findById(dto.getProjectId())
                .orElseThrow(() -> new RuntimeException("Project not found"));
        Flow flow = new Flow();
        flow.setProject(project);
        flow.setName(dto.getName());
        flow.setConfiguration(dto.getConfiguration());
        flow.setStatus("draft");
        Flow saved = flowRepository.save(flow);
        return mapToDTO(saved);
    }

    public FlowDTO getFlow(Long id) {
        return flowRepository.findById(id)
                .map(this::mapToDTO)
                .orElseThrow(() -> new RuntimeException("Flow not found"));
    }

    public List<FlowDTO> getFlowsByProject(Long projectId) {
        return flowRepository.findByProjectId(projectId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public FlowDTO updateFlow(Long id, FlowDTO dto) {
        Flow flow = flowRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Flow not found"));
        if (dto.getName() != null) {
            flow.setName(dto.getName());
        }
        if (dto.getConfiguration() != null) {
            flow.setConfiguration(dto.getConfiguration());
        }
        if (dto.getStatus() != null) {
            flow.setStatus(dto.getStatus());
        }
        Flow updated = flowRepository.save(flow);
        return mapToDTO(updated);
    }

    public FlowDTO updateGeneratedScript(Long id, String script) {
        Flow flow = flowRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Flow not found"));
        flow.setGeneratedScript(script);
        Flow updated = flowRepository.save(flow);
        return mapToDTO(updated);
    }

    public void deleteFlow(Long id) {
        flowRepository.deleteById(id);
    }

    private FlowDTO mapToDTO(Flow flow) {
        return new FlowDTO(
                flow.getId(),
                flow.getProject().getId(),
                flow.getName(),
                flow.getConfiguration(),
                flow.getGeneratedScript(),
                flow.getStatus(),
                flow.getCreatedAt(),
                flow.getUpdatedAt()
        );
    }
}
