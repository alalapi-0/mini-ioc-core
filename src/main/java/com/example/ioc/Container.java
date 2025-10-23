package com.example.ioc; // 声明包路径，容器核心类所在位置

import com.example.ioc.annotations.Component; // 引入 @Component 注解，用于后续识别组件（本轮仅作为类型引用）
import com.example.ioc.annotations.Inject; // 引入 @Inject 注解，后续用于依赖注入（本轮不实现）
import com.example.ioc.annotations.InvokeOnStart; // 引入 @InvokeOnStart 注解，后续用于启动回调（本轮不实现）

import java.util.Map; // 引入 Map，用于维护单例缓存与命名 Bean 映射
import java.util.HashMap; // 引入 HashMap，用于 Map 的默认实现
import java.util.Set; // 引入 Set，用于保存扫描到的组件类型集合
import java.util.HashSet; // 引入 HashSet，用于 Set 的默认实现
import java.util.Objects; // 引入 Objects，用于非空检查等通用工具

/**
 * 迷你 IoC 容器的核心类骨架（Round 3：仅声明字段、构造器与方法签名，不实现逻辑）。
 * <p>
 * 职责概览（将在后续轮次逐步实现）：
 * <ul>
 *   <li>扫描：根据 basePackage 扫描出带 {@link Component} 的类（Round 4）</li>
 *   <li>单例：维护 {@code singletons} 缓存，按类型（和可选名称）提供 Bean（Round 5）</li>
 *   <li>注入：支持构造器优先、字段兜底的依赖注入（Round 6）</li>
 *   <li>回调：启动后调用标注 {@link InvokeOnStart} 且无参的方法（Round 7）</li>
 * </ul>
 * 本轮仅提供可编译的骨架与 TODO，占位返回值确保构建通过。
 */
public class Container { // 定义容器核心类

    // === 配置与状态字段（仅声明，不在本轮实现业务逻辑） ===

    private final String basePackage; // 保存基础扫描包，如 "com.example"；由构造器注入

    private final Map<Class<?>, Object> singletons = new HashMap<>(); // 单例缓存：类型 -> 实例；Round 5 实现
    private final Map<String, Object> namedBeans = new HashMap<>(); // 命名 Bean 缓存：名称 -> 实例；Round 5 可选扩展

    /**
     * 由调用方提供基础包名的构造器。
     *
     * @param basePackage 需要扫描的基础包（非空），例如 "com.example"
     * @throws NullPointerException 如果 basePackage 为 null
     * @throws IllegalArgumentException 如果 basePackage 为空字符串
     */
    public Container(String basePackage) { // 容器构造器，保存基础包名
        Objects.requireNonNull(basePackage, "basePackage must not be null"); // 非空校验，避免 NPE
        if (basePackage.trim().isEmpty()) { // 处理空白字符串的非法输入
            throw new IllegalArgumentException("basePackage must not be blank"); // 抛出非法参数异常
        }
        this.basePackage = basePackage; // 赋值给字段，供后续扫描与初始化使用
    }

    /**
     * 启动容器的入口方法。
     * <p>
     * 目标（后续轮次实现）：
     * 1) 扫描组件类型集合；
     * 2) 实例化并完成依赖注入；
     * 3) 执行 {@link InvokeOnStart} 标注的无参方法。
     */
    public void start() { // 容器启动入口；本轮仅做扫描与打印
        // TODO(Round 7): 完成启动回调的执行逻辑
        // TODO(Round 4~6): 调用扫描、实例化与注入流程

        // === Round 4: 临时调试输出 ===
        final Set<Class<?>> components = scanComponents(this.basePackage); // 调用扫描逻辑并收集组件
        for (Class<?> c : components) { // 遍历扫描结果
            System.out.println("[scan] found component: " + c.getName()); // 打印发现的组件类名
        } // for 循环结束
        // === /临时调试输出 ===
    } // start 方法结束

    /**
     * 扫描基础包下所有被 {@link Component} 标注的类型。
     *
     * @param basePackage 基础包名，形如 "com.example"
     * @return 扫描到并经 {@link Component} 过滤的类型集合
     */
    public Set<Class<?>> scanComponents(String basePackage) { // 扫描基础包下的 @Component 类型
        final Set<Class<?>> components = new HashSet<>(); // 使用 HashSet 去重并保持 O(1) 查找

        final String path = basePackage.replace('.', '/'); // 类路径资源使用斜杠分隔

        try { // 包裹整体扫描逻辑以捕获异常
            final ClassLoader cl = Thread.currentThread().getContextClassLoader(); // 优先使用上下文类加载器
            final java.util.Enumeration<java.net.URL> resources = cl.getResources(path); // 列举所有同名资源

            while (resources.hasMoreElements()) { // 逐个资源处理
                final java.net.URL url = resources.nextElement(); // 取出一个资源 URL
                final String protocol = url.getProtocol(); // 协议可能是 "file" 或 "jar"

                if ("file".equals(protocol)) { // 文件系统场景
                    final String filePath = java.net.URLDecoder.decode(url.getFile(), "UTF-8"); // 解码后得到实际文件路径
                    final java.io.File dir = new java.io.File(filePath); // 将路径包装成 File
                    scanDirectory(basePackage, dir, components, cl); // 委托目录扫描方法
                } else if ("jar".equals(protocol)) { // JAR 包场景
                    scanJarEntries(path, components, cl, url); // 委托 JAR 扫描方法
                } else { // 其他协议
                    // 保持容器健壮性：不因未知协议而失败
                } // 协议分支结束
            } // 资源遍历结束
        } catch (Exception e) { // 捕获并汇总所有扫描过程的异常
            System.out.println("[WARN] scanComponents failed: " + e.getMessage()); // 打印异常摘要
        } // try-catch 结束

        return components; // 返回收集到的组件类型集合
    } // scanComponents 方法结束

