# Create Download Genshin

> Create(기계동력) 모드가 감지되면 월드 입장 시 가짜 "원신 다운로드" 팝업을 표시하는 장난 모드

[![Minecraft](https://img.shields.io/badge/Minecraft-1.20.1-green?style=flat-square)](https://www.minecraft.net/)
[![Fabric](https://img.shields.io/badge/Fabric-0.19.3-blue?style=flat-square)](https://fabricmc.net/)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.4.0-purple?style=flat-square&logo=kotlin)](https://kotlinlang.org/)
[![License](https://img.shields.io/badge/License-MIT-blue?style=flat-square)](LICENSE)

**🌐 언어: [English](README_EN.md) | [中文](README.md) | [日本語](README_JA.md) | [한국어](README_KO.md) | [Русский](README_RU.md)**

---

## 목차

- [기능 소개](#기능-소개)
- [요구 사항](#요구-사항)
- [설치 방법](#설치-方法)
- [설정 설명](#설정-설명)
- [프로젝트 구조](#프로젝트-구조)
- [기술 구현](#기술-구현)
- [자주 묻는 질문](#자주-묻는-질문)
- [빌드 방법](#빌드-方法)
- [개발 가이드](#개발-가이드)
- [라이선스](#라이선스)
- [면책 조항](#면책-조항)

---

## 기능 소개

### 전제 조건

이 모드는 **Create(기계동력) 모드가 감지된 경우에만 활성화**됩니다. Create가 설치되어 있지 않으면 모드는 완전히 무동작이며 아무런 작업도 수행하지 않습니다.

### 모드 1: 가짜 다운로드 모드 (기본값)

월드 입장 시 긴급 시스템 경고 다운로드 대화 상자가 자동으로 표시됩니다:

![가짜 모드 데모](image/gif/Prank.gif)

- 빨간색 경고 테두리 + "시스템 긴급 경고" 표시 빨간색 제목 표시줄
- 시안색 텍스트 "Create(기계동력) 모드가 감지되었습니다"
- 8개의 무서운 경고 메시지가 5초마다 회전
- 진행률 표시줄 색상 변화: 초록 → 노랑 → 주황 → 빨강
- **8% 확률로 진행률 후퇴**, 네트워크 렉 시뮬레이션
- **진행률 99%에서 고정**, 100%에 도달하지 않음
- 가짜 파일 정보: "원신 설치 파일 v5.0.0 - 23.8 GB"
- 가짜 남은 시간 카운트다운
- "다운로드 닫기" 버튼 또는 ESC 키로 닫기 가능

### 모드 2: 실제 다운로드 모드 (설정으로 활성화)

설정 파일에서 활성화하면 팝업이 실제 다운로드 관리자가 됩니다:

![실제 다운로드 모드 데모](image/gif/Download.gif)

- 파란색 테두리 전문 다운로드 인터페이스
- 시안색 텍스트 "Create(기계동력) 모드가 감지되었습니다"
- 비동기 백그라운드 스레드 다운로드, **게임 메인 스레드를 차단하지 않음**
- 실시간 표시: 다운로드 백분율, 다운로드됨/전체 크기, 속도(KB/s), 예상 남은 시간
- 테두리 색상이 상태에 따라 변화: 파랑(다운로드 중) → 초록(완료) → 빨강(실패)
- 다운로드 완료 후 설치 파일 자동 실행 (크로스 플랫폼: Windows/macOS/Linux)
- 네트워크 타임아웃, 권한 문제, 다운로드 실패 시 오류 팝업 표시
- 다운로드 취소 지원

---

## 요구 사항

| 의존성 | 버전 | 필수 | 설명 |
|--------|------|:----:|------|
| Minecraft | 1.20.1 | 예 | 게임 |
| Fabric Loader | >= 0.19.3 | 예 | 모드 로더 |
| Fabric API | 0.92.9+1.20.1 | 예 | Fabric 핵심 API |
| Fabric Language Kotlin | 1.13.12+kotlin.2.4.0 | 예 | Kotlin 언어 지원 |
| [Create](https://modrinth.com/mod/create) | 1.20.1任意 버전 | **예** | **이 모드가 감지된 경우에만 활성화** |
| Java | >= 17 | 예 | 런타임 |

> **중요:** 이 모드는 `FabricLoader.getInstance().isModLoaded("create")`를 사용하여 Create를 감지합니다. Create가 설치되어 있지 않으면 모드가 로드되지만 동작하지 않으며 성능에 영향을 주지 않습니다.

---

## 설치 方法

1. [Fabric Loader](https://fabricmc.net/use/installer/) 설치 (아직 설치하지 않은 경우)
2. [Fabric API](https://modrinth.com/mod/fabric-api) 다운로드 및 설치 → `.minecraft/mods/`에 배치
3. [Fabric Language Kotlin](https://modrinth.com/mod/fabric-language-kotlin) 다운로드 및 설치 → `.minecraft/mods/`에 배치
4. [Create](https://modrinth.com/mod/create) 다운로드 및 설치 → `.minecraft/mods/`에 배치
5. 이 모드의 `create-download-genshin-1.0.0.jar`을 `.minecraft/mods/`에 배치
6. 게임을 시작하고任意 월드에 입장하면 팝업이 표시됩니다

---

## 설정 설명

첫 실행 시 설정 파일이 자동 생성됩니다. 설정 파일이 손상되었거나 필드가 누락된 경우 기본값으로 자동 복원됩니다.

**설정 파일 경로:**
```
.minecraft/config/create-download-genshin/mod_config.json
```

### 기본 설정

```json
{
  "enableRealDownload": false,
  "downloadUrl": "https://ys-api.mihoyo.com/event/download_porter/link/ys_cn/official/pc_backup320",
  "downloadFileName": "yuanshen.exe"
}
```

### 매개변수 설명

| 매개변수 | 타입 | 기본값 | 설명 |
|----------|------|--------|------|
| `enableRealDownload` | Boolean | `false` | `false` = 가짜 모드(기본), `true` = 실제 다운로드 모드 |
| `downloadUrl` | String | miHoYo API | 실제 다운로드 모드의 대상 URL, `enableRealDownload=true`일 때만 유효 |
| `downloadFileName` | String | `yuanshen.exe` | 다운로드 파일 저장 이름, `enableRealDownload=true`일 때만 유효 |

### 모드 전환

1. **게임 종료**
2. `.minecraft/config/create-download-genshin/mod_config.json` 편집
3. `enableRealDownload`를 `true`로 변경 (사용자 정의 다운로드 링크가 필요한 경우 `downloadUrl`도 변경)
4. 파일 저장
5. **게임 재시작** (모드 변경은 재시작 필요)

---

## 다국어 지원

이 모드는 다음 언어를 지원하며, 게임 언어 설정에 따라 자동으로 전환됩니다:

| 언어 | 언어 코드 | 상태 |
|------|-----------|:----:|
| 简体中文 | `zh_cn` | 전체 지원 |
| English | `en_us` | 전체 지원 |
| 日本語 | `ja_jp` | 전체 지원 |
| 한국어 | `ko_kr` | 전체 지원 |
| Русский | `ru_ru` | 전체 지원 |

> 모든 인터페이스 텍스트(제목, 버튼, 메시지, 시간 형식 등)가 현지화되었습니다.

---

## 프로젝트 구조

```
src/
├── main/                                    # 공통 소스 (클라이언트 + 서버)
│   ├── kotlin/com/tututeam/create_download_genshin/
│   │   ├── CreateDownloadGenshin.kt         # 모드 진입점 (설정 초기화, 데이터 디렉토리)
│   │   ├── config/
│   │   │   └── ModConfig.kt                 # 설정 관리 (JSON R/W, 자동 복원)
│   │   └── util/
│   │       ├── FileUtil.kt                  # 파일 유틸리티 (데이터 디렉토리, 경로)
│   │       └── DownloadUtil.kt              # 비동기 다운로드 (CompletableFuture + HttpURLConnection)
│   └── resources/
│       ├── assets/create-download-genshin/
│       │   ├── lang/                        # 현지화 파일
│       │   └── icon.png                     # 모드 아이콘
│       ├── create-download-genshin.mixins.json
│       └── fabric.mod.json                  # 모드 기술자
│
└── client/                                  # 클라이언트 전용 소스 (서버에서 로드되지 않음)
    ├── kotlin/com/tututeam/create_download_genshin/client/
    │   ├── CreateDownloadGenshinClient.kt   # 클라이언트 진입점 (이벤트 등록)
    │   ├── event/
    │   │   └── ClientEvents.kt              # 이벤트 리스너 (Create 감지 + 월드 입장)
    │   └── gui/
    │       ├── FakeDownloadScreen.kt        # 가짜 팝업 (진행률 99% 고정)
    │       └── RealDownloadScreen.kt        # 실제 다운로드 (진행률/속도/ETA)
    └── resources/
        └── create-download-genshin.client.mixins.json
```

### 소스셋 책임

| 소스셋 | 로딩 환경 | 내용 | 사용 가능한 API |
|--------|-----------|------|-----------------|
| `main` (공통) | 클라이언트 + 서버 | 설정, 파일 유틸리티, 다운로드 유틸리티 | Java/Kotlin 표준 라이브러리, Fabric Loader API |
| `client` | 클라이언트만 | 이벤트 리스너, GUI 팝업 | Minecraft 클라이언트 API, Fabric 클라이언트 API |

---

## 기술 구현

### 핵심 메커니즘

| 메커니즘 | 구현 방법 |
|----------|-----------|
| **Create 감지** | `FabricLoader.getInstance().isModLoaded("create")` |
| **트리거** | `ClientPlayConnectionEvents.JOIN` 이벤트 (플레이어가 월드에 접속 시) |
| **스레드 스케줄링** | `client.execute {}`로 메인 스레드에 지연하여 GUI 표시 |
| **비동기 다운로드** | `CompletableFuture.runAsync` 백그라운드 스레드 |
| **HTTP 요청** | Java 내장 `HttpURLConnection` (추가 의존성 없음) |
| **진행률 전달** | `AtomicLong` / `AtomicBoolean` 스레드 안전 읽기/쓰기 |
| **널 안전성** | Kotlin `?.`、`?:`、`coerceIn` 널 안전 구문 |
| **예외 처리** | 모든 IO/네트워크/파일 작업에 `try-catch` |

### 가짜 모드 진행률 알고리즘

```kotlin
// 8~20프레임마다 업데이트 (極저 네트워크 시뮬레이션, 약 0.4~1초마다)
val updateInterval = 8 + random.nextInt(13)
if (tickCount % updateInterval != 0) return

// 진행률 델타 계산
val delta = if (random.nextDouble() < 0.08) {
    // 8% 확률로 후퇴: 0.05%~0.15%
    -(0.0005 + random.nextDouble() * 0.001)
} else {
    // 92% 확률로 증가: 0.03%~0.12%
    0.0003 + random.nextDouble() * 0.0009
}

// 99%에서 하드 록, 100%에 도달하지 않음
progress = (progress + delta).coerceIn(0.0, 0.99)
```

### 이벤트 흐름

```
게임 시작
  └→ CreateDownloadGenshin.onInitialize()
       ├→ ModConfig.init()          // 설정 로드/생성
       └→ FileUtil.initDataDir()    // 데이터 디렉토리 초기화

  └→ CreateDownloadGenshinClient.onInitializeClient()
       └→ ClientEvents.register()   // JOIN 이벤트 리스너 등록

플레이어가 월드에 입장
  └→ ClientPlayConnectionEvents.JOIN 트리거
       └→ client.execute {
            └→ showDownloadScreen()
                 ├→ isCreateModLoaded()  // Create 감지
                 │    └→ false → 무동작으로 반환
                 └→ true → 설정에 따라 적절한 GUI 표시
                      ├→ FakeDownloadScreen (가짜 모드)
                      └→ RealDownloadScreen (실제 모드)
                          └→ DownloadUtil.downloadAsync()  // 백그라운드 다운로드
```

---

## 자주 묻는 질문

### Q: 팝업이 표시되지 않나요?

1. **Create(기계동력) 모드가 설치되어 있는지 확인** — 이 모드는 Create가 감지된 경우에만 팝업을 표시합니다
2. 이 모드의 `.jar` 파일이 `.minecraft/mods/` 디렉토리에 있는지 확인
3. `fabric-api`와 `fabric-language-kotlin`이 설치되어 있는지 확인
4. 게임 로그(`.minecraft/logs/latest.log`)에서 다음 출력 확인:
   - `未检测到机械动力模组（Create），跳过弹窗` → Create 미설치
   - `已检测到机械动力模组（Create），准备弹出下载窗口` → 감지 성공

### Q: 실제 다운로드 모드에서 "다운로드 실패"가 표시되나요?

- 네트워크 연결 확인
- `downloadUrl`이 접근 가능한지 확인 (브라우저에서 테스트)
- 방화벽/프록시 차단 확인
- `.minecraft/logs/latest.log`에서 상세 오류 정보 확인
- 일반적인 오류 코드: `403` (권한 없음), `404` (링크 무효), `timeout` (네트워크 타임아웃)

### Q: 다운로드 링크를 변경하려면?

설정 파일의 `downloadUrl` 필드를 편집하고 게임을 재시작하세요.

### Q: 시스템 파일이 수정되나요?

**아니요.** 가짜 모드 팝업은 순수하게 시각적인 것이며 실제 작업을 수행하지 않습니다. 실제 다운로드 모드는 `.minecraft/create-download-genshin-data/` 디렉토리에 파일만 저장하며 시스템 파일은 수정하지 않습니다.

### Q: 서버가 크래시되나요?

**아니요.** 클라이언트 전용 코드(GUI, 이벤트 리스너, Create 감지)는 모두 `client` 소스셋에 있으며 서버에서는 로드되지 않습니다. 서버는 설정 관리와 파일 유틸리티 클래스만 로드합니다.

### Q: 팝업이 열려 있을 때 게임이 일시정지되나요?

**아니요.** 두 팝업 모두 `isPauseScreen()`을 오버라이드하여 `false`를 반환하므로 게임 월드는 계속 실행됩니다.

### Q: Create 없이 사용할 수 있나요?

아니요. 이 모드는 Create 사용자를 위해 설계되었으며, Create가 설치되어 있지 않으면 모드는 완전히 무동작입니다.

---

## 빌드 方法

### 전제 조건

- JDK 17+
- 네트워크 연결 (첫 빌드 시 의존성 다운로드)

### 컴파일

```bash
# 저장소 클론
git clone https://github.com/wututua/Create_Download_Genshin_Fabric.git
cd Create_Download_Genshin_Fabric

# 빌드 (Linux/macOS)
./gradlew build

# 빌드 (Windows)
gradlew.bat build

# 출력 파일
# build/libs/create-download-genshin-1.0.0.jar      ← mods/에 배치
# build/libs/create-download-genshin-1.0.0-sources.jar ← 소스 패키지
```

### 테스트

```bash
# 테스트 클라이언트 시작
./gradlew runClient
```

---

## 개발 가이드

### 환경 설정

1. JDK 17+ 설치
2. 이 저장소를 Fork하고 클론
3. IntelliJ IDEA로 프로젝트 열기 (권장, Gradle 프로젝트 자동 감지)
4. Gradle 동기화 완료 대기 (첫 실행 시 의존성 다운로드)
5. `./gradlew runClient` 실행하여 테스트 클라이언트 시작

### 코드 표준

- Kotlin 엄격한 널 안전 구문, `!!` 남용 금지
- 모든 공개 API에 KDoc 주석 필요
- IO/네트워크 작업에 `try-catch` 사용, 예외 정보를 로그에 기록
- 클라이언트 코드는 공통 패키지에 배치 금지 (서버 크래시 방지)
- GUI 팝업은 `isPauseScreen()`을 오버라이드하여 `false` 반환 필요

### 기여

Issue 및 Pull Request 제출을 환영합니다. 자세한 내용은 [CONTRIBUTING.md](CONTRIBUTING.md)를 참조하세요.

---

## 변경 로그

[CHANGELOG.md](CHANGELOG.md)를 참조하세요.

---

## 라이선스

이 프로젝트는 [MIT License](LICENSE)에 따라 라이선스됩니다.

---

## 크레딧

- [Fabric](https://fabricmc.net/) — Minecraft 모드 로더
- [Fabric API](https://github.com/FabricMC/fabric) — Fabric 핵심 API
- [Fabric Language Kotlin](https://github.com/FabricMC/fabric-language-kotlin) — Kotlin 언어 지원
- [Create](https://modrinth.com/mod/create) — 기계동력 모드
- [Minecraft](https://www.minecraft.net/) — 게임

---

## 면책 조항

이 모드는 학습 및 오락 목적으로만 사용됩니다. 가짜 모드 팝업은 순수하게 시각적인 장난이며 사용자의 컴퓨터에 실제 영향을 미치지 않습니다. 악의적인 목적으로 이 모드를 사용하지 마세요. 이 모드를 사용함으로써 모든 위험을 감수하는 것에 동의합니다.
