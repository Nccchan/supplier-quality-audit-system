# 購買先品質審査管理システム 納品物サマリー

## 📦 納品物一覧

本パッケージには、GCP0602「購買先の品質審査管理規定」に準拠した購買先品質審査管理システムの完全な実装が含まれています。

---

## 📂 ディレクトリ構成

```
supplier-quality-audit-system/
├── README.md                                    # システム概要・使用方法
├── DELIVERABLES_SUMMARY.md                      # 本ファイル
├── database/                                    # データベース関連
│   ├── schema_design.md                        # スキーマ設計書
│   ├── 01_create_tables.sql                    # テーブル作成DDL
│   └── 02_sample_data.sql                      # サンプルデータ
├── java/                                        # Javaソースコード
│   ├── services/                                # ビジネスロジック
│   │   ├── AuditScoreCalculationService.java   # スコア計算サービス
│   │   ├── SupplierManagementService.java      # 購買先管理サービス
│   │   └── AuditReportPDFService.java          # PDF生成サービス
│   └── batch/                                   # バッチジョブ
│       ├── ReviewReminderBatchJob.java         # 再評価リマインダー
│       └── CorrectiveActionReminderBatchJob.java # 是正期限リマインダー
├── forms/                                       # 画面定義
│   ├── supplier_master_list.html               # 購買先マスター一覧
│   ├── audit_scoring_form.html                 # 審査採点フォーム
│   └── js/                                      # JavaScript
│       └── supplier_alerts.js                  # 視覚的アラート機能
├── workflows/                                   # ワークフロー定義
│   ├── new_supplier_audit_workflow.xml         # 新規審査ワークフロー
│   └── periodic_review_workflow.xml            # 定期再評価ワークフロー
├── config/                                      # 設定ファイル
│   └── authz_roles.xml                         # ロール・権限設定
└── docs/                                        # ドキュメント
    └── DEPLOYMENT_GUIDE.md                     # デプロイメントガイド
```

---

## 📋 納品物詳細

### 1. データベース関連 (database/)

#### schema_design.md
- **内容**: データベーススキーマの詳細設計書
- **含まれる情報**:
  - ER図の説明
  - 全テーブルの詳細仕様（カラム定義、制約、インデックス）
  - リレーションシップの説明
  - データ型の選択理由

#### 01_create_tables.sql
- **内容**: テーブル作成DDLスクリプト
- **テーブル**:
  - SupplierMaster（購買先マスター）
  - AuditHistory（審査履歴）
  - CorrectiveActionHistory（是正処置履歴）
  - AuditScoreDetails（審査採点詳細）
  - NotificationHistory（通知履歴）
- **機能**:
  - 主キー、外部キー制約
  - インデックス（検索性能最適化）
  - デフォルト値設定
  - CHECK制約（データ整合性）

#### 02_sample_data.sql
- **内容**: テスト用サンプルデータ
- **データ**:
  - 5件の購買先データ（様々な等級・ステータス）
  - 8件の審査履歴データ
  - 3件の是正処置データ
  - 採点詳細データ
  - 通知履歴データ

---

### 2. Javaソースコード (java/)

#### services/AuditScoreCalculationService.java
- **機能**: GCP0602準拠のスコア計算ロジック
- **主要メソッド**:
  - `calculateTotalScore()`: 総合点の自動計算
  - `determineRating()`: 評価判定（優/良/可/不可）
  - `determineDecision()`: 判定（合格/是正指示/不合格）
  - `determineGrade()`: 等級判定（1/2/3）
- **計算式**: `(評価点合計 × 100) / (4 × 総項目数 - 4 × 未調査項目数)`
- **特徴**: 
  - 未調査項目の適切な処理
  - ゼロ除算の防止
  - 小数点以下2桁の精度

#### services/SupplierManagementService.java
- **機能**: 購買先マスターの管理
- **主要メソッド**:
  - `updateSupplierRatingAfterAudit()`: 審査後の等級更新
  - `getSuppliersRequiringReview()`: 再評価対象の購買先取得
  - `generateSupplierId()`: 購買先ID生成
  - `getRatingDescription()`: 等級の日本語表記取得
- **特徴**:
  - 次回審査予定日の自動計算（2年後）
  - 再評価期限のアラート判定
  - ISO9001認証期限の管理

