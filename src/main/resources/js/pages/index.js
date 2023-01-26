import BaseClass from "../../js/util/baseClass.js";
import DataStore from "../../js/util/DataStore.js";;
import ProjectClient from "../api/projectClient";
import TaskClient from "../api/taskClient";
import '../../static/css/style.css';
/**
 * Logic needed for the shopping cart page
 */
class IndexPage extends BaseClass {

    constructor() {
        super();
        this.bindClassMethods([
            'renderProjects', 'onGetAllProjectsByUser',
            'onCreateProject', 'onDeleteProject', 'confirmDeletion',
            'toggleDropdown', 'confirmDeletion', 'showPopup',
            'closePopup', 'sortBy', 'clearSort'], this);
        this.dataStore = new DataStore();
    }

    async mount() {
        this.projectClient = new ProjectClient();
        this.taskClient = new TaskClient();
        this.dataStore.addChangeListener(this.renderProjects);
        await this.onGetAllProjectsByUser();
        document.getElementById("create-btn").addEventListener("click", (event) => {

            document.querySelector(".popups-container").innerHTML +=
                `
                <div class="create-form-container popup">
                    <button type="button" class="close-popup-btn">x</button>
                    <form class="create-form create-project-form">
                        <h3>Create a project</h3>
                        <div class="form-group">
                            <p>Project name</p>
                            <input required name="projectName" placeholder="Project name" id="projectName-input">
                        </div>
                        <div class="form-group" style="display:flex;flex-direction: row;">
                            <span>Add a task</span>
                            <button type="button" class="add-task-btn">+</button>
                        </div>
                        <div class="form-group">
                            <p>Task name</p>
                            <input required name="taskName" placeholder="Task name">
                        </div>
                        <button>Submit</button>
                    </form>
                </div>
                `;
            document.querySelector(".close-popup-btn").addEventListener("click", this.closePopup);
            document.querySelector(".create-form").addEventListener("submit", this.onCreateProject);
            this.showPopup(event);
        });
        document.getElementById("apply-sort-btn").addEventListener("click", this.sortBy);
        document.getElementById("clear-sort-btn").addEventListener("click", this.clearSort);
    }

    async renderProjects() {
        let projectDisplay = document.getElementById("display-table-data");
        const projects = this.dataStore.get("projects");
        let html = '';
        if (projects && projects.length > 0) {
            for (let project of projects) {
                html +=
                    `
                     <tr class="project">
                        <td class="project-projectName"><a href="/project/${project.projectName}">${project.projectName}</a></td>
                        <td class="project-completionPercent"><progress value="${project.completionPercent}" max="100" class="progress-bar"></progress></td>
                        <td class="project-totalTimeSpent">${project.totalTimeSpent}</td>
                        <td class="project-creationDate">${this.formatDate(project.creationDate)}</td>
                        <td><button type="button" class="delete-btn"><i data-popup=".confirmation-delete-container" class="fa fa-trash" style="color:indianred;cursor:pointer;" ></i></button></td>
                    </tr>
                    `
                projectDisplay.innerHTML = html;
            }
            document.querySelectorAll(".delete-btn").forEach(
                deleteBtn => deleteBtn.addEventListener("click", this.confirmDeletion));
        } else {
            document.querySelector(".projects-display").innerHTML =
                `
                    <h1>You don't have any projects yet!</h1>
                    <div class="create-form-container">
                        <span>Create a project</span>
                        <form class="create-form">
                            <div class="form-group">
                                <p>Project name</p>
                                <input name="projectName" placeholder="Project name" id="projectName-input">
                            </div>
                            <div class="form-group" style="display:flex;flex-direction: row;">
                                <span>Add a task</span>
                                <button type="button" class="add-task-btn">+</button>
                            </div>
                            <div class="form-group">
                                <p>Task name</p>
                                <input name="taskName" placeholder="Task name">
                            </div>
                            <button>Submit</button>
                        </form>
                    </div>
                `;
            document.querySelector(".create-form").addEventListener("submit", this.onCreateProject);
        }
    }

