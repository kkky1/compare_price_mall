package com.yk.controller;

import com.yk.domain.Product;
import com.yk.dto.SearchRequest;
import com.yk.dto.SearchResponse;
import com.yk.mapper.ProductMapper;
import com.yk.service.ProductService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import util.ResponseResult;

import java.util.List;

/**
 * (Product)表控制层
 *
 * @author makejava
 * @since 2025-07-25 16:34:06
 */
@RestController
@RequestMapping("product")
@Slf4j
public class ProductController {

    @Resource
    private ProductService productService;

    @Resource
    private ProductMapper productMapper;

    @GetMapping("/list")
    public ResponseResult list(@RequestParam(value = "name",required = false)
                               String name){
        List<Product> list = productMapper.selectListByName(name);
        log.debug("查询结果：{}", list);
        return ResponseResult.success(list);
    }

    @GetMapping("/hello")
    public ResponseResult hello(@RequestParam(value = "name",required = false)
                               String name){
        log.debug("查询结果：{}");
        return ResponseResult.success("hello");
    }

    /**
     * 调用 爬虫接口 来爬取对应的商品信息
     */
    @PostMapping("/crawProduct")
    public ResponseResult crawProduct(@RequestBody SearchRequest crawlProductRequest){
        SearchResponse searchResponse = productService.crawProduct(crawlProductRequest);
        return ResponseResult.success(searchResponse);
    }





}

