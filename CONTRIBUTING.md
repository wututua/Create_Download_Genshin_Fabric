# 贡献指南

感谢你对本项目的关注！以下是参与贡献的指南。

## 如何贡献

### 报告问题

1. 前往 [Issues](https://github.com/wututua/Create_Download_Genshin_Fabric/issues) 页面
2. 搜索是否已有相同问题
3. 如果没有，创建新的 Issue，包含：
   - 清晰的问题标题
   - 详细的问题描述
   - 复现步骤
   - 游戏版本和模组版本
   - 相关日志（`.minecraft/logs/latest.log`）

### 提交代码

1. Fork 本仓库
2. 创建你的功能分支：`git checkout -b feature/your-feature`
3. 提交你的修改：`git commit -m 'feat: 添加某功能'`
4. 推送到分支：`git push origin feature/your-feature`
5. 创建 Pull Request

### 代码规范

- 使用 Kotlin 编写，遵循 [Kotlin 编码规范](https://kotlinlang.org/docs/coding-conventions.html)
- 所有公共 API 必须附带 KDoc 中文注释
- IO/网络操作必须使用 `try-catch` 捕获异常
- 使用 Kotlin 空安全语法（`?.`、`?:`、`!!` 仅在确定非空时使用）
- 客户端专属代码放在 `client` 源码集，通用代码放在 `main` 源码集

### Commit 规范

使用 [Conventional Commits](https://www.conventionalcommits.org/zh-hans/) 规范：

```
<type>(<scope>): <description>

[可选正文]

[可选脚注]
```

类型（type）：
- `feat`: 新功能
- `fix`: 修复问题
- `docs`: 文档更新
- `style`: 代码格式调整（不影响功能）
- `refactor`: 代码重构
- `perf`: 性能优化
- `test`: 测试相关
- `chore`: 构建/工具相关

示例：
```
feat(gui): 添加下载速度显示
fix(config): 修复配置文件损坏时的崩溃问题
docs(readme): 更新安装说明
```

## 开发环境

### 前置要求

- JDK 17+
- IntelliJ IDEA（推荐）或其他 IDE
- Git

### 搭建步骤

```bash
# 1. Fork 并克隆仓库
git clone https://github.com/wututua/Create_Download_Genshin_Fabric.git
cd create-download-genshin

# 2. 导入到 IDE
# IntelliJ IDEA: File -> Open -> 选择项目根目录

# 3. 等待 Gradle 同步完成

# 4. 启动测试客户端
./gradlew runClient
```

### 项目结构

```
src/
├── main/          # common通用源码（客户端+服务端）
│   ├── kotlin/    # Kotlin源码
│   └── resources/ # 资源文件（fabric.mod.json、语言文件等）
└── client/        # 客户端专属源码
    ├── kotlin/    # Kotlin源码
    └── resources/ # 客户端资源（mixin配置等）
```

## 行为准则

- 尊重所有参与者
- 接受建设性批评
- 专注于对社区最有利的事情
- 对他人表示同理心

## 问题讨论

如有任何问题，欢迎在 [Discussions](https://github.com/wututua/Create_Download_Genshin_Fabric/discussions) 中讨论。

## 许可证

贡献的代码将基于 [MIT License](LICENSE) 开源。
