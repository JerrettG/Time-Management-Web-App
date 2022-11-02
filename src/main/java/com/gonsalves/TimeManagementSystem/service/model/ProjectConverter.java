package com.gonsalves.TimeManagementSystem.service.model;

import com.gonsalves.TimeManagementSystem.repository.model.ProjectRecord;

public class ProjectConverter {

    public static ProjectRecord convertToRecord(Project project) {
        return ProjectRecord.builder()
                .projectId(project.getProjectId())
                .username(project.getUsername())
                .projectName(project.getProjectName())
                .completionPercent(project.getCompletionPercent())
                .creation_time(project.getCreation_time())
                .totalTimeContributed(project.getTotalTimeContributed())
                .taskRecords(TaskListConverter.convertToRecord(project.getTasks()))
                .build();
    }

    public static Project convertFromRecord(ProjectRecord projectRecord) {
        return Project.builder()
                .projectId(projectRecord.getProjectId())
                .username(projectRecord.getUsername())
                .projectName(projectRecord.getProjectName())
                .tasks(TaskListConverter.convertFromRecord(projectRecord.getTaskRecords()))
                .creation_time(projectRecord.getCreation_time())
                .completionPercent(projectRecord.getCompletionPercent())
                .totalTimeContributed(projectRecord.getTotalTimeContributed())
                .build();
    }



}
