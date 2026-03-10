package com.example.autooperation.service;

import com.example.autooperation.model.AppConfig;
import com.example.autooperation.repository.AppConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class SettingsService {

    private final AppConfigRepository appConfigRepository;

    /**
     * 获取某个分组的所有配置，返回 key->value map
     */
    public Map<String, String> getGroup(String group) {
        List<AppConfig> configs = appConfigRepository.findByGroup(group);
        Map<String, String> result = new HashMap<>();
        for (AppConfig config : configs) {
            result.put(config.getKey(), config.getValue());
        }
        return result;
    }

    /**
     * 获取所有配置，按分组归类
     */
    public Map<String, Map<String, String>> getAll() {
        List<AppConfig> all = appConfigRepository.findAll();
        Map<String, Map<String, String>> result = new HashMap<>();
        for (AppConfig config : all) {
            result.computeIfAbsent(config.getGroup(), k -> new HashMap<>())
                    .put(config.getKey(), config.getValue());
        }
        return result;
    }

    /**
     * 获取单个配置值
     */
    public String get(String key, String defaultValue) {
        return appConfigRepository.findById(key)
                .map(AppConfig::getValue)
                .orElse(defaultValue);
    }

    /**
     * 批量保存某个分组的配置
     */
    public void saveGroup(String group, Map<String, String> settings) {
        for (Map.Entry<String, String> entry : settings.entrySet()) {
            AppConfig config = new AppConfig(entry.getKey(), entry.getValue(), group);
            appConfigRepository.save(config);
        }
        log.info("Settings saved for group: {}", group);
    }

    /**
     * 保存单个配置
     */
    public void save(String key, String value, String group) {
        AppConfig config = new AppConfig(key, value, group);
        appConfigRepository.save(config);
    }
}
