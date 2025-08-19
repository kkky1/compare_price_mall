package com.yk.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

/**
 * 价格提醒消息实体类
 * 用于在消息队列中传递价格下降通知信息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PriceAlertMessage implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 商品ID
     */
    private Long productId;
    
    /**
     * 商品名称
     */
    private String productName;
    
    /**
     * 原价格
     */
    private Double oldPrice;
    
    /**
     * 新价格
     */
    private Double newPrice;
    
    /**
     * 订阅用户ID集合
     */
    private Set<Object> userIds;
    
    /**
     * 提醒时间
     */
    private Date alertTime;
    
    /**
     * 降价幅度
     */
    private Double priceDecrease;
    
    /**
     * 降价百分比
     */
    private Double priceDecreasePercent;
    
    public PriceAlertMessage(Long productId, String productName, Double oldPrice, Double newPrice, Set<Object> userIds) {
        this.productId = productId;
        this.productName = productName;
        this.oldPrice = oldPrice;
        this.newPrice = newPrice;
        this.userIds = userIds;
        this.alertTime = new Date();
        this.priceDecrease = oldPrice - newPrice;
        this.priceDecreasePercent = (this.priceDecrease / oldPrice) * 100;
    }
}
