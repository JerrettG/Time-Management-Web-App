package com.gonsalves.timely.integration;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;


@TestConfiguration
public class DynamoDBTestConfiguration {


    @Bean
    @Primary
    public AmazonDynamoDB buildTestAmazonDynamoDB(@Value("${dynamodb.endpoint}") String dynamoEndpoint) {
        AwsClientBuilder.EndpointConfiguration endpointConfig = new
                AwsClientBuilder.EndpointConfiguration(dynamoEndpoint,
                "us-west-1");

        return AmazonDynamoDBClientBuilder
                .standard()
                .withEndpointConfiguration(endpointConfig)
                .build();
    }
}
