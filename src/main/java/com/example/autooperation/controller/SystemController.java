package com.example.autooperation.controller;

import com.example.autooperation.service.PythonEnvironmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/system")
@RequiredArgsConstructor
public class SystemController {

    private final PythonEnvironmentService pythonEnvironmentService;

    @GetMapping("/python-status")
    public Map<String, Object> getPythonStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("available", pythonEnvironmentService.isPythonAvailable());
        status.put("path", pythonEnvironmentService.getResolvedPath());
        status.put("version", pythonEnvironmentService.getPythonVersion());
        return status;
    }
}
