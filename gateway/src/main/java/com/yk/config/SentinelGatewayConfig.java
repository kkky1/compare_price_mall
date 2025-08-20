
package com.yk.config;

import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.BlockRequestHandler;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;



import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * Sentinel Gateway配置类
 * 配置网关层面的流量控制和熔断降级规则
 */

import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class SentinelGatewayConfig {

    @PostConstruct
    public void init() {
        initFlowRules();
        initDegradeRules();
        log.info("Sentinel Gateway configuration initialized successfully");
    }

    /**
     * 配置自定义阻塞请求处理器
     * 解决Spring Boot 3.x兼容性问题
     */
    @Bean
    public BlockRequestHandler blockRequestHandler() {
        return new CustomBlockRequestHandler();
    }

    /**
     * 初始化流控规则
     */
    private void initFlowRules() {
        List<FlowRule> rules = new ArrayList<>();

        // 商品服务路由级别流控规则
        FlowRule productRouteRule = new FlowRule();
        productRouteRule.setResource("product_route");
        productRouteRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        productRouteRule.setCount(100); // 每秒最多100个请求
        rules.add(productRouteRule);

        // 商品服务具体API流控规则
        FlowRule productListRule = new FlowRule();
        productListRule.setResource("/product/list");
        productListRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        productListRule.setCount(50); // 每秒最多50个请求
        rules.add(productListRule);

        FlowRule productCrawRule = new FlowRule();
        productCrawRule.setResource("/product/crawProduct");
        productCrawRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        productCrawRule.setCount(30); // 每秒最多30个请求
        rules.add(productCrawRule);

        FlowRule productSubscribeRule = new FlowRule();
        productSubscribeRule.setResource("/product/subscribePriceAlert");
        productSubscribeRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        productSubscribeRule.setCount(40); // 每秒最多40个请求
        rules.add(productSubscribeRule);

        FlowRule productMySubscriptionsRule = new FlowRule();
        productMySubscriptionsRule.setResource("/product/mySubscriptions");
        productMySubscriptionsRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        productMySubscriptionsRule.setCount(60); // 每秒最多60个请求
        rules.add(productMySubscriptionsRule);

        // 用户服务流控规则
        FlowRule userRouteRule = new FlowRule();
        userRouteRule.setResource("user_route");
        userRouteRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        userRouteRule.setCount(200); // 每秒最多200个请求
        rules.add(userRouteRule);

        // 用户服务具体API流控规则
        FlowRule userLoginRule = new FlowRule();
        userLoginRule.setResource("/user/login");
        userLoginRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        userLoginRule.setCount(100); // 每秒最多100个请求
        rules.add(userLoginRule);

        FlowRule userInfoRule = new FlowRule();
        userInfoRule.setResource("/user/info");
        userInfoRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        userInfoRule.setCount(150); // 每秒最多150个请求
        rules.add(userInfoRule);

        // 论坛服务流控规则
        FlowRule postRouteRule = new FlowRule();
        postRouteRule.setResource("post_route");
        postRouteRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        postRouteRule.setCount(150); // 每秒最多150个请求
        rules.add(postRouteRule);

        // 支付服务流控规则
        FlowRule paymentRouteRule = new FlowRule();
        paymentRouteRule.setResource("payment_route");
        paymentRouteRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        paymentRouteRule.setCount(80); // 每秒最多80个请求
        rules.add(paymentRouteRule);

        FlowRuleManager.loadRules(rules);
        log.info("Flow rules initialized: {} rules", rules.size());
    }

    /**
     * 初始化熔断降级规则
     */
    private void initDegradeRules() {
        List<DegradeRule> rules = new ArrayList<>();

        // 商品服务熔断规则
        DegradeRule productDegradeRule = new DegradeRule();
        productDegradeRule.setResource("product_route");
        productDegradeRule.setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_COUNT);
        productDegradeRule.setCount(5); // 5个异常后熔断
        productDegradeRule.setTimeWindow(10); // 熔断10秒
        rules.add(productDegradeRule);

        // 用户服务熔断规则
        DegradeRule userDegradeRule = new DegradeRule();
        userDegradeRule.setResource("user_route");
        userDegradeRule.setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_COUNT);
        userDegradeRule.setCount(3); // 3个异常后熔断
        userDegradeRule.setTimeWindow(15); // 熔断15秒
        rules.add(userDegradeRule);

        // 论坛服务熔断规则
        DegradeRule postDegradeRule = new DegradeRule();
        postDegradeRule.setResource("post_route");
        postDegradeRule.setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_COUNT);
        postDegradeRule.setCount(4); // 4个异常后熔断
        postDegradeRule.setTimeWindow(12); // 熔断12秒
        rules.add(postDegradeRule);

        // 支付服务熔断规则
        DegradeRule paymentDegradeRule = new DegradeRule();
        paymentDegradeRule.setResource("payment_route");
        paymentDegradeRule.setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_COUNT);
        paymentDegradeRule.setCount(2); // 2个异常后熔断
        paymentDegradeRule.setTimeWindow(20); // 熔断20秒
        rules.add(paymentDegradeRule);

        DegradeRuleManager.loadRules(rules);
        log.info("Degrade rules initialized: {} rules", rules.size());
    }
}

