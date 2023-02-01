package com.gonsalves.timely.service;

import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import com.gonsalves.timely.exception.ProjectNotFoundException;
import com.gonsalves.timely.exception.TaskAlreadyExistsException;
import com.gonsalves.timely.exception.TaskNotFoundException;
import com.gonsalves.timely.repository.TaskRepository;
import com.gonsalves.timely.repository.model.TaskEntity;
import com.gonsalves.timely.repository.model.TimeLogEntity;
import com.gonsalves.timely.service.model.StatusEnum;
import com.gonsalves.timely.service.model.Task;
import com.gonsalves.timely.service.model.TimeLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    @Autowired
    TaskService(TaskRepository taskRepository) {this.taskRepository = taskRepository;}


    public List<Task> getAllTasksByProjectId(String projectId) {
        List<TaskEntity> taskEntities = taskRepository.getAllTasksByProjectId(projectId);
        return taskEntities.stream()
                .map(this::convertFromEntity)
                .collect(Collectors.toList());
    }

    public Task getTaskByTaskName(String projectId, String taskName) {
        TaskEntity entity = taskRepository.getTaskByTaskName(projectId, taskName)
                .orElseThrow(TaskNotFoundException::new);
        return convertFromEntity(entity);
    }

    public Task createTask(Task task) {
        taskRepository.getTaskByTaskName(task.getProjectId(), task.getTaskName())
                .ifPresent(existingTask -> {
                    throw new TaskAlreadyExistsException();
                });
        if (task.getTimeLogs() == null) {
            task.setTimeLogs(new ArrayList<>());
            task.setTimeSpent("00:00:00");
        }
        TaskEntity entity = convertToEntity(task);
        taskRepository.createTask(entity);

        return task;
    }

    public void updateTask(Task task) {
        if (task.getTimeLogs() != null)
            task.calculateAndUpdateTimeSpent();
        TaskEntity entity = convertToEntity(task);
        try {
            taskRepository.updateTask(entity);
        } catch (ConditionalCheckFailedException e) {
            throw new TaskNotFoundException();
        }
    }

    public void deleteTask(Task task) {
        taskRepository.getTaskByTaskName(task.getProjectId(), task.getTaskName())
                .orElseThrow(TaskNotFoundException::new);

        TaskEntity entity = new TaskEntity();
        entity.setProjectId(task.getProjectId());
        entity.setTaskName(task.getTaskName());
        taskRepository.deleteTask(entity);
    }

    public void startTaskTime(String projectId, String taskName) {
        Task task = convertFromEntity(taskRepository.getTaskByTaskName(projectId, taskName)
                .orElseThrow(TaskNotFoundException::new));
        task.startTime();
        task.setStatus(StatusEnum.IN_PROGRESS.getStatus());
        taskRepository.updateTask(convertToEntity(task));
    }

    public void stopTaskTime(String projectId, String taskName) {
        Task task = convertFromEntity(taskRepository.getTaskByTaskName(projectId, taskName)
                .orElseThrow(TaskNotFoundException::new));
        task.stopTime();
        task.calculateAndUpdateTimeSpent();
        task.setStatus(StatusEnum.PLANNED.getStatus());
        taskRepository.updateTask(convertToEntity(task));
    }


    private TaskEntity convertToEntity(Task task) {
        return new TaskEntity(
                task.getProjectId(),
                task.getTaskName().strip(),
                task.getNotes(),
                task.getTimeSpent(),
                convertToEntity(task.getTimeLogs()),
                task.getStatus()
        );
    }

    private List<TimeLogEntity> convertToEntity(List<TimeLog> timeLogs) {
        return Objects.isNull(timeLogs) ? null : timeLogs.stream()
                .map(timeLog -> new TimeLogEntity(timeLog.getStartDateTime(), timeLog.getEndDateTime()))
                .collect(Collectors.toList());
    }

    private Task convertFromEntity(TaskEntity taskEntity) {
        return new Task(
                taskEntity.getProjectId(),
                taskEntity.getTaskName(),
                taskEntity.getNotes(),
                taskEntity.getTimeSpent(),
                convertFromEntity(taskEntity.getTimeLogEntities()),
                taskEntity.getStatus()
        );
    }

    private List<TimeLog> convertFromEntity(List<TimeLogEntity> timeLogEntities) {
        return Objects.isNull(timeLogEntities) ? null : timeLogEntities.stream()
                .map(entity -> new TimeLog(entity.getStartDateTime(), entity.getEndDateTime()))
                .collect(Collectors.toList());
    }

}