    /**
     * 递归扫描目录，查找以 .class 结尾的文件，并尝试按包名推导加载为 Class。
     *
     * @param basePackage 基础包名（如 "com.example"）
     * @param dir         起始目录（与 basePackage 对应的物理路径）
     * @param out         扫描结果输出集合
     * @param cl          用于加载类的类加载器
     */
    private void scanDirectory(String basePackage,
                               java.io.File dir,
                               Set<Class<?>> out,
                               ClassLoader cl) { // 遍历文件系统目录
        if (dir == null || !dir.exists()) { // 若目录不存在，直接返回
            return; // 安全兜底
        } // 目录存在性检查结束
        final java.io.File[] files = dir.listFiles(); // 列出目录下的所有文件与子目录
        if (files == null) { // I/O 异常或无权限时可能返回 null
            return; // 安全兜底
        } // 文件列表获取结束
        for (java.io.File f : files) { // 逐项遍历
            if (f.isDirectory()) { // 子目录
                final String subPackage = basePackage + "." + f.getName(); // 拼接子包名
                scanDirectory(subPackage, f, out, cl); // 深度优先遍历
            } else if (f.getName().endsWith(".class")) { // 命中字节码文件
                final String simpleClassName = f.getName().substring(0, f.getName().length() - 6); // 6 为 ".class" 长度
                final String fqcn = basePackage + "." + simpleClassName; // 生成 FQCN
                maybeAddComponentClass(fqcn, out, cl); // 委托统一的类加载与判定方法
            } // 文件类型分支结束
        } // for 循环结束
    } // scanDirectory 方法结束

    /**
     * 扫描 JAR 包条目，筛选匹配 path 的 .class 并尝试加载为 Class。
     *
     * @param resourcePath 包路径形式（如 "com/example"）
     * @param out          扫描结果输出集合
     * @param cl           用于加载类的类加载器
     * @param url          指向 JAR 资源的 URL
     */
    private void scanJarEntries(String resourcePath,
                                Set<Class<?>> out,
                                ClassLoader cl,
                                java.net.URL url) { // 遍历 JAR 中的条目
        try { // 捕获 JAR 访问异常
            final java.net.JarURLConnection conn = (java.net.JarURLConnection) url.openConnection(); // 打开连接
            final java.util.jar.JarFile jar = conn.getJarFile(); // 取得 JarFile 句柄

            final java.util.Enumeration<java.util.jar.JarEntry> entries = jar.entries(); // 遍历 JAR 内所有条目
            while (entries.hasMoreElements()) { // 逐条处理
                final java.util.jar.JarEntry entry = entries.nextElement(); // 取出一个条目
                final String name = entry.getName(); // 形如 "com/example/Foo.class"

                if (name.startsWith(resourcePath) && name.endsWith(".class") && !entry.isDirectory()) { // 过滤条件
                    final String fqcn = name
                            .substring(0, name.length() - 6) // 去掉 ".class"
                            .replace('/', '.'); // 将路径分隔符替换为包名分隔符
                    maybeAddComponentClass(fqcn, out, cl); // 统一处理
                } // 条目过滤结束
            } // JAR 条目遍历结束
        } catch (Exception e) { // 捕获连接与遍历过程中的异常
            System.out.println("[WARN] scanJarEntries failed: " + e.getMessage()); // 输出异常信息
        } // try-catch 结束
    } // scanJarEntries 方法结束

    /**
     * 根据 FQCN 尝试加载类；若带有 @Component 注解，则加入结果集合。
     *
     * @param fqcn 完全限定类名（Fully Qualified Class Name）
     * @param out  扫描结果输出集合
     * @param cl   用于加载类的类加载器
     */
    private void maybeAddComponentClass(String fqcn,
                                        Set<Class<?>> out,
                                        ClassLoader cl) { // 统一的类加载与注解判定
        try { // 捕获类加载异常
            final Class<?> clazz = Class.forName(fqcn, false, cl); // 使用 doInitialize=false 降低副作用
            if (clazz.isAnnotationPresent(Component.class)) { // 仅收集带注解的类型
                out.add(clazz); // 放入输出集合
            } // 注解判定结束
        } catch (Throwable ex) { // 捕获所有可能的错误与异常
            System.out.println("[DEBUG] skip class load: " + fqcn + " -> " + ex.getClass().getSimpleName()); // 调试输出
        } // try-catch 结束
    } // maybeAddComponentClass 方法结束

