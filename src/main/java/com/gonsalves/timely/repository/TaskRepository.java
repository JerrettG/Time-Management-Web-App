package com.gonsalves.timely.repository;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBSaveExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ExpectedAttributeValue;
import com.gonsalves.timely.repository.model.TaskEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class TaskRepository {

    private final DynamoDBMapper mapper;

    @Autowired
    TaskRepository(DynamoDBMapper mapper) {this.mapper = mapper;}

    public List<TaskEntity> getAllTasksByProjectId(String projectId) {
        TaskEntity entity = new TaskEntity();
        entity.setProjectId(projectId);
        DynamoDBQueryExpression<TaskEntity> queryExpression = new DynamoDBQueryExpression<TaskEntity>()
                .withHashKeyValues(entity)
                .withConsistentRead(false);
        return mapper.query(TaskEntity.class, queryExpression);
    }

    public Optional<TaskEntity> getTaskByTaskName(String projectId, String taskName) {
        return Optional.ofNullable(mapper.load(TaskEntity.class, projectId, taskName));
    }

    public void createTask(TaskEntity entity) {
        mapper.save(entity);
    }

    public void updateTask(TaskEntity entity) {
        mapper.save(entity, 
                new DynamoDBSaveExpression()
                        .withExpectedEntry(
                                "project_id",
                                new ExpectedAttributeValue(new AttributeValue(entity.getProjectId()))
                        )
                        .withExpectedEntry(
                                "task_name",
                                new ExpectedAttributeValue(new AttributeValue(entity.getTaskName()))
                        )
        );
    }

    public void deleteTask(TaskEntity entity) {
        mapper.delete(entity);
    }
}
