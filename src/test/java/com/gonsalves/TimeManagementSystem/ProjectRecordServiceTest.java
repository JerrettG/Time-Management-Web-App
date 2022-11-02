package com.gonsalves.TimeManagementSystem;

import com.gonsalves.TimeManagementSystem.repository.ProjectRepository;
import com.gonsalves.TimeManagementSystem.service.model.Project;
import com.gonsalves.TimeManagementSystem.service.model.Task;
import com.gonsalves.TimeManagementSystem.service.model.TimeLog;
import com.gonsalves.TimeManagementSystem.service.ProjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ProjectRecordServiceTest {

    @InjectMocks
    private ProjectService projectService;
    @Mock
    private ProjectRepository projectRepository;
    private final String EXISTING_USERNAME = "User";
    private final String EXISTING_PROJECT_NAME = "Test Project";
    private final Project EXISTING_PROJECT = new Project(EXISTING_USERNAME, EXISTING_PROJECT_NAME);

    @BeforeEach
    public void beforeEach() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void getProjectByProjectName_existingProjectForUser_returnsCorrectProject() {
        //GIVEN
        //WHEN
        when(projectRepository.loadProjectByProjectName(EXISTING_USERNAME, EXISTING_PROJECT_NAME)).thenReturn(Arrays.asList(EXISTING_PROJECT));
        Project result = projectService.getProjectByProjectName(EXISTING_USERNAME, EXISTING_PROJECT_NAME);
        //THEN
        assertEquals(EXISTING_PROJECT_NAME,result.getProjectName(), String.format("Expected to return Project with Project name: %s",result.getProjectName()));
    }
    @Test
    public  void getProjectByProjectName_notExistingProjectForUser_returnsNull() {
        //GIVEN
        String notExistingProjectName = UUID.randomUUID().toString();
        //WHEN
        when(projectRepository.loadProjectByProjectName(EXISTING_USERNAME, notExistingProjectName)).thenReturn(new ArrayList<>());
        Project result = projectService.getProjectByProjectName(EXISTING_USERNAME, notExistingProjectName);
        //THEN
        assertNull(result, "Expected result of getting nonexistent project to be null, but was not");
    }
    @Test
    public void getTaskByTaskName_existingTask_returnsCorrectTask() {
        //GIVEN
        Task existingTask = new Task();
        existingTask.setTaskName("Test task");
        EXISTING_PROJECT.getTasks().add(existingTask);
        //WHEN
        Task result = projectService.getTaskByTaskName(existingTask.getTaskName(), EXISTING_PROJECT);
        //THEN
        assertEquals(existingTask.getTaskName(), result.getTaskName(),
                String.format("Expected result of getTask to return a task with task name %s, but was %s", existingTask.getTaskName(), result.getTaskName()));
    }
    @Test
    public void getTaskByTaskName_notExistingTask_returnsNull() {
        //GIVEN
        String notExistingTaskName = UUID.randomUUID().toString();
        //WHEN
        Task result = projectService.getTaskByTaskName(notExistingTaskName, EXISTING_PROJECT);
        //THEN
        assertNull(result, "Expected result of getTask with nonexistent task name to be null, but was not");
    }
    @Test
    public void listUserProjects_existingProjects_returnsAllProjects() {
        //GIVEN
        List<Project> expectedUserProjects = new ArrayList<>(Arrays.asList(EXISTING_PROJECT));
        //WHEN
        when(projectRepository.loadAllProjectsByUsername(EXISTING_USERNAME)).thenReturn(expectedUserProjects);
        List<Project> result = projectService.listUserProjects(EXISTING_USERNAME);
        //THEN
        assertEquals(expectedUserProjects.size(), result.size(), String.format("Expected size of loaded projects to be %d, but was %d", expectedUserProjects.size(), result.size()));
    }
    @Test
    public void listUserProjects_noCurrentProjects_returnsEmptyList() {
        //GIVEN

        //WHEN
        when(projectRepository.loadAllProjectsByUsername(EXISTING_USERNAME)).thenReturn(new ArrayList<>());
        List<Project> result = projectService.listUserProjects(EXISTING_USERNAME);
        //THEN
        assertEquals(0, result.size(),String.format("Expected list user projects with no projects to return an empty list, but was size %d", result.size()));
    }
    @Test
    public void addProject_addsProjectToListOfUserProjects() {
        //GIVEN
        //WHEN
        doNothing().when(projectRepository).save(any());
        projectService.addProject(EXISTING_USERNAME, EXISTING_PROJECT_NAME, UUID.randomUUID().toString(),UUID.randomUUID().toString());
        //THEN
        verify(projectRepository).save(any());
    }
    @Test
    public void deleteProject_callsMapperDeleteMethod() {
        //GIVEN
        //WHEN
        when(projectRepository.loadProjectByProjectName(EXISTING_USERNAME, EXISTING_PROJECT_NAME)).thenReturn(Arrays.asList(EXISTING_PROJECT));
        doNothing().when(projectRepository).delete(EXISTING_PROJECT);
        projectService.deleteProject(EXISTING_USERNAME, EXISTING_PROJECT_NAME);
        //THEN
        verify(projectRepository).delete(EXISTING_PROJECT);
    }

    @Test
    public void editProjectName_existingProject_changesProjectNameInListOfProjects() {
        //GIVEN
        String oldProjectName = EXISTING_PROJECT_NAME;
        String changedName = UUID.randomUUID().toString();
        //WHEN
        when(projectRepository.loadProjectByProjectName(EXISTING_USERNAME, oldProjectName)).thenReturn(Arrays.asList(EXISTING_PROJECT));
        doNothing().when(projectRepository).updateData(eq(EXISTING_USERNAME), eq(oldProjectName), any(Project.class));
        projectService.editProjectName(EXISTING_USERNAME, oldProjectName, changedName);

        //THEN
        verify(projectRepository).updateData(EXISTING_USERNAME, oldProjectName, EXISTING_PROJECT);
    }

    @Test
    public void createNewTask_addsTaskToListOfTasksForProject() {
        //GIVEN
        Task task = new Task();
        String taskName = "Test Task";
        String notes = "Test notes";
        task.setTaskName(taskName);
        task.setNotes(notes);
        //WHEN
        when(projectRepository.loadProjectByProjectName(EXISTING_USERNAME, EXISTING_PROJECT_NAME)).thenReturn(Arrays.asList(EXISTING_PROJECT));
        projectService.createNewTask(EXISTING_USERNAME, EXISTING_PROJECT_NAME, taskName, notes);

        //THEN
        verify(projectRepository).updateData(EXISTING_USERNAME, EXISTING_PROJECT_NAME, EXISTING_PROJECT);
    }
    @Test
    public void deleteTask_existingTask_removesTaskFromListOfTasksForProject() {
        //GIVEN
        String existingTaskName = "Test task";
        String existingTaskNotes = "Test Notes";
        Task task = new Task();
        task.setTaskName(existingTaskName);
        task.setNotes(existingTaskNotes);
        int expected = EXISTING_PROJECT.getTasks().size();
        EXISTING_PROJECT.getTasks().add(task);
        //WHEN
        when(projectRepository.loadProjectByProjectName(EXISTING_USERNAME, EXISTING_PROJECT_NAME)).thenReturn(Arrays.asList(EXISTING_PROJECT));
        doNothing().when(projectRepository).updateData(EXISTING_USERNAME, EXISTING_PROJECT_NAME, EXISTING_PROJECT);
        projectService.deleteTask(EXISTING_USERNAME, EXISTING_PROJECT_NAME, existingTaskName);
        List<Task> tasks = EXISTING_PROJECT.getTasks();
        int result = tasks.size();
        //THEN
        assertEquals(expected, result, "Expected removal of existing task to remove task.");
    }
    @Test
    public void deleteTask_notExistingTask_noChangeToListOfUserProjects() {
        //GIVEN
        String existingTaskName = "Test task";
        String existingTaskNotes = "Test Notes";
        Task task = new Task();
        task.setTaskName(existingTaskName);
        task.setNotes(existingTaskNotes);
        EXISTING_PROJECT.getTasks().add(task);
        int expected = EXISTING_PROJECT.getTasks().size();

        //WHEN
        when(projectRepository.loadProjectByProjectName(EXISTING_USERNAME, EXISTING_PROJECT_NAME)).thenReturn(Arrays.asList(EXISTING_PROJECT));
        doNothing().when(projectRepository).updateData(EXISTING_USERNAME, EXISTING_PROJECT_NAME, EXISTING_PROJECT);
        projectService.deleteTask(EXISTING_USERNAME, EXISTING_PROJECT_NAME, UUID.randomUUID().toString());
        List<Task> tasks = EXISTING_PROJECT.getTasks();
        int result = tasks.size();
        //THEN
        assertEquals(expected, result, "Expected no change from removal of nonexistent task");
    }
    @Test
    public void editTaskName_existingTask_changesTaskNameInListOfTasks() {
        //GIVEN
        String existingTaskName = "Test task";
        String existingTaskNotes = "Test Notes";
        String changedName = "Changed name";
        String projectName = EXISTING_PROJECT_NAME;
        Task task = new Task();
        task.setTaskName(existingTaskName);
        task.setNotes(existingTaskNotes);
        EXISTING_PROJECT.getTasks().add(task);
        //WHEN
        when(projectRepository.loadProjectByProjectName(EXISTING_USERNAME, projectName)).thenReturn(Arrays.asList(EXISTING_PROJECT));
        projectService.editTaskName(EXISTING_USERNAME, EXISTING_PROJECT_NAME, existingTaskName, changedName);
        //THEN
        verify(projectRepository).updateData(EXISTING_USERNAME, EXISTING_PROJECT_NAME, EXISTING_PROJECT);
    }
    @Test
    public void editTaskName_nonexistentTask_noChange() {
        //GIVEN
        String changedName = "Changed Name";
        //WHEN
        when(projectRepository.loadProjectByProjectName(EXISTING_USERNAME, EXISTING_PROJECT_NAME)).thenReturn(Arrays.asList(EXISTING_PROJECT));
        projectService.editTaskName(EXISTING_USERNAME, EXISTING_PROJECT_NAME, UUID.randomUUID().toString(), changedName);
        //THEN
        verify(projectRepository, times(0)).updateData(EXISTING_USERNAME, EXISTING_PROJECT_NAME, EXISTING_PROJECT);
    }
    @Test
    public void markTaskAsComplete_taskMarkedAsCompleteAndCompletionStatusCorrect() {
        //GIVEN
        String existingTaskName = "Test Task";
        Task task = new Task();
        task.setTaskName(existingTaskName);
        EXISTING_PROJECT.getTasks().add(task);
        //WHEN
        when(projectRepository.loadProjectByProjectName(EXISTING_USERNAME, EXISTING_PROJECT_NAME)).thenReturn(Arrays.asList(EXISTING_PROJECT));
        doNothing().when(projectRepository).updateData(EXISTING_USERNAME, EXISTING_PROJECT_NAME, EXISTING_PROJECT);
        projectService.markTaskAsComplete(EXISTING_USERNAME, EXISTING_PROJECT_NAME, existingTaskName);
        boolean result = task.getCompletionStatus();
        //THEN
        assertTrue(result, "Expected completion status to be true after marking task complete");
    }
    @Test
    public void startTaskTime_noNonClosedOutTimeLogs_createsNewTimelog() {
        //GIVEN
        String existingTaskName = "Test Task";
        Task task = new Task();
        task.setTaskName(existingTaskName);
        EXISTING_PROJECT.getTasks().add(task);
        //WHEN
        when(projectRepository.loadProjectByProjectName(EXISTING_USERNAME, EXISTING_PROJECT_NAME)).thenReturn(Arrays.asList(EXISTING_PROJECT));
        projectService.startTaskTime(EXISTING_USERNAME, EXISTING_PROJECT_NAME, existingTaskName);
        //THEN
        verify(projectRepository).updateData(EXISTING_USERNAME, EXISTING_PROJECT_NAME, EXISTING_PROJECT);
    }
    @Test
    public void startTaskTime_nonClosedOutTimeLogs_noNewTimeLogCreated() {
        //GIVEN
        String existingTaskName = "Test Task";
        Task task = new Task();
        task.setTaskName(existingTaskName);
        TimeLog timeLog = new TimeLog();
        timeLog.start();
        task.getTimeLogs().add(timeLog);
        EXISTING_PROJECT.getTasks().add(task);
        //WHEN
        when(projectRepository.loadProjectByProjectName(EXISTING_USERNAME, EXISTING_PROJECT_NAME)).thenReturn(Arrays.asList(EXISTING_PROJECT));
        projectService.startTaskTime(EXISTING_USERNAME, EXISTING_PROJECT_NAME, existingTaskName);
        //THEN
        verify(projectRepository, times(0)).updateData(EXISTING_USERNAME, EXISTING_PROJECT_NAME, EXISTING_PROJECT);
    }
    @Test
    public void endTaskTime_recentLogStartTimeExistsEndTimeNull_createsNewEndTime() {
        //GIVEN
        String existingTaskName = "Test Task";
        Task task = new Task();
        task.setTaskName(existingTaskName);
        TimeLog timeLog = new TimeLog();
        timeLog.start();
        task.getTimeLogs().add(timeLog);
        EXISTING_PROJECT.getTasks().add(task);
        //WHEN
        when(projectRepository.loadProjectByProjectName(EXISTING_USERNAME, EXISTING_PROJECT_NAME)).thenReturn(Arrays.asList(EXISTING_PROJECT));
        projectService.endTaskTime(EXISTING_USERNAME, EXISTING_PROJECT_NAME, existingTaskName);
        //THEN
        verify(projectRepository).updateData(EXISTING_USERNAME, EXISTING_PROJECT_NAME, EXISTING_PROJECT);
    }
    @Test
    public void endTaskTime_noRecentLog_noEndTimeCreated() {
        //GIVEN
        String existingTaskName = "Test Task";
        Task task = new Task();
        task.setTaskName(existingTaskName);
        EXISTING_PROJECT.getTasks().add(task);
        //WHEN
        when(projectRepository.loadProjectByProjectName(EXISTING_USERNAME, EXISTING_PROJECT_NAME)).thenReturn(Arrays.asList(EXISTING_PROJECT));
        projectService.endTaskTime(EXISTING_USERNAME, EXISTING_PROJECT_NAME, existingTaskName);
        //THEN
        verify(projectRepository, times(0)).updateData(EXISTING_USERNAME, EXISTING_PROJECT_NAME, EXISTING_PROJECT);
    }
}
