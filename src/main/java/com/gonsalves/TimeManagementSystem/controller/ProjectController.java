package com.gonsalves.TimeManagementSystem.controller;

import com.gonsalves.TimeManagementSystem.service.model.Project;
import com.gonsalves.TimeManagementSystem.service.model.Task;
import com.gonsalves.TimeManagementSystem.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
@Controller
@RequestMapping("/projects")
public class ProjectController {
    @Autowired
    private ProjectService projectService;

    @RequestMapping("/")
    public String userView(@AuthenticationPrincipal OidcUser principal, Model model) {
        String username = principal.getPreferredUsername();
        model.addAttribute("projects", projectService.listUserProjects(username));
        model.addAttribute("username", username);
        return "view";
    }
    /************** PROJECT METHODS ****************/
    @RequestMapping("/{projectName}")
    public String projectView(@AuthenticationPrincipal OidcUser principal,
                              @PathVariable("projectName") String projectName,
                              Model model) {
        String username = principal.getPreferredUsername();
        Project project = projectService.getProjectByProjectName(username, projectName);
        model.addAttribute("username",username);
        model.addAttribute("project", project);
        model.addAttribute("projectName", projectName);
        model.addAttribute("toDoTasks", project.getTasks());
        return "projectView";
    }

    @PostMapping("/createProject")
    public String createProject(@AuthenticationPrincipal OidcUser principal,
                                @RequestParam(name = "projectName") String projectName,
                                @RequestParam(name = "taskName") String taskName,
                                @RequestParam(name = "notes") String notes) {
        String username = principal.getPreferredUsername();
        projectService.addProject(username, projectName, taskName, notes);
        return "redirect:/projects/";
    }
    @RequestMapping("/{projectName}/delete")
    public String deleteProject(@PathVariable("projectName")String projectName,
                                @AuthenticationPrincipal OidcUser principal){
        String username = principal.getPreferredUsername();
        projectService.deleteProject(username, projectName);
        return "redirect:/projects/";
    }
    @PostMapping("/{projectName}/editProjectName")
    public String editProjectName(@AuthenticationPrincipal OidcUser principal,
                                  @PathVariable("projectName")String projectName,
                                  @ModelAttribute(name = "updatedProjectName") String updatedProjectName) {
        String username = principal.getPreferredUsername();
        projectService.editProjectName(username, projectName, updatedProjectName);
        return "redirect:/projects/";
    }

    /************** TASK METHODS ****************/
    @PostMapping("/{projectName}/createTask")
    public String createNewTask(@AuthenticationPrincipal OidcUser principal,
                                @PathVariable("projectName") String projectName,
                                @ModelAttribute(name = "taskName") String taskName,
                                @ModelAttribute(name = "notes") String notes){
        String username = principal.getPreferredUsername();
        projectService.createNewTask(username, projectName, taskName, notes);
        return String.format("redirect:/projects/%s",projectName);
    }

    @RequestMapping("/{projectName}/task/{taskName}")
    public String taskView(@AuthenticationPrincipal OidcUser principal,
                           @PathVariable("projectName") String projectName,
                           @PathVariable("taskName") String taskName,
                           @RequestParam(value = "status", required = true, defaultValue = "false") boolean status, Model model) {
        String username = principal.getPreferredUsername();
        Project project = projectService.getProjectByProjectName(username, projectName);
        Task task = projectService.getTaskByTaskName(taskName, project);
        model.addAttribute("task", task);
        model.addAttribute("projectName", projectName);
        model.addAttribute("status", status);
        model.addAttribute("notes", "");
        return "taskView";
    }

    @RequestMapping("/{projectName}/task/{taskName}/startTaskTime")
    public String startTaskTime(@AuthenticationPrincipal OidcUser principal,
                                @PathVariable("projectName")String projectName,
                                @PathVariable("taskName") String taskName,
                                Model model) {
        String username = principal.getPreferredUsername();
        projectService.startTaskTime(username, projectName, taskName);
        return String.format("redirect:/projects/%s/task/%s?status=true", projectName, taskName);

    }
    @RequestMapping("/{projectName}/task/{taskName}/endTaskTime")
    public String endTaskTime(@AuthenticationPrincipal OidcUser principal,
                              @PathVariable("projectName")String projectName,
                              @PathVariable("taskName") String taskName, Model model) {
        String username = principal.getPreferredUsername();
        projectService.endTaskTime(username, projectName, taskName);
        return String.format("redirect:/projects/%s/task/%s", projectName, taskName);
    }

    @RequestMapping("/{projectName}/task/{taskName}/delete")
    public String deleteTask(@AuthenticationPrincipal OidcUser principal,
                             @PathVariable("projectName")String projectName,
                             @PathVariable("taskName")String taskName){
        String username = principal.getPreferredUsername();
        projectService.deleteTask(username, projectName, taskName);
        return String.format("redirect:/projects/%s", projectName);
    }
    @RequestMapping("/{projectName}/task/{taskName}/markAsComplete")
    public String markTaskAsComplete(@AuthenticationPrincipal OidcUser principal,
                                     @PathVariable("projectName")String projectName,
                                     @PathVariable("taskName")String taskName){
        String username = principal.getPreferredUsername();
        projectService.markTaskAsComplete(username, projectName, taskName);
        return String.format("redirect:/projects/%s", projectName);
    }

    @PostMapping("/{projectName}/task/{taskName}/editTaskName")
    public String editTaskName(@AuthenticationPrincipal OidcUser principal,
                               @PathVariable("projectName")String projectName,
                               @PathVariable("taskName") String taskName,
                               @ModelAttribute(name = "updatedTaskName") String updatedTaskName)
    {
        String username = principal.getPreferredUsername();
        projectService.editTaskName(username, projectName, taskName, updatedTaskName);
        return String.format("redirect:/projects/%s", projectName);
    }
    @PostMapping("/{projectName}/task/{taskName}/richTextEditor")
    public String editTaskNotes(@AuthenticationPrincipal OidcUser principal,
                              @PathVariable("projectName")String projectName,
                              @PathVariable("taskName") String taskName,
                              @RequestParam(name = "notes")String notes) {
        String username = principal.getPreferredUsername();
        projectService.editTaskNotes(username, projectName, taskName, notes);
        return String.format("redirect:/projects/%s/%s", projectName, taskName);
    }
}
