# 購買先品質審査管理システム (Supplier Quality Audit Management System)

## 📋 システム概要

本システムは、GCP0602「購買先の品質審査管理規定」に準拠した購買先（サプライヤー）の審査・評価・再評価プロセスをデジタル化し、intra-mart Accel Platform環境で効率的に運用するための統合管理システムです。

### 主要機能

1. **新規購買先審査プロセス**
   - 審査申請（様式-1相当）
   - 書類審査（様式-2）および実地審査（様式-5）の採点
   - 自動スコア計算（GCP0602準拠の計算式）
   - 審査結果報告書（様式-8）の生成
   - 品質保証部長による承認ワークフロー
   - 購買先マスターへの自動登録

2. **定期再評価プロセス**
   - 初回登録から2年後の自動リマインダー通知
   - 対象業者選定（付表-6）
   - 実地監査（様式-11）の実施
   - 等級の自動更新
   - フィードバック送信

3. **是正処置管理**
   - 是正処置要求の作成
   - 期限管理（原則30日）
   - 期限3日前および期限超過時の自動通知
   - 是正完了の検証と承認

4. **購買先マスター管理**
   - 購買先情報の一元管理
   - 等級管理（1: 優良、2: 良好、3: 標準）
   - ISO9001認証情報の管理
   - 再評価期限の視覚的アラート表示

5. **レポート生成**
   - 審査結果報告書（様式-8）のPDF生成
   - 実施監査結果（様式-11）のPDF生成
   - 監査証跡の保管

## 🏗️ システム構成

### 技術スタック

- **プラットフォーム**: intra-mart Accel Platform
- **UI/採点ロジック**: IM-QuickWebSystem
- **ワークフロー**: IM-Workflow
- **権限管理**: IM-Authz
- **データベース**: RDBMS（PostgreSQL / Oracle / SQL Server）
- **プログラミング言語**: Java 8+
- **フロントエンド**: HTML5, CSS3, JavaScript

### ディレクトリ構成

```
supplier-quality-audit-system/
├── database/                      # データベース関連
│   ├── schema_design.md          # スキーマ設計書
│   ├── 01_create_tables.sql      # テーブル作成DDL
│   └── 02_sample_data.sql        # サンプルデータ
├── java/                          # Javaソースコード
│   ├── services/                  # ビジネスロジック
│   │   ├── AuditScoreCalculationService.java
│   │   ├── SupplierManagementService.java
│   │   └── AuditReportPDFService.java
│   └── batch/                     # バッチジョブ
│       ├── ReviewReminderBatchJob.java
│       └── CorrectiveActionReminderBatchJob.java
├── forms/                         # 画面定義
│   ├── supplier_master_list.html # 購買先マスター一覧
│   ├── audit_scoring_form.html   # 審査採点フォーム
│   └── js/                        # JavaScript
│       └── supplier_alerts.js    # 視覚的アラート機能
├── workflows/                     # ワークフロー定義
│   ├── new_supplier_audit_workflow.xml
│   └── periodic_review_workflow.xml
├── config/                        # 設定ファイル
│   └── authz_roles.xml           # ロール・権限設定
├── docs/                          # ドキュメント
│   └── DEPLOYMENT_GUIDE.md       # デプロイメントガイド
└── README.md                      # 本ファイル
```

## 📊 データモデル

### 主要テーブル

1. **SupplierMaster（購買先マスター）**
   - 購買先の基本情報と現行ステータス
   - 主キー: SupplierID
   - 重要カラム: CurrentRating（等級）, NextReviewDate（次回審査予定日）

2. **AuditHistory（審査履歴）**
   - 個別の審査実施記録と結果
   - 主キー: AuditID
   - 外部キー: SupplierID
   - 重要カラム: TotalScore（総合点）, Rating（評価）, FinalDecision（判定）

3. **CorrectiveActionHistory（是正処置履歴）**
   - 不適合発生時の是正計画管理
   - 主キー: CorrectiveActionID
   - 外部キー: AuditID
   - 重要カラム: CorrectiveActionDeadline（是正期限）

4. **AuditScoreDetails（審査採点詳細）**
   - 審査項目ごとの採点記録

5. **NotificationHistory（通知履歴）**
   - システムからの通知履歴

詳細は `database/schema_design.md` を参照してください。

## 🔢 スコア計算ロジック

### GCP0602準拠の計算式

```
総合点 = (評価点合計 × 100) / (4 × 総項目数 - 4 × 未調査項目数)
```

### 評価基準

- **優**: 80点以上
- **良**: 70-79点
- **可**: 60-69点
- **不可**: 60点未満

### 判定基準

- **合格 (PASS)**: 80点以上
- **是正指示 (CONDITIONAL)**: 60-79点
- **不合格 (FAIL)**: 60点未満

### 等級

- **等級1（優良）**: 80点以上
- **等級2（良好）**: 70-79点
- **等級3（標準）**: 60-69点

## 🔐 権限管理

### ロール定義

1. **品質保証部長 (QA_MANAGER)**
   - 審査結果の最終承認
   - リスト登録・更新
   - 審査記録の閲覧・編集

2. **品質保証部審査員 (QA_AUDITOR)**
   - 審査の実施
   - 審査記録の作成・編集
   - 是正処置の管理

3. **申請部門 (REQUESTING_DEPT)**
   - 審査申請（様式-1相当）の作成
   - 自部門の申請状況の閲覧

4. **調達部 (PROCUREMENT_DEPT)**
   - 再評価対象業者の選定
   - 審査記録の閲覧
   - 購買先情報の閲覧

