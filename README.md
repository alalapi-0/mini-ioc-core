# 迷你 IOC 容器超详细实现手册（适合零基础）——作业要求原文

> 以下为课程给定的作业说明，保留其原貌，置于 README 最前，便于验收核对。

【在此完整粘贴老师提供的“作业要求”全文，从第 0 节到第 11 节，逐字保留】

---

# mini-ioc-core（项目说明）

本仓库用于按作业要求从零实现一个“注解 + 反射”的迷你 IoC 容器。后续将通过 10 轮迭代逐步完善功能，并在 README 中同步解释与验证方式。

## 一、快速开始

- 环境要求：JDK 8，Maven 3.6+  
- 构建（不运行测试）：  
  ```bash
  mvn -q -DskipTests package
```

> 说明：构建过程中产生的输出将位于 `target/` 目录；该目录已在 `.gitignore` 中忽略，不会提交到仓库。

* 当前状态（Round 1）：已完成项目脚手架、Maven 配置、目录骨架与 `.gitignore`，尚未包含可运行代码。

## 二、目录结构

```
mini-ioc-core/
├─ .gitignore
├─ pom.xml
├─ README.md
└─ src/
   └─ main/
      └─ java/
         └─ com/
            └─ example/
               ├─ ioc/
               │  └─ package-info.java
               ├─ ioc/
               │  └─ annotations/
               │     └─ package-info.java
               └─ demo/
                  └─ package-info.java
```

## 三、十轮计划路线图

1. Round 1：仓库初始化、README 顶置作业要求、`.gitignore`
2. Round 2：定义 `@Component`、`@Inject`、`@InvokeOnStart` 三个注解
3. Round 3：`Container` 骨架与方法签名
4. Round 4：包扫描 `scanComponents`（支持 file 与 jar）
5. Round 5：单例缓存与 `getBean`
6. Round 6：依赖注入（构造器优先 + 字段注入）
7. Round 7：启动回调 `@InvokeOnStart`
8. Round 8：Demo 组件与 `App` 启动类
9. Round 9：README 终稿与作业清单对照
10. Round 10：质量加固与提交自检

## 四、二进制与生成物政策

* 本仓库**不提交**任何二进制文件或构建产物，包含但不限于：`.class`、`.jar`、`.war`、`target/` 目录、压缩包与可执行文件。
* `.gitignore` 已配置忽略上述内容。
* 如需验证运行效果，请在本地构建与运行，不要将生成物写入仓库。

## 五、验证与常见问题（本轮）

* 验证：在仓库根目录执行

  ```bash
  mvn -q -DskipTests package
  ```

  期望：构建成功，仓库中无新增二进制文件被跟踪。
* 常见问题：

  * `JAVA_HOME` 未设置或 JDK 版本低于 1.8
  * IDE 自动生成了不必要的文件，请提交前检查 `git status`


## 注解设计（Round 2）

本轮实现的三大注解围绕“组件声明、依赖注入、启动回调”三个核心职责展开：

- `@Component`：限定在类型级别使用（`@Target(ElementType.TYPE)`），并通过 `@Retention(RetentionPolicy.RUNTIME)` 保留至运行时，从而让容器在扫描阶段识别受管 Bean；`@Documented` 则保证生成文档时不会遗漏组件的标记信息。
- `@Inject`：允许用于字段或构造器（`@Target({ElementType.FIELD, ElementType.CONSTRUCTOR})`），配合运行时保留策略让容器能够分析注入点并完成依赖装配，`@Documented` 方便在 API 文档中呈现依赖关系。
- `@InvokeOnStart`：限定在方法级别（`@Target(ElementType.METHOD)`）以标识启动回调，同样依赖运行时保留策略供容器在完成依赖注入后触发回调逻辑。

```java
@Component("customName")
public class DemoService {}

public class DemoConsumer {
    @Inject
    public DemoConsumer(DemoService service) {}

    @InvokeOnStart
    public void init() {}
}
```

`@InvokeOnStart` 要求方法无参，但 Java 注解本身无法在编译期验证这一约束，因而容器需要在运行时通过反射检查方法签名并在发现参数时抛出异常或给予警告。

后续容器实现将按“包扫描 → 识别 `@Component` → 解析 `@Inject` 注入点 → 在初始化完成后调用 `@InvokeOnStart`”的顺序执行，确保组件生命周期管理一致可控。

**常见坑提醒：**

- 忘记将注解保留到运行时，导致反射阶段获取不到标记。
- 导包错误或使用了错误的包路径，造成编译失败或注解不可见。
- 将注解文件放错目录，破坏既定的包结构和后续扫描逻辑。


```

## 生成后的操作与输出格式要求
- 以文件形式输出，不要把任何构建产物写进仓库。  
- 文件必须与上述路径与内容一致。  
- 所有文本文件使用 UTF-8。  
- 完成后列出最终的文件树和每个文件的首行与末行摘要，方便我人工核对。

## 验收清单（Codex 完成本轮后需回填为“已完成”或问题说明）
- [ ] `.gitignore` 已创建且内容与模板一致  
- [ ] `pom.xml` 可用，`mvn -q -DskipTests package` 构建成功  
- [ ] 三个 `package-info.java` 已创建且含说明注释  
- [ ] `README.md` 顶部为“作业要求原文”，其后为项目说明、目录、二进制政策等  
- [ ] 仓库未新增任何二进制文件被 Git 跟踪

---

按以上规范生成文件。完成后请给出：  
1) 文件树；  
2) `git status` 期望为干净工作区的说明；  
3) 本轮注意点与下一轮计划的简短提示。
