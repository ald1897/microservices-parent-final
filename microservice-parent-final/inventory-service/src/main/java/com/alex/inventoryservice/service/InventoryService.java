package com.alex.inventoryservice.service;

import com.alex.inventoryservice.repository.InventoryRepository;
import com.alex.inventoryservice.model.Inventory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;


import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    @Transactional(readOnly = true)
    public boolean isInStock(String skuCode) {

        // get a list of the inventories
//        List<Inventory> inventoryList = inventoryRepository.findAll();
        Optional<Inventory> inventory = inventoryRepository.findBySkuCode(skuCode);
        // put each inv into its own object
        if (inventoryRepository.findBySkuCode(skuCode).isPresent()) {
              // check if inv sku code matches var skucode
            if (inventory.get().getQty() > 0) {
                log.info("Sku-code {} is in stock.", skuCode);
                return true;
            } else if (inventory.get().getQty() <= 0) {
                log.info("Sku-code {} is out of stock.", skuCode);
                return false;
            }
        } else {
            return false;
        }
        return false;
    }
}
