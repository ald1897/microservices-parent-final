package com.alex.orderservice.dto;

import lombok.*;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class OrderRequest { private List<OrderLineItemsDto> orderLineItemsDtoList;

    public Long getId() {
        // get the id from the orderLineItemsDtoList
        return orderLineItemsDtoList.get(0).getId();

    }
}