package com.gonsalves.timely.integration.controller;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gonsalves.timely.IntegrationTest;
import com.gonsalves.timely.controller.model.TaskCreateRequest;
import com.gonsalves.timely.controller.model.TaskResponse;
import com.gonsalves.timely.controller.model.TaskUpdateRequest;
import com.gonsalves.timely.integration.DynamoDBMapperTestConfiguration;
import com.gonsalves.timely.integration.DynamoDBTestConfiguration;
import com.gonsalves.timely.integration.Utility;
import com.gonsalves.timely.integration.WebSecurityTestConfig;
import com.gonsalves.timely.service.model.TimeLog;
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

@Import({DynamoDBTestConfiguration.class, DynamoDBMapperTestConfiguration.class, WebSecurityTestConfig.class})
@IntegrationTest
public class TaskControllerTest {

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
        attributeDefinitions.add(new AttributeDefinition().withAttributeName("project_id").withAttributeType("S"));
        attributeDefinitions.add(new AttributeDefinition().withAttributeName("task_name").withAttributeType("S"));

        List<KeySchemaElement> keySchema = Arrays.asList(
                new KeySchemaElement().withAttributeName("project_id").withKeyType(KeyType.HASH),
                new KeySchemaElement().withAttributeName("task_name").withKeyType(KeyType.RANGE));

        CreateTableRequest request = new CreateTableRequest()
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
        request.setTableName("Timely-Tasks");

