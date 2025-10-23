package com.example.ioc.annotations; // 声明当前类所在的包路径，用于组织与管理类文件

import java.lang.annotation.Documented; // 引入 @Documented 元注解，使该注解出现在 Javadoc 中
import java.lang.annotation.Retention; // 引入 @Retention 元注解，用于指定注解的保留策略
import java.lang.annotation.RetentionPolicy; // 引入 RetentionPolicy 枚举，指定注解在 RUNTIME 阶段可见
import java.lang.annotation.Target; // 引入 @Target 元注解，用于限定注解的使用位置
import java.lang.annotation.ElementType; // 引入 ElementType 枚举，表示注解可用于的 Java 元素类型

/**
 * 标记一个类为 IoC 容器可管理的“组件（Bean）”。
 * <p>
 * 设计说明：
 * 1) 仅用于“类型级别”（类、接口、枚举），因此 @Target(ElementType.TYPE)。
 * 2) 需要在运行时通过反射读取，因此 @Retention(RetentionPolicy.RUNTIME)。
 * 3) 提供可选的 value() 作为 Bean 名称；若未指定，容器可使用类名或约定规则。
 */
@Documented // 指示该注解会出现在 Javadoc 中，便于文档化
@Retention(RetentionPolicy.RUNTIME) // 指定注解在运行时依然保留，便于反射机制读取
@Target(ElementType.TYPE) // 指定该注解只能放在“类型”（类/接口/枚举）上
public @interface Component { // 定义一个名为 Component 的注解，用于标注可被容器识别的组件
    String value() default ""; // 可选的 Bean 名称，默认为空字符串，表示未显式命名
}
