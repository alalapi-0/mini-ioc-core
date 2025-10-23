package com.example.demo.services; // 演示服务所在包

import com.example.ioc.annotations.Component; // 引入 @Component 注解

/**
 * 一个无依赖的简单服务，用于被其他组件注入。
 */
@Component // 标记为可被容器管理的组件
public class AlphaService { // 简单服务类
    public String name() { // 返回服务名
        return "AlphaService"; // 固定字符串
    } // name 方法结束
} // AlphaService 类结束
