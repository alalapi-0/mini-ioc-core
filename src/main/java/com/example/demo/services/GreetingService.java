package com.example.demo.services; // 演示服务所在包

import com.example.ioc.annotations.Component; // 引入 @Component 注解以声明组件

/**
 * 返回一条问候语的简单服务。 // 类用途说明
 */
@Component // 标记为容器组件
public class GreetingService { // 问候服务实现类
    public String hello() { // 返回问候语的方法
        return "Hello, IOC!"; // 固定内容，用于验证输出
    } // hello 方法结束
} // GreetingService 类结束
