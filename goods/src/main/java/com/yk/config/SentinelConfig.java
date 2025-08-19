package com.yk.config;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

/**
 * Sentinel配置类
 */
@Slf4j
@Configuration
public class SentinelConfig {

    @PostConstruct
    public void initSentinelRules() {
        List<FlowRule> rules = new ArrayList<>();
        
        // 为商品查询接口添加流控规则
        FlowRule productSearchRule = new FlowRule();
        productSearchRule.setResource("ProductController#list");
        productSearchRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        productSearchRule.setCount(10); // 每秒最多10个请求
        rules.add(productSearchRule);
        
        // 为商品爬取接口添加流控规则
        FlowRule productCrawRule = new FlowRule();
        productCrawRule.setResource("ProductController#crawProduct");
        productCrawRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        productCrawRule.setCount(5); // 每秒最多5个请求
        rules.add(productCrawRule);
        
        // 为订阅商品接口添加流控规则
        FlowRule subscribeRule = new FlowRule();
        subscribeRule.setResource("ProductController#subscribePriceAlert");
        subscribeRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        subscribeRule.setCount(20); // 每秒最多20个请求
        rules.add(subscribeRule);
        
        // 为获取商品最低价格接口添加流控规则
        FlowRule getProductMinPriceRule = new FlowRule();
        getProductMinPriceRule.setResource("ProductController#getProductMinPrice");
        getProductMinPriceRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        getProductMinPriceRule.setCount(15); // 每秒最多15个请求
        rules.add(getProductMinPriceRule);
        
        // 为获取价格趋势接口添加流控规则
        FlowRule getPriceTendencyRule = new FlowRule();
        getPriceTendencyRule.setResource("ProductController#getPriceTendency");
        getPriceTendencyRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        getPriceTendencyRule.setCount(20); // 每秒最多20个请求
        rules.add(getPriceTendencyRule);
        
        // 为获取订阅列表接口添加流控规则
        FlowRule mySubscriptionsRule = new FlowRule();
        mySubscriptionsRule.setResource("ProductController#mySubscriptions");
        mySubscriptionsRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        mySubscriptionsRule.setCount(30); // 每秒最多30个请求
        rules.add(mySubscriptionsRule);
        
        FlowRuleManager.loadRules(rules);
        log.info("Sentinel rules initialized successfully.");
    }
}
