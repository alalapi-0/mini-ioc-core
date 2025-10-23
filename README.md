# 迷你 IOC 容器超详细实现手册（适合零基础）——作业要求原文

> 以下为课程给定的作业说明，保留其原貌，置于 README 最前，便于验收核对。

【在此完整粘贴老师提供的“作业要求”全文，从第 0 节到第 11 节，逐字保留】

---

# mini-ioc-core（项目说明）

本仓库用于按作业要求从零实现一个“注解 + 反射”的迷你 IoC 容器，并通过 10 轮迭代逐步完善功能及文档。以下内容为 Round 9 的 README 终稿，覆盖项目特性、运行方式、方法职责、作业映射、调试指引与扩展思路。

## ① 项目简介与特性

- 迷你 IoC（Inversion of Control，控制反转）容器：通过注解声明组件，容器负责实例创建、依赖注入与生命周期管理。
- 注解驱动 + 反射：使用自定义 `@Component`、`@Inject`、`@InvokeOnStart` 注解，结合反射扫描与调用，无需 XML 或额外配置文件。
- 构造器优先 + 字段注入：优先选择带 `@Inject` 的构造器完成依赖装配，若不存在则回退到无参构造器，并在实例化后处理 `@Inject` 字段。
- 启动回调：在容器启动后统一触发 `@InvokeOnStart` 无参方法，可用于打印欢迎语或执行初始化逻辑。
- 纯教学实现：不依赖 Spring/Guice 等框架，仅覆盖核心流程，便于理解 IoC/DI（Dependency Injection，依赖注入）的基本原理。

## ② 快速开始

```bash
# 构建（不运行测试）
mvn -q -DskipTests package

# 运行 Demo（正式入口）
java -cp target/classes com.example.demo.App
```

预期输出（关键行）：

```
Hello, IOC!
Container started.
```

## ③ 目录结构

```
src/main/java/com/example/
├─ ioc/
│  ├─ annotations/
│  │  ├─ Component.java
│  │  ├─ Inject.java
│  │  └─ InvokeOnStart.java
│  └─ Container.java
└─ demo/
   ├─ App.java
   ├─ components/
   │  ├─ StartupRunner.java
   │  ├─ ScanProbe.java
   │  └─ GammaRunner.java
   └─ services/
      ├─ GreetingService.java
      ├─ AlphaService.java
      └─ BetaService.java
```

## ④ 核心流程图（ASCII）

```
App.main()
    │
    ▼
Container.start()
    │
    ├─ scanComponents(basePackage) ──► Set<ComponentClass>
    │
    ├─ for each ComponentClass:
    │     └─ getBean(type)
    │          ├─ singletons.get(type)? yes → return
    │          └─ createInstance(type)
    │               ├─ find @Inject ctor? yes → resolve args via getBean()
    │               ├─ else use no-arg ctor
    │               └─ performFieldInjection(@Inject fields via getBean())
    │          └─ putSingleton(type, instance) [+ namedBeans if value()]
    │
    └─ invokeStartCallbacks()  // call @InvokeOnStart no-arg methods
```

## ⑤ 方法职责表（对照实现轮次）

| 方法 | 职责 | 轮次 |
|---|---|---|
| `scanComponents(String)` | 扫描 `@Component`（file/jar） | Round 4 |
| `getBean(Class<T>)` | 命中缓存/实例化并缓存 | Round 5 |
| `createInstance(Class<T>)` | 构造器优先 + 字段注入 + 循环依赖检测 | Round 6 |
| `start()` | 扫描→实例化→回调 | Round 7 |
| `invokeStartCallbacks()` | 仅无参 `@InvokeOnStart` | Round 7 |
| `singletonCount()` | 观察缓存条目 | Round 5 |

## ⑥ 与“作业 0~11 节”的逐条映射对照表

| 作业章节 | 仓库中的对应实现/文件 | 如何验证 |
|---|---|---|
| 0. 准备和先体验 | `com.example.demo.App`、`GreetingService`、`StartupRunner` | `mvn -q -DskipTests package && java -cp target/classes com.example.demo.App` |
| 1. 三大注解 | `Component.java` / `Inject.java` / `InvokeOnStart.java` | 查看源码或 `jshell --class-path target/classes` 加载并检查 `@Retention(RUNTIME)` |
| 2. Demo 组件 | `GreetingService`、`StartupRunner`、`ScanProbe` | 运行 Demo 输出 `Hello, IOC!` |
| 3. 容器骨架 | `Container.java`（字段/方法签名） | 编译通过；`new Container("com.example").getBasePackage()` 返回配置值 |
| 4. 包扫描 | `scanComponents`、`scanDirectory`、`scanJarEntries` | `jshell --class-path target/classes` 中调用 `new Container("com.example").scanComponents("com.example").size()` |
| 5. 单例缓存 | `getBean`、`putSingleton`、`singletonCount` | `jshell` 中重复 `getBean(ScanProbe.class)`，比较是否同一实例 |
| 6. 依赖注入 | `createInstance`（构造器优先 + 字段注入） | `jshell` 调用 `container.getBean(BetaService.class).ping()` 预期 `beta->AlphaService` |
| 7. 启动回调 | `invokeStartCallbacks` | 运行 Demo，观察启动阶段输出 |
| 8. 从零实现清单 | README“方法职责表”“流程图” | 按文档操作可复现容器功能 |
| 9. 常见报错 | README“⑦ 调试与定位（常见报错）” | 对照症状排查配置/注入问题 |
| 10. 可选练习 | README“⑨ 附录：可选扩展 `@LogExecution`” | 根据思路自行实现扩展注解 |
| 11. 验收清单 | README“⑧ 验收清单（最终自检表）” | 自检勾选确认 |

