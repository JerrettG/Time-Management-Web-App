package com.gonsalves.timely.unit;

import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import com.gonsalves.timely.exception.TaskAlreadyExistsException;
import com.gonsalves.timely.exception.TaskNotFoundException;
import com.gonsalves.timely.repository.TaskRepository;
import com.gonsalves.timely.repository.model.ProjectEntity;
import com.gonsalves.timely.repository.model.TaskEntity;
import com.gonsalves.timely.repository.model.TimeLogEntity;
import com.gonsalves.timely.service.TaskService;
import com.gonsalves.timely.service.model.Task;
import com.gonsalves.timely.service.model.TimeLog;
import net.andreinc.mockneat.MockNeat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;


public class TaskServiceTest {

    @InjectMocks
    private TaskService taskService;
    @Mock
    private TaskRepository taskRepository;

    private final MockNeat mockNeat = MockNeat.threadLocal();

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    public void getAllTasksByProject() {
        String projectId = mockNeat.strings().valStr();
        String taskName = mockNeat.strings().valStr();
        String notes = mockNeat.strings().valStr();
        String timeSpent = mockNeat.localDates().valStr();
        List<TimeLogEntity> timeLogEntities = new ArrayList<>();
        String status = mockNeat.strings().valStr();
        TaskEntity entity = new TaskEntity(
                projectId,
                taskName,
                notes,
                timeSpent,
                timeLogEntities,
                status
        );
        List<TaskEntity> entities = new ArrayList<>(Arrays.asList(entity));


        when(taskRepository.getAllTasksByProjectId(projectId)).thenReturn(entities);

        List<Task> result = taskService.getAllTasksByProjectId(projectId);

        assertEquals(entities.size(), result.size(), "Expected method to return list of all tasks for project, but did not.");

    }

    @Test
    public void getTaskByTaskName() {
        String projectId = mockNeat.strings().valStr();
        String taskName = mockNeat.strings().valStr();
        String notes = mockNeat.strings().valStr();
        String timeSpent = mockNeat.localDates().valStr();
        List<TimeLogEntity> timeLogEntities = new ArrayList<>();
        String status = mockNeat.strings().valStr();
        TaskEntity entity = new TaskEntity(
                projectId,
                taskName,
                notes,
                timeSpent,
                timeLogEntities,
                status
        );

        when(taskRepository.getTaskByTaskName(projectId, taskName)).thenReturn(Optional.ofNullable(entity));

        Task result = taskService.getTaskByTaskName(projectId, taskName);

        assertEquals(entity.getProjectId(), result.getProjectId(), "Expected project id for task to match, but did not.");
        assertEquals(entity.getTaskName(), result.getTaskName(), "Expected task name for task to match, but did not.");
        assertEquals(entity.getNotes(), result.getNotes(), "Expected notes for task to match, but did not.");
        assertEquals(entity.getTimeSpent(), result.getTimeSpent(), "Expected time spent for task to match, but did not.");
        assertEquals(entity.getTimeLogEntities().size(), result.getTimeLogs().size(), "Expected time log size for task to match, but did not.");
        assertEquals(entity.getStatus(), result.getStatus(), "Expected status for task to match, but did not.");
    }

    @Test
    public void createTask_taskDoesNotExist_createsTask() {
        String projectId = mockNeat.strings().valStr();
        String taskName = mockNeat.strings().valStr();
        String notes = mockNeat.strings().valStr();
        String status = mockNeat.strings().valStr();

        Task task = new Task();
        task.setProjectId(projectId);
        task.setTaskName(taskName);
        task.setNotes(notes);
        task.setStatus(status);

        when(taskRepository.getTaskByTaskName(projectId, taskName)).thenReturn(null);
        doNothing().when(taskRepository).createTask(any(TaskEntity.class));

        Task result = taskService.createTask(task);

        String expectedTimeSpent = "00:00:00";
        List<TimeLog> expectedTimeLogs = new ArrayList<>();

        assertEquals(task.getProjectId(), result.getProjectId(), "Expected project id created for task to match, but did not.");
        assertEquals(task.getTaskName(), result.getTaskName(), "Expected task name created for task to match, but did not.");
        assertEquals(task.getNotes(), result.getNotes(), "Expected notes for created task to match, but did not.");
        assertEquals(expectedTimeSpent, result.getTimeSpent(), "Expected time spent for created task to be 00:00:00, but was not.");
        assertEquals(expectedTimeLogs, result.getTimeLogs(), "Expected time logs for created task to be an empty list, but was not.");
        assertEquals(status, result.getStatus(), "Expected status for created task to match, but did not.");
    }

    @Test
    public void createTask_taskExists_throwsTaskAlreadyExistsException() {
        String projectId = mockNeat.strings().valStr();
        String taskName = mockNeat.strings().valStr();
        String notes = mockNeat.strings().valStr();
        String status = mockNeat.strings().valStr();

        Task task = new Task();
        task.setProjectId(projectId);
        task.setTaskName(taskName);
        task.setNotes(notes);
        task.setStatus(status);

        when(taskRepository.getTaskByTaskName(projectId, taskName)).thenReturn(Optional.ofNullable(new TaskEntity()));
        doNothing().when(taskRepository).createTask(any(TaskEntity.class));

        assertThrows(TaskAlreadyExistsException.class, () -> taskService.createTask(task));

    }

    @Test
    public void updateTask_taskExists_updatesTask() {
        String projectId = mockNeat.strings().valStr();
        String taskName = mockNeat.strings().valStr();
        String notes = mockNeat.strings().valStr();
        String status = mockNeat.strings().valStr();

        Task task = new Task();
        task.setProjectId(projectId);
        task.setTaskName(taskName);
        task.setNotes(notes);
        task.setStatus(status);
        task.setTimeLogs(new ArrayList<>());

        doNothing().when(taskRepository).updateTask(any(TaskEntity.class));

        taskService.updateTask(task);

        verify(taskRepository).updateTask(any(TaskEntity.class));

    }

    @Test
    public void updateTask_taskDoesNotExist_throwsTaskNotFoundException() {
        String projectId = mockNeat.strings().valStr();
        String taskName = mockNeat.strings().valStr();
        String notes = mockNeat.strings().valStr();
        String status = mockNeat.strings().valStr();

        Task task = new Task();
        task.setProjectId(projectId);
        task.setTaskName(taskName);
        task.setNotes(notes);
        task.setStatus(status);
        task.setTimeLogs(new ArrayList<>());

        doThrow(ConditionalCheckFailedException.class).when(taskRepository).updateTask(any(TaskEntity.class));

        assertThrows(TaskNotFoundException.class, () -> taskService.updateTask(task));
    }

    @Test
    public void deleteTask_taskExists_deletesTask() {
        String projectId = mockNeat.strings().valStr();
        String taskName = mockNeat.strings().valStr();

        Task task = new Task();
        task.setProjectId(projectId);
        task.setTaskName(taskName);

        when(taskRepository.getTaskByTaskName(projectId, taskName)).thenReturn(Optional.ofNullable(new TaskEntity()));

        taskService.deleteTask(task);

        verify(taskRepository).deleteTask(any(TaskEntity.class));
    }

    @Test
    public void deleteTask_taskDoesNotExist_throwsTaskNotFoundException() {
        String projectId = mockNeat.strings().valStr();
        String taskName = mockNeat.strings().valStr();

        Task task = new Task();
        task.setProjectId(projectId);
        task.setTaskName(taskName);

        when(taskRepository.getTaskByTaskName(projectId, taskName)).thenReturn(null);

        assertThrows(TaskNotFoundException.class, () -> taskService.deleteTask(task));

    }
}
