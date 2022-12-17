package com.alex.orderservice;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.testcontainers.shaded.com.google.common.base.Predicates.equalTo;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(profiles = "test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Testcontainers
public class HelloEndpointIT {

    @Autowired
    private TestRestTemplate restTemplate;

    @LocalServerPort
    private int port;

    @Container
    private static final MySQLContainer mysql = new MySQLContainer("mysql:latest")
            .withDatabaseName("order-service")
            .withUsername("root")
            .withPassword("admin");

    @BeforeAll
    public void initDatabaseProperties() {
        System.setProperty("spring.datasource.url", mysql.getJdbcUrl());
        System.setProperty("spring.datasource.username", mysql.getUsername());
        System.setProperty("spring.datasource.password", mysql.getPassword());
    }

    @Test
    public void hello_endpoint_should_return_hello_world() {
        HttpHeaders headers = new HttpHeaders();
        HttpEntity entity = new HttpEntity(headers);

        ResponseEntity<String> response = this.restTemplate.exchange(createUrlWith("/api/order"), HttpMethod.GET, entity, String.class);

        Assertions.assertEquals(response.getStatusCode(), HttpStatus.OK);
    }

    private String createUrlWith(String endpoint) {
        return "http://localhost:" + port + endpoint;
    }
}