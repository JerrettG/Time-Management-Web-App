package com.gonsalves.timely.config;


import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DynamoDbConfiguration {

    @Bean(name = "dynamoDBMapper")
    @ConditionalOnProperty(name = "dynamodb.override_endpoint", havingValue = "true")
    public DynamoDBMapper localDynamoDBMapper(@Value("${dynamodb.endpoint}") String endpoint) {
        return new DynamoDBMapper(buildLocalAmazonDynamoDB(endpoint), DynamoDBMapperConfig.SaveBehavior.UPDATE_SKIP_NULL_ATTRIBUTES.config());
    }

    public AmazonDynamoDB buildLocalAmazonDynamoDB(String dynamoEndpoint) {
        AwsClientBuilder.EndpointConfiguration endpointConfig = new
                AwsClientBuilder.EndpointConfiguration(dynamoEndpoint,
                "us-west-1");

        return AmazonDynamoDBClientBuilder
                .standard()
                .withEndpointConfiguration(endpointConfig)
                .build();

    }
    @Bean
    public DynamoDBMapper dynamoDBMapper(@Value("${dynamodb.aws.region}") String region, @Value("${dynamodb.aws.access.key}")String accessKey,@Value("${dynamodb.aws.secret.key}")String secretKey ) {
        return new DynamoDBMapper(buildAmazonDynamoDB(region, accessKey, secretKey), DynamoDBMapperConfig.SaveBehavior.UPDATE_SKIP_NULL_ATTRIBUTES.config());
    }

    public AmazonDynamoDB buildAmazonDynamoDB(String region, String accessKey, String secretKey) {
        return AmazonDynamoDBClientBuilder
                .standard()
                .withRegion(region)
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey)))
                .build();
    }

}
