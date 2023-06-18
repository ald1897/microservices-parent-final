package com.alex.orderservice;

import com.alex.orderservice.dto.DeleteOrderLineItemsDto;
import com.alex.orderservice.dto.DeleteOrderRequest;
import com.alex.orderservice.dto.OrderLineItemsDto;
import com.alex.orderservice.dto.OrderRequest;
import com.alex.orderservice.model.OrderLineItems;
import com.alex.orderservice.repository.OrderRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
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
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
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
	@Order(1)
	void shouldPlaceOrder() throws Exception {
		log.info("Starting Test by clearing DB...");
		log.info("Get Order Request...");
		OrderRequest orderRequest = getValidOrderRequest();
		log.info("Setting orderLineItems...");
		List<OrderLineItems> orderLineItems = orderRequest.getOrderLineItemsDtoList()
				.stream()
				.map(this::mapToDto)
				.toList();
		log.info("Hitting /api/order with POST Request containing orderRequestString JSON");
		String orderRequestString = objectMapper.writeValueAsString(orderRequest);
		// Order 1
		mockMvc.perform(MockMvcRequestBuilders.post("/api/order")
						.contentType(MediaType.APPLICATION_JSON)
						.content(orderRequestString))
				.andExpect(status().isCreated());
		// Order 2
		mockMvc.perform(MockMvcRequestBuilders.post("/api/order")
						.contentType(MediaType.APPLICATION_JSON)
						.content(orderRequestString))
				.andExpect(status().isCreated());

		// Clear order repository
		orderRepository.deleteAll();
	}
	@Test
	@Order(2)
	void shouldGetAllOrders() throws Exception {
		log.info("Get Order Request...");
		OrderRequest orderRequest = getValidOrderRequest();
		log.info("Setting orderLineItems...");
		List<OrderLineItems> orderLineItems = orderRequest.getOrderLineItemsDtoList()
				.stream()
				.map(this::mapToDto)
				.toList();
		log.info("Hitting /api/order with POST Request containing orderRequestString JSON");
		String orderRequestString = objectMapper.writeValueAsString(orderRequest);
		// Order 1
		mockMvc.perform(MockMvcRequestBuilders.post("/api/order")
						.contentType(MediaType.APPLICATION_JSON)
						.content(orderRequestString))
				.andExpect(status().isCreated());
		// Order 2
		mockMvc.perform(MockMvcRequestBuilders.post("/api/order")
						.contentType(MediaType.APPLICATION_JSON)
						.content(orderRequestString))
				.andExpect(status().isCreated());

		log.info("Hitting /api/order with GET Request...");
		mockMvc.perform(MockMvcRequestBuilders.get("/api/order"))
				.andExpect(status().isOk());
		Assertions.assertEquals(2, orderRepository.count());
		log.info("Got All {} Orders", orderRepository.count());

		// Clear order repository
		orderRepository.deleteAll();
	}
	@Test
	@Order(3)
	void shouldNotPlaceOrder() throws Exception {
		log.info("Get Order Request...");
		OrderRequest orderRequest = getInvalidOrderRequest();
		log.info("Setting orderLineItems...");
		List<OrderLineItems> orderLineItems = orderRequest.getOrderLineItemsDtoList()
				.stream()
				.map(this::mapToDto)
				.toList();
		log.info("Hitting /api/order with POST Request containing orderRequestString JSON");
		String orderRequestString = objectMapper.writeValueAsString(orderRequest);
		// Attempt Order Request
		try {
			mockMvc.perform(MockMvcRequestBuilders.post("/api/order")
							.contentType(MediaType.APPLICATION_JSON)
							.content(orderRequestString))
					.andExpect(status().isOk());
		} catch (Exception e) {
			log.info("Order Request Failed");
		}
		log.info("Asserting that there are 2 orders in the DB...");
		Assertions.assertEquals(0, orderRepository.count());
		log.info("Asserted that there are 2 orders in the DB");
	}

	@Test
	@Order(4)
	void shouldDeleteAllOrders() throws Exception {
		log.info("Hitting /api/order with DELETE Request...");
		mockMvc.perform(MockMvcRequestBuilders.delete("/api/order"))
				.andExpect(status().isOk());
	}
