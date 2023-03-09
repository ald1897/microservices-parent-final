package com.alex.inventoryservice.repository;

import com.alex.inventoryservice.model.Inventory;
import com.alex.inventoryservice.service.SkuCodeNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long >{
    Inventory findBySkuCode(String skuCode) throws SkuCodeNotFoundException;
    List<Inventory> findBySkuCodeIn(List<String> skuCode) throws SkuCodeNotFoundException;
}
