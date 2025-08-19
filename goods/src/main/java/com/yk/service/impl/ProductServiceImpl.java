package com.yk.service.impl;
import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yk.domain.Product;
import com.yk.dto.PriceTendency;
import com.yk.dto.ProductMinPrice;
import com.yk.dto.SearchRequest;
import com.yk.dto.SearchResponse;
import com.yk.feign.UserFeignClient;
import com.yk.mapper.ProductMapper;
import com.yk.service.ProductService;
import constants.ProductPrice;
import constants.UserConstants;
import domain.User;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import util.ResponseResult;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * (Product)表服务实现类
 *
 * @author makejava
 * @since 2025-07-25 16:34:18
 */
@Service
@Slf4j
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    @Resource
    private ProductMapper productMapper;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;


    @Autowired
    private RestTemplate restTemplate;
    
    @Resource
    private UserFeignClient userFeignClient;

    @Resource
    private HttpServletRequest request;

    @Value("${market.spider.api.url:http://localhost:5000}")
    private String marketSpiderApiUrl;

    @Value("${market.spider.api.specify.url:http://localhost:5001}")
    private String marketSpiderSpecifyApiUrl;

    @Override
    public ResponseResult searchByNameOrList(String name) {


        return ResponseResult.success(productMapper.selectListByName(name) );
    }

    @Override
    public SearchResponse crawProduct(SearchRequest request) {

//       请求远程的爬虫接口
        try {
            log.info("开始搜索商品，关键词: {}, 平台: {}, 页数: {}",
                    request.getKeyword(), request.getPlatform(), request.getMaxPages());
//            查看缓存中是否有该信息
            List<String> searchHistory = (List<String>) redisTemplate.opsForValue().get(UserConstants.PRODUCT_NAME_KEY);
            if (searchHistory == null){
                searchHistory = new ArrayList<>();
            }
            log.debug("缓存中已存在的关键词: {}", searchHistory);

//            查看缓存中是否有该关键词
            if (searchHistory != null && isProductExistInCache(request.getKeyword())){
                log.debug("缓存中已存在该关键词");
//                直接在数据库中查找
                SearchResponse searchResponse = new SearchResponse();
                searchResponse.setStatus("success");
                searchResponse.setMessage("从数据库中查询到商品");
                searchResponse.setKeyword(request.getKeyword());
                searchResponse.setProducts(productMapper.selectListByName(request.getKeyword()));
                searchResponse.setTotalCount(searchResponse.getProducts().size());
                return searchResponse;
            }


            // 1. 调用爬虫API
            String apiUrl = marketSpiderApiUrl + "/api/search";
            ResponseEntity<SearchResponse> response = restTemplate.postForEntity(
                    apiUrl,
                    request,
                    SearchResponse.class
            );

            SearchResponse searchResponse = response.getBody();
            if (searchResponse == null || !"success".equals(searchResponse.getStatus())) {
                throw new RuntimeException("爬虫API调用失败: " +
                        (searchResponse != null ? searchResponse.getMessage() : "未知错误"));
            }
            log.debug("从爬虫API获取到 {} 个商品", searchResponse.getProducts());

            // 2. 保存商品到数据库
            if (searchResponse.getProducts() != null && !searchResponse.getProducts().isEmpty()) {
                // 设置搜索相关信息
                searchResponse.getProducts().forEach(product -> {
                    product.setSearchKeyword(request.getKeyword());
                    product.setSearchPages(request.getMaxPages());
                    product.setStatus(1);
                });

                saveProducts(searchResponse.getProducts());
                log.info("成功保存 {} 个商品到数据库", searchResponse.getProducts().size());
            }

            return searchResponse;

        } catch (Exception e) {
            log.error("搜索商品失败", e);
            SearchResponse errorResponse = new SearchResponse();
            errorResponse.setStatus("error");
            errorResponse.setMessage("搜索失败: " + e.getMessage());
            return errorResponse;
        }

    }

