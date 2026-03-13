package com.example.autooperation.service;

import com.example.autooperation.dto.ExecutionResultDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScriptExecutorService {
    @Value("${app.scripts.dir:./scripts}")
    private String scriptsDir;

    @Value("${app.headless-mode:false}")
    private boolean headlessMode;

    private final PythonEnvironmentService pythonEnv;

    /** 运行中的进程，key = executionLogId */
    private final ConcurrentHashMap<Long, Process> activeProcesses = new ConcurrentHashMap<>();

    /** SSE 连接，key = executionLogId */
    private final ConcurrentHashMap<Long, List<SseEmitter>> activeEmitters = new ConcurrentHashMap<>();

    /** 已输出的日志行缓存（供迟到客户端补全） */
    private final ConcurrentHashMap<Long, List<String>> outputCache = new ConcurrentHashMap<>();

    /**
     * 注册 SSE 连接
     */
    public void registerEmitter(Long logId, SseEmitter emitter) {
        activeEmitters.computeIfAbsent(logId, k -> new CopyOnWriteArrayList<>()).add(emitter);
        emitter.onCompletion(() -> removeEmitter(logId, emitter));
        emitter.onTimeout(() -> removeEmitter(logId, emitter));
        emitter.onError(e -> removeEmitter(logId, emitter));
    }

    /**
     * 获取已输出的缓存行（供迟到客户端补全历史）
     */
    public List<String> getCachedOutput(Long logId) {
        return outputCache.getOrDefault(logId, List.of());
    }

    /**
     * 检查进程是否仍在运行
     */
    public boolean isRunning(Long logId) {
        Process p = activeProcesses.get(logId);
        return p != null && p.isAlive();
    }

    /**
     * 停止执行：终止进程树
     */
    public void stopExecution(Long logId, ExecutionLogService logService) {
        Process process = activeProcesses.remove(logId);
        if (process != null && process.isAlive()) {
            // 终止整个进程树
            process.descendants().forEach(ph -> {
                try { ph.destroyForcibly(); } catch (Exception ignored) {}
            });
            process.destroyForcibly();
            log.info("Execution {} stopped by user", logId);
        }
        // 更新状态为 cancelled
        try {
            logService.completeLog(logId, "cancelled", null, "用户手动停止", null);
        } catch (Exception e) {
            log.error("Failed to update cancelled status for log {}", logId, e);
        }
        // 发送 complete 事件并清理
        broadcastEvent(logId, "complete", "cancelled");
        cleanup(logId);
    }

    /**
     * 异步执行脚本，逐行推送输出
     */
    @Async
    public void executeScriptAsync(String scriptContent, String scriptName, Long logId, ExecutionLogService logService) {
        long startTime = System.currentTimeMillis();
        outputCache.put(logId, new CopyOnWriteArrayList<>());

        if (!pythonEnv.isPythonAvailable()) {
            String errMsg = "Python 环境不可用。请先运行 setup-python.bat 安装嵌入式 Python。";
            broadcastLine(logId, "[ERROR] " + errMsg);
            broadcastEvent(logId, "complete", "failed");
            logService.completeLog(logId, "failed", errMsg, errMsg, 0L);
            cleanup(logId);
            return;
        }

        try {
            File scriptFile = new File(scriptsDir, scriptName + ".py");
            Files.createDirectories(Paths.get(scriptsDir));
            Files.write(scriptFile.toPath(), scriptContent.getBytes(StandardCharsets.UTF_8));

            String pythonPath = pythonEnv.getResolvedPath();
            log.debug("Using Python executable: {}", pythonPath);

            ProcessBuilder pb = new ProcessBuilder(pythonPath, scriptFile.getAbsolutePath());
            pb.redirectErrorStream(true);
            pythonEnv.configurePythonPath(pb);

            // 关键：禁用 Python stdout 缓冲
            pb.environment().put("PYTHONUNBUFFERED", "1");
            // 强制 Python 使用 UTF-8 编码输出
            pb.environment().put("PYTHONIOENCODING", "utf-8");

            // 无头模式检测：无 GUI 环境自动注入
            if (isHeadlessEnvironment()) {
                pb.environment().put("HEADLESS_MODE", "true");
                log.info("Headless environment detected, setting HEADLESS_MODE=true");
            }

            Process process = pb.start();
            activeProcesses.put(logId, process);

            StringBuilder fullOutput = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    // 进程可能已被 stopExecution 终止
                    if (!activeProcesses.containsKey(logId)) {
                        break;
                    }
                    fullOutput.append(line).append("\n");
                    broadcastLine(logId, line);
                    logService.appendOutput(logId, line);
                }
            }

            int exitCode = process.waitFor();
            long executionTime = System.currentTimeMillis() - startTime;

            // 进程可能已被 stopExecution 处理
            if (!activeProcesses.containsKey(logId)) {
                scriptFile.delete();
                return;
            }

            activeProcesses.remove(logId);
            String status = exitCode == 0 ? "success" : "failed";
            String errorMsg = exitCode != 0 ? "Script failed with exit code: " + exitCode : null;

            logService.flushPendingOutput(logId);
            logService.completeLog(logId, status, fullOutput.toString(), errorMsg, executionTime);

            broadcastEvent(logId, "complete", status);
            cleanup(logId);

            scriptFile.delete();
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            activeProcesses.remove(logId);
            String errMsg = e.getMessage();
            broadcastLine(logId, "[ERROR] " + errMsg);
            broadcastEvent(logId, "complete", "failed");
            logService.flushPendingOutput(logId);
            logService.completeLog(logId, "failed", null, errMsg, executionTime);
            cleanup(logId);
            log.error("Error executing script", e);
        }
    }

    public ExecutionResultDTO executeScript(String scriptContent, String scriptName) {
        long startTime = System.currentTimeMillis();
        ExecutionResultDTO result = new ExecutionResultDTO();

        if (!pythonEnv.isPythonAvailable()) {
            result.setSuccess(false);
            result.setErrorMessage("Python 环境不可用。请先运行 setup-python.bat 安装嵌入式 Python。");
            result.setMessage("Python not available");
            result.setExecutionTimeMs(0L);
            return result;
        }

        try {
            File scriptFile = new File(scriptsDir, scriptName + ".py");
            Files.createDirectories(Paths.get(scriptsDir));
            Files.write(scriptFile.toPath(), scriptContent.getBytes(StandardCharsets.UTF_8));

            String pythonPath = pythonEnv.getResolvedPath();
            log.debug("Using Python executable: {}", pythonPath);

            ProcessBuilder pb = new ProcessBuilder(pythonPath, scriptFile.getAbsolutePath());
            pb.redirectErrorStream(true);
            pythonEnv.configurePythonPath(pb);
            pb.environment().put("PYTHONUNBUFFERED", "1");
            pb.environment().put("PYTHONIOENCODING", "utf-8");

            if (isHeadlessEnvironment()) {
                pb.environment().put("HEADLESS_MODE", "true");
            }

            Process process = pb.start();

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            int exitCode = process.waitFor();
            long executionTime = System.currentTimeMillis() - startTime;

            result.setSuccess(exitCode == 0);
            result.setOutput(output.toString());
            result.setExecutionTimeMs(executionTime);

            if (exitCode == 0) {
                result.setMessage("Script executed successfully");
            } else {
                result.setErrorMessage("Script failed with exit code: " + exitCode);
                result.setMessage("Script execution failed");
            }

            scriptFile.delete();
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            result.setSuccess(false);
            result.setErrorMessage(e.getMessage());
            result.setMessage("Error executing script");
            result.setExecutionTimeMs(executionTime);
            log.error("Error executing script", e);
        }

        return result;
    }

    // ========== 内部方法 ==========

    private void broadcastLine(Long logId, String line) {
        // 缓存
        List<String> cache = outputCache.get(logId);
        if (cache != null) {
            cache.add(line);
        }
        // 推送给所有 SSE 客户端
        List<SseEmitter> emitters = activeEmitters.get(logId);
        if (emitters == null || emitters.isEmpty()) return;
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name("output").data(line));
            } catch (Exception e) {
                removeEmitter(logId, emitter);
            }
        }
    }

    private void broadcastEvent(Long logId, String eventName, String data) {
        List<SseEmitter> emitters = activeEmitters.get(logId);
        if (emitters == null || emitters.isEmpty()) return;
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name(eventName).data(data));
                if ("complete".equals(eventName)) {
                    emitter.complete();
                }
            } catch (Exception e) {
                removeEmitter(logId, emitter);
            }
        }
    }

    private void removeEmitter(Long logId, SseEmitter emitter) {
        List<SseEmitter> emitters = activeEmitters.get(logId);
        if (emitters != null) {
            emitters.remove(emitter);
        }
    }

    private void cleanup(Long logId) {
        activeEmitters.remove(logId);
        // 缓存保留 5 分钟后清理（供迟到客户端查询）
        new Thread(() -> {
            try {
                Thread.sleep(5 * 60 * 1000);
            } catch (InterruptedException ignored) {}
            outputCache.remove(logId);
        }).start();
    }

    /**
     * 检测是否应以无头模式运行。
     * 优先级：配置文件 app.headless-mode > 环境变量 HEADLESS_MODE > 自动检测
     */
    private boolean isHeadlessEnvironment() {
        // 1. 配置文件强制开启
        if (headlessMode) {
            return true;
        }
        // 2. 环境变量显式指定
        String envHeadless = System.getenv("HEADLESS_MODE");
        if (envHeadless != null && (envHeadless.equals("1") || envHeadless.equalsIgnoreCase("true"))) {
            return true;
        }
        // 3. Linux 无 DISPLAY 自动检测
        String os = System.getProperty("os.name", "").toLowerCase();
        if (os.contains("linux")) {
            String display = System.getenv("DISPLAY");
            return display == null || display.isEmpty();
        }
        return false;
    }
}
