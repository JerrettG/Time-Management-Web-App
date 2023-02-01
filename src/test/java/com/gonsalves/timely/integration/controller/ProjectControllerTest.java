package com.gonsalves.timely.integration.controller;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gonsalves.timely.IntegrationTest;
import com.gonsalves.timely.controller.model.ProjectCreateRequest;
import com.gonsalves.timely.controller.model.ProjectResponse;
import com.gonsalves.timely.controller.model.ProjectUpdateRequest;
import com.gonsalves.timely.integration.DynamoDBMapperTestConfiguration;
import com.gonsalves.timely.integration.DynamoDBTestConfiguration;
import com.gonsalves.timely.integration.Utility;
import com.gonsalves.timely.integration.WebSecurityTestConfig;
import net.andreinc.mockneat.MockNeat;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@Import({DynamoDBTestConfiguration.class, WebSecurityTestConfig.class})
@IntegrationTest
public class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;
    private Utility utility;
    @Autowired
    private AmazonDynamoDB amazonDynamoDB;
    private  final MockNeat mockNeat = MockNeat.threadLocal();

    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    public void setup() {
        utility = new Utility(mockMvc);
        List<AttributeDefinition> attributeDefinitions= new ArrayList<AttributeDefinition>();
        attributeDefinitions.add(new AttributeDefinition().withAttributeName("user_id").withAttributeType("S"));
        attributeDefinitions.add(new AttributeDefinition().withAttributeName("project_name").withAttributeType("S"));

        List<KeySchemaElement> keySchema = Arrays.asList(
                new KeySchemaElement().withAttributeName("user_id").withKeyType(KeyType.HASH),
                new KeySchemaElement().withAttributeName("project_name").withKeyType(KeyType.RANGE));
        
        CreateTableRequest request = new CreateTableRequest()
                .withTableName("Timely-Projects")
                .withKeySchema(keySchema)
                .withAttributeDefinitions(attributeDefinitions)
                .withProvisionedThroughput(new ProvisionedThroughput()
                        .withReadCapacityUnits(5L)
                        .withWriteCapacityUnits(6L));
        try {
            CreateTableResult response = amazonDynamoDB.createTable(request);
        } catch (AmazonDynamoDBException e) {
            System.out.println(e.getMessage());
        }

        attributeDefinitions= new ArrayList<AttributeDefinition>();
        attributeDefinitions.add(new AttributeDefinition().withAttributeName("project_id").withAttributeType("S"));
        attributeDefinitions.add(new AttributeDefinition().withAttributeName("task_name").withAttributeType("S"));

        keySchema = Arrays.asList(
                new KeySchemaElement().withAttributeName("project_id").withKeyType(KeyType.HASH),
                new KeySchemaElement().withAttributeName("task_name").withKeyType(KeyType.RANGE));

        request = new CreateTableRequest()
                .withTableName("Timely-Tasks")
                .withKeySchema(keySchema)
                .withAttributeDefinitions(attributeDefinitions)
                .withProvisionedThroughput(new ProvisionedThroughput()
                        .withReadCapacityUnits(5L)
                        .withWriteCapacityUnits(6L));
        try {
            CreateTableResult response = amazonDynamoDB.createTable(request);
        } catch (AmazonDynamoDBException e) {
            System.out.println(e.getMessage());
        }
    }
    @AfterEach
    public void cleanUp(){
        DeleteTableRequest request = new DeleteTableRequest();
        request.setTableName("Timely-Projects");

        amazonDynamoDB.deleteTable(request);
        request = new DeleteTableRequest();
        request.setTableName("Timely-Tasks");

        amazonDynamoDB.deleteTable(request);
    }

    @Test
    public void getAllProjectsByUser_returnsListOfAllProjects() throws Exception {
        //GIVEN
        String userId = mockNeat.users().valStr();
        String projectName = mockNeat.strings().val();
        ProjectCreateRequest createRequest = new ProjectCreateRequest(userId, projectName);
        
        utility.projectClient.createProject(createRequest)
                .andExpect(status().isCreated());
        //WHEN
        utility.projectClient.getAllProjectsByUser(userId)
                //THEN
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.length()").value(1)
                );

    }

    @Test
    public void getProjectWithProjectName_validProjectName_returnsCorrectProject() throws Exception {
        //GIVEN
        String userId = mockNeat.users().valStr();
        String projectName = mockNeat.strings().val();
        ProjectCreateRequest createRequest = new ProjectCreateRequest(userId, projectName);

        String responseJson = utility.projectClient.createProject(createRequest)
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        ProjectResponse createdProject = mapper.readValue(responseJson, ProjectResponse.class);
        //WHEN
        utility.projectClient.getProjectByProjectName(userId, projectName)

                //THEN
                .andExpectAll(
                        status().isOk(),
                        jsonPath("userId").value(userId),
                        jsonPath("projectName").value(projectName),
                        jsonPath("projectId").value(createdProject.getProjectId()),
                        jsonPath("creationDate").value(createdProject.getCreationDate()),
                        jsonPath("totalTimeSpent").value(createdProject.getTotalTimeSpent()),
                        jsonPath("completionPercent").value(createdProject.getCompletionPercent())
                );

    }

    @Test
    public void getProjectWithProjectName_invalidProjectName_responseNotFound() throws Exception {
        //GIVEN
        String userId = mockNeat.users().valStr();
        String invalidProjectName = mockNeat.strings().valStr();
        //WHEN
        utility.projectClient.getProjectByProjectName(userId, invalidProjectName)

                //THEN
                .andExpect(status().isNotFound());
    }

    @Test
    public void createProject_notExistingProject_createsProject() throws Exception {
        //GIVEN
        String userId = mockNeat.users().valStr();
        String projectName = mockNeat.strings().val();
        String expectedProjectId = String.format("%s_%s", userId, projectName);
        String expectedTotalTimeSpent = "00:00:00";
        //WHEN
        utility.projectClient.createProject(new ProjectCreateRequest(userId, projectName))
        //THEN
                .andExpectAll(
                        status().isCreated(),
                        jsonPath("userId").value(userId),
                        jsonPath("projectName").value(projectName),
                        jsonPath("projectId").value(expectedProjectId),
                        jsonPath("totalTimeSpent").value(expectedTotalTimeSpent),
                        jsonPath("completionPercent").value(0)
                );
    }


    @Test
    public void createProject_existingProject_responseConflict() throws Exception {
        //GIVEN
        String userId = mockNeat.users().valStr();
        String projectName = mockNeat.strings().val();
        
        ProjectCreateRequest createRequest = new ProjectCreateRequest(userId, projectName);
        utility.projectClient.createProject(createRequest)
                .andExpect(status().isCreated());
        //WHEN
        utility.projectClient.createProject(createRequest)
                //THEN
                .andExpect(status().isConflict());
    }

    @Test
    public void updateProject_existingProject_productUpdated() throws Exception {
        //GIVEN
        String userId = mockNeat.users().valStr();
        String projectName = mockNeat.strings().val();
        ProjectCreateRequest createRequest = new ProjectCreateRequest(userId, projectName);

        String jsonResponse = utility.projectClient.createProject(createRequest)
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        ProjectResponse createResponse = mapper.readValue(jsonResponse, ProjectResponse.class);

        ProjectUpdateRequest updateRequest = new ProjectUpdateRequest(
                createResponse.getUserId(),
                createRequest.getProjectName()
        );

        //WHEN
        utility.projectClient.updateProject(updateRequest)
                //THEN
                .andExpect(status().isAccepted());
        utility.projectClient.getProjectByProjectName(createResponse.getUserId(), createResponse.getProjectName())
                .andExpectAll(
                        status().isOk(),
                        jsonPath("userId").value(createResponse.getUserId()),
                        jsonPath("projectName").value(createResponse.getProjectName()),
                        jsonPath("totalTimeSpent").value(createResponse.getTotalTimeSpent()),
                        jsonPath("completionPercent").value(0)
                );

    }

    @Test
    public void updateProject_notExistingProject_responseBadRequest() throws Exception {
        //GIVEN
        String userId = mockNeat.users().valStr();
        String invalidProjectName = mockNeat.strings().valStr();
        String totalTimeSpent = mockNeat.localDates().toString();
        Integer updatedCompletionPercent = mockNeat.ints().range(0,100).val();
        ProjectUpdateRequest updateRequest = new ProjectUpdateRequest(
                userId,
                invalidProjectName
                );

        //WHEN
        utility.projectClient.updateProject(updateRequest)
                //THEN
                .andExpect(status().isNotFound());
    }


    @Test
    public void deleteProjectWithProjectName_existingProject_deletesProject() throws Exception {
        //GIVEN
        String userId = mockNeat.users().valStr();
        String projectName = mockNeat.strings().val();

        ProjectCreateRequest createRequest = new ProjectCreateRequest(userId, projectName);
        utility.projectClient.createProject(createRequest)
                .andExpect(status().isCreated());
        //WHEN
        utility.projectClient.deleteProject(userId, projectName)
                //THEN
                .andExpect(status().isAccepted());

        utility.projectClient.getProjectByProjectName(userId, projectName)
                .andExpect(status().isNotFound());
    }
    @Test
    public void deleteProjectWithProjectName_notExistingProject_responseBadRequestNoDeletion() throws Exception {
        //GIVEN
        String userId = mockNeat.users().valStr();
        String projectName = mockNeat.strings().val();

        ProjectCreateRequest createRequest = new ProjectCreateRequest(userId, projectName);
        utility.projectClient.createProject(createRequest)
                .andExpect(status().isCreated());

        String invalidProjectName = mockNeat.strings().valStr();
        //WHEN
        utility.projectClient.deleteProject(userId, invalidProjectName)
                //THEN
                .andExpect(status().isNotFound());

        utility.projectClient.getAllProjectsByUser(userId)
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.length()").value(1)
                );

    }
}
