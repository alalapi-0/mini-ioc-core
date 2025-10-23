package com.example.ioc.annotations; // 声明包路径

import java.lang.annotation.Documented; // 引入 @Documented 以便生成到 Javadoc
import java.lang.annotation.Retention; // 引入 @Retention 指定注解保留策略
import java.lang.annotation.RetentionPolicy; // 引入 RetentionPolicy 设置为 RUNTIME
import java.lang.annotation.Target; // 引入 @Target 限定注解的使用位置
import java.lang.annotation.ElementType; // 引入 ElementType 枚举常量集合

/**
 * 标记容器启动完成后需要自动调用的方法。
 * <p>
 * 设计说明：
 * 1) 仅用于方法级别，因此 @Target(ElementType.METHOD)。
 * 2) 需要在运行时通过反射检测与调用，因此 @Retention(RetentionPolicy.RUNTIME)。
 * 3) 约定被标注的方法必须“无参数”；该约束无法由编译器强制，容器应在运行时校验。
 */
@Documented // 让注解信息进入 Javadoc
@Retention(RetentionPolicy.RUNTIME) // 运行时可见，容器可通过反射在启动阶段调用
@Target(ElementType.METHOD) // 仅能作用于方法
public @interface InvokeOnStart { // 定义 InvokeOnStart 注解，用于标注启动时回调的方法
    // 该注解不携带属性；方法签名需为“无参”，由容器的运行时逻辑进行校验与调用
}
