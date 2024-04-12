package com.aaaxing.example.entity;

import lombok.Data;

/**
 * @author axing
 * @date 2024-04-11
 */
@Data
public class Order {

    /**
     * 订单id
     */
    private Long id;

    /**
     * 产品id
     */
    private Long productId;

    /**
     * 数量
     */
    private Integer num;

    /**
     * 请求id
     */
    private String requestId;
}
