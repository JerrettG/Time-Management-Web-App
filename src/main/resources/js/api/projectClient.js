import BaseClass from "../../js/util/baseClass.js";

import axios from "axios";

/**
 * Client to call the cartService
 *
 *
 */
export default class ProjectClient extends BaseClass {

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

    async getAllProjectsByUser(userId, errorCallback) {
        try {
            const response = await this.client.get(`/api/v1/project/all?userId=${userId}`);
            return response.data;
        } catch (error) {
            this.handleError('getAllProjectsByUser', error, errorCallback);
        }
    }

    async getProjectByProjectName(userId, projectName, errorCallback) {
        try {
            const response = await this.client.get(`/api/v1/project?userId=${userId}&projectName=${projectName}`);
            return response.data;
        } catch (error) {
            this.handleError('getProjectByProjectName', error, errorCallback);
        }
    }

    async createProject(userId, projectName, errorCallback) {
        try {
            const response = await this.client.post(`/api/v1/project`,
                {
                    userId: userId,
                    projectName: projectName
                }
            )
            return response.data;
        } catch (error) {
            this.handleError('createProject', error, errorCallback)
        }
    }

    async editProjectName(userId, projectName, updatedProjectName, totalTimeSpent, completionPercent, errorCallback) {
        try {
            const response = await this.client.put(`/api/v1/project?editName=true`,
                    {
                        userId: userId,
                        projectName: projectName,
                        updatedProjectName: updatedProjectName,
                        totalTimeSpent: totalTimeSpent,
                        completionPercent: completionPercent
                    }
                )
            return response.data;
        } catch (error) {
            this.handleError('editProjectName', error, errorCallback)
        }
    }

    async updateProject(userId, projectName, totalTimeSpent, completionPercent, errorCallback) {
        try {
            const response = await this.client.put(`/api/v1/project`,
                {
                    userId: userId,
                    projectName: projectName,
                    totalTimeSpent: totalTimeSpent,
                    completionPercent: completionPercent
                }
            )
            return response.data;
        } catch (error) {
            this.handleError('updateProject', error, errorCallback)
        }
    }

    async deleteProject(userId, projectName, errorCallback) {
        try{
            return await this.client.delete(`/api/v1/project?userId=${userId}&projectName=${projectName}`);
        } catch (error) {
            this.handleError('deleteProject', error, errorCallback);
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