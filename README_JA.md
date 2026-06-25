# Create Download Genshin

> Create（機械動力）モッドが検出された際、ワールド入場時に偽の「原神ダウンロード」ポップアップを表示する悪戯モッド

[![Minecraft](https://img.shields.io/badge/Minecraft-1.20.1-green?style=flat-square)](https://www.minecraft.net/)
[![Fabric](https://img.shields.io/badge/Fabric-0.19.3-blue?style=flat-square)](https://fabricmc.net/)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.4.0-purple?style=flat-square&logo=kotlin)](https://kotlinlang.org/)
[![License](https://img.shields.io/badge/License-MIT-blue?style=flat-square)](LICENSE)

**🌐 言語: [English](README_EN.md) | [中文](README.md) | [日本語](README_JA.md) | [한국어](README_KO.md) | [Русский](README_RU.md)**

---

## 目次

- [機能紹介](#機能紹介)
- [必要条件](#必要条件)
- [インストール手順](#インストール手順)
- [設定説明](#設定説明)
- [プロジェクト構造](#プロジェクト構造)
- [技術実装](#技術実装)
- [よくある質問](#よくある質問)
- [ビルド方法](#ビルド方法)
- [開発ガイド](#開発ガイド)
- [ライセンス](#ライセンス)
- [免責事項](#免責事項)

---

## 機能紹介

### 前提条件

このモッドは**Create（機械動力）モッドが検出された場合のみ有効化**されます。Createがインストールされていない場合、モッドは完全に無動作で、一切の操作を行いません。

### モード1：悪戯虚假ダウンロードモード（デフォルト）

ワールド入場時に、緊急システム警告のダウンロードダイアログが自動的に表示されます：

![悪戯モードデモ](image/gif/Prank.gif)

- 赤い警告ボーダー + 「システム緊急警告」表示の赤いタイトルバー
- シアン色のテキスト「Create（機械動力）モッドが検出されました」
- 8つの恐怖な警告メッセージが5秒ごとにローテーション
- プログレスバーの色が変化：緑 → 黄 → オレンジ → 赤
- **8%の確率でプログレス後退**、ネットワークラグをシミュレート
- **プログレスは99%でロック**、100%になることはない
- 偽のファイル情報：「原神インストーラー v5.0.0 - 23.8 GB」
- 偽の残り時間カウントダウン
- 「ダウンロードを閉じる」ボタンまたはESCキーで閉じ可能

### モード2：リアルダウンロードモード（設定で有効化)

設定ファイルで有効にすると、ポップアップは本物のダウンロードマネージャーになります：

![リアルダウンロードモードデモ](image/gif/Download.gif)

- 青いボーダーのプロフェッショナルなダウンロードインターフェース
- シアン色のテキスト「Create（機械動力）モッドが検出されました」
- 非同期バックグラウンドスレッドでダウンロード、**ゲームメインスレッドをブロックしない**
- リアルタイム表示：ダウンロードパーセンテージ、ダウンロード済み/合計サイズ、速度（KB/s）、推定残り時間
- ボーダーの色が状態に応じて変化：青（ダウンロード中）→ 緑（完了）→ 赤（失敗）
- ダウンロード完了後にインストーラーを自動起動（クロスプラットフォーム：Windows/macOS/Linux）
- ネットワークタイムアウト、権限問題、ダウンロード失敗時にエラーポップアップ表示
- ダウンロードキャンセル対応

---

## 必要条件

| 依存関係 | バージョン | 必須 | 説明 |
|----------|------------|:----:|------|
| Minecraft | 1.20.1 | はい | ゲーム本体 |
| Fabric Loader | >= 0.19.3 | はい | モッドローダー |
| Fabric API | 0.92.9+1.20.1 | はい | FabricコアAPI |
| Fabric Language Kotlin | 1.13.12+kotlin.2.4.0 | はい | Kotlin言語サポート |
| [Create](https://modrinth.com/mod/create) | 任意の1.20.1バージョン | **はい** | **このモッドが検出された場合のみ有効化** |
| Java | >= 17 | はい | ランタイム |

> **重要：** このモッドは `FabricLoader.getInstance().isModLoaded("create")` を使用してCreateを検出します。Createがインストールされていない場合、モッドは読み込まれますが動作せず、パフォーマンスへの影響はゼロです。

---

## インストール手順

1. [Fabric Loader](https://fabricmc.net/use/installer/) をインストール（未インストールの場合）
2. [Fabric API](https://modrinth.com/mod/fabric-api) をダウンロードしてインストール → `.minecraft/mods/` に配置
3. [Fabric Language Kotlin](https://modrinth.com/mod/fabric-language-kotlin) をダウンロードしてインストール → `.minecraft/mods/` に配置
4. [Create](https://modrinth.com/mod/create) をダウンロードしてインストール → `.minecraft/mods/` に配置
5. このモッドの `create-download-genshin-1.0.0.jar` を `.minecraft/mods/` に配置
6. ゲームを起動し、任意のワールドに入場するとポップアップが表示されます

---

## 設定説明

初回起動時に設定ファイルが自動生成されます。設定ファイルが破損しているかフィールドが欠落している場合、デフォルト値に自動復元されます。

**設定ファイルパス：**
```
.minecraft/config/create-download-genshin/mod_config.json
```

### デフォルト設定

```json
{
  "enableRealDownload": false,
  "downloadUrl": "https://ys-api.mihoyo.com/event/download_porter/link/ys_cn/official/pc_backup320",
  "downloadFileName": "yuanshen.exe"
}
```

### パラメータ説明

| パラメータ | 型 | デフォルト | 説明 |
|------------|-----|-----------|------|
| `enableRealDownload` | Boolean | `false` | `false` = 悪戯モード（デフォルト）、`true` = リアルダウンロードモード |
| `downloadUrl` | String | miHoYo API | リアルダウンロードモードの対象URL、`enableRealDownload=true` の場合のみ有効 |
| `downloadFileName` | String | `yuanshen.exe` | ダウンロードファイルの保存名、`enableRealDownload=true` の場合のみ有効 |

### モード切替

1. **ゲームを閉じる**
2. `.minecraft/config/create-download-genshin/mod_config.json` を編集
3. `enableRealDownload` を `true` に変更（カスタムダウンロードリンクが必要な場合は `downloadUrl` も変更）
4. ファイルを保存
5. **ゲームを再起動**（モード変更には再起動が必要）

---

## マルチ言語サポート

このモッドは以下の言語をサポートしており、ゲームの言語設定に応じて自動的に切り替わります：

| 言語 | 言語コード | ステータス |
|------|------------|:----------:|
| 简体中文 | `zh_cn` | 完全サポート |
| English | `en_us` | 完全サポート |
| 日本語 | `ja_jp` | 完全サポート |
| 한국어 | `ko_kr` | 完全サポート |
| Русский | `ru_ru` | 完全サポート |

> すべてのインターフェーステキスト（タイトル、ボタン、メッセージ、時間フォーマットなど）がローカライズされています。

---

## プロジェクト構造

```
src/
├── main/                                    # 共通ソース（クライアント + サーバー）
│   ├── kotlin/com/tututeam/create_download_genshin/
│   │   ├── CreateDownloadGenshin.kt         # モッドエントリー（設定初期化、データディレクトリ）
│   │   ├── config/
│   │   │   └── ModConfig.kt                 # 設定管理（JSON R/W、自動復元）
│   │   └── util/
│   │       ├── FileUtil.kt                  # ファイルユーティリティ（データディレクトリ、パス）
│   │       └── DownloadUtil.kt              # 非同期ダウンロード（CompletableFuture + HttpURLConnection）
│   └── resources/
│       ├── assets/create-download-genshin/
│       │   ├── lang/                        # ローカライゼーションファイル
│       │   └── icon.png                     # モッドアイコン
│       ├── create-download-genshin.mixins.json
│       └── fabric.mod.json                  # モッド記述子
│
└── client/                                  # クライアント専用ソース（サーバーでは読み込まれない）
    ├── kotlin/com/tututeam/create_download_genshin/client/
    │   ├── CreateDownloadGenshinClient.kt   # クライアントエントリー（イベント登録）
    │   ├── event/
    │   │   └── ClientEvents.kt              # イベントリスナー（Create検出 + ワールド入場）
    │   └── gui/
    │       ├── FakeDownloadScreen.kt        # 偽ポップアップ（プログレス99%ロック）
    │       └── RealDownloadScreen.kt        # リアルダウンロード（プログレス/速度/ETA）
    └── resources/
        └── create-download-genshin.client.mixins.json
```

### ソースセットの責任

| ソースセット | 読み込み環境 | 内容 | 使用可能なAPI |
|--------------|--------------|------|---------------|
| `main` (共通) | クライアント + サーバー | 設定、ファイルユーティリティ、ダウンロードユーティリティ | Java/Kotlin標準ライブラリ、Fabric Loader API |
| `client` | クライアントのみ | イベントリスナー、GUIポップアップ | MinecraftクライアントAPI、FabricクライアントAPI |

---

## 技術実装

### コアメカニズム

| メカニズム | 実装方法 |
|------------|----------|
| **Create検出** | `FabricLoader.getInstance().isModLoaded("create")` |
| **トリガー** | `ClientPlayConnectionEvents.JOIN` イベント（プレイヤーがワールドに接続時） |
| **スレッドスケジューリング** | `client.execute {}` でメインスレッドに遅延してGUI表示 |
| **非同期ダウンロード** | `CompletableFuture.runAsync` バックグラウンドスレッド |
| **HTTPリクエスト** | Java組み込み `HttpURLConnection`（追加依存関係なし） |
| **プログレス転送** | `AtomicLong` / `AtomicBoolean` スレッドセーフな読み書き |
| **ヌルセーフティ** | Kotlin `?.`、`?:`、`coerceIn` ヌルセーフ構文 |
| **例外処理** | すべてのIO/ネットワーク/ファイル操作に `try-catch` |

### 悪戯モードのプログレスアルゴリズム

```kotlin
// 8~20フレームごとに更新（極遅ネットワークをシミュレート、約0.4~1秒ごと）
val updateInterval = 8 + random.nextInt(13)
if (tickCount % updateInterval != 0) return

// プログレスデルタを計算
val delta = if (random.nextDouble() < 0.08) {
    // 8%の確率で後退：0.05%~0.15%
    -(0.0005 + random.nextDouble() * 0.001)
} else {
    // 92%の確率で増加：0.03%~0.12%
    0.0003 + random.nextDouble() * 0.0009
}

// 99%でハードロック、100%になることはない
progress = (progress + delta).coerceIn(0.0, 0.99)
```

### イベントフロー

```
ゲーム起動
  └→ CreateDownloadGenshin.onInitialize()
       ├→ ModConfig.init()          // 設定読み込み/作成
       └→ FileUtil.initDataDir()    // データディレクトリ初期化

  └→ CreateDownloadGenshinClient.onInitializeClient()
       └→ ClientEvents.register()   // JOINイベントリスナー登録

プレイヤーがワールドに入場
  └→ ClientPlayConnectionEvents.JOIN がトリガー
       └→ client.execute {
            └→ showDownloadScreen()
                 ├→ isCreateModLoaded()  // Create検出
                 │    └→ false → 無動作で戻る
                 └→ true → 設定に基づいて対応するGUIを表示
                      ├→ FakeDownloadScreen（悪戯モード）
                      └→ RealDownloadScreen（リアルモード）
                          └→ DownloadUtil.downloadAsync()  // バックグラウンドダウンロード
```

---

## よくある質問

### Q: ポップアップが表示されない？

1. **Create（機械動力）モッドがインストールされていることを確認** — このモッドはCreateが検出された場合のみポップアップを表示します
2. このモッドの `.jar` ファイルが `.minecraft/mods/` ディレクトリにあることを確認
3. `fabric-api` と `fabric-language-kotlin` がインストールされていることを確認
4. ゲームログ（`.minecraft/logs/latest.log`）で以下の出力を確認：
   - `未检测到机械动力模组（Create），跳过弹窗` → Createが未インストール
   - `已检测到机械动力模组（Create），准备弹出下载窗口` → 検出成功

### Q: リアルダウンロードモードで「ダウンロード失敗」と表示される？

- ネットワーク接続を確認
- `downloadUrl` がアクセス可能か確認（ブラウザでテスト）
- ファイアウォール/プロキシによるブロックを確認
- `.minecraft/logs/latest.log` で詳細なエラー情報を確認
- 一般的なエラーコード：`403`（権限なし）、`404`（リンク無効）、`timeout`（ネットワークタイムアウト）

### Q: ダウンロードリンクを変更するには？

設定ファイルの `downloadUrl` フィールドを編集し、ゲームを再起動してください。

### Q: システムファイルは変更されますか？

**いいえ。** 悪戯モードのポップアップは純粋に視覚的なもので、実際の操作は行いません。リアルダウンロードモードは `.minecraft/create-download-genshin-data/` ディレクトリにファイルを保存するだけで、システムファイルは変更しません。

### Q: サーバーがクラッシュしますか？

**いいえ。** クライアント専用コード（GUI、イベントリスナー、Create検出）はすべて `client` ソースセットにあり、サーバーでは読み込まれません。サーバーは設定管理とファイルユーティリティクラスのみを読み込みます。

### Q: ポップアップ表示中にゲームは一時停止しますか？

**いいえ。** どちらのポップアップも `isPauseScreen()` をオーバーライドして `false` を返すため、ゲームワールドは動作し続けます。

### Q: Createをインストールせずに使用できますか？

いいえ。このモッドはCreateユーザー向けに設計されており、Createがインストールされていない場合、モッドは完全に無動作です。

---

## ビルド方法

### 前提条件

- JDK 17+
- ネットワーク接続（初回ビルドで依存関係をダウンロード）

### コンパイル

```bash
# リポジトリをクローン
git clone https://github.com/wututua/Create_Download_Genshin_Fabric.git
cd Create_Download_Genshin_Fabric

# ビルド（Linux/macOS）
./gradlew build

# ビルド（Windows）
gradlew.bat build

# 出力ファイル
# build/libs/create-download-genshin-1.0.0.jar      ← mods/ に配置
# build/libs/create-download-genshin-1.0.0-sources.jar ← ソースパッケージ
```

### テスト

```bash
# テストクライアントを起動
./gradlew runClient
```

---

## 開発ガイド

### 環境構築

1. JDK 17+ をインストール
2. このリポジトリをForkしてクローン
3. IntelliJ IDEAでプロジェクトを開く（推奨、Gradleプロジェクトを自動検出）
4. Gradleの同期が完了するまで待機（初回は依存関係をダウンロード）
5. `./gradlew runClient` を実行してテストクライアントを起動

### コード規約

- Kotlin厳密なヌルセーフ構文、`!!` の乱用は禁止
- すべてのパブリックAPIにKDocコメントが必要
- IO/ネットワーク操作には `try-catch` を使用し、例外情報をログに記録
- クライアントコードは共通パッケージに配置禁止（サーバークラッシュ防止）
- GUIポップアップは `isPauseScreen()` をオーバーライドして `false` を返す必要あり

### 貢献

IssueとPull Requestの提出を歓迎します。詳細は [CONTRIBUTING.md](CONTRIBUTING.md) をご覧ください。

---

## 変更履歴

[CHANGELOG.md](CHANGELOG.md) をご覧ください。

---

## ライセンス

このプロジェクトは [MIT License](LICENSE) の下でライセンスされています。

---

## クレジット

- [Fabric](https://fabricmc.net/) — Minecraftモッドローダー
- [Fabric API](https://github.com/FabricMC/fabric) — FabricコアAPI
- [Fabric Language Kotlin](https://github.com/FabricMC/fabric-language-kotlin) — Kotlin言語サポート
- [Create](https://modrinth.com/mod/create) — 機械動力モッド
- [Minecraft](https://www.minecraft.net/) — ゲーム本体

---

## 免責事項

このモッドは学習と娯楽の目的でのみ使用されます。悪戯モードのポップアップは純粋に視覚的なジョークであり、お使いのコンピュータに実際の影響を与えることはありません。悪意のある目的でこのモッドを使用しないでください。このモッドを使用することにより、すべてのリスクを負うことに同意したものとみなされます。
