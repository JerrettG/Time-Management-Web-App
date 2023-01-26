package com.gonsalves.timely.unit;

import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import com.gonsalves.timely.exception.ProjectAlreadyExistsException;
import com.gonsalves.timely.exception.ProjectNotFoundException;
import com.gonsalves.timely.repository.ProjectRepository;
import com.gonsalves.timely.repository.model.ProjectEntity;
import com.gonsalves.timely.repository.model.TimeLogEntity;
import com.gonsalves.timely.service.ProjectService;
import com.gonsalves.timely.service.model.Project;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

public class ProjectServiceTest {


    @InjectMocks
    private ProjectService projectService;
    @Mock
    private ProjectRepository projectRepository;

    private final MockNeat mockNeat = MockNeat.threadLocal();

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    public void getAllProjectsByUser() {
        String userId = mockNeat.strings().valStr();
        String projectName = mockNeat.strings().valStr();
        String projectId = mockNeat.strings().valStr();
        String creationDate = mockNeat.localDates().valStr();
        String totalTimeSpent = mockNeat.localDates().toString();
        Integer completionPercent = mockNeat.ints().range(0,100).val();
        ProjectEntity entity = new ProjectEntity(
                userId,
                projectName,
                projectId,
                creationDate,
                totalTimeSpent,
                completionPercent
        );
        List<ProjectEntity> entities = new ArrayList<>(Arrays.asList(entity));


        when(projectRepository.getAllProjectsByUser(userId)).thenReturn(entities);

        List<Project> result = projectService.getAllProjectsByUser(userId);

        assertEquals(entities.size(), result.size(), "Expected method to return list of all projects for project, but did not.");

    }

    @Test
    public void getProjectByProjectName() {
        String userId = mockNeat.strings().valStr();
        String projectName = mockNeat.strings().valStr();
        String projectId = mockNeat.strings().valStr();
        String creationDate = mockNeat.localDates().valStr();
        String totalTimeSpent = mockNeat.localDates().toString();
        Integer completionPercent = mockNeat.ints().range(0,100).val();
        ProjectEntity entity = new ProjectEntity(
                userId,
                projectName,
                projectId,
                creationDate,
                totalTimeSpent,
                completionPercent
        );

        when(projectRepository.getProjectByProjectName(userId, projectName)).thenReturn(entity);

        Project result = projectService.getProjectByProjectName(userId, projectName);

        assertEquals(entity.getUserId(), result.getUserId(), "Expected user id for project to match, but did not.");
        assertEquals(entity.getProjectName(), result.getProjectName(), "Expected project name for project to match, but did not.");
        assertEquals(entity.getProjectId(), result.getProjectId(), "Expected projectId for project to match, but did not.");
        assertEquals(entity.getTotalTimeSpent(), result.getTotalTimeSpent(), "Expected total time spent for project to match, but was not.");
        assertEquals(entity.getCompletionPercent(), result.getCompletionPercent(), "Expected completion percent for project to match, but was not.");
    }

    @Test
    public void createProject_projectDoesNotExist_createsProject() {
        String userId = mockNeat.strings().valStr();
        String projectName = mockNeat.strings().valStr();

        Project project = new Project();
        project.setUserId(userId);
        project.setProjectName(projectName);

        when(projectRepository.getProjectByProjectName(userId, projectName)).thenReturn(null);
        doNothing().when(projectRepository).createProject(any(ProjectEntity.class));

        Project result = projectService.createProject(project);

        String expectedTotalTimeSpent = "00:00:00";
        Integer expectedCompletionPercent = 0;
        String expectedProjectId = String.format("%s_%s", userId, projectName);


        assertEquals(project.getUserId(), result.getUserId(), "Expected user id for project to match, but did not.");
        assertEquals(project.getProjectName(), result.getProjectName(), "Expected project name for created project to match, but did not.");
        assertEquals(expectedProjectId, result.getProjectId(), "Expected projectId for created project to match, but did not.");
        assertEquals(expectedTotalTimeSpent, result.getTotalTimeSpent(), "Expected total time spent for created project to be 00:00:00, but was not.");
        assertEquals(expectedCompletionPercent, result.getCompletionPercent(), "Expected completion percent for created project to be 0, but was not.");

    }

    @Test
    public void createProject_projectExists_throwsProjectAlreadyExistsException() {
        String userId = mockNeat.strings().valStr();
        String projectName = mockNeat.strings().valStr();

        Project project = new Project();
        project.setUserId(userId);
        project.setProjectName(projectName);

        when(projectRepository.getProjectByProjectName(userId, projectName)).thenReturn(new ProjectEntity());
        doNothing().when(projectRepository).createProject(any(ProjectEntity.class));

        assertThrows(ProjectAlreadyExistsException.class, () -> projectService.createProject(project));

    }

    @Test
    public void updateProject_projectExists_updatesProject() {
        String userId = mockNeat.strings().valStr();
        String projectName = mockNeat.strings().valStr();
        String projectId = mockNeat.strings().valStr();
        String creationDate =  mockNeat.strings().valStr();
        String updatedTotalTimeSpent = mockNeat.localDates().valStr();
        Integer completionPercent = mockNeat.ints().range(0,100).val();

        Project project = new Project(
                userId,
                projectName,
                projectId,
                creationDate,
                updatedTotalTimeSpent,
                completionPercent
        );


        doNothing().when(projectRepository).updateProject(any(ProjectEntity.class));

        projectService.updateProject(project);
        verify(projectRepository).updateProject(any(ProjectEntity.class));
    }

    @Test
    public void updateProject_projectDoesNotExist_throwsProjectNotFoundException() {
        String userId = mockNeat.strings().valStr();
        String projectName = mockNeat.strings().valStr();
        String projectId = mockNeat.strings().valStr();
        String creationDate =  mockNeat.strings().valStr();
        String updatedTotalTimeSpent = mockNeat.localDates().valStr();
        Integer completionPercent = mockNeat.ints().range(0,100).val();

        Project project = new Project(
                userId,
                projectName,
                projectId,
                creationDate,
                updatedTotalTimeSpent,
                completionPercent
        );

        doThrow(ConditionalCheckFailedException.class).when(projectRepository).updateProject(any(ProjectEntity.class));

        assertThrows(ProjectNotFoundException.class, () -> projectService.updateProject(project));
    }

    @Test
    public void deleteProject_projectExists_deletesProject() {
        String userId = mockNeat.strings().valStr();
        String projectName = mockNeat.strings().valStr();

        Project project = new Project();
        project.setUserId(userId);
        project.setProjectName(projectName);

        when(projectRepository.getProjectByProjectName(userId, projectName)).thenReturn(new ProjectEntity());

        projectService.deleteProject(project);

        verify(projectRepository).deleteProject(any(ProjectEntity.class));
    }

    @Test
    public void deleteProject_projectDoesNotExist_throwsProjectNotFoundException() {
        String userId = mockNeat.strings().valStr();
        String projectName = mockNeat.strings().valStr();

        Project project = new Project();
        project.setUserId(userId);
        project.setProjectName(projectName);

        when(projectRepository.getProjectByProjectName(userId, projectName)).thenReturn(null);

        assertThrows(ProjectNotFoundException.class, () -> projectService.deleteProject(project));

    }
}