#### services/AuditReportPDFService.java
- **機能**: 審査報告書のPDF生成
- **対応様式**:
  - 様式-8（審査結果報告書）
  - 様式-11（実施監査結果）
- **主要メソッド**:
  - `generateForm8PDF()`: 様式-8のPDF生成
  - `generateForm11PDF()`: 様式-11のPDF生成
- **特徴**:
  - GCP0602準拠のフォーマット
  - 採点詳細の表形式出力
  - 承認情報の記録
- **注意**: 実際の実装では、Apache PDFBox、iText、JasperReportsなどのライブラリを使用してください

#### batch/ReviewReminderBatchJob.java
- **機能**: 再評価リマインダー通知バッチ
- **実行タイミング**: 日次（推奨: 深夜2時）
- **通知条件**: 次回審査予定日の30日前
- **通知先**: 品質保証部長、調達部長
- **主要メソッド**:
  - `execute()`: バッチ実行エントリーポイント
  - `getSuppliersRequiringReview()`: 対象購買先の抽出
  - `sendReviewReminderNotification()`: 通知メール送信
- **特徴**:
  - 重複通知の防止
  - 実行結果のログ記録
  - エラーハンドリング

#### batch/CorrectiveActionReminderBatchJob.java
- **機能**: 是正処置期限リマインダー通知バッチ
- **実行タイミング**: 日次（推奨: 深夜3時）
- **通知条件**:
  - 是正期限の3日前（警告）
  - 是正期限超過時（緊急）
- **通知先**: 品質保証部長、審査担当者、関係部門
- **主要メソッド**:
  - `execute()`: バッチ実行エントリーポイント
  - `getCorrectiveActionsDueSoon()`: 期限間近の是正処置取得
  - `getOverdueCorrectiveActions()`: 期限超過の是正処置取得
  - `sendReminderNotification()`: 警告通知送信
  - `sendOverdueNotification()`: 緊急通知送信
- **特徴**:
  - 期限超過時のステータス自動更新
  - エスカレーション機能
  - 重複通知の防止

---

### 3. 画面定義 (forms/)

#### supplier_master_list.html
- **機能**: 購買先マスター一覧画面
- **主要機能**:
  - 購買先一覧の表示
  - フィルタリング機能（会社名、等級、ISO認証、ステータス、審査期限）
  - サマリーカード（総数、期限間近、期限超過）
  - 視覚的アラート（色分け表示）
  - アクション（詳細表示、編集、審査履歴表示）
  - Excel出力（プレースホルダー）
- **特徴**:
  - レスポンシブデザイン
  - リアルタイムフィルタリング
  - 等級バッジ表示（色分け）
  - アラートレベル表示（赤/黄/白）

#### audit_scoring_form.html
- **機能**: 審査採点フォーム
- **対応様式**:
  - 様式-2（書類審査）: 10項目
  - 様式-5（実地審査）: 12項目
- **主要機能**:
  - 審査基本情報入力
  - 項目別採点（4点/2点/0点/未調査）
  - リアルタイムスコア計算
  - 評価・判定の自動表示
  - 計算式の可視化
  - コメント入力
- **特徴**:
  - GCP0602準拠の計算式実装
  - 未調査項目の適切な処理
  - 視覚的フィードバック
  - データ収集機能（JSON形式）

#### js/supplier_alerts.js
- **機能**: 購買先一覧の視覚的アラート機能
- **主要機能**:
  - 再評価期限に基づく色分け表示
  - アラートバッジの生成
  - 残り日数/超過日数の計算
  - サマリーカウントの更新
  - ISO9001認証期限のアラート
  - ステータス・等級のスタイリング
- **アラートレベル**:
  - CRITICAL（赤）: 期限超過
  - WARNING（黄）: 1ヶ月以内
  - NORMAL（白）: 通常
- **特徴**:
  - 自動初期化
  - 公開API提供
  - ツールチップ表示

---

### 4. ワークフロー定義 (workflows/)

#### new_supplier_audit_workflow.xml
- **機能**: 新規購買先審査プロセス（GCP0602 付表-4準拠）
- **フロー**:
  1. 審査申請（申請部門）
  2. 審査実施（品質保証部）
  3. 審査結果報告（品質保証部）
  4. 承認（品質保証部長）
  5. 判定分岐（合格/是正指示/不合格）
  6. 是正処置プロセス（必要時）
  7. 購買先リスト登録
  8. 完了通知
