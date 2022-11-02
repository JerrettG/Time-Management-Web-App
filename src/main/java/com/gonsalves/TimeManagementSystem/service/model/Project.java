package com.gonsalves.TimeManagementSystem.service.model;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Project {
    private String projectId;
    private String username;
    private String projectName;
    private List<Task> tasks;
    private int completionPercent;
    private String totalTimeContributed;

    private String creation_time;

    public Project(String username) {
        this.username = username;
        this.projectName = "";
        this.tasks = new ArrayList<>();
        this.completionPercent = 0;
        this.totalTimeContributed = "00:00:00";
    }
    public Project(String username, String projectName) {
        this.username = username;
        this.projectName = projectName;
        this.tasks = new ArrayList<>();
        this.completionPercent = 0;
        this.totalTimeContributed = "00:00:00";
    }
    public String getTotalTimeContributed() {
        this.calculateTotalTimeContributed();
        return this.totalTimeContributed;
    }
    public int getCompletionPercent() {
        this.calculateCompletionPercent();
        return this.completionPercent;
    }
    public void calculateCompletionPercent() {
        if (this.tasks.size()==0)
            this.completionPercent = 0;
        else {
            int completedTasks = 0;
            int totalTasks = this.tasks.size();
            for (Task task: this.tasks) {
                if (task.getCompletionStatus())
                    completedTasks++;
            }
            this.completionPercent = (int) ((double)completedTasks/ (double) totalTasks*100);}
    }

    public void calculateTotalTimeContributed() {
        long totalTimeInSeconds = 0;
        for (Task task : this.tasks) {
            totalTimeInSeconds += task.calculateTimeSpentOnTask();
        }
        long hours = Math.abs(totalTimeInSeconds/3600);
        long minutes = Math.abs(hours*60 - totalTimeInSeconds/60);
        long seconds = Math.abs(hours*3600  - minutes*60 - totalTimeInSeconds);

        this.totalTimeContributed= String.format("%dhrs %02dmins",hours,minutes);
    }

    @Override
    public String toString() {
        return "Project{" +
                "username='" + username + '\'' +
                ", projectName='" + projectName + '\'' +
                ", tasks=" + tasks +
                ", completionPercent=" + completionPercent +
                ", totalTimeContributed='" + totalTimeContributed + '\'' +
                '}';
    }
}