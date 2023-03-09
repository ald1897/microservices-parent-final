package com.alex.orderservice.service;

//import com.alex.inventoryservice.service.InventoryService;
import com.alex.orderservice.dto.*;
import com.alex.orderservice.model.Order;
import com.alex.orderservice.model.OrderLineItems;
import com.alex.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;


@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
//    private final InventoryService inventoryService;


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

        // Create list of Sku Codes present in the order
        List<String> skuCodes = order.getOrderLineItemsList().stream()
                .map(OrderLineItems::getSkuCode)
                .toList();

        // Update Inventory Service by reducing Quantity by amount that was ordered
        List<Integer> qtys = order.getOrderLineItemsList().stream()
                .map(OrderLineItems::getQty)
                .toList();

        Iterator<String> it1 = skuCodes.iterator();
        Iterator<Integer> it2 = qtys.iterator();



        // Check inventory services to see if sku-code is in stock
        InventoryResponse[] inventoryResponseArray = webClient.get().uri("http://localhost:8082/api/inventory",
                uriBuilder -> uriBuilder.queryParam("skuCode", skuCodes).build())
                .retrieve()
                .bodyToMono(InventoryResponse[].class)
                .block();

        boolean allProductsInStock = Arrays.stream(inventoryResponseArray)
                                            .allMatch(InventoryResponse::isInStock);


        if (allProductsInStock) {
            orderRepository.save(order);
            log.info("All Items in stock, saving order.");
            log.info("Order Number: {} was placed", order.getOrderNumber());
            log.info("Updating Inventory from Order Service");
//            inventoryService.updateInventory(skuCodes);
            while (it1.hasNext() && it2.hasNext()) {
                webClient.post().uri("http://localhost:8082/api/inventory",
                                uriBuilder -> uriBuilder.queryParam("skuCode").queryParam("qty").build())
                        .retrieve()
                        .bodyToMono(InventoryUpdate[].class)
                        .block();
            }


        } else {
            throw new IllegalArgumentException("Products are out of stock. Check Later");
        }


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
