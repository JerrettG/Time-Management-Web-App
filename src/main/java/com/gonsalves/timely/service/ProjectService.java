package com.gonsalves.timely.service;

import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import com.gonsalves.timely.exception.ProjectAlreadyExistsException;
import com.gonsalves.timely.exception.ProjectNotFoundException;
import com.gonsalves.timely.repository.ProjectRepository;
import com.gonsalves.timely.repository.model.ProjectEntity;
import com.gonsalves.timely.repository.model.TaskEntity;
import com.gonsalves.timely.repository.model.TimeLogEntity;
import com.gonsalves.timely.service.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final TaskServiceClient taskServiceClient;
    @Autowired
    ProjectService(ProjectRepository projectRepository, TaskServiceClient taskServiceClient) {
        this.projectRepository = projectRepository;
        this.taskServiceClient = taskServiceClient;
    }


    /**
     * Retrieves a list of all {@code Project} that are attached to the specified userId.
     *
     * @param userId the userId of the logged-in user
     * @return a list of Project objects
     */
    public List<Project> getAllProjectsByUser(String userId) {
        List<ProjectEntity> projectEntities = projectRepository.getAllProjectsByUser(userId);
        return projectEntities.stream()
                .map(this::convertFromEntity)
                .collect(Collectors.toList());
    }

    public Project getProjectByProjectName(String userId, String projectName) {
        ProjectEntity entity = projectRepository.getProjectByProjectName(userId, projectName)
                .orElseThrow(ProjectNotFoundException::new);
        return convertFromEntity(entity);
    }

    public Project createProject(Project project) {
        projectRepository.getProjectByProjectName(project.getUserId(), project.getProjectName())
                .ifPresent(existingProject -> {
                    throw new ProjectAlreadyExistsException();
                });

        project.setTotalTimeSpent("00:00:00");
        project.setProjectId(String.format("%s_%s", project.getUserId(), project.getProjectName()));
        project.setCompletionPercent(0);
        project.setCreationDate(LocalDateTime.now().toString());
        ProjectEntity entity = convertToEntity(project);
        projectRepository.createProject(entity);

        return project;
    }

    public void updateProject(Project project) {
        List<TaskServiceClient.Task> tasks = taskServiceClient.getAllTasksForProject(project.getProjectId());
        long totalTimeSpent = tasks.stream()
                .reduce(
                        0L,
                        (partialTimeSpent, task) -> partialTimeSpent + TimeSpentConverter.convertFromString(task.getTimeSpent()),
                        Long::sum);
        double numCompletedTasks = tasks.stream()
                .reduce(
                        0.0,
                        (count, task) -> (task.getStatus().equals(StatusEnum.COMPLETE.getStatus())) ? (count + 1.0) : count,
                        Double::sum);
        int completionPercent = (int) ((numCompletedTasks / tasks.size()) * 100);
        String updatedTimeSpent = TimeSpentConverter.convertToString(totalTimeSpent);
        project.setTotalTimeSpent(updatedTimeSpent);
        project.setCompletionPercent(completionPercent);
        ProjectEntity entity = convertToEntity(project);
        try {
            projectRepository.updateProject(entity);
        } catch (ConditionalCheckFailedException e) {
            throw new ProjectNotFoundException();
        }
    }
    public void deleteProject(Project project) {
        projectRepository.getProjectByProjectName(project.getUserId(), project.getProjectName())
                .orElseThrow(ProjectNotFoundException::new);
        ProjectEntity entity = convertToEntity(project);
        projectRepository.deleteProject(entity);
        String projectId = String.format("%s_%s", project.getUserId(), project.getProjectName());
        taskServiceClient.deleteAllTasksForProject(projectId);
    }


    public ProjectEntity convertToEntity(Project project) {
        return new ProjectEntity(
                project.getUserId(),
                project.getProjectName().strip(),
                project.getProjectId(),
                project.getCreationDate(),
                project.getTotalTimeSpent(),
                project.getCompletionPercent()
        );
    }


    public Project convertFromEntity(ProjectEntity projectEntity) {
        return Project.builder()
                .projectId(projectEntity.getProjectId())
                .userId(projectEntity.getUserId())
                .projectName(projectEntity.getProjectName())
                .creationDate(projectEntity.getCreationDate())
                .completionPercent(projectEntity.getCompletionPercent())
                .totalTimeSpent(projectEntity.getTotalTimeSpent())
                .build();
    }
}