詳細は `config/authz_roles.xml` を参照してください。

## 📅 通知機能

### 自動通知トリガー

1. **再評価リマインダー**
   - タイミング: 初回登録日から2年後の1ヶ月前
   - 通知先: 品質保証部長、調達部長
   - 実行頻度: 日次バッチ

2. **是正期限リマインダー**
   - タイミング: 是正期限の3日前
   - 通知先: 品質保証部長、審査担当者、関係部門
   - 実行頻度: 日次バッチ

3. **是正期限超過通知**
   - タイミング: 是正期限超過時
   - 通知先: 品質保証部長、審査担当者、関係部門
   - 実行頻度: 日次バッチ

## 🚀 インストール手順

### 前提条件

- intra-mart Accel Platform 2023 Spring以降
- Java 8以降
- PostgreSQL 12以降 / Oracle 12c以降 / SQL Server 2016以降
- IM-QuickWebSystem モジュール
- IM-Workflow モジュール
- IM-Authz モジュール

### インストール手順

1. **データベースセットアップ**
   ```sql
   -- テーブル作成
   psql -U username -d database_name -f database/01_create_tables.sql
   
   -- サンプルデータ投入（オプション）
   psql -U username -d database_name -f database/02_sample_data.sql
   ```

2. **Javaクラスのデプロイ**
   ```bash
   # Javaソースをコンパイル
   javac -d build/classes java/services/*.java java/batch/*.java
   
   # JARファイルを作成
   jar cvf sqas.jar -C build/classes .
   
   # intra-martのlibディレクトリにコピー
   cp sqas.jar $IM_HOME/lib/
   ```

3. **画面定義のインポート**
   - intra-mart管理画面にログイン
   - IM-QuickWebSystem > 画面定義インポート
   - `forms/supplier_master_list.html` をインポート
   - `forms/audit_scoring_form.html` をインポート

4. **ワークフロー定義のインポート**
   - IM-Workflow > ワークフロー定義インポート
   - `workflows/new_supplier_audit_workflow.xml` をインポート
   - `workflows/periodic_review_workflow.xml` をインポート

5. **権限設定**
   - IM-Authz > ロール管理
   - `config/authz_roles.xml` を参照してロールと権限を設定

6. **バッチジョブの登録**
   - IM-JobScheduler > ジョブ登録
   - ReviewReminderBatchJob を日次実行で登録
   - CorrectiveActionReminderBatchJob を日次実行で登録

詳細は `docs/DEPLOYMENT_GUIDE.md` を参照してください。

## 📖 使用方法

### 新規購買先審査の流れ

1. **審査申請**
   - 申請部門が新規購買先審査を申請
   - 購買先情報、ISO9001認証情報を入力

2. **審査実施**
   - 品質保証部審査員が書類審査（様式-2）を実施
   - 必要に応じて実地審査（様式-5）を実施
   - システムが自動的に総合点を計算

3. **審査結果報告**
   - 審査員が審査結果報告書（様式-8）を作成
   - 総合所見を記入

4. **承認**
   - 品質保証部長が審査結果を承認
   - 60点未満の場合は不合格、60-79点の場合は是正指示

5. **購買先登録**
   - 承認後、自動的に購買先マスターに登録
   - 等級が設定され、2年後の再評価予定日が設定される

### 定期再評価の流れ

1. **リマインダー通知**
   - 再評価期限の1ヶ月前に自動通知

2. **対象業者選定**
   - 調達部と品質保証部で対象業者を選定

3. **実地監査実施**
   - 品質保証部審査員が実地監査（様式-11）を実施

4. **承認と等級更新**
   - 品質保証部長が承認
   - システムが自動的に等級を更新

5. **フィードバック**
   - 購買先へ監査結果をフィードバック

## 🔧 カスタマイズ

### スコア計算式の変更

`java/services/AuditScoreCalculationService.java` の `calculateTotalScore()` メソッドを編集してください。

### 通知タイミングの変更

バッチジョブクラスの定数を変更してください：
- `ReviewReminderBatchJob.REMINDER_DAYS_BEFORE`
- `CorrectiveActionReminderBatchJob.REMINDER_DAYS_BEFORE`

### 審査項目の追加

`forms/audit_scoring_form.html` の `form2Questions` または `form5Questions` 配列を編集してください。

## 🐛 トラブルシューティング

### スコア計算が正しくない

- 計算式が正しく実装されているか確認
- 未調査項目が正しくカウントされているか確認
- ブラウザのコンソールでエラーを確認

### ワークフローが起動しない

- ワークフロー定義が正しくインポートされているか確認
- ロールマッピングが正しく設定されているか確認
- IM-Workflowのログを確認

### バッチジョブが実行されない

- ジョブスケジューラに正しく登録されているか確認
- Javaクラスがクラスパスに含まれているか確認
- バッチジョブのログを確認

## 📞 サポート

システムに関するお問い合わせは、以下までご連絡ください：

- **開発チーム**: sqas-dev@company.example.com
- **品質保証部**: qa-dept@company.example.com

## 📄 ライセンス

本システムは社内利用専用です。無断での外部配布・転用を禁止します。

## 📝 変更履歴

### Version 1.0 (2025-11-20)
- 初回リリース
- 新規購買先審査プロセスの実装
- 定期再評価プロセスの実装
- 是正処置管理機能の実装
- 自動通知機能の実装
- GCP0602準拠のスコア計算ロジックの実装

---

**購買先品質審査管理システム開発チーム**  
最終更新日: 2025年11月20日
