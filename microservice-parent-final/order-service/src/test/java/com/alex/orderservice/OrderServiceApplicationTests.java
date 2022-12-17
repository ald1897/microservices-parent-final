package com.alex.orderservice;

import com.alex.orderservice.dto.OrderLineItemsDto;
import com.alex.orderservice.dto.OrderRequest;
import com.alex.orderservice.model.OrderLineItems;
import com.alex.orderservice.repository.OrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@AutoConfigureMockMvc
@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles(profiles = "test")
class OrderServiceApplicationTests {

	@Autowired
	private MockMvc mockMvc;

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

	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private OrderRepository orderRepository;

	@Test
	void shouldPlaceOrder() throws Exception {
		log.info("Starting Test by clearing DB...");
//		orderRepository.deleteAll();
		log.info("Get Order Request...");
		OrderRequest orderRequest = getOrderRequest();
		log.info("Setting orderLineItems...");
		List<OrderLineItems> orderLineItems = orderRequest.getOrderLineItemsDtoList()
				.stream()
				.map(this::mapToDto)
				.toList();
		log.info("Hitting /api/order with POST Request containing orderRequestString JSON");
		String orderRequestString = objectMapper.writeValueAsString(orderRequest);
		mockMvc.perform(MockMvcRequestBuilders.post("/api/order")
						.contentType(MediaType.APPLICATION_JSON)
						.content(orderRequestString))
				.andExpect(status().isCreated());
	}

	@Test
	void shouldGetAllOrders() throws Exception {
		log.info("Hitting /api/order with GET Request...");
		mockMvc.perform(MockMvcRequestBuilders.get("/api/order"))
				.andExpect(status().isOk());
		Long count = orderRepository.count();
		log.info("Got All {} Orders", count);

//		orderRepository.deleteAll();
	}

	private OrderRequest getOrderRequest() {
		log.info("Creating new Order Request...");
		OrderRequest orderRequest = new OrderRequest();
		log.info(orderRequest.toString());

		log.info("Creating OrderLineItemsDtoList...");
		List<OrderLineItemsDto> orderLineItemsDtoList = getOrderLineItemsDtoList();
		log.info(orderLineItemsDtoList.toString());

		log.info("Setting OrderLineItemsDtoList for Order Request...");
		orderRequest.setOrderLineItemsDtoList(orderLineItemsDtoList);

		log.info("Printing Order Request...");
		log.info(orderRequest.toString());

		return orderRequest;

	}

	private List<OrderLineItemsDto> getOrderLineItemsDtoList() {
		log.info("Creating OrderLineItemDTOs");
		OrderLineItemsDto testOrderLineItemsDto1 = new OrderLineItemsDto(Long.valueOf(0001), "test_sku_1", BigDecimal.valueOf(111.11), 1);
		OrderLineItemsDto testOrderLineItemsDto2 = new OrderLineItemsDto(Long.valueOf(0002), "test_sku_2", BigDecimal.valueOf(222.22), 2);
		OrderLineItemsDto testOrderLineItemsDto3 = new OrderLineItemsDto(Long.valueOf(0003), "test_sku_3", BigDecimal.valueOf(333.33), 3);
		log.info("Creating OrderLineItemDtoList to House a list of Order Line Item DTOs");
		List<OrderLineItemsDto> orderLineItemsDtoList = new ArrayList<OrderLineItemsDto>();
		log.info("Adding the OrderLineItemDTOs to the OrderLineItemsDTOList... ");
		orderLineItemsDtoList.add(testOrderLineItemsDto1);
		orderLineItemsDtoList.add(testOrderLineItemsDto2);
		orderLineItemsDtoList.add(testOrderLineItemsDto3);
		log.info("Return the OrderLineItemDtoList...");
		return orderLineItemsDtoList;
	}
	private OrderLineItems mapToDto(OrderLineItemsDto orderLineItemsDto) {
		OrderLineItems orderLineItems = new OrderLineItems();
		orderLineItems.setPrice(orderLineItemsDto.getPrice());
		orderLineItems.setQty(orderLineItemsDto.getQty());
		orderLineItems.setSkuCode(orderLineItemsDto.getSkuCode());
		return orderLineItems;
	}


	@Test
	void shouldDeleteAllOrders() throws Exception {
		log.info("Hitting /api/order with DELETE Request...");
		mockMvc.perform(MockMvcRequestBuilders.delete("/api/order"))
				.andExpect(status().isOk());
//		orderRepository.deleteAll();
	}


}

