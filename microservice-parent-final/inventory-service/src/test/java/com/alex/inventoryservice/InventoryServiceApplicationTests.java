package com.alex.inventoryservice;

import com.alex.inventoryservice.model.Inventory;
import com.alex.inventoryservice.repository.InventoryRepository;
import com.alex.inventoryservice.service.InventoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@AutoConfigureMockMvc
@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles(profiles = "test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class InventoryServiceApplicationTests {


	@Autowired
	private MockMvc mockMvc;
	//Test

	private int num;
	@LocalServerPort
	private int port;

	@Container
	private static final MySQLContainer mysql = new MySQLContainer("mysql:latest")
			.withDatabaseName("inventory-service")
			.withUsername("root")
			.withPassword("admin");

	@BeforeAll
	public void initDatabaseProperties() {
		System.setProperty("spring.datasource.url", mysql.getJdbcUrl());
		System.setProperty("spring.datasource.username", mysql.getUsername());
		System.setProperty("spring.datasource.password", mysql.getPassword());
	}

	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private InventoryRepository inventoryRepository;

	@Autowired
	private InventoryService inventoryService;

//	@Test
//	void shouldCheckStockBySkuCode() throws Exception {
//
//		List<Inventory> inventories = inventoryRepository.findAll();
//		for (Inventory inventory : inventories) {
//
//			mockMvc.perform(MockMvcRequestBuilders.get("/api/inventory/{sku-code}", inventory.getSkuCode()))
//					.andExpect(status().isOk());
////			log.info("Got Inventory with Sku-Code {}", inventory.getSkuCode());
//
//		}



//	}
}
