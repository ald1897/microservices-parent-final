package com.alex.orderservice.dto;

import lombok.*;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class OrderRequest { private List<OrderLineItemsDto> orderLineItemsDtoList; }