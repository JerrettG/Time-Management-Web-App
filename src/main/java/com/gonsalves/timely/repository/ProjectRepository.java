package com.gonsalves.timely.repository;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBSaveExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ExpectedAttributeValue;
import com.gonsalves.timely.repository.model.ProjectEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ProjectRepository {


    private final DynamoDBMapper mapper;

    @Autowired
    ProjectRepository(DynamoDBMapper mapper) {this.mapper = mapper;}

    public Optional<ProjectEntity> getProjectByProjectName(String userId, String projectName) {
        return Optional.ofNullable(mapper.load(ProjectEntity.class, userId, projectName));
    }

    public List<ProjectEntity> getAllProjectsByUser(String userId) {
        ProjectEntity entity = new ProjectEntity();
        entity.setUserId(userId);
        DynamoDBQueryExpression<ProjectEntity> queryExpression = new DynamoDBQueryExpression<ProjectEntity>()
                .withHashKeyValues(entity)
                .withConsistentRead(false);
        return mapper.query(ProjectEntity.class, queryExpression);
    }

    public void createProject(ProjectEntity entity) {
        mapper.save(entity);
    }

    public void updateProject(ProjectEntity entity) {
        mapper.save(entity,
                new DynamoDBSaveExpression()
                        .withExpectedEntry(
                                "user_id",
                                new ExpectedAttributeValue(new AttributeValue(entity.getUserId()))
                        )
                        .withExpectedEntry(
                                "project_name",
                                new ExpectedAttributeValue(new AttributeValue(entity.getProjectName()))
                        )
        );
    }

    public void deleteProject(ProjectEntity entity) {
        mapper.delete(entity);
    }


}
