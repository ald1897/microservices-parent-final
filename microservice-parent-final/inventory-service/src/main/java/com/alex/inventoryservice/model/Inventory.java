package com.alex.inventoryservice.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;


@Entity
@Table(name = "t_inventory")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
@Builder
public class Inventory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String skuCode;
    private Integer qty;

    public void resupply(int qty, int resupply) {
        log.info("Resupplying inventory by " + resupply);
        this.qty = qty+resupply;
        log.info("Inventory restocked to " + this.qty);
    }
}
