package com.yk.service;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yk.domain.Product;
import com.yk.dto.ProductMinPrice;
import com.yk.dto.SearchRequest;
import com.yk.dto.SearchResponse;
import org.apache.ibatis.annotations.Mapper;
import util.ResponseResult;

import java.util.List;

/**
 * (Product)表服务接口
 *
 * @author makejava
 * @since 2025-07-25 16:34:17
 */
public interface ProductService extends IService<Product> {

    ResponseResult searchByNameOrList(String name);

    SearchResponse crawProduct(SearchRequest crawlProductRequest);

    ProductMinPrice getProductMinPrice(String productName);

    ResponseResult getSubscribedProducts();
}
