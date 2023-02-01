import BaseClass from "../../js/util/baseClass.js";
import DataStore from "../../js/util/DataStore.js";;
import ProjectClient from "../api/projectClient";
import TaskClient from "../api/taskClient";
import '../../static/css/style.css';


/**
 * Logic needed for the shopping cart page
 */
class ProjectPage extends BaseClass {

    constructor() {
        super();
        this.bindClassMethods([
            'renderTasks','onGetAllTasksForProject',
            'onUpdateTaskStatus', 'onCreateTask', 'onDeleteTask', 'onEditTaskName', 'onDisplayTaskInfo',
            'onStartTaskTimer', 'onStopTaskTimer', 'confirmDeletion', 'onUpdateTask',
            'toggleDropdown', 'showPopup', 'closePopup','sortBy', 'clearSort'], this);
        this.dataStore = new DataStore();
    }

    async mount() {
        this.projectClient = new ProjectClient();
        this.taskClient = new TaskClient();
        let pathname = window.location.pathname;
        let projectName = decodeURIComponent(pathname.substring(pathname.lastIndexOf("/")+1, pathname.length));
        this.projectName = projectName;
        this.projectId = `${userId}_${projectName}`;
        this.createTaskForm = `
                    <form class="create-form form" id="create-task-form">
                        <h3>Create a task</h3>
                        <div class="form-group">
                            <p>Task name</p>
                            <input required name="taskName" placeholder="Task name" id="taskName-input">
                        </div>
                        <div class="form-group">
                            <p>Status</p>
                            <select required name="status" id="status-input">
                                <option value="Planned">Planned</option>
                                <option value="In progress">In progress</option>
                                <option value="Under review">Under review</option>
                                <option value="Complete">Complete</option>
                            </select>
                        </div>
                        <div class="form-group">
                            <p>Notes</p>
                            <textarea form="create-task-form" name="notes" placeholder="Notes..." class="richTextEditor"></textarea>
                        </div>
                        <button>Submit</button>
                    </form> `
        document.getElementById("create-btn").addEventListener("click", event => {
            document.querySelector(".popups-container").innerHTML += `
               <div class="create-form-container popup">
                    <button type="button" class="close-popup-btn">x</button>
                    ${this.createTaskForm}
                </div>
            `;
            tinymce.init({
                selector: '.richTextEditor'
            });
            document.querySelector(".create-form").addEventListener("submit", this.onCreateTask);
            document.querySelector(".close-popup-btn").addEventListener("click", this.closePopup);
            this.showPopup(event);
        });
        document.getElementById("apply-sort-btn").addEventListener("click", this.sortBy);
        document.getElementById("clear-sort-btn").addEventListener("click", this.clearSort);
        this.dataStore.addChangeListener(this.renderTasks);
        await this.onGetAllTasksForProject();
    }

    async renderTasks() {
        let tasksDisplay = document.getElementById("display-table-data");
        const tasks = this.dataStore.get("tasks");
        let html = '';
        if (tasks && tasks.length > 0) {
            for (let task of tasks) {
                let statuses = ["Planned", "In progress", "Under review", "Complete"];
                let options = '';
                statuses.forEach(
                    status =>
                        options += (status === task.status) ? `<option value="${status}" selected>${status}</option>`
                            : `<option value="${status}">${status}</option>`);

                html +=
                    `
                     <tr class="task">
                        <td class="task-taskName" data-taskName="${task.taskName}" data-popup=".taskInfo-container">${task.taskName}</td>
                        <td class="task-timeSpent">${task.timeSpent}</td>
                        <td class="task-status">
                            <div class="select-container">
                                <select name="status">
                                    ${options}
                                </select>
                                <i class="fa-solid fa-caret-down"></i>
                            </div>
                        </td>
                        <td>
                            <button type="button" class="start-btn">Start</button>
                            <button type="button" class="stop-btn">Stop</button>
                            <button type="button" class="delete-btn"><i data-popup=".confirmation-delete-container" class="fa fa-trash" style="color:indianred;cursor:pointer;" ></i></button>
                        </td>
                     </tr>
                    `
            }
            tasksDisplay.innerHTML = html;
            document.querySelectorAll(".start-btn").forEach(
                startBtn => startBtn.addEventListener("click", this.onStartTaskTimer)
            );
            document.querySelectorAll(".stop-btn").forEach(
                stopBtn => stopBtn.addEventListener("click", this.onStopTaskTimer)
            );
            document.querySelectorAll(".delete-btn").forEach(
                deleteBtn => deleteBtn.addEventListener("click", this.confirmDeletion));
            document.querySelectorAll(".task-taskName").forEach(
                taskName => {
                    taskName.addEventListener("focusout", this.onEditTaskName);
                    taskName.addEventListener("click", this.onDisplayTaskInfo);
                });
            document.querySelectorAll('.task-status select').forEach(
                statusSelect => statusSelect.addEventListener("change", this.onUpdateTaskStatus));
        } else {
            document.querySelector(".display").innerHTML =
                `
                    <h1>You don't have any tasks yet!</h1>
                    ${this.createTaskForm}
                `;
            tinymce.init({selector: '.richTextEditor'});
            document.querySelector(".create-form").addEventListener("submit", this.onCreateTask);
        }
    }

