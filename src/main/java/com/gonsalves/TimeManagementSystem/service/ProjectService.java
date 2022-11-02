package com.gonsalves.TimeManagementSystem.service;

import com.gonsalves.TimeManagementSystem.repository.model.ProjectRecord;
import com.gonsalves.TimeManagementSystem.repository.model.TaskRecord;
import com.gonsalves.TimeManagementSystem.service.model.*;
import com.gonsalves.TimeManagementSystem.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProjectService {
    @Autowired
    private ProjectRepository projectRepository;


    public Project getProjectByProjectName(String username, String projectName) {
        List<ProjectRecord> results = projectRepository.loadProjectByProjectName(username, projectName);
        if (results.size() == 0)
            return null;
        return ProjectConverter.convertFromRecord(results.get(0));
    }
    public Task getTaskByTaskName(String taskName, Project project) {
        for (Task task : project.getTasks()) {
            if (task.getTaskName().equals(taskName))
                return task;
        }
        return null;
    }

    /**
     * Retrieves a list of all {@code Project} that are attached to the specified username.
     *
     * @param username the username of the logged-in user
     * @return a list of Project objects
     */
    public List<Project> listUserProjects(String username) {
        List<ProjectRecord> records = projectRepository.loadAllProjectsByUsername(username);
        List<Project> projectList = new ArrayList<>();
        records.forEach(record -> projectList.add(ProjectConverter.convertFromRecord(record)));

        return projectList;
    }

    /**
     * Creates a new {@code Project} item in the DynamoDB Table
     *
     * @param username the username of the logged-in user
     * @param projectName the project name of the project
     * @param taskName the task name of the first task for this project
     * @param notes the notes for the task of this project
     */
    public boolean addProject(String username, String projectName,String taskName, String notes) {
        if (getProjectByProjectName(username, projectName) == null) {
        Project project = Project.builder().username(username).projectName(projectName.strip()).build();
        Task task = new Task(taskName.strip(), notes);
        project.getTasks().add(task);
        projectRepository.save(ProjectConverter.convertToRecord(project));
        return true;
        }
        else
            return false;
    }
    public void deleteProject(String username, String projectName) {
        List<ProjectRecord> record = projectRepository.loadProjectByProjectName(username, projectName);
        if (record.size() > 0)
            projectRepository.delete(record.get(0));
    }
    public void editProjectName(String username, String oldProjectName, String updatedProjectName) {
        Project project = getProjectByProjectName(username, oldProjectName);
        project.setProjectName(updatedProjectName.strip());

        projectRepository.updateData(username, oldProjectName, ProjectConverter.convertToRecord(project));


    }

    /**
     * Creates a new task and adds it to the list of {@code Task} objects in the DynamoDB Table item with
     * project_name=projectName ONLY if there is not already a Task with the same taskName.
     *
     * @param username the username of the logged-in user
     * @param projectName the project name of the task
     * @param taskName the name of the task being created
     * @param notes the notes for the created task
     * @return true if task with taskName does not exist, false otherwise.
     */
    public boolean createNewTask(String username, String projectName, String taskName, String notes) {
        Project project = getProjectByProjectName(username, projectName);
        if (getTaskByTaskName(taskName.strip(), project) == null) {
            Task task = new Task(taskName.strip(), notes);
            project.getTasks().add(task);
            projectRepository.updateData(username, projectName, ProjectConverter.convertToRecord(project));
            return false;
        } else
            return true;
    }

    public void deleteTask(String username, String projectName, String taskName) {
        Project project = getProjectByProjectName(username, projectName);
        Task task = getTaskByTaskName(taskName, project);

        project.getTasks().remove(task);

        projectRepository.updateData(username, projectName, ProjectConverter.convertToRecord(project));
    }

    public void editTaskName(String username, String projectName, String oldTaskName, String updatedTaskName) {
        Project project = getProjectByProjectName(username, projectName);
        Task task = getTaskByTaskName(oldTaskName, project);
        if (task != null) {
            task.setTaskName(updatedTaskName);
            projectRepository.updateData(username,projectName, ProjectConverter.convertToRecord(project));
        }
    }
    public void editTaskNotes(String username, String projectName, String taskName, String notes) {
        Project project = getProjectByProjectName(username, projectName);
        Task task = getTaskByTaskName(taskName, project);
        task.setNotes(notes);

        projectRepository.updateData(username, projectName, ProjectConverter.convertToRecord(project));

    }

    /**
     * Sets the value of completionStatus for the {@code Task} specified by taskName. The completionPercent is
     * recalculated for the {@code Project}.
     *
     * @param username the username of logged-in user
     * @param projectName the project name of the task
     * @param taskName the task name of the task marked as complete
     */
    public void markTaskAsComplete(String username, String projectName, String taskName) {
        Project project = getProjectByProjectName(username, projectName);
        Task task = getTaskByTaskName(taskName, project);
        task.setCompletionStatus(true);
        project.calculateCompletionPercent();

        projectRepository.updateData(username, projectName, ProjectConverter.convertToRecord(project));
    }

    /**
     * Creates a new {@code TimeLog} if there are no TimeLogs that have a null end time. Does not create a TimeLog
     * if most recent TimeLog has a null end time.
     *
     * @param username the username of logged-in user
     * @param projectName the project name of the task
     * @param taskName the task name of the task whose time log is starting
     */
    public void startTaskTime(String username, String projectName, String taskName) {
        Project project = getProjectByProjectName(username, projectName);
        Task task = getTaskByTaskName(taskName, project);
        List<TimeLog> timeLogs = task.getTimeLogs();
        if (timeLogs.size() == 0 || timeLogs.get(timeLogs.size() - 1).getEndDateTime() != null) {
            TimeLog timeLog = new TimeLog();
            timeLog.start();
            timeLogs.add(timeLog);
            projectRepository.updateData(username, projectName, ProjectConverter.convertToRecord(project));
        }
    }

    /**
     * Finishes the most recent {@code TimeLog} that has a non-null start time and no end time. Does nothing if no
     * TimeLog exists or there is no TimeLog without an end time
     *
     * @param username the username of logged-in user
     * @param projectName the project name of the task
     * @param taskName the task name of the task whose time log is being ended
     */
    public void endTaskTime(String username, String projectName, String taskName) {
        Project project = getProjectByProjectName(username, projectName);
        Task task = getTaskByTaskName(taskName, project);
        List<TimeLog> timeLogs = task.getTimeLogs();
        if (timeLogs.size() >0) {
            TimeLog mostRecentTimeLog = timeLogs.get(timeLogs.size() - 1);
            if (mostRecentTimeLog.getStartDateTime() != null && mostRecentTimeLog.getEndDateTime() == null){
                mostRecentTimeLog.stop();
                projectRepository.updateData(username, projectName, ProjectConverter.convertToRecord(project));
            }
        }
    }

}
