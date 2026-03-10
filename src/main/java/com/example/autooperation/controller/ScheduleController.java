package com.example.autooperation.controller;

import com.example.autooperation.dto.ScheduleDTO;
import com.example.autooperation.model.ScheduleTask;
import com.example.autooperation.repository.ScheduleTaskRepository;
import com.example.autooperation.service.ScheduleService;
import com.example.autooperation.service.ScheduleExecutorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ScheduleController {
    private final ScheduleService scheduleService;
    private final ScheduleExecutorService scheduleExecutorService;
    private final ScheduleTaskRepository scheduleTaskRepository;

    @PostMapping
    public ResponseEntity<ScheduleDTO> createSchedule(@RequestBody ScheduleDTO dto) throws Exception {
        ScheduleDTO created = scheduleService.createSchedule(dto);
        if (created.getEnabled()) {
            ScheduleTask task = scheduleTaskRepository.findById(created.getId()).orElseThrow();
            scheduleExecutorService.scheduleTask(task);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ScheduleDTO> getSchedule(@PathVariable Long id) {
        return ResponseEntity.ok(scheduleService.getSchedule(id));
    }

    @GetMapping("/flow/{flowId}")
    public ResponseEntity<List<ScheduleDTO>> getSchedulesByFlow(@PathVariable Long flowId) {
        return ResponseEntity.ok(scheduleService.getSchedulesByFlow(flowId));
    }

    @GetMapping("/enabled")
    public ResponseEntity<List<ScheduleDTO>> getEnabledSchedules() {
        return ResponseEntity.ok(scheduleService.getEnabledSchedules());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ScheduleDTO> updateSchedule(@PathVariable Long id, @RequestBody ScheduleDTO dto) throws Exception {
        ScheduleDTO updated = scheduleService.updateSchedule(id, dto);
        ScheduleTask task = scheduleTaskRepository.findById(id).orElseThrow();
        scheduleExecutorService.updateSchedule(task);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSchedule(@PathVariable Long id) throws Exception {
        scheduleExecutorService.unscheduleTask(id);
        scheduleService.deleteSchedule(id);
        return ResponseEntity.noContent().build();
    }
}
