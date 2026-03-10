package com.example.autooperation.service;

import com.example.autooperation.job.FlowExecutionJob;
import com.example.autooperation.model.ScheduleTask;
import com.example.autooperation.repository.ScheduleTaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduleExecutorService {
    private final Scheduler scheduler;
    private final ScheduleTaskRepository scheduleTaskRepository;

    public void scheduleTask(ScheduleTask task) throws SchedulerException {
        JobDetail jobDetail = JobBuilder.newJob(FlowExecutionJob.class)
                .withIdentity(String.valueOf(task.getId()), "flow-execution")
                .build();

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(String.valueOf(task.getId()), "flow-execution")
                .withSchedule(CronScheduleBuilder.cronSchedule(task.getCronExpression()))
                .build();

        scheduler.scheduleJob(jobDetail, trigger);
        log.info("Scheduled task: {} with cron: {}", task.getId(), task.getCronExpression());
    }

    public void unscheduleTask(Long taskId) throws SchedulerException {
        scheduler.unscheduleJob(TriggerKey.triggerKey(String.valueOf(taskId), "flow-execution"));
        scheduler.deleteJob(JobKey.jobKey(String.valueOf(taskId), "flow-execution"));
        log.info("Unscheduled task: {}", taskId);
    }

    public void initializeSchedules() throws SchedulerException {
        List<ScheduleTask> enabledTasks = scheduleTaskRepository.findByEnabled(true);
        for (ScheduleTask task : enabledTasks) {
            try {
                scheduleTask(task);
            } catch (Exception e) {
                log.error("Failed to schedule task: {}", task.getId(), e);
            }
        }
    }

    public void updateSchedule(ScheduleTask task) throws SchedulerException {
        try {
            unscheduleTask(task.getId());
        } catch (Exception e) {
            log.warn("Task not scheduled yet: {}", task.getId());
        }

        if (task.getEnabled()) {
            scheduleTask(task);
        }
    }
}
