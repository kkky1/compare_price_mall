package com.yk.controller;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.yk.domain.Product;
import com.yk.dto.PriceAlertMessage;
import com.yk.dto.PriceTendency;
import com.yk.dto.ProductMinPrice;
import com.yk.dto.SearchRequest;
import com.yk.dto.SearchResponse;
import com.yk.feign.UserFeignClient;
import com.yk.mapper.ProductMapper;
import com.yk.service.ProductService;
import constants.ProductPrice;
import constants.RabbitMqConstants;
import domain.User;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;
import util.ResponseResult;

import java.util.*;

import static constants.ProductPrice.*;

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

    @Resource
    private UserFeignClient userFeignClient;

    @Resource
    private HttpServletRequest request;

    @GetMapping("/list")
    @SentinelResource(value = "ProductController#list", blockHandler = "listBlockHandler")
    public ResponseResult list(@RequestParam(value = "name",required = false)
                               String name){
        List<Product> list = productMapper.selectListByName(name);
        log.debug("查询结果：{}", list);
        return ResponseResult.success(list);
    }


    /**
     * 测试兜底返回机制
     * @param name
     * @return
     */
    @GetMapping("/hello")
    @SentinelResource(value = "ProductController#hello", blockHandler = "helloBlockHandler")
    public ResponseResult hello(@RequestParam(value = "name",required = false)
                               String name){
        ResponseResult authorization = userFeignClient.getUserInfo(request.getHeader("Authorization"));

        return authorization;
    }





    // 每隔 1  分钟执行一次
    @Scheduled(fixedRate = 60 * 60 * 1000) // 每小时执行一次
    public void checkPrice() {
        log.info("开始检查商品价格");

        // 从redis中读取需要监控的商品id
        List<Long> productIds = (List<Long>) redisTemplate.opsForValue().get(PRODUCT_PRICE_DECREASE_IDS);
        if (productIds == null || productIds.isEmpty()) {
            log.info("没有需要监控的商品");
            return;
        }

        for (Long productId : productIds) {
            try {
                // 查看商品下有多少位用户订阅
                Set<Object> userIds = redisTemplate.opsForSet().members(ProductPrice.PRICE_USER_CONNECTION + productId);
                if (userIds == null || userIds.isEmpty()) {
                    log.debug("商品 {} 没有订阅用户，跳过", productId);
                    continue;
                }

                String productName = productMapper.selectById(productId).getItemName();
                ProductMinPrice productMinPrice = productService.getProductMinPrice(productName);
                if (productMinPrice == null || productMinPrice.getPrice_statistics() == null) {
                    log.warn("商品 {} 获取价格信息失败", productId);
                    continue;
                }
                Double currentPrice = productMinPrice.getPrice_statistics().getMin_price();

                // 获取历史价格
                List<PriceTendency> priceList = null;
                try {
                    Object priceData = redisTemplate.opsForValue().get(PRODUCT_PRICE_TREND + productId);
                    if (priceData instanceof List) {
                        priceList = (List<PriceTendency>) priceData;
                    } else {
                        priceList = new ArrayList<>();
                        log.debug("商品 {} 从Redis获取的价格数据格式不正确，创建新的价格列表", productId);
                    }
                } catch (Exception e) {
                    log.warn("从Redis获取商品 {} 价格趋势失败，创建新的价格列表", productId, e);
                    priceList = new ArrayList<>();
                }
                
                if (priceList == null) {
                    priceList = new ArrayList<>();
                }

                if (priceList.isEmpty()) {
                    // 首次记录价格
                    PriceTendency priceTendency = new PriceTendency();
                    priceTendency.setPrice(currentPrice);
                    priceTendency.setTime(new Date());
                    priceList.add(priceTendency);
                    redisTemplate.opsForValue().set(PRODUCT_PRICE_TREND + productId, priceList);
                    log.info("商品 {} 首次记录价格: {}", productName, currentPrice);
                } else {
                    Double lastPrice = priceList.get(priceList.size() - 1).getPrice();
                    if (currentPrice < lastPrice) {
                        // 价格下降，发送降价通知消息
                        log.info("商品 {} 价格下降: {} -> {}, 通知 {} 个订阅用户", 
                                productName, lastPrice, currentPrice, userIds.size());
                        
                        // 创建价格提醒消息
                        PriceAlertMessage alertMessage = new PriceAlertMessage(
                            productId, productName, lastPrice, currentPrice, userIds
                        );

                        
                        // 发送到消息队列，让消费者处理具体的用户通知
                        rabbitTemplate.convertAndSend(
                            RabbitMqConstants.PRICE_DECREASE_EXCHANGE,
                            RabbitMqConstants.PRICE_DECREASE_ROUTING_KEY, 
                            alertMessage
                        );
                        
                        log.info("商品 {} 价格下降通知已发送到消息队列", productName);
                    } else if (currentPrice > lastPrice) {
                        log.info("商品 {} 价格上升: {} -> {}", productName, lastPrice, currentPrice);
                    } else {
                        log.debug("商品 {} 价格未变化: {}", productName, currentPrice);
                    }
                    
                    // 更新价格记录
                    PriceTendency priceTendency = new PriceTendency();
                    priceTendency.setPrice(currentPrice);
                    priceTendency.setTime(new Date());
                    priceList.add(priceTendency);

                    // 限制只保留最近50条，避免内存膨胀
                    if (priceList.size() > 50) {
                        // 完全避免subList，直接创建新的ArrayList
                        List<PriceTendency> newPriceList = new ArrayList<>();
                        int startIndex = priceList.size() - 50;
                        for (int i = startIndex; i < priceList.size(); i++) {
                            newPriceList.add(priceList.get(i));
                        }
                        priceList = newPriceList;
                        log.debug("商品 {} 价格记录已限制为最近50条", productId);
                    }
                    
                    // 存储到Redis
                    try {
                        redisTemplate.opsForValue().set(PRODUCT_PRICE_TREND + productId, priceList);
                        log.debug("商品 {} 价格趋势已更新到Redis，当前记录数: {}", productId, priceList.size());
                    } catch (Exception e) {
                        log.error("存储商品 {} 价格趋势到Redis失败", productId, e);
                    }
                }

            } catch (Exception e) {
                log.error("监控商品价格失败, productId=" + productId, e);
            }
        }
    }


    @GetMapping("/getProductMinPrice")
    @SentinelResource(value = "ProductController#getProductMinPrice", blockHandler = "getProductMinPriceBlockHandler")
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
    @SentinelResource(value = "ProductController#getPriceTendency", blockHandler = "getPriceTendencyBlockHandler")
    public ResponseResult getPriceTendency(@RequestParam("productId") Long productId){
        try {
            Object priceData = redisTemplate.opsForValue().get(PRODUCT_PRICE_TREND + productId);
            if (priceData instanceof List) {
                List<PriceTendency> priceList = (List<PriceTendency>) priceData;
                return ResponseResult.success( priceList);
            } else {
                log.warn("商品 {} 的价格趋势数据格式不正确", productId);
                return ResponseResult.success( new ArrayList<>());
            }
        } catch (Exception e) {
            log.error("获取商品 {} 价格趋势失败", productId, e);
            return ResponseResult.fail("获取价格趋势失败: " + e.getMessage());
        }
    }







    /**
     * 调用 爬虫接口 来爬取对应的商品信息
     */
    @PostMapping("/crawProduct")
    @SentinelResource(value = "ProductController#crawProduct", blockHandler = "crawProductBlockHandler")
    public ResponseResult crawProduct(@RequestBody SearchRequest crawlProductRequest){
        SearchResponse searchResponse = productService.crawProduct(crawlProductRequest);
        return ResponseResult.success(searchResponse);
    }

    /**
     * 用户订阅商品价格监控
     */
    @GetMapping("/subscribePriceAlert")
    @SentinelResource(value = "ProductController#subscribePriceAlert", blockHandler = "subscribePriceAlertBlockHandler")
    public ResponseResult subscribePriceAlert(@RequestParam("productId") Long productId) {
        log.info("收到订阅请求，商品ID: {}", productId);
        
        try {
            String token = request.getHeader("Authorization");
            if (token == null) {
                return ResponseResult.fail("未提供认证token");
            }

            // 处理Bearer token
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            log.debug("处理后的token: {}", token);

            // 通过OpenFeign获取用户信息
            ResponseResult userResult = userFeignClient.getUserInfo(token);
            if (userResult.getStatus() != 200) {
                log.error("获取用户信息失败: {}", userResult.getMessage());
                return ResponseResult.fail("获取用户信息失败: " + userResult.getMessage());
            }

            log.debug("用户信息获取成功: {}", userResult.getData());

            // 解析用户信息获取用户ID
            Object userData = userResult.getData();
            User user = BeanUtil.copyProperties(userData, User.class);
            Long userId = user.getId();
            
            if (userId == null) {
                log.error("用户信息中没有ID字段");
                return ResponseResult.fail("无法获取用户ID");
            }
            
            log.info("用户 {} 订阅商品 {} 价格监控", userId, productId);
            
            // 将用户添加到商品的订阅列表中
            String subscribeKey = ProductPrice.PRICE_USER_CONNECTION + productId;
            redisTemplate.opsForSet().add(subscribeKey, userId);


            // 将用户添加到商品的订阅列表中
            String userProductKey = ProductPrice.USER_PRODUCT_CONNECTION + userId;
            redisTemplate.opsForSet().add(userProductKey, productId);
            

            
            // 将商品添加到监控列表
            List<Long> productIds = (List<Long>) redisTemplate.opsForValue().get(PRODUCT_PRICE_DECREASE_IDS);
            if (productIds == null) {
                productIds = new ArrayList<>();
            }
            if (!productIds.contains(productId)) {
                productIds.add(productId);
                redisTemplate.opsForValue().set(PRODUCT_PRICE_DECREASE_IDS, productIds);
                log.info("商品 {} 已添加到监控列表", productId);
            }
            
            // 发送延迟消息到队列（用于延迟处理）
            rabbitTemplate.convertAndSend(
                RabbitMqConstants.DELAY_PRODUCT_ALERT_EXCHANGE, 
                RabbitMqConstants.PRODUCT_ALERT_ROUTING_KEY, 
                productId
            );
            log.debug("延迟消息已发送到队列: {}", productId);
            
            log.info("用户 {} 订阅商品 {} 价格监控成功", userId, productId);
            return ResponseResult.success("订阅成功");
            
        } catch (Exception e) {
            log.error("订阅商品价格监控失败", e);
            return ResponseResult.fail("订阅失败: " + e.getMessage());
        }
    }

    /**
     * 用户取消订阅商品价格监控
     */
    @PostMapping("/unsubscribePriceAlert")
    @SentinelResource(value = "ProductController#unsubscribePriceAlert", blockHandler = "unsubscribePriceAlertBlockHandler")
    public ResponseResult unsubscribePriceAlert(@RequestParam Long productId) {
        try {
            String token = request.getHeader("Authorization");
            if (token == null) {
                return ResponseResult.fail("未提供认证token");
            }

            ResponseResult userResult = userFeignClient.getUserInfo(token);
            if (userResult.getStatus() != 200) {
                return ResponseResult.fail("获取用户信息失败: " + userResult.getMessage());
            }

            // 解析用户信息获取用户ID
            User user = (User) userResult.getData();
            Long userId = user.getId();
            
            if (userId == null) {
                return ResponseResult.fail("无法获取用户ID");
            }

            // 从商品订阅列表中移除用户
            String subscribeKey = ProductPrice.PRICE_USER_CONNECTION + productId;
            redisTemplate.opsForSet().remove(subscribeKey, userId);
            
            // 检查该商品是否还有其他订阅用户
            Set<Object> remainingUsers = redisTemplate.opsForSet().members(subscribeKey);
            if (remainingUsers == null || remainingUsers.isEmpty()) {
                // 如果没有其他用户订阅，从监控列表中移除该商品
                List<Long> productIds = (List<Long>) redisTemplate.opsForValue().get(PRODUCT_PRICE_DECREASE_IDS);
                if (productIds != null && productIds.contains(productId)) {
                    productIds.remove(productId);
                    redisTemplate.opsForValue().set(PRODUCT_PRICE_DECREASE_IDS, productIds);
                    log.info("商品 {} 无订阅用户，已从监控列表中移除", productId);
                }
            }
            
            log.info("用户 {} 取消订阅商品 {} 价格监控成功", userId, productId);
            return ResponseResult.success("取消订阅成功");
            
        } catch (Exception e) {
            log.error("取消订阅失败", e);
            return ResponseResult.fail("取消订阅失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户订阅的商品列表
     */
    @GetMapping("/mySubscriptions")
    @SentinelResource(value = "ProductController#mySubscriptions", blockHandler = "mySubscriptionsBlockHandler")
    public ResponseResult getSubscribedProducts() {

        return productService.getSubscribedProducts();



    }

    // ========== Sentinel 限流降级处理方法 ==========
    
    public ResponseResult listBlockHandler(String name, BlockException ex) {
        log.warn("商品列表接口被限流，参数: {}", name);
        return ResponseResult.fail("服务繁忙，请稍后重试");
    }
    
    public ResponseResult helloBlockHandler(String name, BlockException ex) {
        log.warn("hello接口被限流，参数: {}", name);
        return ResponseResult.fail("服务繁忙，请稍后重试");
    }
    
    public ResponseResult getProductMinPriceBlockHandler(String name, BlockException ex) {
        log.warn("获取商品最低价格接口被限流，参数: {}", name);
        return ResponseResult.fail("服务繁忙，请稍后重试");
    }
    
    public ResponseResult getPriceTendencyBlockHandler(Long productId, BlockException ex) {
        log.warn("获取商品价格趋势接口被限流，商品ID: {}", productId);
        return ResponseResult.fail("服务繁忙，请稍后重试");
    }
    
    public ResponseResult crawProductBlockHandler(SearchRequest request, BlockException ex) {
        log.warn("爬取商品接口被限流，关键词: {}", request.getKeyword());
        return ResponseResult.fail("服务繁忙，请稍后重试");
    }
    
    public ResponseResult subscribePriceAlertBlockHandler(Long productId, BlockException ex) {
        log.warn("订阅商品价格监控接口被限流，商品ID: {}", productId);
        return ResponseResult.fail("服务繁忙，请稍后重试");
    }
    
    public ResponseResult unsubscribePriceAlertBlockHandler(Long productId, BlockException ex) {
        log.warn("取消订阅商品价格监控接口被限流，商品ID: {}", productId);
        return ResponseResult.fail("服务繁忙，请稍后重试");
    }
    
    public ResponseResult mySubscriptionsBlockHandler(BlockException ex) {
        log.warn("获取用户订阅列表接口被限流");
        return ResponseResult.fail("服务繁忙，请稍后重试");
    }


}

