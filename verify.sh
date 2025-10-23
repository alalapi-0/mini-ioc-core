#!/bin/bash
# verify.sh — 迷你 IoC 容器作业自检脚本
# 用法: bash verify.sh
# 注意: 需已安装 Maven 与 JDK 8+

set -e  # 任意命令失败即退出

echo "=== [Step 1] 检查 Maven 与 Java 版本 ==="  # 输出步骤提示
mvn -v || { echo "❌ 未检测到 Maven"; exit 1; }  # 检查 Maven 是否可用
java -version || { echo "❌ 未检测到 Java"; exit 1; }  # 检查 Java 是否可用

echo "=== [Step 2] 构建项目（跳过测试） ==="  # 输出步骤提示
mvn -q -DskipTests package || { echo "❌ 构建失败"; exit 1; }  # 构建项目并跳过测试

echo "=== [Step 3] 运行 Demo ==="  # 输出步骤提示
OUTPUT=$(java -cp target/classes com.example.demo.App | tee /dev/tty)  # 运行 Demo 并捕获输出

echo "=== [Step 4] 验证输出关键字 ==="  # 输出步骤提示
if [[ "$OUTPUT" == *"Hello, IOC!"* && "$OUTPUT" == *"Container started."* ]]; then  # 判断输出是否包含关键字
  echo "✅ 运行输出符合预期"  # 输出成功信息
else  # 输出不符合预期时的分支
  echo "❌ 输出不匹配，请检查 GreetingService/StartupRunner 实现"  # 提示输出不匹配
  exit 1  # 退出脚本
fi  # 条件语句结束

echo "=== [Step 5] 检查仓库状态 ==="  # 输出步骤提示
if git status --porcelain | grep -E 'target/|\\.class'; then  # 检查是否存在构建产物
  echo "❌ 检测到二进制或构建产物被追踪，请清理后再提交"  # 提示清理构建产物
  exit 1  # 退出脚本
else  # 未检测到构建产物时的分支
  echo "✅ 无二进制产物被追踪"  # 输出成功信息
fi  # 条件语句结束

echo "=== [Step 6] 自检完成 ==="  # 输出步骤提示
echo "🎉 项目通过基本验证，可提交"  # 输出完成信息