//    根据商品名称查询商品
    public ProductMinPrice getProductMinPrice(String request){
        //       请求远程的爬虫接口
        try {
            log.info("开始搜索商品， 关键词: {} ",request );

            // 1. 调用爬虫API
            if (request == null || request.isEmpty()){
                throw new RuntimeException("商品名称不能为空");
            }
            String encodedRequest = URLEncoder.encode(request, StandardCharsets.UTF_8);
            String apiUrl = marketSpiderSpecifyApiUrl + "/api/specific/search?product_name=" + request;
            System.out.println(apiUrl);
            log.debug("调用的url: {}", apiUrl);
            ResponseEntity<ProductMinPrice> response = restTemplate.getForEntity(
                    apiUrl,
                    ProductMinPrice.class
            );

            ProductMinPrice productMinPrice = response.getBody();
            if (productMinPrice == null || !"success".equals(productMinPrice.getStatus())) {
                throw new RuntimeException("爬虫API调用失败: " +
                        (productMinPrice != null ? productMinPrice.getMessage() : "未知错误"));
            }
            log.debug("从爬虫API获取到 {} 个商品", productMinPrice.getProducts());


            return productMinPrice;

        } catch (Exception e) {
            log.error("搜索商品失败", e);
            ProductMinPrice errorResponse = new ProductMinPrice();
            errorResponse.setStatus("error");
            errorResponse.setMessage("搜索失败: " + e.getMessage());
            return errorResponse;
        }
    }

    @Override
    public ResponseResult getSubscribedProducts() {

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

            ResponseResult userInfo = userFeignClient.getUserInfo(token);
            if (userInfo.getStatus() != 200) {
                log.error("获取用户信息失败: {}", userInfo.getMessage());
                return ResponseResult.fail("获取用户信息失败: " + userInfo.getMessage());
            }

            User user = BeanUtil.copyProperties(userInfo.getData(), User.class);
            Long userId = user.getId();

            if (userId == null) {
                return ResponseResult.fail("无法获取用户ID");
            }

            log.debug("获取用户 {} 的订阅商品列表", userId);

            // 从Redis中获取用户的订阅商品列表
            // 修复：直接获取Long类型的商品ID集合，不是List<Long>
            Set<Object> subscribedProductIds = (Set<Object>) redisTemplate.opsForSet().members(ProductPrice.USER_PRODUCT_CONNECTION + userId);

            log.debug("从Redis中获取到用户 {} 的订阅商品ID: {}", userId, subscribedProductIds);

            if (subscribedProductIds == null || subscribedProductIds.isEmpty()) {
                log.info("用户 {} 暂无订阅商品", userId);
                return ResponseResult.success(Collections.emptyList());
            }
            
            log.debug("用户订阅的商品ID: {}", subscribedProductIds);
            
            List<Product> products = new ArrayList<>();

            // 修复：直接遍历商品ID，不需要get(0)
            for (Object productIdObj : subscribedProductIds) {
                try {
                    Long productId = Long.valueOf(productIdObj.toString());
                    Product product = productMapper.selectById(productId);
                    if (product != null) {
                        products.add(product);
                    } else {
                        log.warn("商品ID {} 在数据库中不存在", productId);
                    }
                } catch (Exception e) {
                    log.warn("处理商品ID {} 时出错: {}", productIdObj, e.getMessage());
                }
            }

            log.info("用户 {} 订阅了 {} 个商品", userId, products.size());
            return ResponseResult.success(products);

        } catch (Exception e) {
            log.error("获取订阅列表失败", e);
            return ResponseResult.fail("获取订阅列表失败: " + e.getMessage());
        }
    }


    //    查看缓存中是否有该缓存
    private boolean isProductExistInCache(String productName) {
        List<String> productNames = (List<String>) redisTemplate.opsForValue().get(UserConstants.PRODUCT_NAME_KEY);
        if (productNames == null) {
            return false;
        }
        for (String name : productNames) {
            if (name.contains(productName)) {
                return true;
            }
        }
        return false;
    }


    @Transactional
    public void saveProducts(List<Product> products) {
        if (products != null && !products.isEmpty()) {
            // 从 Redis 取缓存
            List<String> buffProducts = (List<String>) redisTemplate.opsForValue().get(UserConstants.PRODUCT_NAME_KEY);
            if (buffProducts == null) {
                buffProducts = new ArrayList<>(); // 初始化，避免 NPE
            }

            // 存数据库
            products.forEach(productMapper::insert);

            // 存商品名到缓存
            for (Product product : products) {
                buffProducts.add(product.getItemName());
            }

            // 重新存回 Redis
            redisTemplate.opsForValue().set(UserConstants.PRODUCT_NAME_KEY, buffProducts);
            log.debug("已将 {} 个商品名称存储到 Redis", products.size());
        }
    }




}
