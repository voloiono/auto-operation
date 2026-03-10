package com.example.autooperation.controller;

import com.example.autooperation.service.SettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SettingsController {

    private final SettingsService settingsService;

    /**
     * 获取所有配置（按分组）
     */
    @GetMapping
    public Map<String, Map<String, String>> getAll() {
        return settingsService.getAll();
    }

    /**
     * 获取指定分组的配置
     */
    @GetMapping("/{group}")
    public Map<String, String> getGroup(@PathVariable String group) {
        return settingsService.getGroup(group);
    }

    /**
     * 保存指定分组的配置
     */
    @PutMapping("/{group}")
    public Map<String, Object> saveGroup(@PathVariable String group, @RequestBody Map<String, String> settings) {
        settingsService.saveGroup(group, settings);
        return Map.of("success", true, "message", "配置已保存");
    }
}
