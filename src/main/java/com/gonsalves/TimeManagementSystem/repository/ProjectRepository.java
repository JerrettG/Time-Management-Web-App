package com.gonsalves.TimeManagementSystem.repository;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBSaveExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ExpectedAttributeValue;
import com.gonsalves.TimeManagementSystem.repository.model.ProjectRecord;
import com.gonsalves.TimeManagementSystem.service.model.Project;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class ProjectRepository {

    @Autowired
    private DynamoDBMapper dynamoDBMapper;

    public void save(ProjectRecord project) {
        dynamoDBMapper.save(project);
    }

    /**
     * Uses a query on the global secondary index of the DynamoDB table with PK=owner_username and SK=project_name.
     *
     * @param username the username of logged-in user
     * @param projectName the project name to be loaded
     * @return the project that was loaded
     */

    public List<ProjectRecord> loadProjectByProjectName(String username, String projectName) {
        Map<String, AttributeValue> expressionAttributeValues = new HashMap<String, AttributeValue>();
        expressionAttributeValues.put(":owner_username", new AttributeValue(username));
        expressionAttributeValues.put(":project_name", new AttributeValue(projectName));

        DynamoDBQueryExpression<ProjectRecord> queryExpression = new DynamoDBQueryExpression<ProjectRecord>()
                .withIndexName("owner_username-project_name-index")
                .withKeyConditionExpression("owner_username = :owner_username and project_name = :project_name")
                .withExpressionAttributeValues(expressionAttributeValues)
                .withConsistentRead(false);

        return dynamoDBMapper.query(ProjectRecord.class, queryExpression);
    }

    /**
     * Queries the GSI of the DynamoDB table with PK=owner_username and SK=project_name using just the PK. Results are
     * all the {@code Project}s attached to the user.
     *
     * @param username the username of logged-in user
     * @return the list of results from the query
     */
    public List<ProjectRecord> loadAllProjectsByUsername(String username) {
        ProjectRecord project = new ProjectRecord();
        project.setUsername(username);
        DynamoDBQueryExpression<ProjectRecord> queryExpression = new DynamoDBQueryExpression<ProjectRecord>()
                .withIndexName("owner_username-project_name-index")
                .withHashKeyValues(project)
                .withConsistentRead(false);

       return dynamoDBMapper.query(ProjectRecord.class, queryExpression);
    }

    public void delete(ProjectRecord project) {
        dynamoDBMapper.delete(project);
    }

    /**
     * Uses a DynamoDB save expression to only save the data on the item that has values matching owner_username and
     * project_name.
     *
     * @param username the username of logged-in user
     * @param projectName the name of the project being updated
     * @param projectRecord the updated {@code Project} whose data will overrwrite existing data
     */
    public void updateData(String username, String projectName, ProjectRecord projectRecord) {
        dynamoDBMapper.save(projectRecord,
                new DynamoDBSaveExpression()
                        .withExpectedEntry(
                                "owner_username",
                                new ExpectedAttributeValue(
                                        new AttributeValue(username)))
                        .withExpectedEntry(
                        "project_name",
                                new ExpectedAttributeValue(
                                    new AttributeValue(projectName)
                        )
                    )
        );
    }

}
