package com.example.autooperation.repository;

import com.example.autooperation.model.AppConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppConfigRepository extends JpaRepository<AppConfig, String> {
    List<AppConfig> findByGroup(String group);
}