//
//	@Test
//	@Order(5)
//	void shouldDeleteOrderByOrderId() throws Exception {
//		log.info("Get Order Request...");
//		OrderRequest orderRequest = getValidOrderRequest();
//		log.info("Setting orderLineItems...");
//		List<OrderLineItems> orderLineItems = orderRequest.getOrderLineItemsDtoList()
//				.stream()
//				.map(this::mapToDto)
//				.toList();
//
//		log.info("Hitting /api/order with POST Request containing orderRequestString JSON");
//		String orderRequestString = objectMapper.writeValueAsString(orderRequest);
//		// Order 1
//		mockMvc.perform(MockMvcRequestBuilders.post("/api/order")
//						.contentType(MediaType.APPLICATION_JSON)
//						.content(orderRequestString))
//				.andExpect(status().isCreated());
//		// Order 2
//		mockMvc.perform(MockMvcRequestBuilders.post("/api/order/")
//						.contentType(MediaType.APPLICATION_JSON)
//						.content(orderRequestString))
//				.andExpect(status().isCreated());
//
//
//		log.info("Asserting that there are 2 orders in the DB...");
//		Assertions.assertEquals(2, orderRepository.count());
//		log.info("Asserted that there are 2 orders in the DB");
//
//
//		log.info("Creating Delete Order Request...");
//		DeleteOrderRequest deleteOrderRequest = getDeleteOrderRequest();
//		log.info("Setting orderLineItems for Deletion...");
//		List<OrderLineItems> deleteOrderLineItems = deleteOrderRequest.getDeleteOrderLineItemsDtoList()
//				.stream()
//				.map(this::mapToDtoD)
//				.toList();
//		log.info("Hitting /api/order with DELETE Request...");
//		String deleteOrderRequestString = objectMapper.writeValueAsString(deleteOrderRequest);
//		mockMvc.perform(MockMvcRequestBuilders.post("/api/order/delete")
//				.contentType(MediaType.APPLICATION_JSON)
//				.content(deleteOrderRequestString))
//				.andExpect(status().isOk());
//
//		log.info("Asserting that there is 1 order in the DB...");
//		Assertions.assertEquals(1, orderRepository.count());
//		log.info("Asserted that there is 1 order in the DB");
//
//	}



	private OrderRequest getValidOrderRequest() {
		log.info("Creating new Valid Order Request...");
		OrderRequest orderRequest = new OrderRequest();
		log.info(orderRequest.toString());
		log.info("Creating OrderLineItemsDtoList...");
		List<OrderLineItemsDto> orderLineItemsDtoList = getValidOrderLineItemsDtoList();
		log.info(orderLineItemsDtoList.toString());
		log.info("Setting OrderLineItemsDtoList for Order Request...");
		orderRequest.setOrderLineItemsDtoList(orderLineItemsDtoList);
		log.info("Printing Order Request...");
		log.info(orderRequest.toString());
		return orderRequest;
	}

	private OrderRequest getInvalidOrderRequest() {
		log.info("Creating new Invalid Order Request...");
		OrderRequest orderRequest = new OrderRequest();
		log.info(orderRequest.toString());
		log.info("Creating OrderLineItemsDtoList...");
		List<OrderLineItemsDto> orderLineItemsDtoList = getInvalidOrderLineItemsDtoList();
		log.info(orderLineItemsDtoList.toString());
		log.info("Setting OrderLineItemsDtoList for Order Request...");
		orderRequest.setOrderLineItemsDtoList(orderLineItemsDtoList);
		log.info("Printing Order Request...");
		log.info(orderRequest.toString());
		return orderRequest;
	}

	private DeleteOrderRequest getDeleteOrderRequest() {
		log.info("Creating new Valid Order Request...");
		DeleteOrderRequest deleteOrderRequest = new DeleteOrderRequest();
		log.info(deleteOrderRequest.toString());
		log.info("Creating OrderLineItemsDtoList...");
		List<DeleteOrderLineItemsDto> deleteOrderLineItemsDtoList = getDeleteOrderLineItemsDtoList();
		log.info(deleteOrderLineItemsDtoList.toString());
		log.info("Setting OrderLineItemsDtoList for Order Request...");
		deleteOrderRequest.setDeleteOrderLineItemsDtoList(deleteOrderLineItemsDtoList);
		log.info("Printing Order Request...");
		log.info(deleteOrderRequest.toString());
		return deleteOrderRequest;
	}

	private List<OrderLineItemsDto> getValidOrderLineItemsDtoList() {
		log.info("Creating Valid OrderLineItemDTOs");
		OrderLineItemsDto testOrderLineItemsDto1 = new OrderLineItemsDto(Long.valueOf(0001), "iphone_13", BigDecimal.valueOf(111.11), 1);
		OrderLineItemsDto testOrderLineItemsDto2 = new OrderLineItemsDto(Long.valueOf(0002), "iphone_14", BigDecimal.valueOf(222.22), 2);
		log.info("Creating OrderLineItemDtoList to House a list of Order Line Item DTOs");
		List<OrderLineItemsDto> orderLineItemsDtoList = new ArrayList<OrderLineItemsDto>();
		log.info("Adding the OrderLineItemDTOs to the OrderLineItemsDTOList... ");
		orderLineItemsDtoList.add(testOrderLineItemsDto1);
		orderLineItemsDtoList.add(testOrderLineItemsDto2);
		log.info("Return the OrderLineItemDtoList...");
		return orderLineItemsDtoList;
	}

	private List<OrderLineItemsDto> getInvalidOrderLineItemsDtoList() {
		log.info("Creating Invalid OrderLineItemDTOs");
		OrderLineItemsDto testOrderLineItemsDto1 = new OrderLineItemsDto(Long.valueOf(0001), "invalid_sku_1", BigDecimal.valueOf(111.11), 1);
		OrderLineItemsDto testOrderLineItemsDto2 = new OrderLineItemsDto(Long.valueOf(0002), "iphone_14", BigDecimal.valueOf(222.22), 2);
		log.info("Creating OrderLineItemDtoList to House a list of Order Line Item DTOs");
		List<OrderLineItemsDto> orderLineItemsDtoList = new ArrayList<OrderLineItemsDto>();
		log.info("Adding the OrderLineItemDTOs to the OrderLineItemsDTOList... ");
		orderLineItemsDtoList.add(testOrderLineItemsDto1);
		orderLineItemsDtoList.add(testOrderLineItemsDto2);
		log.info("Return the OrderLineItemDtoList...");
		return orderLineItemsDtoList;
	}

	private List<DeleteOrderLineItemsDto> getDeleteOrderLineItemsDtoList() {
		log.info("Creating Valid OrderLineItemDTOs");
		DeleteOrderLineItemsDto testDeleteOrderLineItemsDto1 = new DeleteOrderLineItemsDto(Long.valueOf(0001));
		log.info("Creating OrderLineItemDtoList to House a list of Order Line Item DTOs");
		List<DeleteOrderLineItemsDto> deleteOrderLineItemsDtoList = new ArrayList<DeleteOrderLineItemsDto>();
		log.info("Adding the OrderLineItemDTOs to the OrderLineItemsDTOList... ");
		deleteOrderLineItemsDtoList.add(testDeleteOrderLineItemsDto1);
		log.info("Return the OrderLineItemDtoList...");
		return deleteOrderLineItemsDtoList;
	}

	private OrderLineItems mapToDto(OrderLineItemsDto orderLineItemsDto) {
	return OrderLineItems.builder()
			.id(orderLineItemsDto.getId())
			.skuCode(orderLineItemsDto.getSkuCode())
			.price(orderLineItemsDto.getPrice())
			.qty(orderLineItemsDto.getQty())
			.build();
	}

	private OrderLineItems mapToDtoD(DeleteOrderLineItemsDto deleteOrderLineItemsDto) {
		return OrderLineItems.builder()
				.id(deleteOrderLineItemsDto.getId())
				.build();
	}
}


