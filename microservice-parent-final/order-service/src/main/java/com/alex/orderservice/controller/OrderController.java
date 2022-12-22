package com.alex.orderservice.controller;

import com.alex.orderservice.dto.OrderRequest;
import com.alex.orderservice.dto.OrderResponse;
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
        return "Order Placed Successfully.";

    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<OrderResponse> getAllOrders(){
        return orderService.getAllOrders();
    }
    @DeleteMapping
    @ResponseStatus(HttpStatus.OK)
    public void deleteAllOrders(){
        orderService.deleteAllOrders();
    }

}
