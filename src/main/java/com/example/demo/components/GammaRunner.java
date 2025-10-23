package com.example.demo.components; // 组件所在包

import com.example.ioc.annotations.Component; // 引入 @Component
import com.example.ioc.annotations.Inject; // 引入 @Inject
import com.example.demo.services.BetaService; // 引入被注入的服务类型

/**
 * 演示字段注入：容器应在实例化后为字段赋值。
 * （Round 7 才会添加 @InvokeOnStart 并自动调用方法）
 */
@Component // 标记为组件
public class GammaRunner { // 字段注入演示类

    @Inject // 该字段应在实例创建后被注入
    private BetaService beta; // 依赖的服务

    public String runOnce() { // 返回调用结果
        return beta.ping(); // 间接触达 AlphaService
    } // runOnce 方法结束
} // GammaRunner 类结束
