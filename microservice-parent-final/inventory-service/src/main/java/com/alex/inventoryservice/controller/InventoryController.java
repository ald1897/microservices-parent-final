package com.alex.inventoryservice.controller;


import com.alex.inventoryservice.dto.InventoryRequest;
import com.alex.inventoryservice.dto.InventoryResponse;
import com.alex.inventoryservice.service.InventoryService;
import com.alex.inventoryservice.service.SkuCodeNotFoundException;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Iterator;
import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @RequestMapping("/all")
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<InventoryResponse> getAllInventories() {
        return inventoryService.getAllInventories();
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public InventoryResponse getInventoryBySkuCode(@RequestParam String skuCode) throws SkuCodeNotFoundException {
        return inventoryService.getInventoryBySkuCode(skuCode);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<InventoryResponse> isInStock(@RequestParam List<String> skuCode,@RequestParam List<Integer> qty) {
        Iterator<String> it1 = skuCode.iterator();
        Iterator<Integer> it2 = qty.iterator();
        Multimap<String, Integer> map = ArrayListMultimap.create();
        while (it1.hasNext() && it2.hasNext()) {
            map.put(it1.next(), it2.next());
        }
        return inventoryService.isInStock(map);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public void updateInventory(@RequestParam String skuCode, @RequestParam Integer qty) throws SkuCodeNotFoundException {
//        Update the inventory based on the sku code
        inventoryService.updateInventory(skuCode, qty);
    }
    @RequestMapping("/add")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void addInventory(@RequestBody InventoryRequest inventoryRequest) {
//        Update the inventory based on the sku code
        inventoryService.addInventory(inventoryRequest);
    }

    @RequestMapping("/delete")
    @DeleteMapping
    @ResponseStatus(HttpStatus.OK)
    public void deleteInventory(@RequestParam String sC) throws SkuCodeNotFoundException {
//        Update the inventory based on the sku code
        inventoryService.deleteInventory(sC);
    }

    @RequestMapping("deleteAll")
    @DeleteMapping
    @ResponseStatus(HttpStatus.OK)
    public void deleteAllInventory() {
//        Update the inventory based on the sku code
        inventoryService.deleteAllInventories();

    }
}