- **ロール**:
  - REQUESTING_DEPT（申請部門）
  - QA_AUDITOR（品質保証部審査員）
  - QA_MANAGER（品質保証部長）
  - PROCUREMENT_DEPT（調達部）
- **特徴**:
  - 判定に応じた自動分岐
  - 是正処置の期限管理
  - SLA（Service Level Agreement）設定
  - イベントハンドラー

#### periodic_review_workflow.xml
- **機能**: 定期再評価プロセス（GCP0602 付表-5, 付表-6準拠）
- **フロー**:
  1. 対象業者選定（調達部・品質保証部）
  2. 実地監査日程調整
  3. 実地監査実施（様式-11）
  4. 監査結果報告書作成
  5. 承認（品質保証部長）
  6. 判定分岐（合格/是正指示/不合格）
  7. 是正処置プロセス（必要時）
  8. 等級更新
  9. フィードバック送信（付表-6）
  10. 完了通知/停止処理
- **ロール**:
  - PROCUREMENT_DEPT（調達部）
  - QA_DEPT（品質保証部）
  - QA_AUDITOR（品質保証部審査員）
  - QA_MANAGER（品質保証部長）
- **特徴**:
  - 2年サイクルの再評価管理
  - 等級の自動更新
  - 不合格時の購買先停止処理
  - フィードバック機能

---

### 5. 設定ファイル (config/)

#### authz_roles.xml
- **機能**: IM-Authz用のロール・権限設定
- **定義内容**:
  - 6つのロール定義
  - 15の権限定義
  - 7つのリソースタイプ定義
  - ロールマッピング例
- **ロール**:
  - QA_MANAGER（品質保証部長）
  - QA_AUDITOR（品質保証部審査員）
  - QA_DEPT（品質保証部一般）
  - REQUESTING_DEPT（申請部門）
  - PROCUREMENT_DEPT（調達部）
  - SYSTEM_ADMIN（システム管理者）
- **権限**:
  - 審査関連（申請、実施、閲覧、編集、承認）
  - 購買先関連（登録、閲覧、更新、削除）
  - 再評価関連（対象選定）
  - 是正処置関連（作成、編集、承認）
  - レポート関連（生成、閲覧）
  - システム管理

---

### 6. ドキュメント (docs/)

#### DEPLOYMENT_GUIDE.md
- **内容**: 詳細なデプロイメント手順書
- **章立て**:
  1. 前提条件
  2. 環境準備
  3. データベースセットアップ
  4. アプリケーションデプロイ
  5. 設定とカスタマイズ
  6. 動作確認
  7. トラブルシューティング
  8. チェックリスト
- **特徴**:
  - ステップバイステップの手順
  - PostgreSQL/Oracle/SQL Server対応
  - CLI/GUI両方の手順
  - トラブルシューティングガイド
  - 包括的なチェックリスト

#### README.md
- **内容**: システム概要・使用方法
- **章立て**:
  1. システム概要
  2. システム構成
  3. データモデル
  4. スコア計算ロジック
  5. 権限管理
  6. 通知機能
  7. インストール手順
  8. 使用方法
  9. カスタマイズ
  10. トラブルシューティング
- **特徴**:
  - 初心者にも分かりやすい説明
  - 豊富な使用例
  - カスタマイズガイド

---

## 🎯 実装済み機能

### ✅ コア機能

1. **GCP0602準拠のスコア計算**
   - 計算式: `(評価点合計 × 100) / (4 × 総項目数 - 4 × 未調査項目数)`
   - 評価判定: 優/良/可/不可
   - 判定: 合格/是正指示/不合格
   - 等級: 1（優良）/2（良好）/3（標準）

2. **新規購買先審査プロセス**
   - 審査申請（様式-1相当）
   - 書類審査（様式-2）
   - 実地審査（様式-5）
   - 審査結果報告書（様式-8）
   - 承認ワークフロー
   - 購買先マスター登録

