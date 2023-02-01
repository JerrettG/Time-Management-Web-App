package com.gonsalves.timely.service;

import com.amazonaws.Response;
import com.gonsalves.timely.controller.model.TaskResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
@Component
public class TaskServiceClient {

    private RestTemplate restTemplate;

    public TaskServiceClient() {
        this.restTemplate = new RestTemplateBuilder()
            .rootUri("http://localhost:8080")
            .setConnectTimeout(Duration.ofSeconds(5))
            .build();
    }

    public List<Task> getAllTasksForProject(String projectId) {
        ResponseEntity<Task[]> response = restTemplate.getForEntity(String.format("/api/v1/task/all?projectId=%s", projectId), Task[].class);
        return Arrays.asList(response.getBody());
    }

    public void deleteAllTasksForProject(String projectId) {
        ResponseEntity<Task[]> response = restTemplate.getForEntity(String.format("/api/v1/task/all?projectId=%s", projectId), Task[].class);
        List<Task> tasks = Arrays.asList(response.getBody());

        tasks.forEach(task -> restTemplate.delete(
                        "/api/v1/task?projectId={projectId}&taskName={taskName}", projectId, task.getTaskName()));
    }



    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Task {
        private String projectId;
        private String taskName;
        private String notes;
        private String timeSpent;
        private String status;
    }

}
