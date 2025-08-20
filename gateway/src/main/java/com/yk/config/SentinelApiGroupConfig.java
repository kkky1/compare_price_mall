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
 * Sentinel API分组配置
 * 为具体的API端点配置流控规则
 */
@Slf4j
@Configuration
public class SentinelApiGroupConfig {

    @PostConstruct
    public void initApiGroupRules() {
        initProductApiRules();
        initUserApiRules();
        log.info("Sentinel API group rules initialized successfully");
    }

    /**
     * 初始化商品服务API规则
     */
    private void initProductApiRules() {
        List<FlowRule> rules = new ArrayList<>();
        
        // 商品列表API
        FlowRule productListRule = new FlowRule();
        productListRule.setResource("/product/list");
        productListRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        productListRule.setCount(50);
        rules.add(productListRule);
        
        // 商品爬取API
        FlowRule productCrawRule = new FlowRule();
        productCrawRule.setResource("/product/crawProduct");
        productCrawRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        productCrawRule.setCount(30);
        rules.add(productCrawRule);
        
        // 商品订阅API
        FlowRule productSubscribeRule = new FlowRule();
        productSubscribeRule.setResource("/product/subscribePriceAlert");
        productSubscribeRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        productSubscribeRule.setCount(40);
        rules.add(productSubscribeRule);
        
        // 商品取消订阅API
        FlowRule productUnsubscribeRule = new FlowRule();
        productUnsubscribeRule.setResource("/product/unsubscribePriceAlert");
        productUnsubscribeRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        productUnsubscribeRule.setCount(40);
        rules.add(productUnsubscribeRule);
        
        // 我的订阅列表API
        FlowRule productMySubscriptionsRule = new FlowRule();
        productMySubscriptionsRule.setResource("/product/mySubscriptions");
        productMySubscriptionsRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        productMySubscriptionsRule.setCount(60);
        rules.add(productMySubscriptionsRule);
        
        // 商品价格趋势API
        FlowRule productPriceTendencyRule = new FlowRule();
        productPriceTendencyRule.setResource("/product/getPriceTendency");
        productPriceTendencyRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        productPriceTendencyRule.setCount(80);
        rules.add(productPriceTendencyRule);
        
        // 商品最低价格API
        FlowRule productMinPriceRule = new FlowRule();
        productMinPriceRule.setResource("/product/getProductMinPrice");
        productMinPriceRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        productMinPriceRule.setCount(70);
        rules.add(productMinPriceRule);
        
        FlowRuleManager.loadRules(rules);
        log.info("Product API rules initialized: {} rules", rules.size());
    }

    /**
     * 初始化用户服务API规则
     */
    private void initUserApiRules() {
        List<FlowRule> rules = new ArrayList<>();
        
        // 用户登录API
        FlowRule userLoginRule = new FlowRule();
        userLoginRule.setResource("/user/login");
        userLoginRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        userLoginRule.setCount(100);
        rules.add(userLoginRule);
        
        // 用户信息API
        FlowRule userInfoRule = new FlowRule();
        userInfoRule.setResource("/user/info");
        userInfoRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        userInfoRule.setCount(150);
        rules.add(userInfoRule);
        
        // 用户注册API
        FlowRule userRegisterRule = new FlowRule();
        userRegisterRule.setResource("/user/register");
        userRegisterRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        userRegisterRule.setCount(80);
        rules.add(userRegisterRule);
        
        // Token验证API
        FlowRule userValidateRule = new FlowRule();
        userValidateRule.setResource("/user/validate");
        userValidateRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        userValidateRule.setCount(200);
        rules.add(userValidateRule);
        
        FlowRuleManager.loadRules(rules);
        log.info("User API rules initialized: {} rules", rules.size());
    }
}
