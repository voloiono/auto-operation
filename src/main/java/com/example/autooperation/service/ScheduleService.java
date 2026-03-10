package com.example.autooperation.service;

import com.example.autooperation.dto.ScheduleDTO;
import com.example.autooperation.model.ScheduleTask;
import com.example.autooperation.repository.FlowRepository;
import com.example.autooperation.repository.ScheduleTaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScheduleService {
    private final ScheduleTaskRepository scheduleTaskRepository;
    private final FlowRepository flowRepository;

    public ScheduleDTO createSchedule(ScheduleDTO dto) {
        ScheduleTask task = new ScheduleTask();
        task.setFlow(flowRepository.findById(dto.getFlowId())
                .orElseThrow(() -> new RuntimeException("Flow not found")));
        task.setCronExpression(dto.getCronExpression());
        task.setEnabled(dto.getEnabled() != null ? dto.getEnabled() : false);
        task.setNotifyEmail(dto.getNotifyEmail());
        ScheduleTask saved = scheduleTaskRepository.save(task);
        return mapToDTO(saved);
    }

    public ScheduleDTO getSchedule(Long id) {
        return scheduleTaskRepository.findById(id)
                .map(this::mapToDTO)
                .orElseThrow(() -> new RuntimeException("Schedule not found"));
    }

    public List<ScheduleDTO> getSchedulesByFlow(Long flowId) {
        return scheduleTaskRepository.findByFlowId(flowId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<ScheduleDTO> getEnabledSchedules() {
        return scheduleTaskRepository.findByEnabled(true).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public ScheduleDTO updateSchedule(Long id, ScheduleDTO dto) {
        ScheduleTask task = scheduleTaskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Schedule not found"));
        task.setCronExpression(dto.getCronExpression());
        task.setEnabled(dto.getEnabled());
        task.setNotifyEmail(dto.getNotifyEmail());
        ScheduleTask updated = scheduleTaskRepository.save(task);
        return mapToDTO(updated);
    }

    public ScheduleDTO updateLastExecution(Long id) {
        ScheduleTask task = scheduleTaskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Schedule not found"));
        task.setLastExecution(LocalDateTime.now());
        ScheduleTask updated = scheduleTaskRepository.save(task);
        return mapToDTO(updated);
    }

    public void deleteSchedule(Long id) {
        scheduleTaskRepository.deleteById(id);
    }

    private ScheduleDTO mapToDTO(ScheduleTask task) {
        return new ScheduleDTO(
                task.getId(),
                task.getFlow().getId(),
                task.getCronExpression(),
                task.getEnabled(),
                task.getNotifyEmail(),
                task.getLastExecution(),
                task.getNextExecution(),
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }
}
