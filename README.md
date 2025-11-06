# 迷你 IOC 容器超详细实现手册（适合零基础）——作业要求原文

> 以下为课程给定的作业说明，保留其原貌，置于 README 最前，便于验收核对。

【在此完整粘贴老师提供的“作业要求”全文，从第 0 节到第 11 节，逐字保留】

---

# mini-ioc-core（项目说明）

## 🧭 项目总览

- 这是一个从零实现的迷你 IoC（Inversion of Control，控制反转）容器，通过**自定义注解 + 反射**来完成组件扫描、单例管理、依赖注入以及启动回调。
- 仓库中同时提供演示项目 `com.example.demo`，让零基础读者也能看懂如何使用容器、如何编写组件，并通过控制台输出验证结果。
- 项目经历了多轮 Prompt 迭代（见下文“多轮 Prompt 演进记录”），当前版本已经具备：包扫描、构造器优先的依赖注入、字段注入、循环依赖检测、启动回调等核心功能。
- 所有源码均配有中文注释，适合快速理解；README 汇总了环境准备、技术栈、运行流程和常见问题，确保拿到仓库即可上手。

## 🧱 环境准备

| 工具 | 版本建议 | 说明 |
| --- | --- | --- |
| JDK | 8 及以上 | 代码按照 Java 8 语法编写，`pom.xml` 中的编译目标也是 1.8。 |
| Maven | 3.6+（推荐 3.9.x） | 负责依赖下载与构建，仓库不引入第三方依赖，使用默认中央仓库即可。 |
| Git | 任意较新版本 | 用于获取源码与提交作业。 |
| Shell / CMD | `bash` 或 `cmd`/PowerShell | `verify.sh`、`verify.bat` 提供了一键自检脚本。 |

> 小贴士：如果你不确定本地是否满足要求，可直接运行 `bash verify.sh`（macOS/Linux）或 `verify.bat`（Windows），脚本会依次检测 Maven、Java、构建结果、Demo 输出与仓库状态。

## 🛠 技术栈

- **Java SE 8**：使用标准库中的反射（`java.lang.reflect`）实现注解扫描与依赖注入。
- **Maven**：使用 `maven-compiler-plugin` 进行编译，无额外依赖，适合教学与理解原理。
- **注解与反射**：自定义 `@Component`、`@Inject`、`@InvokeOnStart` 注解，通过类加载器、`Class` 与 `Method` 等反射 API 实现容器逻辑。
- **脚本工具**：`verify.sh` 与 `verify.bat` 以中文注释演示如何批量完成构建、自检，便于提交前快速验证。

## 🔁 多轮 Prompt 演进记录

1. **Round 0**：创建仓库骨架，确认提交规范与 README 的作业要求占位。
2. **Round 1**：搭建包结构与基础占位类，`package-info.java` 初步说明模块用途。
3. **Round 2**：实现核心注解 `@Component`、`@Inject`、`@InvokeOnStart`，明确运行时可见性。
4. **Round 3**：编写 `Container` 骨架，声明字段、构造器和方法签名，为后续逻辑预留结构。
5. **Round 4**：完成包扫描能力，支持文件系统与 JAR 内部的 `.class` 搜索。
6. **Round 5**：实现单例缓存与 `getBean` 入口，确保同一类型只创建一次。
7. **Round 6**：补全 `createInstance`、构造器注入、字段注入与循环依赖检测。
8. **Round 7**：新增 `start()` 与 `invokeStartCallbacks()`，在启动后调用标注 `@InvokeOnStart` 的无参方法。
9. **Round 8**：整理 Demo 入口 `App`、服务与组件，演示完整启动流程并输出“Hello, IOC!”。
10. **Round 9**：完善 README、添加调试指南与作业映射表，中文注释覆盖全部源码。
11. **Round 10**：加入自检脚本 `verify.sh` / `verify.bat`，形成提交前核对表。

当前仓库即处于 Round 10 之后的状态，兼顾教学说明与可运行代码。

## 🚀 项目使用方法（零基础友好）

1. **获取源码**
   ```bash
   git clone https://example.com/mini-ioc-core.git
   cd mini-ioc-core
   ```
   若由老师下发压缩包，直接解压后进入目录即可。

2. **（可选）阅读 README 与源码注释**：先浏览本文件的“项目总览”“运行流程详解”章节，再打开 `src/main/java` 目录查看带中文注释的类。

