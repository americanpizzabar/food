# FoodAI - AI食事管理アプリ

Claude AI（claude-sonnet-4-6）を活用したAndroid食事管理アプリです。

## 主な機能

### 📸 写真解析
- 料理写真をカメラで撮影または選択
- AIがレシピ名、材料、調理手順、カロリー、栄養素を自動推定
- 間違いがあれば各項目を直接編集可能
- 盛り付けアドバイス・リメイク案も提案

### 🍳 AIレシピ提案
- 今の気分・食べたいもの・体調・必要栄養素を入力
- 冷蔵庫の在庫を優先した3種類のレシピを提案
- アレルギー・苦手食材を自動除外
- 各レシピから買い物リストを1タップ生成

### 📔 食事日記
- 料理の写真・評価（★1〜5）・感想・気分を記録
- 日付・評価・料理名で検索・並び替え
- 食後体調（エネルギー・消化）のログ

### 🛒 買い物リスト（オフライン対応）
- カテゴリ別グループ表示
- チェックで購入済みマーク
- 完了品を一括削除
- ホーム画面ウィジェット対応

### 🏠 パントリー（在庫）管理
- 冷蔵庫・食材棚の在庫管理
- 期限切れアラート（3日前）
- 在庫食材を優先したレシピ提案

### 📅 献立カレンダー
- 週間カレンダーで献立を計画
- 朝食・昼食・夕食・間食のスロット管理
- ルーティン登録（例：月曜は消化に良いもの）

### 🔗 URLレシピ取り込み
- WebサイトのURLからレシピを自動抽出・保存

### 📱 SNS共有カード
- 栄養素・感想をまとめたInstagram向けキャプション自動生成

### 👤 パーソナライズ
- アレルギー・苦手食材の登録
- 食事制限設定（ベジタリアン等）
- 体調ログとの相関分析

## セットアップ

### 必要環境
- Android Studio Ladybug以降
- Android 8.0（API 26）以上の端末
- Claude API キー（[Anthropic](https://console.anthropic.com)で取得）

### APIキー設定

`local.properties`に追加：
```properties
CLAUDE_API_KEY=your_api_key_here
```

または アプリの設定画面から入力。

### ビルド
```bash
./gradlew assembleDebug
```

## アーキテクチャ

```
Clean Architecture + MVVM
├── data/           Room DB, Retrofit, Repository実装
├── domain/         モデル, Repositoryインターフェース, UseCases
└── presentation/   Compose UI, ViewModels
```

**技術スタック**: Kotlin, Jetpack Compose (Material3), Room, Hilt, Retrofit, Claude API, CameraX, Glance Widgets, DataStore, WorkManager
