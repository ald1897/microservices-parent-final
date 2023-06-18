package com.alex.orderservice.service;

import com.alex.orderservice.dto.*;
import com.alex.orderservice.model.Order;
import com.alex.orderservice.model.OrderLineItems;
import com.alex.orderservice.repository.OrderRepository;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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
    private final WebClient.Builder webClientBuilder;

    public void placeOrder(OrderRequest orderRequest) {
        //placeOrder Method takes in the OrderRequest object passed in from the controller
        log.info("Inside placeOrder of OrderService");
        log.info("OrderRequest: " + orderRequest);
        //Create new order object, set the order number, and set the orderLineItemsList
        Order order = createOrder(orderRequest);
        log.info("Order: " + order);
        // Create a list of skuCodes from the orderLineItemsList
        List<String> skuCodes = createSkuCodeList(order);
        log.info("SkuCodes: " + skuCodes);
        // Create a list of qtys from the orderLineItemsList
        List<Integer> qtys = createQtyList(order);
        log.info("Qtys: " + qtys);
        //Create a method that creates 2 iterators and a map to house the skuCode and qty
        Multimap<String, Integer> map = createIteratorMap(order, skuCodes, qtys);
        log.info("Multimap: " + map);
        // Create a method to check if the order skuCodes are valid and if the qtys are valid
        InventoryResponse[] validOrder = checkIfValidOrder(order, skuCodes, qtys);
        log.info("Checking if order is valid & if all items are in stock");
        // if the validOrder is returned then check allItemsInStock
        if (validOrder != null) {
            // Create a method to check if all items are in stock
            boolean allItemsInStock = checkIfAllItemsInStock(validOrder);
            log.info("All items in stock: " + allItemsInStock);
            // Create a method to update the inventory
            if (allItemsInStock) {
                log.info("All Items in stock, Updating inventory");
                updateInventory(map);
                log.info("Saving Order.");
                saveOrder(order);
            } else {
                log.info("All Items not in Stock, not saving order.");
                throw new RuntimeException("All Items not in Stock, not saving order.");
            }
        } else {
            log.info("validOrder is NULL, not saving order.");
            throw new RuntimeException("validOrder is NULL, not saving order.");
        }
    }

    private  InventoryResponse[] checkIfValidOrder(Order order, List<String> skuCodes, List<Integer> qtys) {
        // Create an array of inventoryResponses by executing a GET request to http://localhost:8080/api/inventory?skuCode=x&qty=y
        log.info("Checking if order is valid & if all items are in stock");
       InventoryResponse[] inventoryResponseArray = new InventoryResponse[0];
       log.info("Inventory Response Array: " + Arrays.toString(inventoryResponseArray));
        try {
            inventoryResponseArray = webClientBuilder.build().get()
                    .uri("http://inventory-service/api/inventory",
                            uriBuilder -> uriBuilder.queryParam("skuCode", skuCodes).queryParam("qty", qtys).build())
                    .retrieve()
                    .bodyToMono(InventoryResponse[].class)
                    .block();
            // add logging so i can see the inventoryResponse
            log.info("Inventory Response Array: " + Arrays.toString(inventoryResponseArray));
        } catch (Exception e) {
            //  Block of code to handle errors
            log.error("Error during inventory check: " + e.getMessage());
            inventoryResponseArray = null;
        }
        if (inventoryResponseArray == null) {
            log.error("Error during inventory check: Inventory Response Empty. Order is invalid.");
        } else {
            return inventoryResponseArray;}

        return null;
    }

    private boolean checkIfAllItemsInStock(InventoryResponse[] validOrder) {
        // Create a boolean to check if all items are in stock
        boolean allItemsInStock = true;
        // Iterate through the inventoryResponseArray to check if all items are in stock
        for (InventoryResponse inventoryResponse : validOrder) {
            if (inventoryResponse.isInStock() == false) {
                allItemsInStock = false;
            }
        }
        return allItemsInStock;
    }

    private void saveOrder(Order order) {
        log.info("All Items in stock, saving order.");
        orderRepository.save(order);
        log.info("Order: {} was placed", order.getId());
    }

    private Multimap<String, Integer> createIteratorMap(Order order, List<String> skuCodes, List<Integer> qtys) {
        // Create iterators to loop through each list in sync
        Iterator<String> it1 = skuCodes.iterator();
        Iterator<Integer> it2 = qtys.iterator();

        // Store iterator data here in key/value pairs
        Multimap<String, Integer> map = ArrayListMultimap.create();
        while (it1.hasNext() && it2.hasNext()) {
            map.put(it1.next(), it2.next());
        }
        return map;
    }

    private List<Integer> createQtyList(Order order) {
        return order.getOrderLineItemsList().stream()
                .map(OrderLineItems::getQty)
                .toList();
    }

    private void updateInventory(Multimap<String, Integer> map) {
        // Iterate through multimap list so skuCode and qty are lined up
        for (Map.Entry entry : map.entries()) {

            // Store skuCode and qty in a variable
            String skuCode = String.valueOf(entry.getKey());
            Integer qty = (Integer) entry.getValue();

            // Send a post request to inventory service with the skuCode and qty to be deducted
            log.info("Sending POST Request to inventory-service/api/inventory?skuCode=skuCode&qty=qty for skuCode: " + skuCode + " and qty: " + qty + "");
            InventoryUpdate inventoryUpdate = webClientBuilder.build().post()
                    .uri("http://inventory-service/api/inventory",
                            uriBuilder -> uriBuilder.queryParam("skuCode", skuCode).queryParam("qty", qty).build())
                    .retrieve()
                    .bodyToMono(InventoryUpdate.class)
                    .block();
        }
        log.info("Inventory Updated");
    }

    private List<String> createSkuCodeList(Order order) {
        return order.getOrderLineItemsList().stream()
                .map(OrderLineItems::getSkuCode)
                .toList();
    }

    private Order createOrder(OrderRequest orderRequest) {
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());
        List<OrderLineItems> orderLineItems = orderRequest.getOrderLineItemsDtoList()
                .stream()
                .map(this::mapToDto)
                .toList();
        order.setOrderLineItemsList(orderLineItems);
        return order;
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

    public void deleteOrderById(Long orderId) throws OrderIdNotFoundException{
       Optional<Order> order = orderRepository.findById(orderId);
        if (order.isPresent()) {
            orderRepository.deleteById(orderId);
            log.info("Order Deleted with ID: {}", orderId);
        } else {
            log.info("Order with ID: {} does not exist", orderId);
        }
    }

}
