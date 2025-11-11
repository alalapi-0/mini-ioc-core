# mini-ioc-core

一个使用 Java 原生注解与反射实现的迷你 IoC 容器示例项目。仓库包含容器核心实现 `com.example.ioc` 与演示代码 `com.example.demo`，适合作为理解控制反转与依赖注入的入门素材。

## 功能概览
- 扫描指定基础包下标注了 `@Component` 的类。
- 维护单例缓存，`getBean` 会返回同一个实例。
- 支持构造器优先、字段兜底的依赖注入（`@Inject`）。
- 启动时自动执行标注 `@InvokeOnStart` 且无参的方法。

## 环境要求
| 工具 | 版本建议 | 说明 |
| --- | --- | --- |
| JDK | 8+ | 使用 Java 8 语法与标准库反射能力。 |
| Maven | 3.6+ | 仅依赖默认中央仓库，无第三方库。 |
| Shell | bash/PowerShell/CMD | 运行构建与示例程序即可。 |

## 快速开始
1. 构建
   ```bash
   mvn -q -DskipTests package
   ```
2. 运行 Demo
   ```bash
   java -cp target/classes com.example.demo.App
   ```
3. 预期输出
   ```
   Hello, IOC!
   Container started.
   ```

想要一次性完成检查，可运行 `bash verify.sh`（Windows 使用 `verify.bat`），脚本会执行构建、启动 Demo 并检查仓库是否干净。

## 目录结构
```
src/main/java/com/example/
├─ ioc/
│  ├─ annotations/      // 定义 @Component/@Inject/@InvokeOnStart
│  └─ Container.java    // 容器核心实现
└─ demo/
   ├─ App.java          // 演示入口
   ├─ components/       // 组件示例（StartupRunner/GammaRunner/...）
   └─ services/         // 服务示例（GreetingService/AlphaService/...）
```

## 容器工作流程速览
1. 创建 `new Container("com.example")` 时保存基础扫描包。
2. `start()` 会：
   - 调用 `scanComponents` 枚举 classpath 中的 `@Component` 类型。
   - 逐个执行 `getBean`，若缓存缺失则通过 `createInstance` 完成构造器/字段注入。
   - 调用 `invokeStartCallbacks`，对所有单例查找并执行无参的 `@InvokeOnStart` 方法。
3. 运行期间可通过 `getBean(SomeType.class)` 获取容器管理的单例。

### 依赖注入要点
- 构造器注入：在类上保留一个带 `@Inject` 的构造器，容器会按参数类型递归获取依赖。
- 字段注入：在字段上标注 `@Inject`，实例创建后容器会设置字段值（包含私有字段）。
- 容器使用一个简单的集合 `inCreation` 追踪当前正在创建的类型，遇到循环依赖会抛出 `IllegalStateException`。

## 示例：获取自定义 Bean
```java
Container container = new Container("com.example");
container.start();
GammaRunner runner = container.getBean(GammaRunner.class);
System.out.println(runner.runOnce()); // 输出 beta->AlphaService
```

## 常见问题
| 现象 | 可能原因 | 建议 |
| --- | --- | --- |
| `ClassNotFoundException` | 包名/路径不在 `basePackage` 下 | 确保类位于 `com.example` 等扫描包中。 |
| `NoSuchMethodException` | 缺少无参构造器且未标注 `@Inject` 构造器 | 添加无参构造器或唯一的 `@Inject` 构造器。 |
| `IllegalStateException: Circular dependency` | 组件互相依赖形成环 | 拆分依赖或引入接口降低耦合。 |
| 未触发 `@InvokeOnStart` | 方法有参数或类未被扫描 | 确认方法无参且位于带 `@Component` 的类中。 |

## 开发提示
- 修改组件后重新运行 `java -cp ...` 观察依赖注入与回调顺序。
- `singletonCount()` 可用于断言容器中已创建的单例数量，方便调试。
- 在实验时可新建自己的包结构，避免直接修改参考实现。

## 提交前自检
```bash
bash verify.sh      # macOS/Linux
# 或者
verify.bat          # Windows
```

脚本会检测 Java/Maven 版本、执行构建与 Demo，并确认仓库未包含构建产物。
