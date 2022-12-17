package com.alex.orderservice.dto.OrderResponse;

import com.alex.orderservice.dto.OrderLineItemsDto;
import com.alex.orderservice.model.OrderLineItems;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponse {
    private List<OrderLineItems> orderLineItemsList;
}
