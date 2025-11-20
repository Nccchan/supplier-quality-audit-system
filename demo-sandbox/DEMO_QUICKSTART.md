# 📦 SQAS デモ環境 クイックスタートガイド

## 🎯 概要

このデモ環境は、GCP0602準拠の購買先品質審査管理システムの主要機能を、**intra-mart環境なし**で体験できる独立したサンドボックスです。

## ✨ デモ環境の特徴

- **簡単起動**: `docker compose up` だけで全環境が立ち上がります
- **サンプルデータ投入済み**: すぐに動作確認できます
- **メール確認**: MailHogでバッチ通知をブラウザで確認できます
- **完全な機能**: スコア計算、アラート、ワークフロー、PDF生成をすべて体験できます

## 🚀 起動方法

### 前提条件

- Docker Desktop または Docker Engine + Docker Compose がインストール済み
- ポート 5432, 8025, 8080 が空いていること

### 起動手順

```bash
# 1. デモディレクトリに移動
cd demo-sandbox

# 2. Docker Composeで起動
docker compose up -d

# 3. 起動確認（すべてのコンテナが "Up" になるまで待つ）
docker compose ps

# 4. ログ確認（オプション）
docker compose logs -f app
```

### アクセス

起動後、以下のURLにアクセスできます：

- **Web UI**: http://localhost:8080
- **MailHog（メール確認）**: http://localhost:8025

## 📋 デモ機能

### 1. 購買先マスター一覧

**URL**: http://localhost:8080/suppliers

**機能**:
- 登録済み購買先の一覧表示
- 視覚的アラート（色分け）
  - 🔴 赤: 再評価期限超過
  - 🟡 黄: 再評価期限1ヶ月以内
  - 🟢 緑: 正常
- 等級表示（1: 優良, 2: 良好, 3: 標準）
- ISO9001認証状況

### 2. 審査採点フォーム

**URL**: http://localhost:8080/audit-scoring

**機能**:
- GCP0602準拠の評価項目入力
- 自動スコア計算
  - 計算式: `(評価点合計 × 100) / (4 × 総項目数 - 4 × 未調査項目数)`
- リアルタイム判定
  - 評価: 優(80+), 良(70-79), 可(60-69), 不可(<60)
  - 判定: 合格/是正指示/不合格
  - 等級: 1/2/3

**使い方**:
1. 各評価項目で点数を選択（4点/2点/0点/未調査）
2. 「スコア計算」ボタンをクリック
3. 計算結果が表示されます

### 3. ワークフローシミュレーター

**URL**: http://localhost:8080/workflow-simulator

**機能**:
- 新規審査フローの可視化（付表-4準拠）
- 定期再評価フローの可視化（付表-5準拠）
- 各ステップの担当者・様式・処理内容を表示

### 4. バッチジョブテスト

**場所**: ホーム画面（http://localhost:8080）

**機能**:

#### 再評価リマインダー
- 再評価予定日が30日以内の購買先に通知
- 実行後、MailHog（http://localhost:8025）でメールを確認

#### 是正処置リマインダー
- 是正処置期限が3日以内の場合: 警告メール
- 是正処置期限超過の場合: 緊急通知メール
- 実行後、MailHog（http://localhost:8025）でメールを確認

## 🧪 テストシナリオ

### シナリオ1: 購買先一覧の確認

```bash
# 1. 購買先一覧を開く
open http://localhost:8080/suppliers

# 2. 確認ポイント
# - 5社のサンプルデータが表示される
# - 再評価期限が近い購買先が黄色または赤色で表示される
# - 等級（1, 2, 3）が表示される
```

### シナリオ2: 審査採点の実施

```bash
# 1. 審査採点フォームを開く
open http://localhost:8080/audit-scoring

# 2. すべての項目で「4点 - 適合」を選択
# 3. 「スコア計算」をクリック
# 4. 結果確認
#    - 総合点: 100.00点
#    - 評価: 優
#    - 判定: 合格
#    - 等級: 1

# 5. 別のパターンを試す
# - 一部の項目を「2点」または「0点」に変更
# - 「未調査」を選択した場合の計算式の変化を確認
```

### シナリオ3: バッチ通知のテスト

```bash
# 1. MailHogを開く
open http://localhost:8025

# 2. ホーム画面を開く
open http://localhost:8080

# 3. 「再評価リマインダー」ボタンをクリック
# 4. MailHogで受信メールを確認
#    - 件名: [SQAS] 再評価リマインダー: XXX株式会社
#    - 本文: 購買先情報と再評価予定日

# 5. 「是正処置リマインダー」ボタンをクリック
# 6. MailHogで受信メールを確認
#    - 警告メールと期限超過メールが送信される
```

### シナリオ4: ワークフローの理解

