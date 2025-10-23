package com.example.demo.components; // 启动组件所在包

import com.example.ioc.annotations.Component; // 引入 @Component 以标记组件
import com.example.ioc.annotations.Inject; // 引入 @Inject 以进行字段注入
import com.example.ioc.annotations.InvokeOnStart; // 引入 @InvokeOnStart 以启用启动回调
import com.example.demo.services.GreetingService; // 引入依赖的 GreetingService

/**
 * 在容器启动后自动执行的演示组件。 // 类作用说明
 */
@Component // 标记为组件
public class StartupRunner { // 启动回调演示类

    @Inject // 字段注入：由容器在实例化后注入 GreetingService
    private GreetingService greeting; // 依赖的问候服务

    @InvokeOnStart // 标记该方法应在容器启动完成后被自动调用
    public void onStart() { // 无参启动回调
        System.out.println(greeting.hello()); // 预期输出：Hello, IOC!
        System.out.println("Container started."); // 预期输出：Container started.
    } // onStart 方法结束
} // StartupRunner 类结束
