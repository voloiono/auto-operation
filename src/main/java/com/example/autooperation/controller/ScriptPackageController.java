package com.example.autooperation.controller;

import com.example.autooperation.service.ScriptGeneratorService;
import com.example.autooperation.service.ScriptPackageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/scripts")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ScriptPackageController {
    private final ScriptPackageService scriptPackageService;
    private final ScriptGeneratorService scriptGeneratorService;

    @PostMapping("/package")
    public ResponseEntity<byte[]> packageScript(@RequestBody PackageRequest request) {
        try {
            // networkMode: "online" 或 "offline"
            boolean offlineMode = "offline".equals(request.getNetworkMode());

            // 根据运行模式包装脚本
            String script = request.getScript();
            String executionMode = request.getExecutionMode();
            if (executionMode != null && !"once".equals(executionMode)) {
                script = scriptGeneratorService.wrapWithExecutionMode(
                        script,
                        executionMode,
                        request.getRestartDelay() != null ? request.getRestartDelay() : 30,
                        request.getMaxRetries() != null ? request.getMaxRetries() : 0,
                        request.getScheduleTimes(),
                        request.getIntervalMinutes() != null ? request.getIntervalMinutes() : 60,
                        Boolean.TRUE.equals(request.getAutoStart()),
                        Boolean.TRUE.equals(request.getRunHidden()),
                        request.getRepeatMode(),
                        request.getRepeatDays(),
                        Boolean.TRUE.equals(request.getRdpMode())
                );
            }

            boolean runHidden = Boolean.TRUE.equals(request.getRunHidden());
            byte[] packagedFile = scriptPackageService.packageScript(
                    script,
                    request.getPlatform(),
                    request.getName(),
                    offlineMode,
                    request.getBrowserVersions(),
                    runHidden
            );

            String filename = request.getName() + (request.getPlatform().equals("windows") ? ".exe" : "");

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(packagedFile);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(e.getMessage().getBytes());
        }
    }

    public static class PackageRequest {
        private String script;
        private String platform;
        private String name;
        private String networkMode;
        private Map<String, String> browserVersions;  // { "edge": "143", "chrome": "120" }
        private String executionMode;  // "once" | "daemon" | "scheduled"
        private Integer restartDelay;
        private Integer maxRetries;
        private List<String> scheduleTimes;
        private Integer intervalMinutes;
        private Boolean autoStart;
        private Boolean runHidden;
        private String repeatMode;
        private List<Integer> repeatDays;
        private Boolean rdpMode;

        public String getScript() {
            return script;
        }

        public void setScript(String script) {
            this.script = script;
        }

        public String getPlatform() {
            return platform;
        }

        public void setPlatform(String platform) {
            this.platform = platform;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getNetworkMode() {
            return networkMode;
        }

        public void setNetworkMode(String networkMode) {
            this.networkMode = networkMode;
        }

        public Map<String, String> getBrowserVersions() {
            return browserVersions;
        }

        public void setBrowserVersions(Map<String, String> browserVersions) {
            this.browserVersions = browserVersions;
        }

        public String getExecutionMode() {
            return executionMode;
        }

        public void setExecutionMode(String executionMode) {
            this.executionMode = executionMode;
        }

        public Integer getRestartDelay() {
            return restartDelay;
        }

        public void setRestartDelay(Integer restartDelay) {
            this.restartDelay = restartDelay;
        }

        public Integer getMaxRetries() {
            return maxRetries;
        }

        public void setMaxRetries(Integer maxRetries) {
            this.maxRetries = maxRetries;
        }

        public List<String> getScheduleTimes() {
            return scheduleTimes;
        }

        public void setScheduleTimes(List<String> scheduleTimes) {
            this.scheduleTimes = scheduleTimes;
        }

        public Integer getIntervalMinutes() {
            return intervalMinutes;
        }

        public void setIntervalMinutes(Integer intervalMinutes) {
            this.intervalMinutes = intervalMinutes;
        }

        public Boolean getAutoStart() {
            return autoStart;
        }

        public void setAutoStart(Boolean autoStart) {
            this.autoStart = autoStart;
        }

        public Boolean getRunHidden() {
            return runHidden;
        }

        public void setRunHidden(Boolean runHidden) {
            this.runHidden = runHidden;
        }

        public String getRepeatMode() {
            return repeatMode;
        }

        public void setRepeatMode(String repeatMode) {
            this.repeatMode = repeatMode;
        }

        public List<Integer> getRepeatDays() {
            return repeatDays;
        }

        public void setRepeatDays(List<Integer> repeatDays) {
            this.repeatDays = repeatDays;
        }

        public Boolean getRdpMode() {
            return rdpMode;
        }

        public void setRdpMode(Boolean rdpMode) {
            this.rdpMode = rdpMode;
        }
    }
}
