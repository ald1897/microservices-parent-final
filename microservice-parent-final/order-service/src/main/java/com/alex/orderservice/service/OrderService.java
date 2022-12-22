package com.alex.orderservice.service;

import com.alex.orderservice.config.WebClientConfig;
import com.alex.orderservice.dto.OrderLineItemsDto;
import com.alex.orderservice.dto.OrderRequest;
import com.alex.orderservice.dto.OrderResponse.OrderResponse;
import com.alex.orderservice.model.Order;
import com.alex.orderservice.model.OrderLineItems;
import com.alex.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.weaver.ast.Or;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.file.WatchEvent;
import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;

    private final WebClient webClient;

    //placeOrder Method takes in the OrderRequest object passed in from the controller
    public void placeOrder(OrderRequest orderRequest){
        //Then it creates a new Order Object called order
        Order order = new Order();
        //Then it sets the order number with order.setOrderNumber method
        order.setOrderNumber(UUID.randomUUID().toString());
        // Then we have to map the Order line items coming in from the OrderRequest to the OrderLineItems(model) object
        //orderRequest.getOrderLineItemsDtoList() gets the list of items in an order
        List<OrderLineItems> orderLineItems = orderRequest.getOrderLineItemsDtoList()
                // stream the list
                .stream()
                // Map each item in the list using the mapToDto method
                .map(this::mapToDto)
                //Add the items to a list
                .toList();

        // Set the order.orderLineItemsList field to the list you just mapped
        order.setOrderLineItemsList(orderLineItems);

        List<String> skuCodes = order.getOrderLineItemsList().stream()
                .map(OrderLineItems::getSkuCode)
                .toList();

        // Check inventory services to see if sku-code is in stock
        Boolean result = webClient.get().uri("https://localhost:8082/api/inventory",
                uriBuilder -> uriBuilder.queryParam("skuCodes", skuCodes).build())
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();
        if (result) {
            orderRepository.save(order);
            log.info("Item is in stock, saving order.");
        } else {
            throw new IllegalArgumentException("Product is out of stock. Check Later");
        }
        log.info("Order Number: {} was placed", order.getOrderNumber());
    }

    public List<OrderResponse> getAllOrders() {
        List<Order> orders = orderRepository.findAll();

        Long count = orderRepository.count();
        log.info("Got All {} Orders", count);

        return orders.stream().map(this::mapToOrderResponse).toList();

    }

    private OrderLineItems mapToDto(OrderLineItemsDto orderLineItemsDto) {
        OrderLineItems orderLineItems = new OrderLineItems();
        orderLineItems.setPrice(orderLineItemsDto.getPrice());
        orderLineItems.setQty(orderLineItemsDto.getQty());
        orderLineItems.setSkuCode(orderLineItemsDto.getSkuCode());
        return orderLineItems;
    }

    private OrderResponse mapToOrderResponse(Order order) {
        return OrderResponse.builder()
                .orderLineItemsList(order.getOrderLineItemsList())
                .build();
    }

    public void deleteAllOrders() {
        Long count = orderRepository.count();
        log.info("Deleting All {} Orders", count);
        orderRepository.deleteAll();
        log.info("Orders Deleted.", count);

    }
}
