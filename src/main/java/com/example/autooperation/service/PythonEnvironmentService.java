package com.example.autooperation.service;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@Service
@Slf4j
public class PythonEnvironmentService {

    @Value("${app.python.executable:python}")
    private String pythonExecutable;

    @Value("${app.python.auto-setup:false}")
    private boolean autoSetup;

    @Getter
    private boolean pythonAvailable;

    @Getter
    private String pythonVersion;

    @Getter
    private String resolvedPath;

    @PostConstruct
    public void init() {
        resolvedPath = resolveConfiguredPath();
        pythonAvailable = checkPython(resolvedPath);

        if (!pythonAvailable && autoSetup) {
            log.info("Python not found, attempting auto-setup...");
            runSetupScript();
            resolvedPath = resolveConfiguredPath();
            pythonAvailable = checkPython(resolvedPath);
        }

        if (pythonAvailable) {
            log.info("Python environment OK: {} ({})", pythonVersion, resolvedPath);
        } else {
            log.warn("Python environment not available. Run setup-python.bat to configure.");
        }
    }

    /**
     * 将配置的路径解析为绝对路径（相对路径基于 user.dir）。
     * 如果文件不存在，返回原始配置值。
     */
    private String resolveConfiguredPath() {
        Path path = Paths.get(pythonExecutable).normalize();
        if (!path.isAbsolute()) {
            path = Paths.get(System.getProperty("user.dir")).resolve(path).normalize();
        }
        if (Files.exists(path)) {
            return path.toAbsolutePath().toString();
        }
        return pythonExecutable;
    }

    /**
     * 验证 python 可执行文件是否真正可用。
     * 运行 python --version，检查输出必须包含 "Python" 字样，
     * 以排除 Windows Store 的占位 stub（退出码 0 但无输出）。
     */
    private boolean checkPython(String pythonPath) {
        try {
            ProcessBuilder pb = new ProcessBuilder(pythonPath, "--version");
            pb.redirectErrorStream(true);
            Process process = pb.start();

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line);
                }
            }

            int exitCode = process.waitFor();
            String versionOutput = output.toString().trim();

            // Windows Store stub 会退出码 0 但无 "Python" 输出
            if (exitCode == 0 && versionOutput.startsWith("Python")) {
                pythonVersion = versionOutput;
                return true;
            }

            log.debug("Python check failed: exitCode={}, output='{}'", exitCode, versionOutput);
        } catch (Exception e) {
            log.debug("Python check failed: {}", e.getMessage());
        }
        pythonVersion = null;
        return false;
    }

    /**
     * 将嵌入式 Python 目录及 Scripts 子目录加入 ProcessBuilder 的 PATH。
     */
    public void configurePythonPath(ProcessBuilder pb) {
        if (!pythonAvailable) {
            return;
        }
        Path pythonDir = Paths.get(resolvedPath).getParent();
        if (pythonDir != null && Files.isDirectory(pythonDir)) {
            Map<String, String> env = pb.environment();
            String currentPath = env.getOrDefault("PATH", env.getOrDefault("Path", ""));
            String scriptsPath = pythonDir.resolve("Scripts").toAbsolutePath().toString();
            env.put("PATH", pythonDir.toAbsolutePath() + File.pathSeparator
                    + scriptsPath + File.pathSeparator + currentPath);
        }
    }

    private void runSetupScript() {
        try {
            Path setupScript = Paths.get(System.getProperty("user.dir"), "setup-python.bat");
            if (!Files.exists(setupScript)) {
                log.warn("setup-python.bat not found at {}", setupScript);
                return;
            }

            log.info("Running setup-python.bat...");
            ProcessBuilder pb = new ProcessBuilder("cmd", "/c", setupScript.toAbsolutePath().toString());
            pb.directory(Paths.get(System.getProperty("user.dir")).toFile());
            pb.redirectErrorStream(true);
            pb.inheritIO();

            Process process = pb.start();
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                log.info("setup-python.bat completed successfully");
            } else {
                log.error("setup-python.bat failed with exit code: {}", exitCode);
            }
        } catch (Exception e) {
            log.error("Failed to run setup-python.bat", e);
        }
    }
}
