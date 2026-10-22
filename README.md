<div align="center">

# Agent GUI（Claude, Codex and More）

> A fork of CC GUI (originally Claude Code GUI)

<img width="120" alt="Image" src="./docs/images/agent-gui-logo.png" />

**English** · [简体中文](./README.zh-CN.md)

![][github-issues-shield] ![][github-mit]

</div>

> This fork uses the Agent GUI display name and is based on the open-source CC GUI project (originally Claude Code GUI). Regarding security, a
  /security-review audit will be conducted before each minor version release, and a comprehensive claude-code-security audit will be performed every 10 minor versions. 

A powerful IntelliJ IDEA plugin that provides a visual interface for **Claude Code**, **OpenAI Codex** and more AI coding CLIs, making AI-assisted programming more efficient and intuitive.

<img width="850" alt="Image" src="/docs/img/banner.png" />

---

## Installation

This fork does not yet have its own Marketplace listing. You can [build it from source](#local-development-and-debugging).

The upstream plugin remains available in the [JetBrains Marketplace](https://plugins.jetbrains.com/plugin/29342-cc-gui-claude-or-codex-).

---

## Performance Tips

### macOS Scrolling Performance

If you experience laggy scrolling in the chat panel or settings on macOS (especially with Retina displays), disable GPU acceleration:

**Add to VM options** (Help → Edit Custom VM Options):
```
-Dide.browser.jcef.gpu.disable=true
```

Then restart the IDE.

---

## Key Features

### Multi AI Engine Support
- **Claude Code** - Anthropic's official AI programming assistant, supporting Opus 4.5 and other models
- **OpenAI Codex** - OpenAI's powerful code generation engine
- **Grok CLI** (Beta) - xAI's command-line coding assistant
- **Kimi CLI** (Beta) - Moonshot AI's command-line coding assistant
- **OpenCode** (Beta) - open-source AI coding agent for the terminal
- **PI CLI** (Beta) - PI command-line coding assistant
- **OMP CLI** (Beta) - OMP command-line coding assistant
- **DeepSeek Harness** (Beta) - DeepSeek's command-line coding harness

### Intelligent Conversation
- Context-aware AI coding assistant
- @file reference support for precise code context
- Image sending support for visual requirement description
- Conversation rewind feature for flexible history adjustment
- Enhanced prompts for better AI understanding

### Agent System
- Built-in Agent system for automated complex tasks
- Skills slash command system (/init, /review, etc.)
- MCP server support to extend AI capabilities

### Developer Experience
- Comprehensive permission management and security controls
- Code DIFF comparison feature
- File navigation and code jumping
- Dark/Light theme switching
- Font scaling and IDE font synchronization
- Internationalization support (auto-switch between Chinese/English)

### Session Management
- History session records and search
- Session favorites
- Message export support
- Provider management (cc-switch compatible)
- Usage statistics analysis

---

## Project Status

The project is under active development with continuous updates. For version history and iteration progress, please read [CHANGELOG.md](CHANGELOG.md)

---

### Collaborative Contributing

For contributing guidelines, please read [CONTRIBUTING.md](CONTRIBUTING.md)

---


## Local Development and Debugging

### 1. Prerequisites

- JDK 17 (the source and Gradle Java toolchain target Java 17; JDK 21 is not required)
- Node.js 20+ and npm

Verify the active environment first:

```bash
java -version
node --version
npm --version
```

### 2. Debug Plugin

```bash
./gradlew clean runIde
```

Gradle runs `npm ci` from the lockfiles automatically, so there is no need to install dependencies manually in `webview` or `ai-bridge` first.

### 3. Build Plugin

```bash
./gradlew clean buildPlugin

# The generated plugin package will be in the build/distributions/ directory (package size approximately 40MB)
```

TLS, timeout, or connection-reset errors while downloading Maven artifacts are repository/network failures rather than Java compilation errors. The project includes a Maven Central fallback mirror; rerunning the build also reuses artifacts that were already downloaded.

---

## License

MIT

---

## Upstream

Agent GUI is an independent fork of [CC GUI](https://github.com/zhukunpenglinyutong/jetbrains-cc-gui), originally named Claude Code GUI. We thank the upstream maintainers and all contributors for their work. The upstream project remains the authoritative source for its own Marketplace listing, community channels, sponsors, and support.

<!-- LINK GROUP -->

[github-issues-shield]: https://img.shields.io/github/issues/xinhehe666/jetbrains-cc-gui?color=ff80eb&labelColor=black&style=flat-square
[github-mit]: https://img.shields.io/badge/github-MIT-blue?logo=github
