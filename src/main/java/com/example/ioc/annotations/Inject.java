package com.example.ioc.annotations; // 声明当前类所在的包路径

import java.lang.annotation.Documented; // 引入 @Documented，使注解出现在 Javadoc 中
import java.lang.annotation.Retention; // 引入 @Retention 指定注解保留策略
import java.lang.annotation.RetentionPolicy; // 引入 RetentionPolicy 以选择 RUNTIME
import java.lang.annotation.Target; // 引入 @Target 限定注解可用位置
import java.lang.annotation.ElementType; // 引入 ElementType 枚举常量集合

/**
 * 标记“依赖注入（Dependency Injection）”的注入点。
 * <p>
 * 设计说明：
 * 1) 同时支持“字段注入”和“构造器注入”，因此 @Target 选择 FIELD 与 CONSTRUCTOR。
 * 2) 需要在运行时通过反射识别注入点，因此 @Retention(RetentionPolicy.RUNTIME)。
 * 3) 不包含任何属性，仅作为“标记注解”使用。
 */
@Documented // 该注解会出现在 Javadoc 中
@Retention(RetentionPolicy.RUNTIME) // 运行时可见，容器可通过反射读取注入点信息
@Target({ElementType.FIELD, ElementType.CONSTRUCTOR}) // 允许标注在字段与构造器上
public @interface Inject { // 定义 Inject 注解，表示依赖注入点
    // 无属性的标记注解；是否必填由容器逻辑在运行时决定
}