3. **定期再評価プロセス**
   - 2年サイクルの自動管理
   - 対象業者選定（付表-6）
   - 実地監査（様式-11）
   - 等級更新
   - フィードバック（付表-6）

4. **是正処置管理**
   - 是正処置要求の作成
   - 期限管理（原則30日）
   - 期限通知（3日前、超過時）
   - 是正完了の検証

5. **自動通知機能**
   - 再評価リマインダー（1ヶ月前）
   - 是正期限リマインダー（3日前）
   - 是正期限超過通知
   - 日次バッチ実行

6. **購買先マスター管理**
   - 購買先情報の一元管理
   - 等級管理
   - ISO9001認証管理
   - 視覚的アラート表示

7. **レポート生成**
   - 様式-8（審査結果報告書）PDF生成
   - 様式-11（実施監査結果）PDF生成
   - 監査証跡の保管

8. **権限管理**
   - ロールベースアクセス制御
   - 6つのロール定義
   - 15の権限定義
   - 部門別アクセス制御

---

## 🚀 デプロイ手順概要

1. **データベースセットアップ**
   ```bash
   psql -U username -d database_name -f database/01_create_tables.sql
   psql -U username -d database_name -f database/02_sample_data.sql
   ```

2. **Javaクラスのデプロイ**
   ```bash
   javac -d build/classes java/services/*.java java/batch/*.java
   jar cvf sqas.jar -C build/classes .
   cp sqas.jar $IM_HOME/lib/
   ```

3. **画面定義のインポート**
   - intra-mart管理画面からインポート
   - または CLI: `im-cli import-screen --file forms/supplier_master_list.html`

4. **ワークフロー定義のインポート**
   - intra-mart管理画面からインポート
   - または CLI: `im-cli import-workflow --file workflows/new_supplier_audit_workflow.xml`

5. **権限設定**
   - `config/authz_roles.xml` を参照してロールと権限を設定

6. **バッチジョブの登録**
   - ReviewReminderBatchJob: 日次 02:00
   - CorrectiveActionReminderBatchJob: 日次 03:00

詳細は `docs/DEPLOYMENT_GUIDE.md` を参照してください。

---

## 📊 技術仕様

### プラットフォーム
- **intra-mart Accel Platform**: 2023 Spring以降
- **Java**: 8以降
- **データベース**: PostgreSQL 12+ / Oracle 12c+ / SQL Server 2016+

### 使用モジュール
- **IM-QuickWebSystem**: UI/採点ロジック
- **IM-Workflow**: 承認・通知
- **IM-Authz**: 権限管理
- **IM-JobScheduler**: バッチジョブ

### コード統計
- **Javaクラス**: 5ファイル
- **SQLスクリプト**: 2ファイル
- **HTMLフォーム**: 2ファイル
- **JavaScriptファイル**: 1ファイル
- **XMLファイル**: 3ファイル
- **ドキュメント**: 4ファイル

---

## 📝 注意事項

1. **PDF生成ライブラリ**
   - `AuditReportPDFService.java` は疑似実装です
   - 実際の実装では、Apache PDFBox、iText、JasperReportsなどのライブラリを使用してください

2. **データベース接続**
   - `data-source.xml` でデータベース接続情報を設定してください
   - 接続プール設定を環境に合わせて調整してください

3. **メール送信**
   - `mail.xml` でメール送信設定を行ってください
   - SMTPサーバーの認証情報を設定してください

4. **ロールマッピング**
   - 実際のユーザー・組織マスターと連携してロールマッピングを行ってください

5. **カスタマイズ**
   - スコア計算式、通知タイミング、審査項目などは要件に応じてカスタマイズ可能です

---

## 🔒 セキュリティ

- パスワードは必ず環境変数または暗号化ストレージで管理してください
- 本番環境ではサンプルデータを投入しないでください
- ログファイルに機密情報が含まれないよう注意してください
- 定期的にバックアップを取得してください

---

## 📞 サポート

システムに関するお問い合わせは、以下までご連絡ください：

- **開発チーム**: sqas-dev@company.example.com
- **品質保証部**: qa-dept@company.example.com

---

## 📄 ライセンス

本システムは社内利用専用です。無断での外部配布・転用を禁止します。

---

**購買先品質審査管理システム開発チーム**  
納品日: 2025年11月20日  
バージョン: 1.0
