package com.example.autooperation.controller;

import com.example.autooperation.dto.ExecutionLogDTO;
import com.example.autooperation.service.ExecutionLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/execution-logs")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ExecutionLogController {
    private final ExecutionLogService executionLogService;

    @GetMapping("/{id}")
    public ResponseEntity<ExecutionLogDTO> getLog(@PathVariable Long id) {
        return ResponseEntity.ok(executionLogService.getLog(id));
    }

    @GetMapping("/flow/{flowId}")
    public ResponseEntity<List<ExecutionLogDTO>> getLogsByFlow(@PathVariable Long flowId) {
        return ResponseEntity.ok(executionLogService.getLogsByFlow(flowId));
    }
}