    async onGetAllProjectsByUser(event) {
        this.showLoading(document.getElementById("display-table-data"), 15);
        let result = await this.projectClient.getAllProjectsByUser(userId, this.errorHandler);
        this.dataStore.set("projects", result);
        if (result) {
            this.showMessage("Projects loaded successfully");
        } else {
            this.errorHandler("Error getting product catalog. Try again...");
        }
    }

    async onCreateProject(event) {
        event.preventDefault();

        const createForm = document.querySelector(".create-project-form");
        const formData = new FormData(createForm);

        const projectName = formData.get("projectName").trim();

        const taskNames = formData.getAll("taskName");
        let result = await this.projectClient.createProject(userId, projectName, this.errorHandler);
        if (result) {
            this.showMessage(`Project with name ${projectName} created successfully`);
            for (let taskName of taskNames) {
                let result = await this.taskClient.createTask(`${userId}_${projectName}`, taskName, "", "Planned", this.errorHandler);
                if (result) {
                    this.showMessage(`Task with name ${taskName} created successfully`);
                } else {
                    this.errorHandler("Error creating task. Try again....")
                }
            }
            this.closePopup(event);
            await this.onGetAllProjectsByUser();
        } else {
            this.errorHandler("Error creating project. Try again...")
        }
    }

    async onDeleteProject(event) {
        let deleteBtn = event.srcElement;

        let projectName = deleteBtn.getAttribute("data-projectName");
        let result = await this.projectClient.deleteProject(userId, projectName, this.errorHandler);

        if (result) {
            this.showMessage(`Project with name ${projectName} deleted successfully`);
            this.closePopup(event);
            await this.onGetAllProjectsByUser();
        } else {
            this.errorHandler("Error deleting project. Try again...");
        }
    }


    async confirmDeletion(event) {
        let projectName = event.srcElement.closest(".project").querySelector(".project-projectName").innerText;
        let popupsContainer = document.querySelector(".popups-container");
        popupsContainer.innerHTML +=
            `
            <div class="confirmation-delete-container popup">
                <div class="confirmation-delete">
                    <p>Are you sure you want to delete <strong>${projectName}?</strong></p>
                    <p>Once this action has been completed, it cannot be undone.</p>
                    <div class="form-group">
                        <p>Type the name of the project below to <strong>delete</strong></p>
                        <input type="text" data-projectName="${projectName}" placeholder="${projectName}">
                    </div>
                    <div class="form-group btns-container">
                        <button type="button" class="delete-btn" disabled data-projectName="${projectName}">delete</button>
                        <button type="button" class="cancel-btn">cancel</button>
                    </div>
                </div>
            </div>
            `;
        let deleteInput = popupsContainer.querySelector(".confirmation-delete input");
        deleteInput.addEventListener("keyup", function (event) {
            if (deleteInput.value === deleteInput.getAttribute("data-projectName")) {
                deleteInput.closest(".form-group").nextElementSibling.querySelector(".delete-btn").disabled = false;
            }
        });
        popupsContainer.querySelector(".confirmation-delete-container .delete-btn").addEventListener("click", this.onDeleteProject);
        popupsContainer.querySelector(".cancel-btn").addEventListener("click", this.closePopup);

        this.showPopup(event);
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
        let projects = this.dataStore.get("projects");
        let sortCategorySelect = document.getElementById("sortBy-category");
        let sortOrderSelect = document.getElementById("sortBy-order");

        let sortCategory = sortCategorySelect.options[sortCategorySelect.selectedIndex].getAttribute("data-sortBy");
        let sortOrder = sortOrderSelect.options[sortOrderSelect.selectedIndex].getAttribute("data-sortBy");
        if (sortOrder === "asc") {
            projects.sort((a, b) => {
                return a[sortCategory].localeCompare(b[sortCategory]);
            });
        } else {
            projects.sort((a, b) => {
                return b[sortCategory].localeCompare(a[sortCategory]);
            })
        }
        this.dataStore.set("projects", projects);
    }

    clearSort(event) {
        let projects = this.dataStore.get("projects");
        projects.sort((a,b) => a.projectName.localeCompare(b.projectName));
        this.dataStore.set("projects", projects);
    }
}


const main = async () => {
    const indexPage = new IndexPage();
    await indexPage.mount();
}

window.addEventListener('DOMContentLoaded', main);