        amazonDynamoDB.deleteTable(request);
    }

    @Test
    public void getAllTasksByProject_returnsListOfAllTasks() throws Exception {
        //GIVEN
        String projectId = mockNeat.users().valStr();
        String taskName = mockNeat.strings().valStr();
        String notes = mockNeat.strings().val();
        String timeSpent = mockNeat.localDates().valStr();
        List<TimeLog> timeLogs = new ArrayList<>();
        String status = mockNeat.strings().valStr();
        TaskCreateRequest createRequest = new TaskCreateRequest(projectId, taskName, notes, status);

        utility.taskClient.createTask(createRequest)
                .andExpect(status().isCreated());
        //WHEN
        utility.taskClient.getAllTasksByProject(projectId)
                //THEN
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.length()").value(1)
                );

    }

    @Test
    public void getTaskWithTaskName_validTaskName_returnsCorrectTask() throws Exception {
        //GIVEN
        String projectId = mockNeat.users().valStr();
        String taskName = mockNeat.strings().valStr();
        String notes = mockNeat.strings().val();
        String expectedTimeSpent = "00:00:00";
        List<TimeLog> expectedTimeLogs = new ArrayList<>();
        String status = mockNeat.strings().valStr();
        TaskCreateRequest createRequest = new TaskCreateRequest(projectId, taskName, notes, status);

        String responseJson = utility.taskClient.createTask(createRequest)
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        TaskResponse createdTask = mapper.readValue(responseJson, TaskResponse.class);
        //WHEN
        utility.taskClient.getTaskByTaskName(projectId, taskName)

                //THEN
                .andExpectAll(
                        status().isOk(),
                        jsonPath("projectId").value(projectId),
                        jsonPath("taskName").value(taskName),
                        jsonPath("timeSpent").value(expectedTimeSpent),
                        jsonPath("notes").value(notes),
                        jsonPath("status").value(status),
                        jsonPath("timeLogs").value(expectedTimeLogs)
                );

    }

    @Test
    public void getTaskWithTaskName_invalidTaskName_responseNotFound() throws Exception {
        //GIVEN
        String projectId = mockNeat.users().valStr();
        String invalidTaskName = mockNeat.strings().valStr();

        //WHEN
        utility.taskClient.getTaskByTaskName(projectId, invalidTaskName)

                //THEN
                .andExpect(status().isNotFound());
    }

    @Test
    public void createTask_notExistingTask_createsTask() throws Exception {
        //GIVEN
        String projectId = mockNeat.users().valStr();
        String taskName = mockNeat.strings().valStr();
        String notes = mockNeat.strings().val();
        String expectedTimeSpent = "00:00:00";
        List<TimeLog> expectedTimeLogs = new ArrayList<>();
        String status = mockNeat.strings().valStr();
        TaskCreateRequest createRequest = new TaskCreateRequest(projectId, taskName, notes, status);

        //WHEN
        utility.taskClient.createTask(createRequest)
                //THEN
                .andExpectAll(
                        status().isCreated(),
                        jsonPath("projectId").value(projectId),
                        jsonPath("taskName").value(taskName),
                        jsonPath("timeSpent").value(expectedTimeSpent),
                        jsonPath("notes").value(notes),
                        jsonPath("status").value(status),
                        jsonPath("timeLogs").value(expectedTimeLogs)
                );
    }


    @Test
    public void createTask_existingTask_responseConflict() throws Exception {
        //GIVEN
        String projectId = mockNeat.users().valStr();
        String taskName = mockNeat.strings().valStr();
        String notes = mockNeat.strings().val();
        String timeSpent = mockNeat.localDates().valStr();
        List<TimeLog> timeLogs = new ArrayList<>();
        String status = mockNeat.strings().valStr();
        TaskCreateRequest createRequest = new TaskCreateRequest(projectId, taskName, notes, status);

        utility.taskClient.createTask(createRequest)
                .andExpect(status().isCreated());
        //WHEN
        utility.taskClient.createTask(createRequest)
                //THEN
                .andExpect(status().isConflict());
    }

    @Test
    public void updateTask_existingTask_productUpdated() throws Exception {
        //GIVEN
        String projectId = mockNeat.users().valStr();
        String taskName = mockNeat.strings().valStr();
        String notes = mockNeat.strings().val();
        String timeSpent = mockNeat.localDates().valStr();
        List<TimeLog> timeLogs = new ArrayList<>();
        String status = mockNeat.strings().valStr();
        TaskCreateRequest createRequest = new TaskCreateRequest(projectId, taskName, notes, status);
        

        String jsonResponse = utility.taskClient.createTask(createRequest)
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        TaskResponse createResponse = mapper.readValue(jsonResponse, TaskResponse.class);

        String updatedNotes = mockNeat.strings().val();
        TaskUpdateRequest updateRequest = new TaskUpdateRequest(
                createResponse.getProjectId(),
                createRequest.getTaskName(),
                null,
                updatedNotes,
                createResponse.getTimeSpent(), 
                createResponse.getTimeLogs(),
                createResponse.getStatus()
        );

        //WHEN
        utility.taskClient.updateTask(updateRequest, false)
                //THEN
                .andExpect(status().isAccepted());
        utility.taskClient.getTaskByTaskName(createResponse.getProjectId(), createResponse.getTaskName())
                .andExpectAll(
                        status().isOk(),
                        jsonPath("projectId").value(createResponse.getProjectId()),
                        jsonPath("taskName").value(createResponse.getTaskName()),
                        jsonPath("timeSpent").value(createResponse.getTimeSpent()),
                        jsonPath("notes").value(updatedNotes),
                        jsonPath("status").value(createResponse.getStatus()),
                        jsonPath("timeLogs").value(createResponse.getTimeLogs())
                );

    }

    @Test
    public void updateTask_notExistingTask_responseBadRequest() throws Exception {
        //GIVEN
        String projectId = mockNeat.users().valStr();
        String invalidTaskName = mockNeat.strings().valStr();
        String notes = mockNeat.strings().val();
        String timeSpent = mockNeat.localDates().valStr();
        List<TimeLog> timeLogs = new ArrayList<>();
        String status = mockNeat.strings().valStr();
        
        TaskUpdateRequest updateRequest = new TaskUpdateRequest(
                projectId,
                invalidTaskName,
                null,
                notes,
                timeSpent,
                timeLogs,
                status
        );

        //WHEN
        utility.taskClient.updateTask(updateRequest, false)
                //THEN
                .andExpect(status().isNotFound());
    }

    @Test
    public void updateTask_editTaskNameOfExistingTask_updatesTask() throws Exception {
        //GIVEN
        String projectId = mockNeat.users().valStr();
        String taskName = mockNeat.strings().valStr();
        String notes = mockNeat.strings().val();
        String timeSpent = mockNeat.localDates().valStr();
        List<TimeLog> timeLogs = new ArrayList<>();
        String status = mockNeat.strings().valStr();
        TaskCreateRequest createRequest = new TaskCreateRequest(projectId, taskName, notes, status);


        String jsonResponse = utility.taskClient.createTask(createRequest)
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        TaskResponse createResponse = mapper.readValue(jsonResponse, TaskResponse.class);

        String updatedTaskName = mockNeat.strings().val();
        TaskUpdateRequest updateRequest = new TaskUpdateRequest(
                createResponse.getProjectId(),
                createRequest.getTaskName(),
                updatedTaskName,
                notes,
                createResponse.getTimeSpent(),
                createResponse.getTimeLogs(),
                createResponse.getStatus()
        );

        //WHEN
        utility.taskClient.updateTask(updateRequest, true)
                //THEN
                .andExpect(status().isAccepted());
        utility.taskClient.getTaskByTaskName(createResponse.getProjectId(), updatedTaskName)
                .andExpectAll(
                        status().isOk(),
                        jsonPath("projectId").value(createResponse.getProjectId()),
                        jsonPath("taskName").value(updatedTaskName),
                        jsonPath("timeSpent").value(createResponse.getTimeSpent()),
                        jsonPath("notes").value(notes),
                        jsonPath("status").value(createResponse.getStatus()),
                        jsonPath("timeLogs").value(createResponse.getTimeLogs())
                );
    }
    @Test
    public void updateTask_editTaskNameOfNotExistingTask_responseBadRequest() throws Exception {
        //GIVEN
        String projectId = mockNeat.users().valStr();
        String invalidTaskName = mockNeat.strings().valStr();
        String notes = mockNeat.strings().val();
        String timeSpent = mockNeat.localDates().valStr();
        List<TimeLog> timeLogs = new ArrayList<>();
        String status = mockNeat.strings().valStr();

        String updatedTaskName = mockNeat.strings().valStr();
        TaskUpdateRequest updateRequest = new TaskUpdateRequest(
                projectId,
                invalidTaskName,
                updatedTaskName,
                notes,
                timeSpent,
                timeLogs,
                status
        );

        //WHEN
        utility.taskClient.updateTask(updateRequest, false)
                //THEN
                .andExpect(status().isNotFound());
    }

    @Test
    public void deleteTaskWithTaskName_existingTask_deletesTask() throws Exception {
        //GIVEN
        String projectId = mockNeat.users().valStr();
        String taskName = mockNeat.strings().valStr();
        String notes = mockNeat.strings().val();
        String timeSpent = mockNeat.localDates().valStr();
        List<TimeLog> timeLogs = new ArrayList<>();
        String status = mockNeat.strings().valStr();
        TaskCreateRequest createRequest = new TaskCreateRequest(projectId, taskName, notes, status);

        utility.taskClient.createTask(createRequest)
                .andExpect(status().isCreated());
        //WHEN
        utility.taskClient.deleteTask(projectId, taskName)
                //THEN
                .andExpect(status().isAccepted());

        utility.taskClient.getTaskByTaskName(projectId, taskName)
                .andExpect(status().isNotFound());
    }
    @Test
    public void deleteTaskWithTaskName_notExistingTask_responseBadRequestNoDeletion() throws Exception {
        //GIVEN
        String projectId = mockNeat.users().valStr();
        String taskName = mockNeat.strings().valStr();
        String notes = mockNeat.strings().val();
        String timeSpent = mockNeat.localDates().valStr();
        List<TimeLog> timeLogs = new ArrayList<>();
        String status = mockNeat.strings().valStr();
        TaskCreateRequest createRequest = new TaskCreateRequest(projectId, taskName, notes, status);

        utility.taskClient.createTask(createRequest)
                .andExpect(status().isCreated());

        String invalidTaskName = mockNeat.strings().valStr();
        //WHEN
        utility.taskClient.deleteTask(projectId, invalidTaskName)
                //THEN
                .andExpect(status().isNotFound());

        utility.taskClient.getAllTasksByProject(projectId)
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.length()").value(1)
                );

    }
}
