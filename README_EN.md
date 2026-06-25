# Create Download Genshin

> A prank mod that shows a fake "Genshin Impact download" popup when entering a world with Create mod installed

[![Minecraft](https://img.shields.io/badge/Minecraft-1.20.1-green?style=flat-square)](https://www.minecraft.net/)
[![Fabric](https://img.shields.io/badge/Fabric-0.19.3-blue?style=flat-square)](https://fabricmc.net/)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.4.0-purple?style=flat-square&logo=kotlin)](https://kotlinlang.org/)
[![License](https://img.shields.io/badge/License-MIT-blue?style=flat-square)](LICENSE)

**🌐 Language: [English](README_EN.md) | [中文](README.md) | [日本語](README_JA.md) | [한국어](README_KO.md) | [Русский](README_RU.md)**

---

## Table of Contents

- [Features](#features)
- [Requirements](#requirements)
- [Installation](#installation)
- [Configuration](#configuration)
- [Project Structure](#project-structure)
- [Technical Details](#technical-details)
- [FAQ](#faq)
- [Building](#building)
- [Development](#development)
- [License](#license)
- [Disclaimer](#disclaimer)

---

## Features

### Prerequisite

This mod **only activates when Create (Mechanical Power) mod is detected**. If Create is not installed, the mod will be completely silent and won't perform any actions.

### Mode 1: Fake Download Mode (Default)

Automatically pops up a fake system warning download dialog when entering a world:

![Prank Mode Demo](image/gif/Prank.gif)

- Red warning border + red title bar showing "System Emergency Warning"
- Cyan text showing "Create mod detected on your system"
- 8 scary warning messages rotating every 5 seconds
- Progress bar color changes: green → yellow → orange → red
- **8% chance of progress regression**, simulating network lag
- **Progress locks at 99%**, never reaches 100%
- Fake file info: "Genshin Installer v5.0.0 - 23.8 GB"
- Fake remaining time countdown
- User can close by clicking "Close Download" button or pressing ESC

### Mode 2: Real Download Mode (Enabled via Config)

After enabling in config, the popup becomes a real download manager:

![Real Download Mode Demo](image/gif/Download.gif)

- Blue border professional download interface
- Cyan text showing "Create mod detected on your system"
- Async background thread downloading, **never blocks the game main thread**
- Real-time display: download percentage, downloaded/total size, speed (KB/s), estimated time remaining
- Border color changes: blue (downloading) → green (completed) → red (failed)
- Auto-opens installer after download (cross-platform: Windows/macOS/Linux)
- Error popup for network timeout, permission issues, download failure
- Supports cancel download

---

## Requirements

| Dependency | Version | Required | Description |
|------------|---------|:--------:|-------------|
| Minecraft | 1.20.1 | Yes | Game |
| Fabric Loader | >= 0.19.3 | Yes | Mod loader |
| Fabric API | 0.92.9+1.20.1 | Yes | Fabric core API |
| Fabric Language Kotlin | 1.13.12+kotlin.2.4.0 | Yes | Kotlin language support |
| [Create](https://modrinth.com/mod/create) | Any 1.20.1 version | **Yes** | **Mod activates only when this is detected** |
| Java | >= 17 | Yes | Runtime |

> **Important:** This mod uses `FabricLoader.getInstance().isModLoaded("create")` to detect Create. When Create is not installed, the mod loads but performs no actions, with zero performance impact.

---

## Installation

1. Install [Fabric Loader](https://fabricmc.net/use/installer/) (if not already installed)
2. Download and install [Fabric API](https://modrinth.com/mod/fabric-api) → put in `.minecraft/mods/`
3. Download and install [Fabric Language Kotlin](https://modrinth.com/mod/fabric-language-kotlin) → put in `.minecraft/mods/`
4. Download and install [Create](https://modrinth.com/mod/create) → put in `.minecraft/mods/`
5. Put `create-download-genshin-1.0.0.jar` in `.minecraft/mods/`
6. Launch the game and enter any world to see the popup

---

## Configuration

The mod auto-generates config file on first launch. Auto-restores defaults if config is corrupted or has missing fields.

**Config file path:**
```
.minecraft/config/create-download-genshin/mod_config.json
```

### Default Config

```json
{
  "enableRealDownload": false,
  "downloadUrl": "https://ys-api.mihoyo.com/event/download_porter/link/ys_cn/official/pc_backup320",
  "downloadFileName": "yuanshen.exe"
}
```

### Parameters

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `enableRealDownload` | Boolean | `false` | `false` = Fake mode (default), `true` = Real download mode |
| `downloadUrl` | String | miHoYo API | Target URL for real download mode, only effective when `enableRealDownload=true` |
| `downloadFileName` | String | `yuanshen.exe` | Saved filename, only effective when `enableRealDownload=true` |

### Switching Modes

1. **Close the game**
2. Edit `.minecraft/config/create-download-genshin/mod_config.json`
3. Change `enableRealDownload` to `true` (optionally modify `downloadUrl` for custom download link)
4. Save file
5. **Restart the game** (mode changes require restart)

---

## Multi-Language Support

This mod supports the following languages, automatically switching based on game language settings:

| Language | Language Code | Status |
|----------|---------------|:------:|
| 简体中文 | `zh_cn` | Full Support |
| English | `en_us` | Full Support |
| 日本語 | `ja_jp` | Full Support |
| 한국어 | `ko_kr` | Full Support |
| Русский | `ru_ru` | Full Support |

> All interface text (titles, buttons, messages, time formats, etc.) are fully localized.

---

## Project Structure

```
src/
├── main/                                    # common source (client + server)
│   ├── kotlin/com/tututeam/create_download_genshin/
│   │   ├── CreateDownloadGenshin.kt         # Mod entry (init config, data dir)
│   │   ├── config/
│   │   │   └── ModConfig.kt                 # Config management (JSON R/W, auto-recovery)
│   │   └── util/
│   │       ├── FileUtil.kt                  # File utils (data dir, path, byte formatting)
│   │       └── DownloadUtil.kt              # Async download (CompletableFuture + HttpURLConnection)
│   └── resources/
│       ├── assets/create-download-genshin/
│       │   ├── lang/                        # Localization files
│       │   └── icon.png                     # Mod icon
│       ├── create-download-genshin.mixins.json
│       └── fabric.mod.json                  # Mod descriptor
│
└── client/                                  # Client-only source (not loaded on server)
    ├── kotlin/com/tututeam/create_download_genshin/client/
    │   ├── CreateDownloadGenshinClient.kt   # Client entry (register events)
    │   ├── event/
    │   │   └── ClientEvents.kt              # Event listener (Create detection + world join)
    │   └── gui/
    │       ├── FakeDownloadScreen.kt        # Fake popup (progress locks at 99%)
    │       └── RealDownloadScreen.kt        # Real download (progress/speed/ETA)
    └── resources/
        └── create-download-genshin.client.mixins.json
```

### Source Set Responsibilities

| Source Set | Loading Environment | Contents | Available APIs |
|------------|---------------------|----------|----------------|
| `main` (common) | Client + Server | Config, file utils, download utils | Java/Kotlin stdlib, Fabric Loader API |
| `client` | Client only | Event listeners, GUI popups | Minecraft client API, Fabric client API |

---

## Technical Details

### Core Mechanisms

| Mechanism | Implementation |
|-----------|----------------|
| **Create Detection** | `FabricLoader.getInstance().isModLoaded("create")` |
| **Trigger** | `ClientPlayConnectionEvents.JOIN` event (when player connects to world) |
| **Thread Scheduling** | `client.execute {}` delays to main thread for GUI |
| **Async Download** | `CompletableFuture.runAsync` background thread |
| **HTTP Request** | Java built-in `HttpURLConnection` (no extra dependencies) |
| **Progress Transfer** | `AtomicLong` / `AtomicBoolean` thread-safe read/write |
| **Null Safety** | Kotlin `?.`, `?:`, `coerceIn` null-safe syntax |
| **Exception Handling** | All IO/network/file operations wrapped in `try-catch` |

### Fake Mode Progress Algorithm

```kotlin
// Update every 8~20 frames (simulating very slow network, ~0.4-1s per update)
val updateInterval = 8 + random.nextInt(13)
if (tickCount % updateInterval != 0) return

// Calculate progress delta
val delta = if (random.nextDouble() < 0.08) {
    // 8% chance regression: 0.05%~0.15%
    -(0.0005 + random.nextDouble() * 0.001)
} else {
    // 92% chance increment: 0.03%~0.12%
    0.0003 + random.nextDouble() * 0.0009
}

// Hard cap at 99%, never reaches 100%
progress = (progress + delta).coerceIn(0.0, 0.99)
```

### Event Flow

```
Game Launch
  └→ CreateDownloadGenshin.onInitialize()
       ├→ ModConfig.init()          // Load/create config
       └→ FileUtil.initDataDir()    // Init data directory

  └→ CreateDownloadGenshinClient.onInitializeClient()
       └→ ClientEvents.register()   // Register JOIN event listener

Player Enters World
  └→ ClientPlayConnectionEvents.JOIN triggered
       └→ client.execute {
            └→ showDownloadScreen()
                 ├→ isCreateModLoaded()  // Detect Create
                 │    └→ false → Silent return, no action
                 └→ true → Show appropriate GUI based on config
                      ├→ FakeDownloadScreen (Fake mode)
                      └→ RealDownloadScreen (Real mode)
                          └→ DownloadUtil.downloadAsync()  // Background download
```

---

## FAQ

### Q: Popup doesn't appear?

1. **Confirm Create (Mechanical Power) mod is installed** — This mod only pops up when Create is detected
2. Confirm this mod's `.jar` file is in `.minecraft/mods/` directory
3. Confirm `fabric-api` and `fabric-language-kotlin` are installed
4. Check game log (`.minecraft/logs/latest.log`) for these outputs:
   - `未检测到机械动力模组（Create），跳过弹窗` → Create not installed
   - `已检测到机械动力模组（Create），准备弹出下载窗口` → Detection successful

### Q: Real download mode shows "Download Failed"?

- Check network connection
- Confirm `downloadUrl` is accessible (test in browser)
- Check for firewall/proxy blocking
- View `.minecraft/logs/latest.log` for detailed error info
- Common error codes: `403` (forbidden), `404` (not found), `timeout` (network timeout)

### Q: How to change the download link?

Edit the `downloadUrl` field in config file, save and restart the game.

### Q: Will it modify my system files?

**No.** The fake mode popup is purely visual, performs no actual operations. Real download mode only saves files to `.minecraft/create-download-genshin-data/` directory, does not modify system files.

### Q: Will it crash the server?

**No.** All client-only code (GUI, event listeners, Create detection) is in the `client` source set, not loaded on server. Server only loads config management and file utility classes.

### Q: Does the game pause when popup is open?

**No.** Both popups override `isPauseScreen()` to return `false`, the game world continues running.

### Q: Can I use it without installing Create?

No. This mod is designed specifically for Create users. Without Create installed, the mod is completely silent.

---

## Building

### Prerequisites

- JDK 17+
- Network connection (first build downloads dependencies)

### Compile

```bash
# Clone repository
git clone https://github.com/wututua/Create_Download_Genshin_Fabric.git
cd Create_Download_Genshin_Fabric

# Build (Linux/macOS)
./gradlew build

# Build (Windows)
gradlew.bat build

# Output files
# build/libs/create-download-genshin-1.0.0.jar      ← Put in mods/
# build/libs/create-download-genshin-1.0.0-sources.jar ← Source package
```

### Test

```bash
# Launch test client
./gradlew runClient
```

---

## Development

### Setup

1. Install JDK 17+
2. Fork and clone this repository
3. Open project with IntelliJ IDEA (recommended, auto-detects Gradle project)
4. Wait for Gradle sync to complete (first time downloads dependencies)
5. Run `./gradlew runClient` to launch test client

### Code Standards

- Kotlin strict null-safe syntax, no abuse of `!!`
- All public APIs must have KDoc comments
- IO/network operations must use `try-catch`, log exception info
- Client code must not appear in common package (prevents server crashes)
- GUI popups must override `isPauseScreen()` returning `false`

### Contributing

Welcome to submit Issues and Pull Requests. See [CONTRIBUTING.md](CONTRIBUTING.md).

---

## Changelog

See [CHANGELOG.md](CHANGELOG.md).

---

## License

This project is licensed under the [MIT License](LICENSE).

---

## Credits

- [Fabric](https://fabricmc.net/) — Minecraft mod loader
- [Fabric API](https://github.com/FabricMC/fabric) — Fabric core API
- [Fabric Language Kotlin](https://github.com/FabricMC/fabric-language-kotlin) — Kotlin language support
- [Create](https://modrinth.com/mod/create) — Mechanical Power mod
- [Minecraft](https://www.minecraft.net/) — Game

---

## Disclaimer

This mod is for learning and entertainment purposes only. The fake mode popup is purely a visual prank and will not cause any actual impact on your computer. Do not use this mod for malicious purposes. By using this mod, you agree to assume all risks.
