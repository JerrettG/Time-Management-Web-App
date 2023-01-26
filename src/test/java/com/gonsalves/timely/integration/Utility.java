package com.gonsalves.timely.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gonsalves.timely.controller.model.ProjectCreateRequest;
import com.gonsalves.timely.controller.model.ProjectUpdateRequest;
import com.gonsalves.timely.controller.model.TaskCreateRequest;
import com.gonsalves.timely.controller.model.TaskUpdateRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

public class Utility {

    private final MockMvc mockMvc;

    private final ObjectMapper mapper;

    public ProjectClient projectClient;
    public TaskClient taskClient;

    public Utility(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
        this.mapper = new ObjectMapper();
        this.projectClient = new ProjectClient();
        this.taskClient = new TaskClient();
    }

    public class ProjectClient {
        public ResultActions getAllProjectsByUser(String userId) throws Exception {
            return mockMvc.perform(get("/api/v1/project/all?userId={userId}", userId)
                    .accept(MediaType.APPLICATION_JSON));
        }
        public ResultActions getProjectByProjectName(String userId, String projectName) throws Exception {
            return mockMvc.perform(get("/api/v1/project/?userId={userId}&projectName={projectName}", userId, projectName)
                    .accept(MediaType.APPLICATION_JSON));
        }

        public ResultActions createProject(ProjectCreateRequest request) throws Exception {
            return mockMvc.perform(post("/api/v1/project")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(request)));
        }
        public ResultActions updateProject(ProjectUpdateRequest request, boolean editName) throws Exception {
            return mockMvc.perform(put("/api/v1/project?editName={editName}", editName)
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(request)));
        }
        public ResultActions deleteProject(String userId, String projectName) throws Exception{
            return mockMvc.perform(delete("/api/v1/project?userId={userId}&projectName={projectName}", userId, projectName)
                    .accept(MediaType.APPLICATION_JSON));
        }
    }
    public class TaskClient {
        public ResultActions getAllTasksByProject(String projectId) throws Exception {
            return mockMvc.perform(get("/api/v1/task/all?projectId={projectId}", projectId)
                    .accept(MediaType.APPLICATION_JSON));
        }
        public ResultActions getTaskByTaskName(String projectId, String taskName) throws Exception {
            return mockMvc.perform(get("/api/v1/task/?projectId={projectId}&taskName={taskName}", projectId, taskName)
                    .accept(MediaType.APPLICATION_JSON));
        }

        public ResultActions createTask(TaskCreateRequest request) throws Exception {
            return mockMvc.perform(post("/api/v1/task")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(request)));
        }
        public ResultActions updateTask(TaskUpdateRequest request, boolean editName) throws Exception {
            return mockMvc.perform(put("/api/v1/task?editName={editName}", editName)
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(request)));
        }
        public ResultActions deleteTask(String projectId, String taskName) throws Exception{
            return mockMvc.perform(delete("/api/v1/task?projectId={projectId}&taskName={taskName}", projectId, taskName)
                    .accept(MediaType.APPLICATION_JSON));
        }
    }
}
