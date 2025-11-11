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
    // 用于检测简单的循环依赖（如 A 依赖 B，B 又依赖 A）
    private final Set<Class<?>> inCreation = new HashSet<>(); // 记录当前递归创建链上的类型

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
    public void start() { // 容器启动入口：扫描组件 → 实例化单例 → 执行启动回调
        final Set<Class<?>> components = scanComponents(this.basePackage); // 第一步：扫描基础包，找出所有带 @Component 的类型
        System.out.println("[info] components discovered: " + components.size()); // 可选调试：打印扫描到的组件总数

        for (Class<?> type : components) { // 第二步：实例化所有组件（这一步会填充 singletons）
            try { // 尝试通过 getBean 触发创建或获取单例
                Object bean = getBean(type); // 通过 getBean 触发：命中缓存则返回，否则创建并放入缓存（含依赖注入）
                System.out.println("[init] singleton ready: " + type.getName()); // 可选调试：打印完成实例化的类名
            } catch (RuntimeException ex) { // 捕获实例化过程中的运行时异常
                System.out.println("[ERROR] failed to init component: " + type.getName() + " -> " + ex.getMessage()); // 打印错误但不中断后续流程（简化容错）
            } // try-catch 结束
        } // 组件实例化循环结束

        invokeStartCallbacks(); // 第三步：执行所有带 @InvokeOnStart 的无参方法
    } // start 方法结束

    /**
     * 遍历已创建的单例实例，反射调用所有被 @InvokeOnStart 标注且“无参”的方法。
     * 对于带参数的方法将跳过并打印警告，避免运行期出错。
     * 任意一个回调抛出异常时，只打印错误并继续调用其他回调（简单容错策略）。
     */
    private void invokeStartCallbacks() { // 启动回调的集中执行逻辑
        final java.util.Collection<Object> beans = new java.util.ArrayList<>(singletons.values()); // 将当前单例快照出来，避免遍历过程中结构变动（按本项目语义，变动概率很低）
        for (Object bean : beans) { // 遍历每个单例
            final Class<?> clazz = bean.getClass(); // 获取运行时类型
            final java.lang.reflect.Method[] methods = clazz.getDeclaredMethods(); // 列出声明方法
            for (java.lang.reflect.Method m : methods) { // 遍历方法
                if (m.isAnnotationPresent(InvokeOnStart.class)) { // 仅处理标注了 @InvokeOnStart 的方法
                    if (m.getParameterCount() != 0) { // 若方法带参数
                        System.out.println("[WARN] @InvokeOnStart must be no-arg: "
                                + clazz.getName() + "#" + m.getName()); // 打印警告并跳过：容器约定只调用无参方法
                        continue; // 跳过本方法
                    } // 参数数量检查结束
                    final boolean old = m.isAccessible(); // 记录旧访问性
                    if (!old) { // 若为私有方法
                        m.setAccessible(true); // 打开访问权限
                    } // 访问权限调整结束
                    try { // 包裹反射调用，确保异常被捕获
                        long t0 = System.nanoTime(); // 可选：统计耗时（基础计时）
                        m.invoke(bean); // 反射调用回调方法
                        long t1 = System.nanoTime(); // 记录结束时间
                        System.out.println("[start] invoked: "
                                + clazz.getName() + "#" + m.getName()
                                + " (" + (t1 - t0) + " ns)"); // 简要打印调用信息与耗时
                    } catch (Exception e) { // 捕获调用中的异常
                        System.out.println("[ERROR] @InvokeOnStart failed on "
                                + clazz.getName() + "#" + m.getName()
                                + " -> " + e.getClass().getSimpleName() + ": " + e.getMessage()); // 打印错误但不中断其他回调
                    } finally { // 无论调用成功与否都执行的收尾逻辑
                        if (!old) { // 若之前调整过访问权限
                            m.setAccessible(false); // 恢复访问权限
                        } // 恢复访问权限结束
                    } // try-catch-finally 结束
                } // @InvokeOnStart 判定结束
            } // 方法遍历结束
        } // 单例遍历结束
    } // invokeStartCallbacks 方法结束

    /**
     * 扫描基础包下所有被 {@link Component} 标注的类型。
     *
     * @param basePackage 基础包名，形如 "com.example"
     * @return 扫描到并经 {@link Component} 过滤的类型集合
     */
    public Set<Class<?>> scanComponents(String basePackage) { // 扫描基础包下的 @Component 类型
        Objects.requireNonNull(basePackage, "basePackage must not be null"); // 允许外部直接调用时做保护
        final String trimmedBasePackage = basePackage.trim(); // 去掉首尾空白，避免构造路径时出现多余分隔符
        if (trimmedBasePackage.isEmpty()) { // 空字符串没有扫描意义
            throw new IllegalArgumentException("basePackage must not be blank");
        }

        final Set<Class<?>> components = new HashSet<>(); // 使用 HashSet 去重并保持 O(1) 查找

        final String path = trimmedBasePackage.replace('.', '/'); // 类路径资源使用斜杠分隔

        try { // 包裹整体扫描逻辑以捕获异常
            ClassLoader cl = Thread.currentThread().getContextClassLoader(); // 优先使用上下文类加载器
            if (cl == null) { // 某些运行时（例如早期的单元测试）可能返回 null
                cl = Container.class.getClassLoader(); // 回落到容器类自身的类加载器
            }
            if (cl == null) { // 仍然为 null 时无法继续扫描
                throw new IllegalStateException("No ClassLoader available for component scanning");
            }
            final java.util.Enumeration<java.net.URL> resources = cl.getResources(path); // 列举所有同名资源

            while (resources.hasMoreElements()) { // 逐个资源处理
                final java.net.URL url = resources.nextElement(); // 取出一个资源 URL
                final String protocol = url.getProtocol(); // 协议可能是 "file" 或 "jar"

                if ("file".equals(protocol)) { // 文件系统场景
                    final String filePath = java.net.URLDecoder.decode(url.getFile(), "UTF-8"); // 解码后得到实际文件路径
                    final java.io.File dir = new java.io.File(filePath); // 将路径包装成 File
                    scanDirectory(trimmedBasePackage, dir, components, cl); // 委托目录扫描方法
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
            try (java.util.jar.JarFile jar = conn.getJarFile()) { // 使用 try-with-resources 确保资源关闭
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
            }
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
    public <T> T createInstance(Class<T> type) { // 依赖注入版实例创建：构造器优先 + 字段注入
        if (inCreation.contains(type)) { // 循环依赖检测：若 type 已在创建链中，说明出现了 A↔B 或更长环路
            throw new IllegalStateException("Circular dependency detected while creating: " + type.getName()); // 抛出清晰错误
        } // 循环依赖判定结束
        inCreation.add(type); // 将当前类型加入“正在创建”集合
        try { // 捕获整个创建流程中的反射异常
            final java.lang.reflect.Constructor<T> ctor = findInjectConstructor(type); // 查找注入构造器（允许为 null）
            final T instance; // 预先声明实例变量
            if (ctor != null) { // 找到了唯一的 @Inject 构造器
                final Object[] args = resolveConstructorArgs(ctor); // 解析构造器参数（递归 getBean）
                if (!ctor.isAccessible()) { // 确保可访问
                    ctor.setAccessible(true); // 打开访问权限
                } // 可访问性处理结束
                instance = ctor.newInstance(args); // 使用注入构造器创建实例
            } else { // 没有 @Inject 构造器，退回到无参构造器策略
                final java.lang.reflect.Constructor<T> noArg = type.getDeclaredConstructor(); // 获取无参构造器
                if (!noArg.isAccessible()) { // 私有构造器也允许
                    noArg.setAccessible(true); // 打开访问权限
                } // 可访问性处理结束
                instance = noArg.newInstance(); // 通过无参构造器创建实例
            } // 构造器分支结束
            performFieldInjection(instance); // 字段注入：为所有带 @Inject 的字段赋值（私有字段允许）
            return instance; // 返回完成注入的实例（注意：放入单例缓存在 getBean 中统一处理）
        } catch (NoSuchMethodException e) { // 不存在无参构造器且也没有 @Inject 构造器
            throw new IllegalStateException("No suitable constructor for type: " + type.getName()
                    + ". Provide an @Inject constructor or a no-arg constructor.", e); // 指引修复方式
        } catch (ReflectiveOperationException e) { // 反射期间出现的其余异常
            throw new IllegalStateException("Failed to instantiate type: " + type.getName(), e); // 包装为运行时异常
        } finally { // 确保无论成功或失败都移除标记
            inCreation.remove(type); // 创建结束：务必从“正在创建”集合中移除，避免误判
        } // finally 结束
    } // createInstance 方法结束

    /**
     * 查找标注了 @Inject 的唯一构造器。
     * 若不存在返回 null；若存在多个，抛出异常提示开发者修正。
     */
    private <T> java.lang.reflect.Constructor<T> findInjectConstructor(Class<T> type) { // 选择注入构造器
        java.lang.reflect.Constructor<?>[] ctors = type.getDeclaredConstructors(); // 获取所有构造器
        java.lang.reflect.Constructor<T> found = null; // 记录唯一的注入构造器
        for (java.lang.reflect.Constructor<?> c : ctors) { // 遍历构造器
            if (c.isAnnotationPresent(Inject.class)) { // 该构造器带有 @Inject
                if (found != null) { // 已经找到过一个，再遇到第二个则冲突
                    throw new IllegalStateException("Multiple @Inject constructors in: " + type.getName()); // 报错
                } // 冲突判定结束
                @SuppressWarnings("unchecked") // 向下转型到目标类型构造器
                java.lang.reflect.Constructor<T> cast = (java.lang.reflect.Constructor<T>) c; // 安全转换
                found = cast; // 记录该构造器
            } // 注解判定结束
        } // 构造器遍历结束
        return found; // 可能为 null（表示无 @Inject 构造器）
    } // findInjectConstructor 方法结束

    /**
     * 解析构造器参数：对每个参数类型递归获取 Bean。
     *
     * @param ctor 目标构造器（已选定）
     * @return 参数实例数组，与构造器参数顺序一致
     */
    private Object[] resolveConstructorArgs(java.lang.reflect.Constructor<?> ctor) { // 解析构造器参数
        final Class<?>[] paramTypes = ctor.getParameterTypes(); // 获取参数类型数组
        final Object[] args = new Object[paramTypes.length]; // 准备承载参数实例的数组
        for (int i = 0; i < paramTypes.length; i++) { // 顺序解析
            args[i] = getBean(paramTypes[i]); // 递归获取对应类型的 Bean（可能触发进一步实例化）
        } // 参数解析循环结束
        return args; // 返回已解析的参数实例数组
    } // resolveConstructorArgs 方法结束

    /**
     * 对实例执行字段注入：为所有带 @Inject 的字段赋值。
     *
     * @param instance 已构造的对象实例
     */
    private void performFieldInjection(Object instance) { // 字段注入实现
        final Class<?> clazz = instance.getClass(); // 获取运行时类型
        final java.lang.reflect.Field[] fields = clazz.getDeclaredFields(); // 获取声明字段列表
        for (java.lang.reflect.Field f : fields) { // 遍历每个字段
            if (f.isAnnotationPresent(Inject.class)) { // 仅处理带 @Inject 的字段
                final Class<?> depType = f.getType(); // 读取字段类型
                final Object dep = getBean(depType); // 递归获取依赖实例
                final boolean old = f.isAccessible(); // 记录旧的可访问状态
                if (!old) { // 若为私有字段
                    f.setAccessible(true); // 打开访问权限
                } // 可访问性处理结束
                try { // 赋值过程可能抛出异常
                    f.set(instance, dep); // 赋值依赖
                } catch (IllegalAccessException e) { // 不太可能（已开启可访问）
                    throw new IllegalStateException("Failed to inject field: " + f.getName()
                            + " of " + clazz.getName(), e); // 抛出清晰错误
                } finally { // 可选：恢复原访问性
                    if (!old) { // 如果之前是不可访问
                        f.setAccessible(false); // 恢复
                    } // 恢复访问性结束
                } // try-finally 结束
            } // 注解判定结束
        } // 字段遍历结束
    } // performFieldInjection 方法结束

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
