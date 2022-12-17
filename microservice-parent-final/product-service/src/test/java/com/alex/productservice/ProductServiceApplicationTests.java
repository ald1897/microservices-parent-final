package com.alex.productservice;

import com.alex.productservice.dto.ProductRequest;
import com.alex.productservice.dto.ProductResponse;
import com.alex.productservice.model.Product;
import com.alex.productservice.repository.ProductRepository;
import com.alex.productservice.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@Slf4j
class ProductServiceApplicationTests {
	@Container
	static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:6.0.2");
	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private ProductRepository productRepository;
	@Autowired
	private ProductService productService;

	@DynamicPropertySource
	static void setProperties(DynamicPropertyRegistry dynamicPropertyRegistry) {
		dynamicPropertyRegistry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);

	}


	@Test
	void shouldCreateProduct() throws Exception {
		ProductRequest productRequest = getProductRequest();
		String productRequestString = objectMapper.writeValueAsString(productRequest);
		mockMvc.perform(MockMvcRequestBuilders.post("/api/product")
						.contentType(MediaType.APPLICATION_JSON)
						.content(productRequestString))
				.andExpect(status().isCreated());
		Assertions.assertEquals(1, productRepository.findAll().size());
		log.info(productRequest.toString());
		productRepository.deleteAll();
	}
	@Test
	void shouldGetAllProducts() throws Exception {
		productService.createProduct(getProductRequest());
		mockMvc.perform(MockMvcRequestBuilders.get("/api/product"))
				.andExpect(status().isOk());
		log.info("Got All Products!");
		log.info(productRepository.findAll().toString());
		productRepository.deleteAll();

	}
	private ProductResponse mapToProductResponse(Product product) {
		return ProductResponse.builder()
				.id(product.getId())
				.name(product.getName())
				.description(product.getDescription())
				.price(product.getPrice())
				.build();
	}
	@Test
	void shouldGetProductById() throws Exception {
		productService.createProduct(getProductRequest());
		productService.createProduct(getProductRequest());
		List<Product> products = productRepository.findAll();
		for (Product product : products) {
			mockMvc.perform(MockMvcRequestBuilders.get("/api/product/{id}", product.getId()))
					.andExpect(status().isOk());
			log.info("Got Product with ID {}", product.getId());
		}
		productRepository.deleteAll();

	}
	@Test
	void cannotGetProductById() throws Exception {
		productService.createProduct(getProductRequest());
		String badId = "1234";
		List<Product> products = productRepository.findAll();
		for (Product product : products) {
			mockMvc.perform(MockMvcRequestBuilders.get("/api/product/{id}", badId))
					.andExpect(status().isNotFound());
			log.info("Product Not Found with ID: {}", badId);
		}
		productRepository.deleteAll();

	}
	@Test
	void shouldDeleteProductById() throws Exception {
		productService.createProduct(getProductRequest());
		productService.createProduct(getProductRequest());
		// Store All products in list
		List<Product> products = productRepository.findAll();
		// Log list from Product Service
		log.info(productService.getAllProducts().toString());
		// For one product in the list of products, delete the product based on the id
		int i = 0;
		for (Product product : products) {
			while (i < 1) {
				mockMvc.perform(MockMvcRequestBuilders.delete("/api/product/{id}", product.getId()))
						.andExpect(status().isOk());
				productRepository.deleteById(product.getId());
				i++;
			}
		}
		// Log list from Product Service
		log.info(productService.getAllProducts().toString());
		productRepository.deleteAll();
	}

	private ProductRequest getProductRequest() {
		return ProductRequest.builder()
				.name("iPhone 13")
				.description("iPhone 13")
				.price(BigDecimal.valueOf(9999))
				.build();
	}
}