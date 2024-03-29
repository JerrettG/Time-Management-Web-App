package com.gonsalves.timely;

import com.gonsalves.timely.integration.DynamoDBMapperTestConfiguration;
import com.gonsalves.timely.integration.DynamoDBTestConfiguration;
import com.gonsalves.timely.integration.WebSecurityTestConfig;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJson;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
@ContextConfiguration(initializers = {
        DynamoDbInitializer.class,
})
@AutoConfigureMockMvc
@AutoConfigureJson
@AutoConfigureJsonTesters
public @interface IntegrationTest {

}
