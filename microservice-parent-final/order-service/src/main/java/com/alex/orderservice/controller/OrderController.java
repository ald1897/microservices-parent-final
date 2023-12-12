package com.alex.orderservice.controller;

import com.alex.orderservice.dto.DeleteOrderRequest;
import com.alex.orderservice.dto.OrderRequest;
import com.alex.orderservice.dto.OrderResponse;
import com.alex.orderservice.model.Order;
import com.alex.orderservice.service.OrderIdNotFoundException;
import com.alex.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public String placeOrder(@RequestBody OrderRequest orderRequest){
        orderService.placeOrder(orderRequest);
        return "Order Placed Successfully for " + orderRequest.getOrderLineItemsDtoList().size() + " items in the amount of $ " + orderRequest.getOrderLineItemsDtoList().stream().mapToDouble(orderLineItemsDto -> orderLineItemsDto.getPrice().doubleValue()).sum() + "";
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<OrderResponse> getAllOrders(){
        return orderService.getAllOrders();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public String updateOrder(@RequestBody OrderRequest orderRequest){
        orderService.updateOrder(orderRequest);
        return "Order Placed Successfully for " + orderRequest.getOrderLineItemsDtoList().size() + " items in the amount of $ " + orderRequest.getOrderLineItemsDtoList().stream().mapToDouble(orderLineItemsDto -> orderLineItemsDto.getPrice().doubleValue()).sum() + "";
    }

    @GetMapping("/{orderId}")
    @ResponseStatus(HttpStatus.OK)
    public OrderResponse getOrderById(@PathVariable Long orderId) throws OrderIdNotFoundException {
        return orderService.getOrderById(orderId);
    }

    @DeleteMapping("/{orderId}")
    @ResponseStatus(HttpStatus.OK)
    public void deleteOrderById(@PathVariable Long orderId) throws OrderIdNotFoundException {
        orderService.deleteOrderById(orderId);
    }

}
