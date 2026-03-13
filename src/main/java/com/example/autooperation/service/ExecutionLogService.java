package com.example.autooperation.service;

import com.example.autooperation.dto.ExecutionLogDTO;
import com.example.autooperation.model.ExecutionLog;
import com.example.autooperation.model.Flow;
import com.example.autooperation.repository.ExecutionLogRepository;
import com.example.autooperation.repository.FlowRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExecutionLogService {
    private final ExecutionLogRepository executionLogRepository;
    private final FlowRepository flowRepository;

    /** 批量追加缓冲：每 10 行或 3 秒刷一次 DB */
    private final ConcurrentHashMap<Long, StringBuilder> pendingOutput = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, Integer> pendingLineCount = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, Long> lastFlushTime = new ConcurrentHashMap<>();

    private static final int FLUSH_LINE_THRESHOLD = 10;
    private static final long FLUSH_TIME_THRESHOLD_MS = 3000;

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
     * 增量追加输出行（带批量优化）
     */
    public void appendOutput(Long logId, String line) {
        pendingOutput.computeIfAbsent(logId, k -> new StringBuilder());
        pendingLineCount.merge(logId, 1, Integer::sum);
        lastFlushTime.putIfAbsent(logId, System.currentTimeMillis());

        StringBuilder sb = pendingOutput.get(logId);
        synchronized (sb) {
            sb.append(line).append("\n");
        }

        int lineCount = pendingLineCount.getOrDefault(logId, 0);
        long elapsed = System.currentTimeMillis() - lastFlushTime.getOrDefault(logId, System.currentTimeMillis());

        if (lineCount >= FLUSH_LINE_THRESHOLD || elapsed >= FLUSH_TIME_THRESHOLD_MS) {
            flushPendingOutput(logId);
        }
    }

    /**
     * 将缓冲的输出刷入数据库
     */
    public void flushPendingOutput(Long logId) {
        StringBuilder sb = pendingOutput.get(logId);
        if (sb == null) return;

        String chunk;
        synchronized (sb) {
            chunk = sb.toString();
            sb.setLength(0);
        }
        pendingLineCount.put(logId, 0);
        lastFlushTime.put(logId, System.currentTimeMillis());

        if (chunk.isEmpty()) return;

        try {
            executionLogRepository.findById(logId).ifPresent(logEntity -> {
                String existing = logEntity.getOutput();
                logEntity.setOutput(existing == null ? chunk : existing + chunk);
                executionLogRepository.save(logEntity);
            });
        } catch (Exception e) {
            log.error("Failed to flush output for log {}", logId, e);
        }
    }

    /**
     * 脚本执行完毕后更新日志
     */
    public void completeLog(Long logId, String status, String output, String errorMessage, Long executionTimeMs) {
        // 清理缓冲
        pendingOutput.remove(logId);
        pendingLineCount.remove(logId);
        lastFlushTime.remove(logId);

        ExecutionLog logEntity = executionLogRepository.findById(logId)
                .orElseThrow(() -> new RuntimeException("Log not found"));
        logEntity.setStatus(status);
        if (output != null) {
            logEntity.setOutput(output);
        }
        logEntity.setErrorMessage(errorMessage);
        logEntity.setExecutionTimeMs(executionTimeMs);
        logEntity.setCompletedAt(LocalDateTime.now());
        executionLogRepository.save(logEntity);
    }

    public ExecutionLogDTO createLog(Long flowId, String status, String output, String errorMessage, Long executionTimeMs) {
        Flow flow = flowRepository.findById(flowId)
                .orElseThrow(() -> new RuntimeException("Flow not found"));
        ExecutionLog logEntity = new ExecutionLog();
        logEntity.setFlow(flow);
        logEntity.setStatus(status);
        logEntity.setOutput(output);
        logEntity.setErrorMessage(errorMessage);
        logEntity.setExecutionTimeMs(executionTimeMs);
        logEntity.setCompletedAt(LocalDateTime.now());
        ExecutionLog saved = executionLogRepository.save(logEntity);
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

    private ExecutionLogDTO mapToDTO(ExecutionLog logEntity) {
        return new ExecutionLogDTO(
                logEntity.getId(),
                logEntity.getFlow().getId(),
                logEntity.getStatus(),
                logEntity.getOutput(),
                logEntity.getErrorMessage(),
                logEntity.getExecutionTimeMs(),
                logEntity.getStartedAt(),
                logEntity.getCompletedAt()
        );
    }
}