```bash
# 1. ワークフローシミュレーターを開く
open http://localhost:8080/workflow-simulator

# 2. 「新規審査フロー」タブを確認
#    - 9ステップのプロセスを確認
#    - 各ステップの担当者と様式を確認

# 3. 「定期再評価フロー」タブを確認
#    - 再評価プロセスの流れを確認
#    - 是正処置管理の仕組みを確認
```

## 🗄️ データベース接続

デモ環境のPostgreSQLに直接接続することもできます：

```bash
# psqlで接続
docker compose exec db psql -U sqas_user -d sqas_demo

# または、任意のPostgreSQLクライアントで接続
# ホスト: localhost
# ポート: 5432
# データベース: sqas_demo
# ユーザー: sqas_user
# パスワード: sqas_password
```

### サンプルクエリ

```sql
-- 購買先一覧
SELECT * FROM SupplierMaster;

-- 審査履歴
SELECT * FROM AuditHistory;

-- 是正処置履歴
SELECT * FROM CorrectiveActionHistory;

-- 再評価が近い購買先
SELECT CompanyName, NextReviewDate 
FROM SupplierMaster 
WHERE NextReviewDate <= CURRENT_DATE + INTERVAL '30 days'
ORDER BY NextReviewDate;
```

## 🛑 停止方法

```bash
# コンテナを停止（データは保持）
docker compose stop

# コンテナを停止して削除（データも削除）
docker compose down

# コンテナ、ボリューム、イメージをすべて削除
docker compose down -v --rmi all
```

## 🔄 再起動方法

```bash
# 停止後に再起動
docker compose start

# または、完全に再構築
docker compose down
docker compose up -d --build
```

## 📊 システム構成

```
┌─────────────────────────────────────────┐
│         Web UI (Port 8080)              │
│  - 購買先一覧                            │
│  - 審査採点フォーム                       │
│  - ワークフローシミュレーター              │
└─────────────────┬───────────────────────┘
                  │
┌─────────────────▼───────────────────────┐
│    Spring Boot Application              │
│  - AuditScoreService (スコア計算)        │
│  - SupplierService (購買先管理)          │
│  - BatchJobService (バッチ通知)          │
└─────────┬───────────────────┬───────────┘
          │                   │
┌─────────▼─────────┐ ┌───────▼───────────┐
│  PostgreSQL       │ │   MailHog         │
│  (Port 5432)      │ │   (Port 8025)     │
│  - サンプルデータ   │ │   - メール確認UI   │
└───────────────────┘ └───────────────────┘
```

## 🐛 トラブルシューティング

### ポートが既に使用されている

```bash
# 使用中のポートを確認
lsof -i :8080
lsof -i :5432
lsof -i :8025

# docker-compose.ymlのポート番号を変更
# 例: "8081:8080" に変更
```

### コンテナが起動しない

```bash
# ログを確認
docker compose logs

# 特定のサービスのログを確認
docker compose logs app
docker compose logs db

# コンテナの状態を確認
docker compose ps
```

### データベース接続エラー

```bash
# データベースの起動を待つ
docker compose logs db

# "database system is ready to accept connections" が表示されるまで待つ

# アプリケーションを再起動
docker compose restart app
```

### ブラウザでアクセスできない

```bash
# コンテナが起動しているか確認
docker compose ps

# すべて "Up" になっているか確認

# ブラウザのキャッシュをクリア
# または、シークレットモードで開く
```

## 📚 次のステップ

デモ環境で動作を理解したら、実際のintra-mart環境へのデプロイを検討してください：

1. **デプロイガイドを確認**: `../docs/DEPLOYMENT_GUIDE.md`
2. **intra-mart環境の準備**: IM-QuickWebSystem, IM-Workflow, IM-Authz
3. **データベース設定**: `../database/01_create_tables.sql` を実行
4. **ファイルのインポート**: Java, 画面定義, ワークフロー, 権限設定
5. **動作確認**: デモ環境と同じ機能が動作することを確認

## 💡 ヒント

- **メール送信のテスト**: MailHogは実際にメールを送信せず、すべてブラウザで確認できます
- **データのリセット**: `docker compose down -v` でデータベースをリセットできます
- **カスタマイズ**: `application.yml` で設定を変更できます
- **ログ監視**: `docker compose logs -f app` でリアルタイムログを確認できます

## 🆘 サポート

問題が発生した場合は、以下を確認してください：

1. Docker Desktopが起動しているか
2. 必要なポートが空いているか
3. ログにエラーメッセージが表示されていないか
4. README.mdとDEPLOYMENT_GUIDE.mdを確認

---

**SQAS Demo Sandbox v1.0.0** | GCP0602準拠 | intra-mart Accel Platform対応
