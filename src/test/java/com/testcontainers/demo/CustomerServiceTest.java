package com.testcontainers.demo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

@Testcontainers
class CustomerServiceTest {

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:15.2-alpine")
                    .withCopyFileToContainer(MountableFile.forClasspathResource("init-db.sql"), "/docker-entrypoint-initdb.d/")
                    //.withClasspathResourceMapping("init-db.sql", "/docker-entrypoint-initdb.d/", BindMode.READ_ONLY)
            ;

    CustomerService customerService;

    @BeforeEach
    void setUp() {
        customerService = new CustomerService(postgres.getJdbcUrl(),
                postgres.getUsername(),
                postgres.getPassword());
    }

    @Test
    void shouldGetCustomers() {
        customerService.createCustomer(new Customer(1L, "George"));
        customerService.createCustomer(new Customer(2L, "John"));

        List<Customer> customers = customerService.getAllCustomers();
        assertFalse(customers.isEmpty());
    }

}
