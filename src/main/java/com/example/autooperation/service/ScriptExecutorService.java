package com.example.autooperation.service;

import com.example.autooperation.dto.ExecutionResultDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScriptExecutorService {
    @Value("${app.scripts.dir:./scripts}")
    private String scriptsDir;

    private final PythonEnvironmentService pythonEnv;

    /**
     * 异步执行脚本，完成后更新执行日志
     */
    @Async
    public void executeScriptAsync(String scriptContent, String scriptName, Long logId, ExecutionLogService logService) {
        ExecutionResultDTO result = executeScript(scriptContent, scriptName);
        try {
            logService.completeLog(logId,
                    result.getSuccess() ? "success" : "failed",
                    result.getOutput(),
                    result.getErrorMessage(),
                    result.getExecutionTimeMs());
        } catch (Exception e) {
            log.error("Failed to update execution log {}", logId, e);
        }
    }

    public ExecutionResultDTO executeScript(String scriptContent, String scriptName) {
        long startTime = System.currentTimeMillis();
        ExecutionResultDTO result = new ExecutionResultDTO();

        // 先检查 Python 是否真正可用
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
            Files.write(scriptFile.toPath(), scriptContent.getBytes());

            String pythonPath = pythonEnv.getResolvedPath();
            log.debug("Using Python executable: {}", pythonPath);

            ProcessBuilder pb = new ProcessBuilder(pythonPath, scriptFile.getAbsolutePath());
            pb.redirectErrorStream(true);
            pythonEnv.configurePythonPath(pb);

            Process process = pb.start();

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
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
}
