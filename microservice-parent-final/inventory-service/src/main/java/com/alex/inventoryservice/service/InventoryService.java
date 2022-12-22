package com.alex.inventoryservice.service;

import com.alex.inventoryservice.dto.InventoryResponse;
import com.alex.inventoryservice.repository.InventoryRepository;
import com.alex.inventoryservice.model.Inventory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;


import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    @Transactional(readOnly = true)
    public List<InventoryResponse> isInStock(List<String> skuCode) {

        // get a list of the inventories
//        List<Inventory> inventoryList = inventoryRepository.findAll();
        return inventoryRepository.findBySkuCodeIn(skuCode).stream()
                .map(inventory ->
                        InventoryResponse.builder()
                                .skuCode(inventory.getSkuCode())
                                .isInStock(inventory.getQty() > 0)
                                .build()
        ).toList();
    }
}
