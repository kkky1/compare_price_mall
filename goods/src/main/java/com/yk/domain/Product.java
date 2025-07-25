package com.yk.domain;

import java.util.Date;
import java.io.Serializable;

/**
 * 商品信息表(Product)实体类
 *
 * @author makejava
 * @since 2025-08-17 15:46:09
 */
public class Product implements Serializable {
    private static final long serialVersionUID = -52114246894514472L;
    /**
     * 主键ID
     */
    private Long id;
    /**
     * 平台(taobao/jd)
     */
    private String platform;
    /**
     * 商品链接
     */
    private String itemLink;
    /**
     * 商品名称
     */
    private String itemName;
    /**
     * 商品价格
     */
    private String itemPrice;
    /**
     * 商品图片URL
     */
    private String itemImage;
    /**
     * 店铺名称
     */
    private String itemShop;
    /**
     * 店铺链接
     */
    private String shopLink;
    /**
     * 搜索关键词
     */
    private String searchKeyword;
    /**
     * 搜索页数
     */
    private Integer searchPages;
    /**
     * 状态(1:正常 0:无效)
     */
    private Integer status;
    /**
     * 创建时间
     */
    private Date createdAt;
    /**
     * 更新时间
     */
    private Date updatedAt;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getItemLink() {
        return itemLink;
    }

    public void setItemLink(String itemLink) {
        this.itemLink = itemLink;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getItemPrice() {
        return itemPrice;
    }

    public void setItemPrice(String itemPrice) {
        this.itemPrice = itemPrice;
    }

    public String getItemImage() {
        return itemImage;
    }

    public void setItemImage(String itemImage) {
        this.itemImage = itemImage;
    }

    public String getItemShop() {
        return itemShop;
    }

    public void setItemShop(String itemShop) {
        this.itemShop = itemShop;
    }

    public String getShopLink() {
        return shopLink;
    }

    public void setShopLink(String shopLink) {
        this.shopLink = shopLink;
    }

    public String getSearchKeyword() {
        return searchKeyword;
    }

    public void setSearchKeyword(String searchKeyword) {
        this.searchKeyword = searchKeyword;
    }

    public Integer getSearchPages() {
        return searchPages;
    }

    public void setSearchPages(Integer searchPages) {
        this.searchPages = searchPages;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

}

