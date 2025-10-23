package com.example.demo.components; // 演示组件所在包（在 com.example 下，确保被扫描到）

import com.example.ioc.annotations.Component; // 引入 @Component 注解以标记为组件

/**
 * 极简扫描探针，用于验证 Round 4 的包扫描是否生效。
 * 无任何业务逻辑。
 */
@Component // 标记该类应被容器识别为组件
public class ScanProbe { // 空类：仅用于被扫描到
    // no-op
} // ScanProbe 结束
