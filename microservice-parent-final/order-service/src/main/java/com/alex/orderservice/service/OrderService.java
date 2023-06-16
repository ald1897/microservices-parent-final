package com.alex.orderservice.service;

//import com.alex.inventoryservice.service.InventoryService;
import com.alex.orderservice.dto.*;
import com.alex.orderservice.model.Order;
import com.alex.orderservice.model.OrderLineItems;
import com.alex.orderservice.repository.OrderRepository;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
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


    private final WebClient.Builder webClientBuilder;

    //placeOrder Method takes in the OrderRequest object passed in from the controller
    public void placeOrder(OrderRequest orderRequest) {
        log.info("Inside placeOrder of OrderService");
        log.info("OrderRequest: " + orderRequest);
        //Create new order object, set the order number, and set the orderLineItemsList
        Order order = createOrder(orderRequest);
        // add logging
        log.info("Order: " + order);
        // Create a list of skuCodes from the orderLineItemsList
        List<String> skuCodes = createSkuCodeList(order);
        // add logging
        log.info("SkuCodes: " + skuCodes);
        // Create a list of qtys from the orderLineItemsList
        List<Integer> qtys = createQtyList(order);
        // add logging
        log.info("Qtys: " + qtys);
        //Create a method that creates 2 iterators and a map to house the skuCode and qty
        Multimap<String, Integer> map = createIteratorMap(order, skuCodes, qtys);
        // add logging
        log.info("Multimap: " + map);
        // Create a method to Update Inventory Service by reducing Quantity by amount that was ordered for each skuCode
        updateInventory(order);
        // add logging
        log.info("Updating Inventory from Order Service");
        // Create a method to save the order
        saveOrder(order);
        // add logging
        log.info("All Items in stock, saving order.");
        log.info("Order Number: {} was placed", order.getOrderNumber());
        // Create an array of inventoryResponses by executing a GET request to http://localhost:8082/api/inventory?skuCode=x&qty=y
        InventoryResponse[] inventoryResponseArray = new InventoryResponse[0];
        try {
            inventoryResponseArray = webClientBuilder.build().get()
                    .uri("http://inventory-service/api/inventory",
                            uriBuilder -> uriBuilder.queryParam("skuCode", skuCodes).queryParam("qty", qtys).build())
                    .retrieve()
                    .bodyToMono(InventoryResponse[].class)
                    .block();
        } catch (Exception e) {
            //  Block of code to handle errors
            throw new IllegalArgumentException("Sku Code does not exist. Try again later.");


        }
//         Check if all items in the array are isInStock = TRUE
        assert inventoryResponseArray != null;
        for (InventoryResponse inventoryResponse : inventoryResponseArray) {
            if (!inventoryResponse.isInStock()) {
                log.info("Item is not in stock");
                throw new IllegalArgumentException("Item is not in stock. Try again later.");
            }
        }
        boolean allProductsInStock = true;

        // IF all items are in stock for the required amount, save the order and update the inventory amounts for the items
        if (allProductsInStock) {
            orderRepository.save(order);
            log.info("All Items in stock, saving order.");
            log.info("Order Number: {} was placed", order.getOrderNumber());

            log.info("Updating Inventory from Order Service");
            // Iterate through multimap list so skuCode and qty are lined up
            for (Map.Entry entry : map.entries()) {

                // Store skuCode and qty in a variable
                String skuCode = String.valueOf(entry.getKey());
                Integer qty = (Integer) entry.getValue();

                // Send a post request to inventory service with the skuCode and qty to be deducted
                log.info("Hitting inventory endpoint inventory-service/api/inventory...");
                InventoryUpdate inventoryUpdate = webClientBuilder.build().post()
                        .uri("http://inventory-service/api/inventory",
                                uriBuilder -> uriBuilder.queryParam("skuCode", skuCode).queryParam("qty", qty).build())
                        .retrieve()
                        .bodyToMono(InventoryUpdate.class)
                        .block();
            }

        } else {
            throw new IllegalArgumentException("Products are out of stock. Check Later");
        }


    }

    private void saveOrder(Order order) {

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

    private void updateInventory(Order order) {
        List<String> skuCodes = createSkuCodeList(order);
        List<Integer> qtys = order.getOrderLineItemsList().stream()
                .map(OrderLineItems::getQty)
                .toList();

        // Create iterators to loop through each list in sync
        Iterator<String> it1 = skuCodes.iterator();
        Iterator<Integer> it2 = qtys.iterator();

        // Store iterator data here in key/value pairs
        Multimap<String, Integer> map = ArrayListMultimap.create();
        while (it1.hasNext() && it2.hasNext()) {
            map.put(it1.next(), it2.next());
        }
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
}
