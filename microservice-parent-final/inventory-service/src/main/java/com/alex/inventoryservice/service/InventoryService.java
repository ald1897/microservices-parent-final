package com.alex.inventoryservice.service;

import com.alex.inventoryservice.dto.InventoryResponse;
import com.alex.inventoryservice.dto.InventoryUpdate;
import com.alex.inventoryservice.repository.InventoryRepository;
import com.alex.inventoryservice.model.Inventory;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.StreamUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;


import java.util.Optional;
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
                                .build()
                ).toList();
    }

//    @Transactional()
//    @SneakyThrows
//    public void updateInventory(List<String> skuCode, List<Integer> qty) {
//        log.info("Updating Inventory for " + skuCode);
//
//        List<Pair> list = new ArrayList<Pair>();
//
//        for (String sc : skuCode) {
//            Pair pair = new Pair();
//            list.add(pair);
//            pair.setString(sc);
//        }
//        for(Integer q : qty) {
//            pair.setInteger(q);
//        }
//
//
//        for (Pair p : list) {
//            System.out.println(p.getString());
//            System.out.println(p.getInteger());
//
//        }
////        for (String sc : skuCode) {
////
////            for(Integer q : qty) {
////                Inventory inventory = inventoryRepository.findBySkuCode(sc);
////                log.info(String.valueOf("inventory.getQty() " + inventory.getQty()));
////                log.info(String.valueOf("inventory.getSkuCode() " + inventory.getSkuCode()));
////                log.info(String.valueOf("inventory.getID() " + inventory.getId()));
////                log.info(String.valueOf("sc : " + sc));
////                log.info(String.valueOf("q : " + q));
////                inventory.setQty(inventory.getQty()-q);
////                log.info(String.valueOf("After Inventory Update inventory.getQty() " + inventory.getQty()));
////
////            }
////
////        }
////        Inventory inventory = inventoryRepository.findBySkuCode(skuCode).stream()
////                .map(inventory ->
////                        InventoryUpdate.builder()
////                                .skuCode(inventory.getSkuCode())
////                                .qty(inventory.getQty())
////                                .build()
////                ).toList();
////        log.info(inventoryList.toString());
//
//    }

    @Transactional()
    @SneakyThrows
    public void updateInventory(String skuCode, Integer qty) {
        log.info("Updating Inventory for " + skuCode + " By amount of " + qty);
        Inventory inventory = inventoryRepository.findBySkuCode(skuCode);
        int new_qty = inventory.getQty()-qty;
        inventory.setQty(new_qty);
        log.info("New Qty for " + skuCode + ": " + inventory.getQty());

    }

}