package com.example.autooperation.job;

import com.example.autooperation.model.Flow;
import com.example.autooperation.model.ScheduleTask;
import com.example.autooperation.repository.FlowRepository;
import com.example.autooperation.repository.ScheduleTaskRepository;
import com.example.autooperation.service.ScriptGeneratorService;
import com.example.autooperation.service.ScriptExecutorService;
import com.example.autooperation.service.ExecutionLogService;
import com.example.autooperation.service.EmailService;
import com.example.autooperation.dto.ExecutionResultDTO;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
@Slf4j
public class FlowExecutionJob implements Job {
    @Autowired
    private FlowRepository flowRepository;

    @Autowired
    private ScheduleTaskRepository scheduleTaskRepository;

    @Autowired
    private ScriptGeneratorService scriptGeneratorService;

    @Autowired
    private ScriptExecutorService scriptExecutorService;

    @Autowired
    private ExecutionLogService executionLogService;

    @Autowired
    private EmailService emailService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            Long scheduleTaskId = Long.parseLong(context.getJobDetail().getKey().getName());
            ScheduleTask scheduleTask = scheduleTaskRepository.findById(scheduleTaskId)
                    .orElseThrow(() -> new RuntimeException("Schedule task not found"));

            Flow flow = scheduleTask.getFlow();
            log.info("Executing flow: {}", flow.getName());

            String script = flow.getGeneratedScript();
            if (script == null) {
                script = scriptGeneratorService.generatePythonScript(flow.getConfiguration());
            }

            ExecutionResultDTO result = scriptExecutorService.executeScript(script, "flow_" + flow.getId());

            executionLogService.createLog(flow.getId(),
                    result.getSuccess() ? "success" : "failed",
                    result.getOutput(),
                    result.getErrorMessage(),
                    result.getExecutionTimeMs());

            scheduleTask.setLastExecution(LocalDateTime.now());
            scheduleTaskRepository.save(scheduleTask);

            if (scheduleTask.getNotifyEmail() != null) {
                emailService.sendExecutionNotification(
                        scheduleTask.getNotifyEmail(),
                        flow.getName(),
                        result.getSuccess(),
                        result.getOutput(),
                        result.getErrorMessage()
                );
            }

            log.info("Flow execution completed: {}", flow.getName());
        } catch (Exception e) {
            log.error("Error executing flow", e);
            throw new JobExecutionException(e);
        }
    }
}
