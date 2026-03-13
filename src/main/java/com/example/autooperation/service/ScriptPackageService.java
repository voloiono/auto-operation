package com.example.autooperation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScriptPackageService {
    @Value("${app.scripts.dir:./scripts}")
    private String scriptsDir;

    private final PythonEnvironmentService pythonEnv;

    // 驱动缓存目录
    private Path getDriverCacheDir() {
        return Path.of(System.getProperty("user.dir"), "python", "drivers");
    }

    public byte[] packageScript(String scriptContent, String platform, String name,
                                boolean offlineMode, Map<String, String> browserVersions,
                                boolean runHidden) throws Exception {
        if (!pythonEnv.isPythonAvailable()) {
            throw new RuntimeException("Python 环境不可用。请先运行 setup-python.bat 安装嵌入式 Python。");
        }

        Path tempDir = Files.createTempDirectory("pyinstaller_");
        Path scriptFile = tempDir.resolve(name + ".py");

        try {
            Files.write(scriptFile, scriptContent.getBytes(StandardCharsets.UTF_8));

            String pythonPath = pythonEnv.getResolvedPath();
            log.info("Using Python executable for packaging: {}", pythonPath);
            log.info("Packaging mode: {}", offlineMode ? "OFFLINE (bundled drivers)" : "ONLINE (auto download)");

            // 第一步：安装依赖
            installDependencies(pythonPath, tempDir);

            // 第二步：准备驱动目录（仅离线模式）
            Path driversDir = null;
            if (offlineMode) {
                driversDir = tempDir.resolve("drivers");
                prepareDriversForVersions(driversDir, browserVersions);
            }

            // 第三步：PyInstaller 打包
            runPyInstaller(pythonPath, tempDir, scriptFile, driversDir, name, offlineMode, runHidden);

            Path distDir = tempDir.resolve("dist");
            String exeName = "windows".equals(platform) ? name + ".exe" : name;
            Path executableFile = distDir.resolve(exeName);

            if (!Files.exists(executableFile)) {
                throw new RuntimeException("打包文件不存在");
            }

            byte[] exeContent = Files.readAllBytes(executableFile);

            deleteDirectory(tempDir.toFile());
            return exeContent;
        } catch (Exception e) {
            deleteDirectory(tempDir.toFile());
            throw e;
        }
    }

    /**
     * 准备指定版本的驱动（优先使用缓存，没有则自动下载，失败则使用通用驱动）
     */
    private void prepareDriversForVersions(Path targetDriversDir, Map<String, String> browserVersions) throws Exception {
        Files.createDirectories(targetDriversDir);
        Path cacheDir = getDriverCacheDir();
        Files.createDirectories(cacheDir);

        if (browserVersions == null || browserVersions.isEmpty()) {
            log.warn("No browser versions specified for offline mode");
            return;
        }

        for (Map.Entry<String, String> entry : browserVersions.entrySet()) {
            String browserType = entry.getKey();
            String version = entry.getValue();

            String driverPrefix = getDriverPrefix(browserType);
            if (driverPrefix == null) {
                log.warn("Unknown browser type: {}", browserType);
                continue;
            }

            // IE 驱动特殊处理：版本号是 Selenium 版本而非浏览器版本，固定使用 4.14.0
            if ("ie".equals(browserType) && (version == null || version.isBlank())) {
                version = "4.14.0";
                log.info("IE driver: using Selenium version {}", version);
            }

            // Firefox 驱动特殊处理：geckodriver 版本与浏览器版本无关，固定使用 0.34.0
            if ("firefox".equals(browserType) && (version == null || version.isBlank())) {
                version = "0.34.0";
                log.info("Firefox driver: using geckodriver version {}", version);
            }

            String versionedName = driverPrefix + "_" + version + ".exe";
            String defaultName = driverPrefix + ".exe";
            Path cachedDriver = cacheDir.resolve(versionedName);
            Path genericDriver = cacheDir.resolve(defaultName);

            // 1. 检查缓存中是否有该版本驱动
            if (Files.exists(cachedDriver)) {
                log.info("Using cached driver: {}", versionedName);
                Files.copy(cachedDriver, targetDriversDir.resolve(defaultName), StandardCopyOption.REPLACE_EXISTING);
                continue;
            }

            // 2. 缓存中没有版本驱动，尝试下载
            log.info("Driver {} not found in cache, attempting to download...", versionedName);
            boolean downloadSuccess = false;
            try {
                downloadDriver(browserType, version, cachedDriver);
                log.info("Downloaded driver: {}", versionedName);
                Files.copy(cachedDriver, targetDriversDir.resolve(defaultName), StandardCopyOption.REPLACE_EXISTING);
                downloadSuccess = true;
            } catch (Exception e) {
                log.warn("Failed to download {} version {}: {}", browserType, version, e.getMessage());
            }

            // 3. 下载失败，检查是否有通用驱动
            if (!downloadSuccess) {
                if (Files.exists(genericDriver)) {
                    log.info("Using generic driver as fallback: {}", defaultName);
                    Files.copy(genericDriver, targetDriversDir.resolve(defaultName), StandardCopyOption.REPLACE_EXISTING);
                } else {
                    String errorMsg = String.format(
                            "Driver not available for %s version %s. " +
                            "Please manually place the driver at: %s\\%s or %s\\%s",
                            browserType, version,
                            cacheDir, versionedName,
                            cacheDir, defaultName
                    );
                    log.error(errorMsg);
                    throw new RuntimeException(errorMsg);
                }
            }
        }

        log.info("All drivers prepared successfully");
    }

    /**
     * 下载指定版本的浏览器驱动（直接从官方源下载）
     */
    private void downloadDriver(String browserType, String version, Path targetPath) throws Exception {
        log.info("Downloading {} driver version {}", browserType, version);

        String downloadUrl = getDriverDownloadUrl(browserType, version);
        if (downloadUrl == null) {
            throw new RuntimeException("无法获取 " + browserType + " 版本 " + version + " 的下载地址");
        }

        log.info("Download URL: {}", downloadUrl);

        Path tempZip = Files.createTempFile("driver_", ".zip");
        try {
            downloadFile(downloadUrl, tempZip);

            String driverFileName = getDriverPrefix(browserType) + ".exe";
            extractDriverFromZip(tempZip, targetPath, driverFileName);
            log.info("Driver extracted successfully: {}", targetPath);

        } finally {
            Files.deleteIfExists(tempZip);
        }
    }

    /**
     * 获取驱动下载 URL
     */
    private String getDriverDownloadUrl(String browserType, String version) throws Exception {
        return switch (browserType) {
            case "edge" -> getEdgeDriverUrl(version);
            case "chrome" -> getChromeDriverUrl(version);
            case "firefox" -> getFirefoxDriverUrl(version);
            case "ie" -> getIeDriverUrl(version);
            default -> null;
        };
    }

    /**
     * Edge 驱动下载 URL
     * 需要将主版本号（如143）转换为完整版本号（如143.0.3650.80）
     */
    private String getEdgeDriverUrl(String version) throws Exception {
        String fullVersion = resolveEdgeVersion(version);
        return "https://msedgedriver.microsoft.com/" + fullVersion + "/edgedriver_win64.zip";
    }

    /**
     * 解析 Edge 完整版本号
     * 从 https://msedgedriver.azureedge.net/ 查询可用版本
     */
    private String resolveEdgeVersion(String majorVersion) throws Exception {
        log.info("Resolving Edge version for major version: {}", majorVersion);

        // 尝试常见的版本格式
        String[] suffixes = {".0.3650.80", ".0.2849.0", ".0.2739.0", ".0.3800.82", ".0.0.0"};

        for (String suffix : suffixes) {
            String testVersion = majorVersion + suffix;
            String testUrl = "https://msedgedriver.microsoft.com/" + testVersion + "/edgedriver_win64.zip";
            try {
                if (urlExists(testUrl)) {
                    log.info("Found Edge version: {}", testVersion);
                    return testVersion;
                }
            } catch (Exception e) {
                log.debug("Version {} not found", testVersion);
            }
        }

        // 如果都不存在，返回默认格式
        String defaultVersion = majorVersion + ".0.0.0";
        log.warn("Could not find exact Edge version, using default: {}", defaultVersion);
        return defaultVersion;
    }

    /**
     * Chrome 驱动下载 URL
     */
    private String getChromeDriverUrl(String version) throws Exception {
        int majorVersion = Integer.parseInt(version.split("\\.")[0]);

        if (majorVersion >= 115) {
            // Chrome 115+ 使用 Chrome for Testing，需要查询完整版本号
            String fullVersion = resolveChromeCfTVersion(version);
            return "https://storage.googleapis.com/chrome-for-testing-public/" + fullVersion + "/win64/chromedriver-win64.zip";
        } else {
            // Chrome 114 及以下使用旧地址
            return "https://chromedriver.storage.googleapis.com/" + version + "/chromedriver_win32.zip";
        }
    }

    /**
     * 解析 Chrome for Testing 完整版本号
     * 从 https://googlechromelabs.github.io/chrome-for-testing/latest-versions-per-milestone.json 查询
     */
    private String resolveChromeCfTVersion(String majorVersion) throws Exception {
        log.info("Resolving Chrome for Testing version for major version: {}", majorVersion);

        try {
            URL url = new URL("https://googlechromelabs.github.io/chrome-for-testing/latest-versions-per-milestone.json");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);

            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed to fetch Chrome version info");
            }

            StringBuilder response = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
            }
            conn.disconnect();

            // 解析 JSON 查找对应版本
            // 格式: "milestones":{"131":{"milestone":"131","version":"131.0.6778.69"...}
            String json = response.toString();
            String searchKey = "\"" + majorVersion + "\":{\"milestone\":\"" + majorVersion + "\",\"version\":\"";
            int idx = json.indexOf(searchKey);
            if (idx != -1) {
                int start = idx + searchKey.length();
                int end = json.indexOf("\"", start);
                String fullVersion = json.substring(start, end);
                log.info("Found Chrome version: {}", fullVersion);
                return fullVersion;
            }

            log.warn("Could not find Chrome version in API response, using input version: {}", majorVersion);
            return majorVersion;
        } catch (Exception e) {
            log.warn("Failed to resolve Chrome version from API: {}, using input version", e.getMessage());
            return majorVersion;
        }
    }

    /**
     * Firefox 驱动下载 URL
     * geckodriver 版本号与 Firefox 浏览器版本号无关，固定使用已验证可用的版本。
     * 下载地址：https://github.com/mozilla/geckodriver/releases/download/v0.34.0/geckodriver-v0.34.0-win64.zip
     */
    private String getFirefoxDriverUrl(String version) throws Exception {
        String geckodriverVersion = "0.34.0";
        String url = "https://github.com/mozilla/geckodriver/releases/download/v"
                + geckodriverVersion + "/geckodriver-v" + geckodriverVersion + "-win64.zip";
        log.info("Using geckodriver download URL: {}", url);
        return url;
    }

    /**
     * IE 驱动下载 URL
     * IEDriverServer 版本跟随 Selenium 版本（与 IE 浏览器版本无关）。
     * 固定使用已验证可用的 Selenium 4.14.0 版本，32 位驱动（兼容性最佳）。
     * 下载地址：https://github.com/SeleniumHQ/selenium/releases/download/selenium-4.14.0/IEDriverServer_Win32_4.14.0.zip
     */
    private String getIeDriverUrl(String version) throws Exception {
        // 固定使用已验证可用的版本
        String seleniumVersion = "4.14.0";
        String url = "https://github.com/SeleniumHQ/selenium/releases/download/selenium-"
                + seleniumVersion + "/IEDriverServer_Win32_" + seleniumVersion + ".zip";
        log.info("Using IEDriverServer download URL: {}", url);
        return url;
    }

    /**
     * 检查 URL 是否存在
     */
    private boolean urlExists(String urlString) {
        try {
            URL url = new URL(urlString);
            Proxy proxy = detectProxy(urlString);
            HttpURLConnection conn = (HttpURLConnection) (proxy != null ? url.openConnection(proxy) : url.openConnection());
            conn.setRequestMethod("HEAD");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            conn.setInstanceFollowRedirects(true);
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");

            int responseCode = conn.getResponseCode();
            conn.disconnect();
            return responseCode == 200;
        } catch (Exception e) {
            log.debug("URL check failed for {}: {}", urlString, e.getMessage());
            return false;
        }
    }

    /**
     * 下载文件（自动使用系统代理，手动跟随重定向）
     */
    private void downloadFile(String urlString, Path targetPath) throws Exception {
        int maxRetries = 3;
        Exception lastException = null;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                // 手动跟随重定向（GitHub releases 会 302 到 objects.githubusercontent.com，
                // HttpURLConnection 默认不跟随跨域重定向）
                String currentUrl = urlString;
                HttpURLConnection conn = null;
                for (int redirects = 0; redirects < 5; redirects++) {
                    URL url = new URL(currentUrl);
                    Proxy proxy = detectProxy(currentUrl);
                    conn = (HttpURLConnection) (proxy != null ? url.openConnection(proxy) : url.openConnection());
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(30000);
                    conn.setReadTimeout(60000);
                    conn.setInstanceFollowRedirects(false);
                    conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");

                    int responseCode = conn.getResponseCode();
                    if (responseCode == 301 || responseCode == 302 || responseCode == 307) {
                        String location = conn.getHeaderField("Location");
                        conn.disconnect();
                        if (location == null) {
                            throw new RuntimeException("Redirect without Location header");
                        }
                        log.info("Following redirect: {} -> {}", currentUrl, location);
                        currentUrl = location;
                        continue;
                    }
                    if (responseCode != 200) {
                        conn.disconnect();
                        throw new RuntimeException("HTTP " + responseCode);
                    }
                    break;
                }

                long contentLength = conn.getContentLengthLong();
                log.info("Starting download: {} (size: {} bytes)", currentUrl,
                        contentLength > 0 ? contentLength : "unknown");

                try (InputStream in = conn.getInputStream();
                     OutputStream out = Files.newOutputStream(targetPath)) {
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    long totalBytes = 0;
                    long lastLogTime = System.currentTimeMillis();
                    while ((bytesRead = in.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                        totalBytes += bytesRead;
                        // 每 3 秒打印一次下载进度
                        long now = System.currentTimeMillis();
                        if (now - lastLogTime > 3000) {
                            if (contentLength > 0) {
                                log.info("Download progress: {} / {} bytes ({}%)",
                                        totalBytes, contentLength, totalBytes * 100 / contentLength);
                            } else {
                                log.info("Download progress: {} bytes", totalBytes);
                            }
                            lastLogTime = now;
                        }
                    }
                    log.info("Download complete: {} bytes", totalBytes);
                }
                conn.disconnect();
                return;

            } catch (Exception e) {
                lastException = e;
                log.warn("Download attempt {} failed: {}", attempt, e.getMessage());
                if (attempt < maxRetries) {
                    long waitTime = 1000L * (long) Math.pow(2, attempt - 1);
                    log.info("Retrying in {} ms...", waitTime);
                    Thread.sleep(waitTime);
                }
            }
        }

        throw new RuntimeException("下载失败，已重试 " + maxRetries + " 次: " + lastException.getMessage(), lastException);
    }

    /**
     * 检测系统代理设置
     * 浏览器能下载但 Java 卡住，通常是因为 Java 没走系统代理（梯子）。
     * 此方法通过 ProxySelector 获取系统配置的代理，也支持通过环境变量/JVM 参数配置。
     */
    private Proxy detectProxy(String urlString) {
        try {
            // 1. 检查 JVM 参数 -Dhttp.proxyHost / -Dhttps.proxyHost
            String proxyHost = System.getProperty("https.proxyHost",
                    System.getProperty("http.proxyHost"));
            String proxyPort = System.getProperty("https.proxyPort",
                    System.getProperty("http.proxyPort", "7890"));
            if (proxyHost != null && !proxyHost.isEmpty()) {
                Proxy proxy = new Proxy(Proxy.Type.HTTP,
                        new InetSocketAddress(proxyHost, Integer.parseInt(proxyPort)));
                log.debug("Using JVM proxy: {}:{}", proxyHost, proxyPort);
                return proxy;
            }

            // 2. 检查环境变量 HTTPS_PROXY / HTTP_PROXY
            String envProxy = System.getenv("HTTPS_PROXY");
            if (envProxy == null || envProxy.isEmpty()) {
                envProxy = System.getenv("https_proxy");
            }
            if (envProxy == null || envProxy.isEmpty()) {
                envProxy = System.getenv("HTTP_PROXY");
            }
            if (envProxy == null || envProxy.isEmpty()) {
                envProxy = System.getenv("http_proxy");
            }
            if (envProxy != null && !envProxy.isEmpty()) {
                // 格式: http://127.0.0.1:7890 或 socks5://127.0.0.1:1080
                envProxy = envProxy.replaceFirst("^https?://", "");
                String[] parts = envProxy.split(":");
                if (parts.length == 2) {
                    Proxy proxy = new Proxy(Proxy.Type.HTTP,
                            new InetSocketAddress(parts[0], Integer.parseInt(parts[1])));
                    log.debug("Using env proxy: {}", envProxy);
                    return proxy;
                }
            }

            // 3. 通过系统 ProxySelector 检测（可读取 Windows 系统代理设置）
            List<Proxy> proxies = ProxySelector.getDefault().select(new URI(urlString));
            for (Proxy proxy : proxies) {
                if (proxy.type() != Proxy.Type.DIRECT) {
                    log.debug("Using system proxy: {}", proxy);
                    return proxy;
                }
            }
        } catch (Exception e) {
            log.debug("Proxy detection failed: {}", e.getMessage());
        }

        return null;
    }

    /**
     * 从 ZIP 文件中提取驱动
     */
    private void extractDriverFromZip(Path zipFile, Path targetPath, String driverFileName) throws Exception {
        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipFile))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                String entryName = entry.getName();
                // 匹配驱动文件（可能在子目录中）
                if (entryName.endsWith(driverFileName) || entryName.endsWith(driverFileName.replace(".exe", ""))) {
                    Files.copy(zis, targetPath, StandardCopyOption.REPLACE_EXISTING);
                    log.info("Extracted driver: {} -> {}", entryName, targetPath);
                    return;
                }
            }
        }
        throw new RuntimeException("ZIP 文件中未找到驱动: " + driverFileName);
    }

    /**
     * 获取浏览器对应的驱动文件名前缀
     */
    private String getDriverPrefix(String browserType) {
        return switch (browserType) {
            case "edge" -> "msedgedriver";
            case "chrome" -> "chromedriver";
            case "firefox" -> "geckodriver";
            case "ie" -> "IEDriverServer";
            default -> null;
        };
    }

    private void runPyInstaller(String pythonPath, Path tempDir, Path scriptFile,
                                Path driversDir, String name, boolean offlineMode, boolean runHidden) throws Exception {
        List<String> cmd = new ArrayList<>();
        cmd.add(pythonPath);
        cmd.add("-m");
        cmd.add("PyInstaller");
        cmd.add("--onefile");
        cmd.add(runHidden ? "--noconsole" : "--console");
        cmd.add("--name=" + name);
        cmd.add("--distpath=" + tempDir.resolve("dist").toString());
        cmd.add("--workpath=" + tempDir.resolve("build").toString());
        cmd.add("--specpath=" + tempDir.toString());

        // 离线模式：把 drivers 目录打包进 exe
        if (offlineMode && driversDir != null && Files.exists(driversDir)) {
            try (var files = Files.list(driversDir)) {
                if (files.findAny().isPresent()) {
                    String sep = System.getProperty("os.name").toLowerCase().contains("win") ? ";" : ":";
                    cmd.add("--add-data=" + driversDir.toString() + sep + "drivers");
                    log.info("Bundling drivers directory into exe (offline mode)");
                }
            }
        }

        // hidden imports for selenium
        cmd.add("--hidden-import=selenium");
        cmd.add("--hidden-import=selenium.webdriver");
        cmd.add("--hidden-import=selenium.webdriver.common.by");
        cmd.add("--hidden-import=selenium.webdriver.support.ui");
        cmd.add("--hidden-import=selenium.webdriver.support.expected_conditions");
        cmd.add("--hidden-import=selenium.webdriver.chrome.service");
        cmd.add("--hidden-import=selenium.webdriver.chrome.options");
        cmd.add("--hidden-import=selenium.webdriver.firefox.service");
        cmd.add("--hidden-import=selenium.webdriver.firefox.options");
        cmd.add("--hidden-import=selenium.webdriver.edge.service");
        cmd.add("--hidden-import=selenium.webdriver.edge.options");
        cmd.add("--hidden-import=selenium.webdriver.ie.service");
        cmd.add("--hidden-import=selenium.webdriver.ie.options");
        cmd.add("--hidden-import=selenium.webdriver.common.action_chains");
        cmd.add("--hidden-import=selenium.webdriver.common.keys");

        cmd.add("--clean");
        cmd.add("--noconfirm");
        cmd.add(scriptFile.toString());

        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.directory(tempDir.toFile());
        pb.redirectErrorStream(true);
        pb.environment().put("PYTHONIOENCODING", "utf-8");
        pythonEnv.configurePythonPath(pb);

        Process process = pb.start();
        String processOutput = captureProcessOutput(process);
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            log.error("PyInstaller packaging failed:\n{}", processOutput);
            throw new RuntimeException("打包失败: " + extractErrorSummary(processOutput));
        }

        log.info("PyInstaller packaging succeeded");
    }

    private void deleteDirectory(File directory) {
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteDirectory(file);
                }
            }
        }
        directory.delete();
    }

    private void installDependencies(String pythonPath, Path workDir) throws Exception {
        log.info("Installing dependencies...");

        ProcessBuilder pb = new ProcessBuilder(
                pythonPath, "-m", "pip", "install",
                "--quiet",
                "pyinstaller",
                "selenium"
        );
        pb.directory(workDir.toFile());
        pb.redirectErrorStream(true);
        pb.environment().put("PYTHONIOENCODING", "utf-8");
        pythonEnv.configurePythonPath(pb);

        Process process = pb.start();
        String output = captureProcessOutput(process);
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            log.error("pip install failed:\n{}", output);
            throw new RuntimeException("依赖安装失败: " + extractErrorSummary(output));
        }

        log.info("Dependencies installed");
    }

    private String captureProcessOutput(Process process) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        }
        return sb.toString();
    }

    private String extractErrorSummary(String fullOutput) {
        if (fullOutput == null || fullOutput.isEmpty()) {
            return "未知错误";
        }
        String[] lines = fullOutput.split("\n");
        int start = Math.max(0, lines.length - 5);
        StringBuilder summary = new StringBuilder();
        for (int i = start; i < lines.length; i++) {
            String trimmed = lines[i].trim();
            if (!trimmed.isEmpty()) {
                if (summary.length() > 0) summary.append(" | ");
                summary.append(trimmed);
            }
        }
        return summary.length() > 0 ? summary.toString() : "未知错误";
    }
}
