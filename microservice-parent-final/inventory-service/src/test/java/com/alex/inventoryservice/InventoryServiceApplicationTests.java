package com.alex.inventoryservice;

import com.alex.inventoryservice.dto.InventoryRequest;
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
import org.springframework.http.MediaType;
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

	//should get all invs
	@Test
	@Order(1)
	void shouldGetAllInvs() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.get("/api/inventory/all"))
				.andExpect(status().isOk());
		log.info("Got all inventories");
	}

	@Test
	@Order(2)
	void shouldNotGetAllInvs() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.get("/api/failTest"))
				.andExpect(status().isNotFound());
		log.info("Did not get all inventories");
	}

	@Test
	@Order(3)
	void shouldGetInvBySkuCode() throws Exception {
		List<Inventory> inventories = inventoryRepository.findAll();
		for (Inventory inventory : inventories) {

			mockMvc.perform(MockMvcRequestBuilders.get("/api/inventory/item?skuCode={sku-Code}", inventory.getSkuCode()))
					.andExpect(status().isOk());
			log.info("Got Inventory with Sku-Code {}", inventory.getSkuCode());

		}
	}

	@Test
	@Order(4)
	void shouldNotGetInvBySkuCode() throws Exception {
		//add try catch
		try {
			mockMvc.perform(MockMvcRequestBuilders.get("/api/inventory/item?skuCode={sku-code}", "failTest"))
					.andExpect(status().isNotFound());
			log.info("Did not get Inventory with Sku-Code {}", "failTest");
		} catch (Exception e) {
			log.info("Did not get Inventory with Sku-Code {}", "failTest");
		}
	}

	@Test
	@Order(5)
	void shouldReturnTrueIfInvIsInStock() throws Exception {
		List<Inventory> inventories = inventoryRepository.findAll();
		for (Inventory inventory : inventories) {

			mockMvc.perform(MockMvcRequestBuilders.get("/api/inventory/check_stock?sku-code={}&qty={}", inventory.getSkuCode(), 1))
					.andExpect(status().isOk());
			log.info("Got Inventory with Sku-Code {}", inventory.getSkuCode());

		}
	}

	@Test
	@Order(6)
	void shouldReturnFalseIfInvIsNotInStock() throws Exception {
		List<Inventory> inventories = inventoryRepository.findAll();
		for (Inventory inventory : inventories) {

			mockMvc.perform(MockMvcRequestBuilders.get("/api/inventory/check_stock?sku-code={}&qty={}", inventory.getSkuCode(), 5500000))
					.andExpect(status().isOk());
			log.info("Got Inventory with Sku-Code {}", inventory.getSkuCode());

		}
	}

	@Test
	@Order(7)
	void shouldUpdateInventory() throws Exception {
		List<Inventory> inventories = inventoryRepository.findAll();
		for (Inventory inventory : inventories) {
			inventory.setQty(100);
			mockMvc.perform(MockMvcRequestBuilders.put("/api/inventory/update?sku-code={}&qty={}", inventory.getSkuCode(), inventory.getQty()))
					.andExpect(status().isOk());
			log.info("Updated Inventory with Sku-Code {}", inventory.getSkuCode());
		}
	}

	@Test
	@Order(8)
	void shouldNotUpdateInventory() throws Exception {
		List<Inventory> inventories = inventoryRepository.findAll();
		for (Inventory inventory : inventories) {
			inventory.setQty(100);
			mockMvc.perform(MockMvcRequestBuilders.put("/api/inventory/update?sku-code={}&qty={}", "failTest", inventory.getQty()))
					.andExpect(status().isNotFound());
			log.info("Did not update Inventory with Sku-Code {}", "failTest");
		}
	}

	@Test
	@Order(9)
	void shouldAddInventory() throws Exception {
	InventoryRequest inventoryRequest = createInventoryRequest();
		mockMvc.perform(MockMvcRequestBuilders.post("/api/inventory/add")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(inventoryRequest)))
				.andExpect(status().isOk());
		log.info("Added Inventory with Sku-Code {}", inventoryRequest.getSkuCode());
	}

	@Test
	@Order(10)
	void shouldNotAddInventory() throws Exception {
		InventoryRequest inventoryRequest = createBadInventoryRequest();
		mockMvc.perform(MockMvcRequestBuilders.post("/api/inventory/add")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(inventoryRequest)))
				.andExpect(status().isBadRequest());
		log.info("Did not add Inventory with Sku-Code {}", inventoryRequest.getSkuCode());
	}

	@Test
	@Order(11)
	void shouldDeleteInventory() throws Exception {
		List<Inventory> inventories = inventoryRepository.findAll();
		for (Inventory inventory : inventories) {
			mockMvc.perform(MockMvcRequestBuilders.delete("/api/inventory/delete?skuCode={sku-code}", inventory.getSkuCode()))
					.andExpect(status().isOk());
			log.info("Deleted Inventory with Sku-Code {}", inventory.getSkuCode());
		}
	}

	@Test
	@Order(12)
	void shouldNotDeleteInventory() throws Exception {
		Inventory inventory = inventoryRepository.findBySkuCode("failTest");
		mockMvc.perform(MockMvcRequestBuilders.delete("/api/inventory/delete?skuCode={sku-code}", inventory.getSkuCode()))
				.andExpect(status().isNotFound());
		log.info("Did not delete Inventory with Sku-Code {}", "failTest");
	}

	private InventoryRequest createInventoryRequest() {
		InventoryRequest inventoryRequest = new InventoryRequest();
		inventoryRequest.setSkuCode("test-sku-code");
		inventoryRequest.setQty(100);
		return inventoryRequest;
	}
	private InventoryRequest createBadInventoryRequest() {
		InventoryRequest inventoryRequest = new InventoryRequest();
		inventoryRequest.setSkuCode("failTest");
		return inventoryRequest;
	}


}
