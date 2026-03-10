package com.example.autooperation.controller;

import com.example.autooperation.dto.FlowDTO;
import com.example.autooperation.dto.ExecutionResultDTO;
import com.example.autooperation.dto.ExecutionLogDTO;
import com.example.autooperation.service.FlowService;
import com.example.autooperation.service.ScriptGeneratorService;
import com.example.autooperation.service.ScriptExecutorService;
import com.example.autooperation.service.ExecutionLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/flows")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class FlowController {
    private final FlowService flowService;
    private final ScriptGeneratorService scriptGeneratorService;
    private final ScriptExecutorService scriptExecutorService;
    private final ExecutionLogService executionLogService;

    @PostMapping
    public ResponseEntity<FlowDTO> createFlow(@RequestBody FlowDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(flowService.createFlow(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FlowDTO> getFlow(@PathVariable Long id) {
        return ResponseEntity.ok(flowService.getFlow(id));
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<FlowDTO>> getFlowsByProject(@PathVariable Long projectId) {
        return ResponseEntity.ok(flowService.getFlowsByProject(projectId));
    }

    /**
     * 获取项目的唯一流程，如果不存在则自动创建
     */
    @GetMapping("/project/{projectId}/main")
    public ResponseEntity<FlowDTO> getOrCreateProjectFlow(@PathVariable Long projectId) {
        return ResponseEntity.ok(flowService.getOrCreateProjectFlow(projectId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FlowDTO> updateFlow(@PathVariable Long id, @RequestBody FlowDTO dto) {
        return ResponseEntity.ok(flowService.updateFlow(id, dto));
    }

    @PostMapping("/{id}/generate")
    public ResponseEntity<FlowDTO> generateScript(@PathVariable Long id) {
        try {
            FlowDTO flow = flowService.getFlow(id);
            String script = scriptGeneratorService.generatePythonScript(flow.getConfiguration());
            return ResponseEntity.ok(flowService.updateGeneratedScript(id, script));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/{id}/execute")
    public ResponseEntity<ExecutionLogDTO> executeFlow(@PathVariable Long id) {
        try {
            FlowDTO flow = flowService.getFlow(id);
            // 重新生成脚本，确保使用最新的生成逻辑
            String script = scriptGeneratorService.generatePythonScript(flow.getConfiguration());
            flow = flowService.updateGeneratedScript(id, script);

            // 创建 running 状态的执行日志，立即返回
            ExecutionLogDTO logDTO = executionLogService.createRunningLog(id);

            // 后台线程异步执行脚本
            scriptExecutorService.executeScriptAsync(
                    flow.getGeneratedScript(), "flow_" + id, logDTO.getId(), executionLogService);

            return ResponseEntity.ok(logDTO);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFlow(@PathVariable Long id) {
        flowService.deleteFlow(id);
        return ResponseEntity.noContent().build();
    }
}
