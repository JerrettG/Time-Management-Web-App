package com.gonsalves.timely.controller;

import com.gonsalves.timely.controller.model.*;
import com.gonsalves.timely.exception.TaskAlreadyExistsException;
import com.gonsalves.timely.exception.TaskNotFoundException;
import com.gonsalves.timely.service.TaskService;
import com.gonsalves.timely.service.model.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/task")
public class TaskController {

    private final TaskService taskService;

    @Autowired
    TaskController(TaskService taskService) {this.taskService = taskService;}


    @GetMapping("/all")
    public ResponseEntity<List<TaskResponse>> getAllTasksByUser(
            @RequestParam("projectId")String projectId) {
        List<Task> tasks = taskService.getAllTasksByProjectId(projectId);
        List<TaskResponse> responses = tasks.stream().map(this::convertToResponse).collect(Collectors.toList());
        return ResponseEntity.ok().body(responses);
    }



    @GetMapping("/")
    public ResponseEntity<TaskResponse> getTaskByTaskName(
            @RequestParam("projectId")String projectId,
            @RequestParam("taskName")String taskName) {
        try {
            Task task = taskService.getTaskByTaskName(projectId, taskName);
            TaskResponse response = convertToResponse(task);
            return ResponseEntity.ok().body(response);
        } catch (TaskNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PostMapping
    public ResponseEntity<TaskResponse> createTask(@RequestBody TaskCreateRequest request) {
        Task task = Task.builder()
                .projectId(request.getProjectId())
                .taskName(request.getTaskName())
                .notes(request.getNotes())
                .status(request.getStatus())
                .build();
        try {
            Task createdTask = taskService.createTask(task);
            TaskResponse response = convertToResponse(createdTask);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (TaskAlreadyExistsException e) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
    }
//NOTE: taskName cannot be updated at this endpoint as you cannot update a key value
    @PutMapping
    public ResponseEntity<TaskResponse> updateTask(
            @RequestParam(value = "editName", defaultValue = "false")boolean editName,
            @RequestBody TaskUpdateRequest request) {
        Task task = Task.builder()
                .projectId(request.getProjectId())
                .taskName(request.getTaskName())
                .notes(request.getNotes())
                .status(request.getStatus())
                .build();
        try {
            if (editName) {
                Task existingTask = taskService.getTaskByTaskName(request.getProjectId(), request.getTaskName());
                taskService.deleteTask(existingTask);
                existingTask.setTaskName(request.getUpdatedTaskName());
                taskService.createTask(existingTask);
            } else {
                taskService.updateTask(task);
            }
            return ResponseEntity.accepted().build();
        } catch (TaskNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
    @PutMapping("/start")
    public ResponseEntity<TaskResponse> startTaskTime(@RequestBody TaskStartTimeRequest request) {
        try {
            taskService.startTaskTime(request.getProjectId(), request.getTaskName());
            return ResponseEntity.accepted().build();
        } catch (TaskNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/stop")
    public ResponseEntity<TaskResponse> stopTaskTime(@RequestBody TaskStopTimeRequest request) {
        try {
            taskService.stopTaskTime(request.getProjectId(), request.getTaskName());
            return ResponseEntity.accepted().build();
        } catch (TaskNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping
    public ResponseEntity<TaskResponse> deleteTask(
            @RequestParam("projectId")String projectId,
            @RequestParam("taskName")String taskName
            ) {
        Task task = Task.builder()
                .projectId(projectId)
                .taskName(taskName)
                .build();
        try {
            taskService.deleteTask(task);
            return ResponseEntity.accepted().build();
        } catch (TaskNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    private TaskResponse convertToResponse(Task task) {
        return new TaskResponse(
                task.getProjectId(),
                task.getTaskName(),
                task.getNotes(),
                task.getTimeSpent(),
                task.getTimeLogs(),
                task.getStatus()
        );
    }
}
