package com.example.autooperation.repository;

import com.example.autooperation.model.ScheduleTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ScheduleTaskRepository extends JpaRepository<ScheduleTask, Long> {
    List<ScheduleTask> findByFlowId(Long flowId);
    List<ScheduleTask> findByEnabled(Boolean enabled);
}
