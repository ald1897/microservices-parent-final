package com.alex.inventoryservice.controller;


import com.alex.inventoryservice.dto.InventoryResponse;
import com.alex.inventoryservice.dto.InventoryUpdate;
import com.alex.inventoryservice.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<InventoryResponse> isInStock(@RequestParam List<String> skuCode) {
        return inventoryService.isInStock(skuCode);

    }

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public void updateInventory(@RequestParam String skuCode, @RequestParam Integer qty) {
//        Update the inventory based on the sku code
        inventoryService.updateInventory(skuCode, qty);

    }
}
