package com.gonsalves.timely.controller;

import com.gonsalves.timely.controller.model.ProjectCreateRequest;
import com.gonsalves.timely.controller.model.ProjectResponse;
import com.gonsalves.timely.controller.model.ProjectUpdateRequest;
import com.gonsalves.timely.exception.ProjectAlreadyExistsException;
import com.gonsalves.timely.exception.ProjectNotFoundException;
import com.gonsalves.timely.service.ProjectService;
import com.gonsalves.timely.service.model.Project;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/project")
public class ProjectController {

    private final ProjectService projectService;

    @Autowired
    ProjectController(ProjectService projectService) {this.projectService = projectService;}


    @GetMapping("/all")
    public ResponseEntity<List<ProjectResponse>> getAllProjectsByUser(
            @RequestParam("userId")String userId){
        List<Project> projects = projectService.getAllProjectsByUser(userId);
        List<ProjectResponse> responses = projects.stream().map(this::convertToResponse).collect(Collectors.toList());
        return ResponseEntity.ok().body(responses);
    }



    @GetMapping("/")
    public ResponseEntity<ProjectResponse> getProjectByProjectName(
            @RequestParam("projectName")String projectName,
            @RequestParam("userId")String userId) {
        try {
            Project project = projectService.getProjectByProjectName(userId, projectName);
            ProjectResponse response = convertToResponse(project);
            return ResponseEntity.ok().body(response);
        } catch (ProjectNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(@RequestBody ProjectCreateRequest request) {
        Project project = Project.builder()
                .userId(request.getUserId())
                .projectName(request.getProjectName())
                .build();
        try {
            Project createdProject = projectService.createProject(project);
            ProjectResponse response = convertToResponse(createdProject);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (ProjectAlreadyExistsException e) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
    }
    //NOTE: projectName cannot be updated as it is a key value
    @PutMapping
    public ResponseEntity<ProjectResponse> updateProject(
            @RequestParam(value = "editName", defaultValue = "false")boolean editName,
            @RequestBody ProjectUpdateRequest request) {
        Project project = Project.builder()
                .userId(request.getUserId())
                .projectName(request.getProjectName())
                .totalTimeSpent(request.getTotalTimeSpent())
                .completionPercent(request.getCompletionPercent())
                .build();
        try {
            if (editName) {
                Project existingProject = projectService.getProjectByProjectName(request.getUserId(), request.getProjectName());
                projectService.deleteProject(existingProject);
                project.setProjectName(request.getUpdatedProjectName());
                projectService.createProject(project);
            } else {
                projectService.updateProject(project);
            }
            return ResponseEntity.accepted().build();
        } catch (ProjectNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping
    public ResponseEntity<ProjectResponse> deleteProject(
            @RequestParam("projectName")String projectName,
            @RequestParam("userId")String userId) {
        Project project = Project.builder()
                .userId(userId)
                .projectName(projectName)
                .build();
        try {
            projectService.deleteProject(project);
            return ResponseEntity.accepted().build();
        } catch (ProjectNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    private ProjectResponse convertToResponse(Project project) {
        return new ProjectResponse(
                project.getUserId(),
                project.getProjectName(),
                project.getProjectId(),
                project.getCreationDate(),
                project.getTotalTimeSpent(),
                project.getCompletionPercent()
        );
    }

}