    async onGetAllTasksForProject(event) {
        this.showLoading(document.getElementById("display-table-data"), 15);
        let result = await this.taskClient.getAllTasksForProject(this.projectId, this.errorHandler);
        this.dataStore.set("tasks", result);
        if (result) {
            // this.showMessage("Projects loaded successfully");
        } else {
            this.errorHandler("Error getting product catalog. Try again...");
        }
    }

    async onCreateTask(event) {
        event.preventDefault();
        const createForm = event.srcElement;

        const formData = new FormData(createForm);

        const taskName = formData.get("taskName").trim();
        const notes = formData.get("notes");
        const status = formData.get("status");

        let result = await this.taskClient.createTask(this.projectId, taskName, notes, status, this.errorHandler);
        if (result) {
            this.projectClient.updateProject(userId, this.projectName, this.errorHandler);
            // this.showMessage(`Task with name ${taskName} created successfully`);
            if (createForm.id === "create-task-form-popup") {
                this.closePopup(event);
            }
            window.location.reload();
        } else {
            this.errorHandler("Error creating task. Try again...")
        }
    }

    async onDeleteTask(event) {
        let deleteBtn = event.srcElement;
        let taskName = deleteBtn.getAttribute("data-taskName");
        let result = await this.taskClient.deleteTask(this.projectId, taskName, this.errorHandler);
        if (result) {
            this.projectClient.updateProject(userId, this.projectName, this.errorHandler);
            // this.showMessage(`Task with ${taskName} has been deleted successfully`);
            this.closePopup(event);
            await this.onGetAllTasksForProject();
        } else {
            this.errorHandler("Error deleting task. Try again...");
        }
    }

    async confirmDeletion(event) {
        let taskName = event.srcElement.closest(".task").querySelector(".task-taskName").getAttribute("data-taskName");
        let popupsContainer = document.querySelector(".popups-container");
        popupsContainer.innerHTML +=
            `
            <div class="confirmation-delete-container popup">
                <div class="confirmation-delete form">
                    <p>Are you sure you want to delete <strong>${taskName}?</strong></p>
                    <p>Once this action has been completed, it cannot be undone.</p>
                    <div class="form-group btns-container">
                        <button type="button" class="delete-btn" data-taskName="${taskName}">delete</button>
                        <button type="button" class="cancel-btn">cancel</button>
                    </div>
                </div>
            </div>
            `;
        popupsContainer.querySelector(".confirmation-delete-container .delete-btn").addEventListener("click", this.onDeleteTask);
        popupsContainer.querySelector(".cancel-btn").addEventListener("click", this.closePopup);

        this.showPopup(event);
    }

    async onUpdateTaskStatus(event) {
        let statusSelect = event.srcElement;
        let status = statusSelect[statusSelect.selectedIndex].getAttribute("value");
        let taskName = statusSelect.closest(".task").querySelector(".task-taskName").getAttribute("data-taskName");

        let result = await this.taskClient.updateTaskStatus(this.projectId, taskName, status, this.errorHandler);
        if (result) {
            await this.projectClient.updateProject(userId, this.projectName, this.errorHandler);
            // this.showMessage(`Task with name ${taskName} status was updated successfully`);
        } else {
            this.errorHandler("Error updating task status. Try again...");
        }
    }

    async onEditTaskName(event) {
        let taskName = event.srcElement;
        taskName.contentEditable = false;
        let previousTaskName = taskName.getAttribute("data-taskName");
        let updatedTaskName = taskName.innerText.trim();
        taskName.innerText = updatedTaskName;

        if (previousTaskName !== updatedTaskName) {
            let result = await this.taskClient.editTaskName(this.projectId, previousTaskName, updatedTaskName, this.errorHandler);
            if (result) {
                taskName.setAttribute("data-taskName", updatedTaskName);
                // this.showMessage("Task name updated successfully!");
            } else {
                this.errorHandler("Error updating task name. Try again...");
            }
        }
    }

