# 🎯 SQAS デモ環境 (Demo Sandbox)

購買先品質審査管理システム（GCP0602準拠）のデモ環境です。

## 📦 概要

このデモ環境は、intra-mart環境なしで、システムの主要機能を体験できる独立したサンドボックスです。

## 🚀 クイックスタート

```bash
# Docker Composeで起動
docker compose up -d

# アクセス
# Web UI: http://localhost:8080
# MailHog: http://localhost:8025
```

詳細は [DEMO_QUICKSTART.md](./DEMO_QUICKSTART.md) を参照してください。

## ✨ 主要機能

- **購買先マスター一覧**: 視覚的アラート（色分け）付き
- **審査採点フォーム**: GCP0602準拠の自動スコア計算
- **ワークフローシミュレーター**: 新規審査・再評価プロセスの可視化
- **バッチ通知テスト**: メール送信のシミュレーション（MailHog）

## 🏗️ 技術スタック

- **Backend**: Spring Boot 3.2.0 + Java 17
- **Database**: PostgreSQL 15
- **Mail**: MailHog (開発用SMTPサーバー)
- **Frontend**: Thymeleaf + Vanilla JavaScript

## 📁 ディレクトリ構造

```
demo-sandbox/
├── docker-compose.yml          # Docker Compose設定
├── Dockerfile                  # アプリケーションのDockerfile
├── pom.xml                     # Maven設定
├── src/
│   └── main/
│       ├── java/               # Javaソースコード
│       │   └── jp/co/company/sqas/demo/
│       │       ├── DemoApplication.java
│       │       ├── controller/
│       │       ├── service/
│       │       └── model/
│       └── resources/
│           ├── application.yml # Spring Boot設定
│           ├── static/         # 静的ファイル（CSS）
│           └── templates/      # Thymeleafテンプレート
├── docker/
│   └── init-db.sh             # データベース初期化スクリプト
└── demo-output/               # PDF出力先（マウント）
```

## 🔧 開発

### ローカル開発（Docker不使用）

```bash
# PostgreSQLを起動（別途）
# MailHogを起動（別途）

# アプリケーションを起動
./mvnw spring-boot:run

# または
./mvnw package
java -jar target/sqas-demo.jar
```

### 環境変数

`application.yml` で以下の環境変数を設定できます：

- `SPRING_DATASOURCE_URL`: データベース接続URL
- `SPRING_DATASOURCE_USERNAME`: データベースユーザー名
- `SPRING_DATASOURCE_PASSWORD`: データベースパスワード
- `SPRING_MAIL_HOST`: SMTPホスト
- `SPRING_MAIL_PORT`: SMTPポート

## 📊 API エンドポイント

### 購買先管理
- `GET /api/suppliers` - 購買先一覧取得

### スコア計算
- `POST /api/calculate-score` - 審査スコア計算

### バッチジョブ
- `POST /api/batch/review-reminder` - 再評価リマインダー実行
- `POST /api/batch/corrective-action-reminder` - 是正処置リマインダー実行

### PDF生成
- `POST /api/generate-pdf` - PDF生成

## 🧪 テスト

```bash
# curlでAPIテスト
curl http://localhost:8080/api/suppliers

# スコア計算テスト
curl -X POST http://localhost:8080/api/calculate-score \
  -H "Content-Type: application/json" \
  -d '{"scores":[{"itemId":"1-1","score":4},{"itemId":"1-2","score":4}]}'

# バッチジョブテスト
curl -X POST http://localhost:8080/api/batch/review-reminder
```

## 🗄️ データベース

サンプルデータは自動的に投入されます：

- 購買先マスター: 5社
- 審査履歴: 5件
- 是正処置履歴: 2件

データベースに直接接続：

```bash
docker compose exec db psql -U sqas_user -d sqas_demo
```

## 📧 メール確認

MailHog Web UI: http://localhost:8025

バッチジョブから送信されたメールをブラウザで確認できます。

## 🛑 停止・削除

```bash
# 停止
docker compose stop

# 停止して削除
docker compose down

# データも削除
docker compose down -v
```

## 📚 関連ドキュメント

- [DEMO_QUICKSTART.md](./DEMO_QUICKSTART.md) - デモ環境の使い方
- [../README.md](../README.md) - システム全体の概要
- [../docs/DEPLOYMENT_GUIDE.md](../docs/DEPLOYMENT_GUIDE.md) - intra-mart環境へのデプロイ手順

## 🔗 本番環境との違い

| 項目 | デモ環境 | 本番環境（intra-mart） |
|------|---------|----------------------|
| UI | Thymeleaf + HTML | IM-QuickWebSystem |
| ワークフロー | シミュレーター（可視化のみ） | IM-Workflow |
| 権限管理 | なし | IM-Authz |
| メール送信 | MailHog（テスト用） | 実際のSMTPサーバー |
| PDF生成 | 簡易版 | 本格的なPDF生成 |

デモ環境は**学習・理解**を目的としており、本番環境の動作を忠実に再現しています。

---

**SQAS Demo Sandbox v1.0.0** | GCP0602準拠
