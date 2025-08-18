package com.yk.controller;

import com.yk.domain.Product;
import com.yk.dto.PriceTendency;
import com.yk.dto.ProductMinPrice;
import com.yk.dto.SearchRequest;
import com.yk.dto.SearchResponse;
import com.yk.mapper.ProductMapper;
import com.yk.service.ProductService;
import constants.RabbitMqConstants;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;
import util.ResponseResult;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static constants.ProductPrice.PRODUCT_PRICE_DECREASE_IDS;
import static constants.ProductPrice.PRODUCT_PRICE_TREND;

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
    private RabbitTemplate rabbitTemplate;

    @Resource
    private RedisTemplate redisTemplate;

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





    // 每隔 1  分钟执行一次
    @Scheduled(fixedRate = 60 * 1000) // 每分钟执行一次
    public void checkPrice() {
        log.info("开始检查商品价格");
        // 从redis中读取需要监控的商品id
        List<Long> productIds = (List<Long>) redisTemplate.opsForValue().get(PRODUCT_PRICE_DECREASE_IDS);
        if (productIds == null || productIds.isEmpty()) {
            return;
        }

        for (Long productId : productIds) {
            try {
                String productName = productMapper.selectById(productId).getItemName();
                ProductMinPrice productMinPrice = productService.getProductMinPrice(productName);
                if (productMinPrice == null || productMinPrice.getPrice_statistics() == null) {
                    continue;
                }
                Double currentPrice = productMinPrice.getPrice_statistics().getMin_price();

                // 获取历史价格
                List<PriceTendency> priceList = (List<PriceTendency>) redisTemplate.opsForValue().get(PRODUCT_PRICE_TREND + productId);
                if (priceList == null) {
                    priceList = new ArrayList<>();
                }

                if (priceList.isEmpty()) {
                    PriceTendency priceTendency = new PriceTendency();
                    priceTendency.setPrice(currentPrice);
                    priceTendency.setTime(new Date());
                    priceList.add(priceTendency);
                    redisTemplate.opsForValue().set(PRODUCT_PRICE_TREND + productId, priceList);
                } else {
                    Double lastPrice = priceList.get(priceList.size() - 1).getPrice();
                    if (currentPrice < lastPrice) {
                        // 价格下降,通知用户
                        String msg = "商品 " + productName + " 的价格下降了, 当前价格为 " + currentPrice + ", 上一次的价格为 " + lastPrice;
                        rabbitTemplate.convertAndSend(RabbitMqConstants.PRICE_DECREASE_EXCHANGE,
                                RabbitMqConstants.PRICE_DECREASE_ROUTING_KEY, msg);
                    }
                    PriceTendency priceTendency = new PriceTendency();
                    priceTendency.setPrice(currentPrice);
                    priceTendency.setTime(new Date());
                    priceList.add(priceTendency);

                    // 限制只保留最近50条，避免内存膨胀
                    if (priceList.size() > 50) {
                        priceList = priceList.subList(priceList.size() - 50, priceList.size());
                    }
                    redisTemplate.opsForValue().set(PRODUCT_PRICE_TREND + productId, priceList);
                }

            } catch (Exception e) {
                log.error("监控商品价格失败, productId=" + productId, e);
            }
        }
    }


    @GetMapping("/getProductMinPrice")
    public ResponseResult getProductMinPrice(@RequestParam("name") String name){
        ProductMinPrice productMinPrice = productService.getProductMinPrice(name);
        return ResponseResult.success(productMinPrice);
    }

    /**
     * 获取商品价格趋势
     * @param productId
     * @return
     */
    @GetMapping("/getPriceTendency")
    public ResponseResult getPriceTendency(@RequestParam("productId") Long productId){
        List<PriceTendency> priceList = (List<PriceTendency>) redisTemplate.opsForValue().get(PRODUCT_PRICE_TREND + productId);
        return ResponseResult.success(priceList);
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