    async onDisplayTaskInfo(event) {
        let taskName = event.target.getAttribute("data-taskName");
        let task = await this.taskClient.getTaskByTaskName(this.projectId, taskName);

        let popupsContainer = document.querySelector(".popups-container");
        popupsContainer.innerHTML =
            `
             <div class="taskInfo-container popup">
                <button type="button" class="close-popup-btn">x</button>
                <form data-taskName="${taskName}" class="task-info form" id="task-info">
                    <div class="form-group">
                        <p>Task name</p>
                        <input name="taskName" value="${taskName}"> 
                    </div>
                    <div class="form-group">
                        <p>Notes</p>
                        <textarea name="notes" class="richTextEditor">${task.notes ? task.notes : ""}</textarea>
                    </div>
                    <button type="submit" id="apply-changes">Apply</button>
                </form>
            </div>
            `;
        tinymce.init({
            selector: '.richTextEditor'
        });
        document.querySelector(".close-popup-btn").addEventListener("click", this.closePopup);
        document.querySelector("#task-info").addEventListener("submit", this.onUpdateTask);
        this.showPopup(event);
    }

    async onUpdateTask(event) {
        event.preventDefault();
        let taskInfo = event.target;
        let formData = new FormData(taskInfo);
        let previousTaskName = taskInfo.getAttribute("data-taskName");
        let currentTaskName = formData.get("taskName");
        let notes = formData.get("notes");
        if (currentTaskName !== previousTaskName) {
            let result = await this.taskClient.editTaskName(this.projectId, previousTaskName, currentTaskName, this.errorHandler);
            if (result) {
                // this.showMessage(`Task with name ${previousTaskName} updated name successfully`);
            } else {
                this.errorHandler("Error updating task name. Try again...");
            }
        }

        let result = await this.taskClient.updateTaskNotes(this.projectId, currentTaskName, notes, this.errorHandler);
        if (result) {
            // this.showMessage(`Task with name ${currentTaskName} notes updated successfully}`);
            this.closePopup(event);
            this.onGetAllTasksForProject();
        } else {
            this.errorHandler("Error trying to update notes. Try again...");
        }
    }

    async onStartTaskTimer(event) {
        let startBtn = event.srcElement;
        let task = startBtn.closest(".task");
        let taskName = task.querySelector(".task-taskName").innerText;

        let result = this.taskClient.startTaskTime(this.projectId, taskName, this.errorHandler);
        if (result) {
            let statusSelect = task.querySelector(".task-status select");
            statusSelect[statusSelect.selectedIndex].removeAttribute("selected");
            statusSelect[1].setAttribute("selected", true);
            // this.showMessage(`Task with name ${taskName} has been started`);
        } else {
            this.errorHandler("Error starting task. Try again...");
        }
    }

    async onStopTaskTimer(event) {
        let stopBtn = event.srcElement;
        let task = stopBtn.closest(".task");
        let taskName = task.querySelector(".task-taskName").innerText;

        let result = await this.taskClient.stopTaskTime(this.projectId, taskName, this.errorHandler);
        if (result) {
            result = this.projectClient.updateProject(userId, this.projectName, this.errorHandler);
            if (result)
                // this.showMessage("Project time has been updated");
            // this.showMessage(`Task with name ${taskName} has been stopped`);
            await this.onGetAllTasksForProject();
        } else {
            this.errorHandler("Error stopping task. Try again...");
        }
    }


    showPopup(event) {
        let btn = event.srcElement;
        let popup = btn.getAttribute("data-popup");
        document.querySelector(popup).style.display = "block";
        document.querySelector(".popup-screen").style.display = "block";
    }

    closePopup(event) {
        let btn = event.srcElement;
        let popup = btn.closest(".popup");
        popup.remove();
        document.querySelector(".popup-screen").style.display = "none";
    }

    sortBy(event) {
        let tasks = this.dataStore.get("tasks");
        let sortCategorySelect = document.getElementById("sortBy-category");
        let sortOrderSelect = document.getElementById("sortBy-order");

        let sortCategory = sortCategorySelect.options[sortCategorySelect.selectedIndex].getAttribute("data-sortBy");
        let sortOrder = sortOrderSelect.options[sortOrderSelect.selectedIndex].getAttribute("data-sortBy");
        if (sortOrder === "asc") {
            tasks.sort((a, b) => {
                return a[sortCategory].localeCompare(b[sortCategory]);
            });
        } else {
            tasks.sort((a, b) => {
                return b[sortCategory].localeCompare(a[sortCategory]);
            });
        }
        this.dataStore.set("tasks", tasks);
    }

    clearSort(event) {
        let tasks = this.dataStore.get("tasks");
        tasks.sort((a,b) => a.taskName.localeCompare(b.taskName));
        this.dataStore.set("tasks", tasks);
    }
}


const main = async () => {
    const projectPage = new ProjectPage();
    await projectPage.mount();
}

window.addEventListener('DOMContentLoaded', main);