3. **构建项目**
   ```bash
   mvn -q -DskipTests package
   ```
   Maven 会在 `target/classes` 产出字节码，整个过程不会下载额外第三方依赖，几秒内即可完成。

4. **运行演示程序**
   ```bash
   java -cp target/classes com.example.demo.App
   ```
   控制台应输出：
   ```
   Hello, IOC!
   Container started.
   ```

5. **一键自检（推荐）**
   - macOS/Linux：`bash verify.sh`
   - Windows：`verify.bat`

   脚本会自动执行步骤 3~4，并检查是否有二进制文件被错误提交，最后提示是否可以提交作业。

6. **进行实验**：尝试修改 `GreetingService` 返回值、添加新的组件或注入点，再次运行 `App`，观察容器如何自动管理依赖。

## 🔍 运行流程详解（从启动到每个方法）

以下内容逐行解释代码的运行顺序，适合第一次接触 IoC 的读者。

### 1. `App.main`（文件：`src/main/java/com/example/demo/App.java`）
1. JVM 调用 `main` 方法。
2. 创建 `Container` 实例，传入基础包名 `"com.example"`，告诉容器需要扫描哪些类。
3. 调用 `container.start()`，进入容器的生命周期管理流程。

### 2. `Container.start`（文件：`src/main/java/com/example/ioc/Container.java`）
1. 调用 `scanComponents(basePackage)`：扫描 `com.example` 下所有 `.class` 文件，筛选带 `@Component` 的类型，得到组件集合。
2. 遍历每个组件类型，调用 `getBean(type)`：确保该类型的单例已经创建；如缓存中没有，则会触发实例化与依赖注入。
3. 全部实例就绪后，执行 `invokeStartCallbacks()`：查找所有标注了 `@InvokeOnStart` 的无参方法，逐一调用，打印欢迎信息或执行初始化逻辑。

### 3. `scanComponents`
1. 根据包名计算路径（例如 `com/example`）。
2. 使用类加载器遍历所有资源 URL（既包含文件目录，也可能是 JAR）。
3. 资源是目录时调用 `scanDirectory`，逐层递归查找 `.class` 文件。
4. 资源是 JAR 时调用 `scanJarEntries`，遍历条目寻找对应的 `.class`。
5. 每当找到类文件，都会调用 `maybeAddComponentClass` 尝试加载，并判断是否带有 `@Component` 注解，满足条件才加入结果集合。

### 4. `getBean`
1. 先在 `singletons` Map 中查找，命中则直接返回已有实例。
2. 未命中时调用 `createInstance(type)` 真正创建对象。
3. 创建成功后交给 `putSingleton` 统一写入缓存，并根据 `@Component("name")` 的可选名称记录到 `namedBeans`。

### 5. `createInstance`
1. 检查是否出现循环依赖（`inCreation` Set），避免 A↔B 互相注入导致无限递归。
2. 查找是否存在唯一的 `@Inject` 构造器：
   - 如果有，使用 `resolveConstructorArgs` 对每个参数递归调用 `getBean`，得到依赖，再通过反射调用构造器。
   - 如果没有，退回到无参构造器，直接创建实例。
3. 调用 `performFieldInjection(instance)`：扫描对象的所有字段，对带 `@Inject` 的字段递归调用 `getBean` 获取依赖后赋值。
4. 返回创建好的实例，并在 `finally` 中将类型从 `inCreation` 集合中移除。

### 6. `invokeStartCallbacks`
1. 遍历所有已缓存的单例实例。
2. 对每个实例列出声明的方法，查找 `@InvokeOnStart` 标注。
3. 确认方法无参数后，打开访问权限（包括私有方法），调用它并记录耗时，最后恢复访问权限。
4. 若方法抛出异常，仅记录错误信息，不影响其他回调执行。

### 7. Demo 组件如何协作
1. `GreetingService.hello()` 返回字符串 “Hello, IOC!”。
2. `StartupRunner` 在字段上标注 `@Inject`，容器实例化它时会为 `greeting` 字段注入 `GreetingService`。
3. `StartupRunner.onStart()` 标注 `@InvokeOnStart`，因此在 `invokeStartCallbacks` 阶段会自动调用，打印问候语与“Container started.”。
4. `AlphaService`、`BetaService`、`GammaRunner` 用于演示构造器注入与字段注入：`BetaService` 通过构造器注入 `AlphaService`，`GammaRunner` 字段注入 `BetaService`，调用 `runOnce()` 会返回 `beta->AlphaService`。

### 8. ASCII 流程图（再次回顾）
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

