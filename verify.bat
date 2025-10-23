@echo off & REM 关闭命令回显
REM verify.bat — 迷你 IoC 容器作业自检脚本
REM 用法: verify.bat
REM 需已安装 Maven 与 JDK 8+

REM 输出步骤提示
echo === [Step 1] 检查 Maven 与 Java 版本 ===
REM 检查 Maven 是否可用
mvn -v || (echo ❌ 未检测到 Maven & exit /b 1)
REM 检查 Java 是否可用
java -version || (echo ❌ 未检测到 Java & exit /b 1)

REM 输出步骤提示
echo === [Step 2] 构建项目（跳过测试） ===
REM 构建项目并跳过测试
mvn -q -DskipTests package || (echo ❌ 构建失败 & exit /b 1)

REM 输出步骤提示
echo === [Step 3] 运行 Demo ===
REM 启用延迟环境变量扩展
setlocal enabledelayedexpansion
REM 运行 Demo 并捕获输出
for /f "delims=" %%A in ('java -cp target\classes com.example.demo.App') do (
  set "OUTPUT=!OUTPUT!%%A"  & REM 累积输出内容
  echo %%A  & REM 实时打印输出
)

REM 输出步骤提示
echo === [Step 4] 验证输出关键字 ===
REM 检查输出是否包含关键字
echo %OUTPUT% | find "Hello, IOC!" >nul && echo %OUTPUT% | find "Container started." >nul
if errorlevel 1 (
  echo ❌ 输出不匹配，请检查 GreetingService/StartupRunner 实现  & REM 提示输出不匹配
  exit /b 1  & REM 返回失败状态
) else (
  echo ✅ 运行输出符合预期  & REM 提示输出符合预期
)

REM 输出步骤提示
echo === [Step 5] 检查仓库状态 ===
REM 搜索是否存在构建产物
git status --porcelain | findstr /R "target/ .*\.class" >nul
if errorlevel 1 (
  echo ✅ 无二进制产物被追踪  & REM 提示仓库干净
) else (
  echo ❌ 检测到二进制或构建产物被追踪，请清理后再提交  & REM 提示清理构建产物
  exit /b 1  & REM 返回失败状态
)

REM 输出步骤提示
echo === [Step 6] 自检完成 ===
REM 输出完成信息
echo 🎉 项目通过基本验证，可提交
