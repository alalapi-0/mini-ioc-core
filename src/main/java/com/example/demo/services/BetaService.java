package com.example.demo.services; // 演示服务所在包

import com.example.ioc.annotations.Component; // 引入 @Component
import com.example.ioc.annotations.Inject; // 引入 @Inject

/**
 * 通过构造器注入 AlphaService 的服务。
 */
@Component // 标记为组件
public class BetaService { // 依赖 AlphaService 的服务

    private final AlphaService alpha; // 保存构造器注入的依赖

    @Inject // 指定该构造器用于依赖注入
    public BetaService(AlphaService alpha) { // 构造器注入 AlphaService
        this.alpha = alpha; // 赋值依赖
    } // 构造器结束

    public String ping() { // 供调试的方法
        return "beta->" + alpha.name(); // 使用被注入的依赖
    } // ping 方法结束
} // BetaService 类结束
