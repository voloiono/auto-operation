package com.example.autooperation.service;

import com.example.autooperation.dto.ExecutionLogDTO;
import com.example.autooperation.model.ExecutionLog;
import com.example.autooperation.model.Flow;
import com.example.autooperation.repository.ExecutionLogRepository;
import com.example.autooperation.repository.FlowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExecutionLogService {
    private final ExecutionLogRepository executionLogRepository;
    private final FlowRepository flowRepository;

    /**
     * 创建一条 "running" 状态的执行日志（异步执行前调用）
     */
    public ExecutionLogDTO createRunningLog(Long flowId) {
        Flow flow = flowRepository.findById(flowId)
                .orElseThrow(() -> new RuntimeException("Flow not found"));
        ExecutionLog log = new ExecutionLog();
        log.setFlow(flow);
        log.setStatus("running");
        ExecutionLog saved = executionLogRepository.save(log);
        return mapToDTO(saved);
    }

    /**
     * 脚本执行完毕后更新日志
     */
    public void completeLog(Long logId, String status, String output, String errorMessage, Long executionTimeMs) {
        ExecutionLog log = executionLogRepository.findById(logId)
                .orElseThrow(() -> new RuntimeException("Log not found"));
        log.setStatus(status);
        log.setOutput(output);
        log.setErrorMessage(errorMessage);
        log.setExecutionTimeMs(executionTimeMs);
        log.setCompletedAt(LocalDateTime.now());
        executionLogRepository.save(log);
    }

    public ExecutionLogDTO createLog(Long flowId, String status, String output, String errorMessage, Long executionTimeMs) {
        Flow flow = flowRepository.findById(flowId)
                .orElseThrow(() -> new RuntimeException("Flow not found"));
        ExecutionLog log = new ExecutionLog();
        log.setFlow(flow);
        log.setStatus(status);
        log.setOutput(output);
        log.setErrorMessage(errorMessage);
        log.setExecutionTimeMs(executionTimeMs);
        log.setCompletedAt(LocalDateTime.now());
        ExecutionLog saved = executionLogRepository.save(log);
        return mapToDTO(saved);
    }

    public List<ExecutionLogDTO> getLogsByFlow(Long flowId) {
        return executionLogRepository.findByFlowIdOrderByStartedAtDesc(flowId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public ExecutionLogDTO getLog(Long id) {
        return executionLogRepository.findById(id)
                .map(this::mapToDTO)
                .orElseThrow(() -> new RuntimeException("Log not found"));
    }

    private ExecutionLogDTO mapToDTO(ExecutionLog log) {
        return new ExecutionLogDTO(
                log.getId(),
                log.getFlow().getId(),
                log.getStatus(),
                log.getOutput(),
                log.getErrorMessage(),
                log.getExecutionTimeMs(),
                log.getStartedAt(),
                log.getCompletedAt()
        );
    }
}
