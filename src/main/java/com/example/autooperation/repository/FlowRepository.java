package com.example.autooperation.repository;

import com.example.autooperation.model.Flow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface FlowRepository extends JpaRepository<Flow, Long> {
    List<Flow> findByProjectId(Long projectId);

    // 获取项目的第一个流程（按ID升序，最早创建的）
    Optional<Flow> findFirstByProjectIdOrderByIdAsc(Long projectId);

    // 检查项目是否已有流程
    boolean existsByProjectId(Long projectId);
}