    /**
     * 根据类型获取（或创建）Bean 实例。
     * <p>
     * 逻辑（后续实现）：
     * 1) 命中单例缓存直接返回；
     * 2) 未命中则调用 {@link #createInstance(Class)} 创建并缓存；
     * 3) 支持按类型解析与（可选）按名称解析。
     *
     * @param type 需要获取的 Bean 类型
     * @param <T>  类型参数，表示返回的具体泛型类型
     * @return 对应类型的单例实例；本轮返回 null 作为占位
     */
    @SuppressWarnings("unchecked") // 抑制泛型转换告警：Map 按类型保存，读取时需要强转
    public <T> T getBean(Class<T> type) { // 基于类型的 Bean 获取入口
        if (type == null) { // 防御式编程，避免 NPE
            throw new IllegalArgumentException("type must not be null"); // 抛出非法参数异常
        }

        final Object cached = singletons.get(type); // 从单例 Map 中按类型查找
        if (cached != null) { // 如果已存在实例
            return (T) cached; // 直接强转返回
        }

        final T instance = createInstance(type); // 委托实例化方法（Round 6 再扩展 DI）
        if (instance == null) { // 安全兜底：应当不会为 null
            throw new IllegalStateException("createInstance returned null for type: " + type.getName()); // 失败即报错
        }

        putSingleton(type, instance); // 统一入口，负责写 singletons 与 namedBeans

        return instance; // 返回单例
    }

    /**
     * 创建指定类型的实例，并完成依赖注入。
     * <p>
     * 逻辑（后续实现）：
     * 1) 构造器注入：优先选择带 {@link Inject} 的构造器；无则尝试无参构造器；
     * 2) 字段注入：为带 {@link Inject} 的字段赋值（含私有字段可访问性处理）；
     * 3) 循环依赖：本项目不支持直接的 A↔B 循环，需在实现中检测并报错。
     *
     * @param type 目标类型
     * @param <T>  返回泛型
     * @return 新创建的实例；本轮返回 null 作为占位
     */
    @SuppressWarnings("unchecked") // 反射创建后需要强转到目标类型
    public <T> T createInstance(Class<T> type) { // 实例创建（最小版）：仅支持无参构造器
        try {
            final java.lang.reflect.Constructor<T> ctor = type.getDeclaredConstructor(); // 获取无参构造器
            if (!ctor.isAccessible()) { // 若是非 public 构造器
                ctor.setAccessible(true); // 打开可访问性
            }
            return ctor.newInstance(); // 调用无参构造器创建实例
        } catch (NoSuchMethodException e) { // 没有无参构造器的情况
            throw new IllegalStateException("No default constructor for type: " + type.getName()
                    + ". Round 5 only supports no-arg constructor; DI will be added in Round 6.", e); // 抛出清晰错误
        } catch (ReflectiveOperationException e) { // 其余反射异常（如非法访问、实例化失败等）
            throw new IllegalStateException("Failed to instantiate type: " + type.getName(), e); // 包装为运行时异常
        }
    }

    /**
     * 将实例写入单例缓存，并在存在 @Component("name") 时写入命名 Bean 缓存。
     *
     * @param type     Bean 的类型键（Class）
     * @param instance Bean 实例
     * @param <T>      实例的泛型类型
     */
    private <T> void putSingleton(Class<T> type, T instance) { // 统一写入 singletons 与 namedBeans
        singletons.put(type, instance); // 将类型 -> 实例 写入单例 Map

        final Component comp = type.getAnnotation(Component.class); // 获取 @Component 注解（若有）
        if (comp != null) { // 只有在类型被标注了 @Component 时才考虑命名
            final String name = comp.value(); // 读取 value() 作为 Bean 名称
            if (name != null && !name.trim().isEmpty()) { // 非空名称才写入
                namedBeans.put(name.trim(), instance); // 将 名称 -> 实例 放入命名 Bean Map
            }
        }
    }

    // === 可选：对外只读视图（便于调试/验证），本轮提供签名占位 ===

    /**
     * 获取当前已缓存的单例实例数量（调试与验收用）。
     *
     * @return 单例缓存的条目数；本轮返回 0
     */
    public int singletonCount() { // 返回当前单例缓存的条目数
        return singletons.size(); // 直接读取 Map 大小
    }

    /**
     * 获取基础扫描包名（只读）。
     *
     * @return basePackage 的只读副本
     */
    public String getBasePackage() { // 提供基础包名的只读访问器
        return this.basePackage; // 直接返回字段值
    }
}