> `jshell` 示例（可直接复制执行）：
>
> ```bash
> jshell --class-path target/classes <<'EOF'
> import com.example.ioc.Container;
> import com.example.demo.components.ScanProbe;
> import com.example.demo.services.BetaService;
> var c = new Container("com.example");
> c.start();
> var probeA = c.getBean(ScanProbe.class);
> var probeB = c.getBean(ScanProbe.class);
> System.out.println(probeA == probeB);
> System.out.println(c.getBean(BetaService.class).ping());
> /exit
> EOF
> ```

## ⑦ 调试与定位（常见报错）

- `ClassNotFoundException`：类名或路径转换错误，或组件未放在 `basePackage` 下；检查 `scanComponents` 生成的包路径与包声明是否一致。
- `IllegalAccessException`：未调用 `setAccessible(true)` 即访问私有构造器/字段；确认 `createInstance` 与 `performFieldInjection` 中的可访问性设置。
- `NoSuchMethodException`：类缺少无参构造器且没有 `@Inject` 构造器；为目标类新增无参构造器或唯一的 `@Inject` 构造器。
- `IllegalStateException: Circular dependency`：组件形成 A↔B 等循环依赖；通过引入接口、拆分职责或懒加载手段打破环路。
- 未触发 `@InvokeOnStart`：方法带参或组件未被扫描；确保方法无参、位于 `com.example` 包下且类含 `@Component`。
- Windows 路径包含空格：`scanDirectory` 读取 `file:` URL 时需 `URLDecoder.decode(..., "UTF-8")`；保持默认实现即可避免 `%20`。

## ⑧ 验收清单（最终自检表）

- [ ] 能构建与运行：`mvn -q -DskipTests package && java -cp target/classes com.example.demo.App`
- [ ] 控制台包含 `Hello, IOC!` 与 `Container started.`
- [ ] 三大注解均为 `RUNTIME` 且用途清晰
- [ ] 扫描能发现 Demo 组件
- [ ] 同类多次 `getBean` 返回同一实例
- [ ] 构造器注入与字段注入均验证通过
- [ ] `@InvokeOnStart` 无参方法被自动调用
- [ ] README 逐条映射作业 0~11 节
- [ ] 未提交任何二进制/构建产物（`.gitignore` 生效）

## ⑨ 附录：可选扩展 `@LogExecution`

- 设计目标：为启动回调提供轻量级计时/日志扩展，可配置日志级别（`level`）、时间单位（`unit`）、自定义提示语（`message`）。
- 注解示意：

  ```java
  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.METHOD)
  public @interface LogExecution {
      String level() default "INFO";
      ChronoUnit unit() default ChronoUnit.MILLIS;
      String message() default "";
  }
  ```

- 挂钩思路：在 `invokeStartCallbacks()` 中包裹回调调用逻辑，若方法同时标注 `@LogExecution`，则记录开始/结束时间并按配置格式化输出。
- 伪代码：

  ```java
  long t0 = System.nanoTime();
  method.invoke(bean);
  long t1 = System.nanoTime();
  LogExecution meta = method.getAnnotation(LogExecution.class);
  if (meta != null) {
      long elapsed = meta.unit().between(Instant.ofEpochSecond(0, t0), Instant.ofEpochSecond(0, t1));
      System.out.printf("[%s] %s %s (%d %s)%n", meta.level(), bean.getClass().getSimpleName(), method.getName(), elapsed, meta.unit());
  }
  ```

  > 仅提供思路，当前仓库未引入该注解的实际实现。

## ⑩ 二进制与生成物政策（重申）

- 本仓库严禁提交任何构建产物，包括 `.class`、`.jar`、`.war`、`target/` 目录等；`.gitignore` 已配置忽略。
- PR 审核中如发现二进制文件，应在合并前删除并追加 `.gitignore` 规则，确保版本库仅包含源码与文档。

---

> Round 9 已完成 README 终稿整理。后续 Round 10 将聚焦于最小化测试/自检脚本与提交核对表，持续保持零二进制提交，可选地说明如何在本地生成 Javadoc（不纳入仓库）。

## 🧾 提交前核对表（Round 10）

在提交前请执行一次自检脚本：

#### Linux/macOS：

```bash
bash verify.sh
```

#### Windows：

```bat
verify.bat
```

#### 脚本验证内容

1. 检查 Maven 与 Java 版本。
2. 编译项目（跳过测试）。
3. 运行 Demo 并检测输出是否包含：

   ```
   Hello, IOC!
   Container started.
   ```
4. 检查是否有二进制/构建产物被追踪。
5. 输出 “🎉 项目通过基本验证，可提交” 即代表通过。

#### 若脚本执行失败

* 请先检查源码、清理 `target/`，重新构建。
* 确保 `GreetingService` 与 `StartupRunner` 位于 `com.example.demo` 包下。
* 如仍失败，可手动执行：

  ```bash
  mvn clean && mvn -q -DskipTests package && java -cp target/classes com.example.demo.App
  ```
