package com.example.demo; // 定义应用启动入口所在的 demo 包

import com.example.ioc.Container; // 引入容器核心类以便创建与启动 IoC 容器实例

/**
 * 项目正式启动入口（Round 8）。
 * <p>
 * 作用：
 * 1) 创建 IoC 容器实例，指定基础包为 "com.example"（覆盖 demo 与 ioc）。
 * 2) 调用容器的 start() 方法，触发：扫描组件 → 依赖注入 → 执行 @InvokeOnStart 回调。
 * 3) 控制台期望看到的关键输出由 StartupRunner 打印：
 *    - "Hello, IOC!"
 *    - "Container started."
 */
public class App { // 定义应用主类作为 JVM 启动入口
    public static void main(String[] args) { // Java 应用的标准入口方法，接收命令行参数
        Container container = new Container("com.example"); // 初始化容器并指定扫描根包为 com.example
        container.start(); // 启动容器以触发组件扫描、依赖注入与启动回调输出
    } // 结束 main 方法定义
} // 结束 App 类定义
