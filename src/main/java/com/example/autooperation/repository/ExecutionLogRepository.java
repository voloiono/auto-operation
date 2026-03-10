package com.example.autooperation.repository;

import com.example.autooperation.model.ExecutionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ExecutionLogRepository extends JpaRepository<ExecutionLog, Long> {
    List<ExecutionLog> findByFlowIdOrderByStartedAtDesc(Long flowId);
}
