import BaseClass from "../../js/util/baseClass.js";

import axios from "axios";

/**
 * Client to call the cartService
 *
 *
 */
export default class TaskClient extends BaseClass {

    constructor(props = {}) {
        super();
        const methodsToBind = []
        this.bindClassMethods(methodsToBind);
        this.props = props;
        this.clientLoaded(axios);
    }

    /**
     * Run any functions that are supposed to be called once the client has loaded successfully.
     */
    clientLoaded(client) {
        this.client = client;
        if (this.props.hasOwnProperty("onReady")){
            this.props.onReady();
        }
    }

    async getAllTasksForProject(projectId, errorCallback) {
        try {
            const response = await this.client.get(`/api/v1/task/all?projectId=${projectId}`);
            return response.data;
        } catch (error) {
            this.handleError('getAllTasksForProject', error, errorCallback);
        }
    }

    async getTaskByTaskName(projectId, taskName, errorCallback) {
        try {
            const response = await this.client.get(`/api/v1/task?projectId=${projectId}&taskName=${taskName}`);
            return response.data;
        } catch (error) {
            this.handleError('getTaskByTaskName', error, errorCallback);
        }
    }

    async createTask(projectId, taskName, notes, status = "Planned", errorCallback) {
        try {
            const response = await this.client.post(`/api/v1/task`,
                {
                    projectId: projectId,
                    taskName: taskName,
                    notes: notes,
                    status: status
                }
            )
            return response.data;
        } catch (error) {
            this.handleError('createTask', error, errorCallback)
        }
    }

    async editTaskName(projectId, taskName, updatedTaskName, notes, timeSpent, timeLogs, status, errorCallback) {
        try {
            const response = await this.client.put(`/api/v1/task?editName=true`,
                {
                    projectId: projectId,
                    taskName: taskName,
                    updatedTaskName: updatedTaskName,
                    notes: notes,
                    timeSpent: timeSpent,
                    timeLogs: timeLogs,
                    status: status
                }
            )
            return response.data;
        } catch (error) {
            this.handleError('editTaskName', error, errorCallback)
        }
    }

    async updateTask(projectId, taskName, updatedTaskName, notes, timeSpent, timeLogs, status, errorCallback) {
        try {
            const response = await this.client.put(`/api/v1/task`,
                {
                    projectId: projectId,
                    taskName: taskName,
                    notes: notes,
                    timeSpent: timeSpent,
                    timeLogs: timeLogs,
                    status: status
                }
            )
            return response.data;
        } catch (error) {
            this.handleError('updateTask', error, errorCallback)
        }
    }

    async deleteTask(projectId, taskName, errorCallback) {
        try{
            return await this.client.delete(`/api/v1/task?projectId=${projectId}&taskName=${taskName}`);
        } catch (error) {
            this.handleError('deleteTask', error, errorCallback);
        }
    }

    async startTaskTime(projectId, taskName, errorCallback) {
        try {
            const response = await this.client.put('/api/v1/task/start', {
                projectId: projectId,
                taskName: taskName
            });
            return response.data;
        } catch (error) {
            this.handleError('startTaskTime', error, errorCallback);
        }
    }

    async stopTaskTime(projectId, taskName, errorCallback) {
        try {
            const response = await this.client.put('/api/v1/task/stop', {
                projectId: projectId,
                taskName: taskName
            });
            return response.data;
        } catch (error) {
            this.handleError('stopTaskTime', error, errorCallback);
        }
    }


    /**
     * Helper method to log the error and run any error functions.
     * @param error The error received from the server.
     * @param errorCallback (Optional) A function to execute if the call fails.
     */
    handleError(method, error, errorCallback) {
        console.error(method + " failed - " + error);
        if (error.response.data.message !== undefined) {
            console.error(error.response.data.message);
        }
        if (errorCallback) {
            errorCallback(method + " failed - " + error);
        }
    }
}