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
