<div align="center">

# Agent GUI（Claude, Codex and More）

> 基于 CC GUI 的分支（原名：Claude Code GUI）

<img width="120" alt="Image" src="./docs/images/agent-gui-logo.png" />

**简体中文** · [English](./README.md)

![][github-issues-shield] ![][github-mit]

</div>

> 本分支使用 Agent GUI 作为展示名称，代码基于开源项目 CC GUI（原名：Claude Code GUI）。对于安全方面，后续每个小版本发版前都进行 /security-review 审查，每隔10个小版本进行一次整体的 claude-code-security 审查。

一个功能强大的 IntelliJ IDEA 插件，为开发者提供 **Claude Code**、**OpenAI Codex** 以及更多 AI 编程 CLI的可视化操作界面，让 AI 辅助编程变得更加高效和直观。

<img width="850" alt="Image" src="/docs/img/banner.png" />

---

## 插件下载

本分支还没有独立的 Marketplace 页面，可以[从源码构建](#本地开发调试)。

上游插件仍可在 [JetBrains Marketplace](https://plugins.jetbrains.com/plugin/29342-cc-gui-claude-or-codex-) 下载。

---

## 核心特性

### 多 AI 引擎支持
- **Claude Code** - Anthropic 官方 AI 编程助手，支持 Opus 4.5 等多模型
- **OpenAI Codex** - OpenAI 强大的代码生成引擎
- **Grok CLI**（Beta）- xAI 的命令行 AI 编程助手
- **Kimi CLI**（Beta）- 月之暗面（Moonshot AI）的命令行 AI 编程助手
- **OpenCode**（Beta）- 开源终端 AI 编程 Agent
- **PI CLI**（Beta）- PI 命令行 AI 编程助手
- **OMP CLI**（Beta）- OMP 命令行 AI 编程助手
- **DeepSeek Harness**（Beta）- DeepSeek 的命令行编程 Harness

### 智能对话功能
- 上下文感知的 AI 编程助手
- 支持 @文件引用，精准指定代码上下文
- 图片发送支持，可视化描述需求
- 对话回退功能，灵活调整对话历史
- 强化提示词，优化 AI 理解能力

### Agent 智能体
- 内置 Agent 系统，自动化执行复杂任务
- Skills 斜杠命令系统（/init, /review 等）
- MCP 服务器支持，扩展 AI 能力边界

### 开发者体验
- 完善的权限管理和安全控制
- 代码 DIFF 对比功能
- 文件跳转和代码导航
- 深色/浅色主题切换
- 字体缩放和 IDE 字体同步
- 国际化支持（中/英文自动切换）

### 会话管理
- 历史会话记录和搜索
- 会话收藏功能
- 消息导出支持
- 供应商管理（兼容 cc-switch）
- 使用统计分析

---

## 项目状态

项目处于活跃开发阶段，代码持续更新中。版本历史和迭代进度请阅读 [CHANGELOG.md](CHANGELOG.md)

---

### 贡献代码

贡献代码前请阅读 [CONTRIBUTING.md](CONTRIBUTING.md)

---


## 本地开发调试

### 1. 环境要求

- JDK 17（插件源码和 Gradle Java Toolchain 均以 Java 17 为目标，不要求 JDK 21）
- Node.js 20+ 与 npm

可先确认当前环境：

```bash
java -version
node --version
npm --version
```

### 2. 调试插件

```bash
./gradlew clean runIde
```

Gradle 会根据 `package-lock.json` 自动执行 `npm ci`，无需提前进入 `webview` 或 `ai-bridge` 手动安装依赖。

### 3. 构建插件

```bash
./gradlew clean buildPlugin

# 生成的插件包会在 build/distributions/ 目录下（包体大约40MB）
```

如果下载 Maven 依赖时遇到 TLS、超时或连接重置，这通常是仓库网络问题，不是 Java 编译错误。项目已配置 Maven Central 的备用镜像，重试构建即可复用已经下载的依赖。

---

## License

MIT

---

## 上游项目

Agent GUI 是 [CC GUI](https://github.com/zhukunpenglinyutong/jetbrains-cc-gui)（原名 Claude Code GUI）的独立分支。感谢上游维护者与所有贡献者。上游项目的 Marketplace 页面、交流群、赞助与支持渠道仍以上游仓库为准。

<!-- LINK GROUP -->

[github-issues-shield]: https://img.shields.io/github/issues/xinhehe666/jetbrains-agent-gui?color=ff80eb&labelColor=black&style=flat-square
[github-mit]: https://img.shields.io/badge/github-MIT-blue?logo=github