## 📁 目录结构

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

## 📚 方法职责速查表

| 方法 | 职责 | 对应轮次 |
| --- | --- | --- |
| `scanComponents(String)` | 扫描 `@Component`（支持 file/jar） | Round 4 |
| `getBean(Class<T>)` | 命中缓存或实例化并缓存 | Round 5 |
| `createInstance(Class<T>)` | 构造器优先 + 字段注入 + 循环依赖检测 | Round 6 |
| `start()` | 启动容器：扫描 → 实例化 → 回调 | Round 7 |
| `invokeStartCallbacks()` | 调用所有无参 `@InvokeOnStart` 方法 | Round 7 |
| `singletonCount()` | 返回当前单例缓存数量 | Round 5 |

## 📘 作业章节映射对照表

| 作业章节 | 仓库中的对应实现/文件 | 如何验证 |
| --- | --- | --- |
| 0. 准备和先体验 | `com.example.demo.App`、`GreetingService`、`StartupRunner` | `mvn -q -DskipTests package && java -cp target/classes com.example.demo.App` |
| 1. 三大注解 | `Component.java` / `Inject.java` / `InvokeOnStart.java` | 阅读源码或在 `jshell` 中反射检查 `@Retention(RUNTIME)` |
| 2. Demo 组件 | `GreetingService`、`StartupRunner`、`ScanProbe` | 运行 Demo 输出 `Hello, IOC!` |
| 3. 容器骨架 | `Container.java`（字段/方法签名） | `new Container("com.example").getBasePackage()` 返回配置值 |
| 4. 包扫描 | `scanComponents`、`scanDirectory`、`scanJarEntries` | `jshell --class-path target/classes` 中调用 `new Container("com.example").scanComponents("com.example").size()` |
| 5. 单例缓存 | `getBean`、`putSingleton`、`singletonCount` | `jshell` 中重复 `getBean(ScanProbe.class)`，比较实例是否相同 |
| 6. 依赖注入 | `createInstance`（构造器优先 + 字段注入） | `jshell` 中执行 `container.getBean(BetaService.class).ping()` 预期 `beta->AlphaService` |
| 7. 启动回调 | `invokeStartCallbacks` | 运行 Demo，观察启动阶段输出 |
| 8. 从零实现清单 | README“方法职责表”“运行流程详解” | 按文档操作即可复现容器功能 |
| 9. 常见报错 | README“🧪 调试与定位” | 对照症状排查配置/注入问题 |
| 10. 可选练习 | README“🔮 附录：可选扩展 `@LogExecution`” | 参考思路自行实现扩展注解 |
| 11. 验收清单 | README“✅ 验收清单” | 自检勾选确认 |

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

## 🧪 调试与定位（常见报错）

- `ClassNotFoundException`：类名或路径转换错误，或组件未放在 `basePackage` 下；检查 `scanComponents` 生成的包路径与包声明是否一致。
- `IllegalAccessException`：未调用 `setAccessible(true)` 即访问私有构造器/字段；确认 `createInstance` 与 `performFieldInjection` 中的可访问性设置。
- `NoSuchMethodException`：类缺少无参构造器且没有 `@Inject` 构造器；为目标类新增无参构造器或唯一的 `@Inject` 构造器。
- `IllegalStateException: Circular dependency`：组件形成 A↔B 等循环依赖；通过引入接口、拆分职责或懒加载手段打破环路。
- 未触发 `@InvokeOnStart`：方法带参或组件未被扫描；确保方法无参、位于 `com.example` 包下且类含 `@Component`。
- Windows 路径包含空格：`scanDirectory` 已调用 `URLDecoder.decode(..., "UTF-8")`，通常无需额外处理，但若自定义路径转换请注意还原 `%20`。

## ✅ 验收清单（最终自检表）

- [ ] 能构建与运行：`mvn -q -DskipTests package && java -cp target/classes com.example.demo.App`
- [ ] 控制台包含 `Hello, IOC!` 与 `Container started.`
- [ ] 三大注解均为 `RUNTIME` 且用途清晰
- [ ] 扫描能发现 Demo 组件
- [ ] 同类多次 `getBean` 返回同一实例
- [ ] 构造器注入与字段注入均验证通过
- [ ] `@InvokeOnStart` 无参方法被自动调用
- [ ] README 逐条映射作业 0~11 节
- [ ] 未提交任何二进制/构建产物（`.gitignore` 生效）

## 🔮 附录：可选扩展 `@LogExecution`

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
