package com.alex.inventoryservice.service;

import com.alex.inventoryservice.dto.InventoryRequest;
import com.alex.inventoryservice.dto.InventoryResponse;
import com.alex.inventoryservice.dto.InventoryUpdate;
import com.alex.inventoryservice.repository.InventoryRepository;
import com.alex.inventoryservice.model.Inventory;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.StreamUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;


import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    @Transactional(readOnly = true)
    @SneakyThrows
    public List<InventoryResponse> isInStock(List<String> skuCode) {
        log.info("Checking Inventory...");
        return inventoryRepository.findBySkuCodeIn(skuCode).stream()
                .map(inventory ->
                        InventoryResponse.builder()
                                .skuCode(inventory.getSkuCode())
                                .isInStock(inventory.getQty() > 0)
                                .qty(inventory.getQty())
                                .build()
                ).toList();
    }

    @Transactional(readOnly = true)
    @SneakyThrows
    public List<InventoryResponse> isInStock(Multimap<String, Integer> skuQtyMap) {

        log.info("Checking if skuCode is in stock...");

        //Create list of inventory responses to be returned
        List<InventoryResponse> inventoryResponses = new ArrayList<>();
        // Iterate through skuCodes in MM
        for (String skuCode : skuQtyMap.keySet()) {
            // Story qty in variable to compare with current stock
            int totalQty = skuQtyMap.get(skuCode).stream().mapToInt(Integer::intValue).sum();
//            log.info(String.valueOf(totalQty));
            // Get the inventory for the skuCode
            Inventory inventory;
            try {
                inventory = inventoryRepository.findBySkuCode(skuCode);
                inventoryResponses.add(InventoryResponse.builder()
                        .skuCode(inventory.getSkuCode())
                        .isInStock(inventory.getQty() > totalQty)
                        .qty(inventory.getQty())
                        .build());
//                log.info(String.valueOf(inventoryResponses));
            } catch (Exception e) {
                // Return a null object if the skuCode is not found
                log.info("SkuCode not found");
                return null;
            }
        }
        return inventoryResponses;
    }

    @Transactional()
    @SneakyThrows
    public void updateInventory(String skuCode, Integer qty) throws SkuCodeNotFoundException {

//        log.info("Updating Inventory for " + skuCode + " By amount of " + qty);

        Inventory inventory = inventoryRepository.findBySkuCode(skuCode);

        int new_qty = inventory.getQty()-qty;

        // If the remaining amount of inventory is >= 0 (not sold out)
        inventory.setQty(new_qty);

        log.info("Updated Qty for " + skuCode + ": " + inventory.getQty());
        if (inventory.getQty() < 0) {
            log.warn(skuCode + " IS BACK ORDERED BY " + (inventory.getQty()*-1) + " UNITS. TRIGGERING RESUPPLY LOGIC!");
            inventory.resupply(inventory.getQty(), 100);
        }
    }

    @Transactional()
    @SneakyThrows
    public void addInventory(InventoryRequest inventoryRequest) {
         Inventory inventory = Inventory.builder()
                .skuCode(inventoryRequest.getSkuCode())
                .id(inventoryRequest.getId())
                .qty(inventoryRequest.getQty())
                .build();

        inventoryRepository.save(inventory);
        log.info("Inventory {} is saved with qty of {}", inventory.getSkuCode(), inventory.getQty());
    }

    @Transactional(readOnly = true)
    @SneakyThrows
    public List<InventoryResponse> getAllInventories() {

        List<Inventory> inventoryList = inventoryRepository.findAll();
        log.info("Getting all {} Inventories...", inventoryList.size());

        return inventoryList.stream().map(this::mapToInventoryResponse).toList();
    }

    private InventoryResponse mapToInventoryResponse(Inventory inventory) {
        return InventoryResponse.builder()
                .skuCode(inventory.getSkuCode())
                .isInStock(inventory.getQty() > 0)
                .qty(inventory.getQty())
                .build();
    }
}