package com.yk.service.impl;
import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yk.domain.Product;
import com.yk.dto.ProductMinPrice;
import com.yk.dto.SearchRequest;
import com.yk.dto.SearchResponse;
import com.yk.mapper.ProductMapper;
import com.yk.service.ProductService;
import constants.UserConstants;
import jakarta.annotation.Resource;
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
import java.util.List;
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
    private RedisTemplate<String, List<String>> redisTemplate;

    @Autowired
    private RestTemplate restTemplate;

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
            List<String> searchHistory = redisTemplate.opsForValue().get(UserConstants.PRODUCT_NAME_KEY);
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



//    查看缓存中是否有该缓存
    private boolean isProductExistInCache(String productName) {
        List<String> productNames = redisTemplate.opsForValue().get(UserConstants.PRODUCT_NAME_KEY);
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
            List<String> buffProducts = redisTemplate.opsForValue().get(UserConstants.PRODUCT_NAME_KEY);
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
