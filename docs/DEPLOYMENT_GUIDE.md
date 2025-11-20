# 購買先品質審査管理システム デプロイメントガイド

## 📋 目次

1. [前提条件](#前提条件)
2. [環境準備](#環境準備)
3. [データベースセットアップ](#データベースセットアップ)
4. [アプリケーションデプロイ](#アプリケーションデプロイ)
5. [設定とカスタマイズ](#設定とカスタマイズ)
6. [動作確認](#動作確認)
7. [トラブルシューティング](#トラブルシューティング)
8. [チェックリスト](#チェックリスト)

---

## 前提条件

### ソフトウェア要件

| ソフトウェア | バージョン | 必須/推奨 |
|------------|----------|---------|
| intra-mart Accel Platform | 2023 Spring以降 | 必須 |
| Java Development Kit (JDK) | 8以降 | 必須 |
| PostgreSQL | 12以降 | 推奨 |
| Oracle Database | 12c以降 | 推奨 |
| SQL Server | 2016以降 | 推奨 |
| Apache Tomcat | 9.0以降 | 必須（intra-martに含まれる） |

### intra-martモジュール要件

- IM-QuickWebSystem
- IM-Workflow
- IM-Authz
- IM-JobScheduler
- IM-Storage（オプション）

### 権限要件

- データベース管理者権限（テーブル作成、インデックス作成）
- intra-mart システム管理者権限
- アプリケーションサーバーへのデプロイ権限

---

## 環境準備

### 1. 作業ディレクトリの作成

```bash
# デプロイ用ディレクトリを作成
mkdir -p /opt/sqas-deployment
cd /opt/sqas-deployment

# システムファイルを配置
# （提供されたファイル一式をコピー）
```

### 2. 環境変数の設定

```bash
# intra-martホームディレクトリ
export IM_HOME=/opt/intra-mart

# データベース接続情報
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=sqas_db
export DB_USER=sqas_user
export DB_PASSWORD=your_secure_password
```

### 3. データベースユーザーの作成

#### PostgreSQLの場合

```sql
-- データベースとユーザーを作成
CREATE DATABASE sqas_db
    WITH ENCODING='UTF8'
    LC_COLLATE='ja_JP.UTF-8'
    LC_CTYPE='ja_JP.UTF-8'
    TEMPLATE=template0;

CREATE USER sqas_user WITH PASSWORD 'your_secure_password';

-- 権限を付与
GRANT ALL PRIVILEGES ON DATABASE sqas_db TO sqas_user;

-- スキーマ権限
\c sqas_db
GRANT ALL ON SCHEMA public TO sqas_user;
```

#### Oracleの場合

```sql
-- ユーザーを作成
CREATE USER sqas_user IDENTIFIED BY your_secure_password
    DEFAULT TABLESPACE users
    TEMPORARY TABLESPACE temp
    QUOTA UNLIMITED ON users;

-- 権限を付与
GRANT CONNECT, RESOURCE TO sqas_user;
GRANT CREATE VIEW, CREATE PROCEDURE, CREATE SEQUENCE TO sqas_user;
```

---

## データベースセットアップ

### 1. テーブル作成

```bash
# PostgreSQLの場合
psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -f database/01_create_tables.sql

# Oracleの場合
sqlplus sqas_user/your_secure_password@//localhost:1521/ORCL @database/01_create_tables.sql
```

### 2. インデックスの確認

```sql
-- インデックスが正しく作成されたか確認
SELECT tablename, indexname 
FROM pg_indexes 
WHERE schemaname = 'public' 
  AND tablename IN ('suppliermaster', 'audithistory', 'correctiveactionhistory')
ORDER BY tablename, indexname;
```

### 3. サンプルデータの投入（オプション）

```bash
# テスト環境の場合のみ実行
psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -f database/02_sample_data.sql
```

### 4. データベース接続テスト

```sql
-- 接続テスト
SELECT COUNT(*) FROM SupplierMaster;
SELECT COUNT(*) FROM AuditHistory;
SELECT COUNT(*) FROM CorrectiveActionHistory;
```

---

## アプリケーションデプロイ

### 1. Javaクラスのコンパイルとデプロイ

```bash
# コンパイル用ディレクトリを作成
mkdir -p build/classes

# Javaソースをコンパイル
javac -cp "$IM_HOME/lib/*" \
    -d build/classes \
    java/services/*.java \
    java/batch/*.java

# JARファイルを作成
cd build/classes
jar cvf sqas.jar jp/
cd ../..

# intra-martのlibディレクトリにコピー
cp build/classes/sqas.jar $IM_HOME/lib/

# 権限設定
chmod 644 $IM_HOME/lib/sqas.jar
```

### 2. 画面定義のインポート

#### 手順

1. intra-mart管理画面にログイン
   - URL: `http://your-server:8080/imart/system`
   - ユーザー: システム管理者

2. IM-QuickWebSystem > 画面定義管理 > インポート

3. 以下のファイルをインポート
   - `forms/supplier_master_list.html`
   - `forms/audit_scoring_form.html`

4. 画面IDを設定
   - supplier_master_list: `sqas_supplier_list`
   - audit_scoring_form: `sqas_audit_scoring`

#### CLIでのインポート（オプション）

```bash
# intra-mart CLIツールを使用
$IM_HOME/bin/im-cli import-screen \
    --file forms/supplier_master_list.html \
    --screen-id sqas_supplier_list \
    --screen-name "購買先マスター一覧"
```

### 3. JavaScriptファイルのデプロイ

```bash
# JavaScriptファイルをWebリソースディレクトリにコピー
cp forms/js/supplier_alerts.js $IM_HOME/webapps/imart/scripts/sqas/

# 権限設定
chmod 644 $IM_HOME/webapps/imart/scripts/sqas/supplier_alerts.js
```

### 4. ワークフロー定義のインポート

#### 手順

1. IM-Workflow > ワークフロー定義管理 > インポート

2. 以下のファイルをインポート
   - `workflows/new_supplier_audit_workflow.xml`
   - `workflows/periodic_review_workflow.xml`

3. ワークフローIDを確認
   - new_supplier_audit: `new_supplier_audit`
   - periodic_review: `periodic_review`

4. ワークフローを有効化

#### CLIでのインポート（オプション）

```bash
# intra-mart CLIツールを使用
$IM_HOME/bin/im-cli import-workflow \
    --file workflows/new_supplier_audit_workflow.xml \
    --workflow-id new_supplier_audit

$IM_HOME/bin/im-cli import-workflow \
    --file workflows/periodic_review_workflow.xml \
    --workflow-id periodic_review
```

### 5. 権限設定のインポート

#### 手順

1. IM-Authz > ロール管理 > インポート

2. `config/authz_roles.xml` を参照してロールを作成

3. 各ロールに権限を割り当て

4. ユーザーにロールを割り当て

#### ロールマッピング例

```xml
<!-- 品質保証部長 -->
<role-mapping role-id="QA_MANAGER">
    <user-id>yamada_taro</user-id>
</role-mapping>

<!-- 品質保証部審査員 -->
<role-mapping role-id="QA_AUDITOR">
    <user-id>sato_hanako</user-id>
    <user-id>tanaka_jiro</user-id>
</role-mapping>
```

### 6. バッチジョブの登録

#### 手順

1. IM-JobScheduler > ジョブ管理 > 新規登録

2. **再評価リマインダーバッチ**
   - ジョブID: `review_reminder_batch`
   - ジョブ名: 再評価リマインダー通知
   - Javaクラス: `jp.co.company.sqas.batch.ReviewReminderBatchJob`
   - スケジュール: 毎日 02:00 実行
   - タイムゾーン: Asia/Tokyo

3. **是正処置リマインダーバッチ**
   - ジョブID: `corrective_action_reminder_batch`
   - ジョブ名: 是正処置期限リマインダー通知
   - Javaクラス: `jp.co.company.sqas.batch.CorrectiveActionReminderBatchJob`
   - スケジュール: 毎日 03:00 実行
   - タイムゾーン: Asia/Tokyo

#### CLIでの登録（オプション）

```bash
# 再評価リマインダーバッチ
$IM_HOME/bin/im-cli register-job \
    --job-id review_reminder_batch \
    --job-name "再評価リマインダー通知" \
    --class jp.co.company.sqas.batch.ReviewReminderBatchJob \
    --schedule "0 2 * * *"

# 是正処置リマインダーバッチ
$IM_HOME/bin/im-cli register-job \
    --job-id corrective_action_reminder_batch \
    --job-name "是正処置期限リマインダー通知" \
    --class jp.co.company.sqas.batch.CorrectiveActionReminderBatchJob \
    --schedule "0 3 * * *"
```

---

## 設定とカスタマイズ

### 1. データベース接続設定

`$IM_HOME/conf/data-source.xml` を編集

```xml
<data-source>
    <resource-ref-name>jdbc/sqas</resource-ref-name>
    <jndi-name>jdbc/sqas</jndi-name>
    <connection-url>jdbc:postgresql://localhost:5432/sqas_db</connection-url>
    <user-name>sqas_user</user-name>
    <password>your_secure_password</password>
    <driver-class-name>org.postgresql.Driver</driver-class-name>
    <max-pool-size>20</max-pool-size>
    <min-pool-size>5</min-pool-size>
</data-source>
```

### 2. メール送信設定

`$IM_HOME/conf/mail.xml` を編集

```xml
<mail-session>
    <session-name>sqas_mail</session-name>
    <smtp-host>smtp.company.example.com</smtp-host>
    <smtp-port>587</smtp-port>
    <smtp-auth>true</smtp-auth>
    <smtp-user>sqas-system@company.example.com</smtp-user>
    <smtp-password>your_mail_password</smtp-password>
    <from-address>sqas-system@company.example.com</from-address>
    <from-name>購買先品質審査管理システム</from-name>
</mail-session>
```

### 3. ログ設定

`$IM_HOME/conf/log4j2.xml` に以下を追加

```xml
<Logger name="jp.co.company.sqas" level="INFO" additivity="false">
    <AppenderRef ref="SQAS_LOG"/>
</Logger>

<RollingFile name="SQAS_LOG" fileName="${sys:im.home}/logs/sqas.log"
             filePattern="${sys:im.home}/logs/sqas-%d{yyyy-MM-dd}.log">
    <PatternLayout pattern="%d{yyyy-MM-dd HH:mm:ss} [%t] %-5level %logger{36} - %msg%n"/>
    <Policies>
        <TimeBasedTriggeringPolicy interval="1" modulate="true"/>
    </Policies>
    <DefaultRolloverStrategy max="30"/>
</RollingFile>
```

### 4. システムパラメータの設定

intra-mart管理画面 > システム設定 > パラメータ管理

| パラメータ名 | 値 | 説明 |
|-----------|---|-----|
| sqas.review.reminder.days | 30 | 再評価リマインダーの通知日数 |
| sqas.corrective.reminder.days | 3 | 是正期限リマインダーの通知日数 |
| sqas.corrective.deadline.days | 30 | 是正処置の標準期限日数 |
| sqas.document.audit.weight | 0.4 | 書類審査の重み |
| sqas.onsite.audit.weight | 0.6 | 実地審査の重み |

---

## 動作確認

### 1. データベース接続確認

```bash
# intra-mart管理画面 > システム情報 > データソース
# jdbc/sqas が正常に接続できることを確認
```

### 2. 画面表示確認

1. 購買先マスター一覧画面にアクセス
   - URL: `http://your-server:8080/imart/sqas_supplier_list`
   - サンプルデータが表示されることを確認

2. 審査採点フォームにアクセス
   - URL: `http://your-server:8080/imart/sqas_audit_scoring`
   - フォームが正常に表示されることを確認

### 3. スコア計算機能の確認

1. 審査採点フォームで各項目に点数を入力
2. 総合点が自動計算されることを確認
3. 評価と判定が正しく表示されることを確認

### 4. ワークフロー起動確認

1. 新規購買先審査ワークフローを起動
2. 各ノードが正常に遷移することを確認
3. 承認処理が正常に動作することを確認

### 5. バッチジョブ実行確認

```bash
# 手動でバッチジョブを実行
$IM_HOME/bin/im-cli execute-job --job-id review_reminder_batch

# ログを確認
tail -f $IM_HOME/logs/sqas.log
```

### 6. 通知機能の確認

1. テストデータで期限間近の購買先を作成
2. バッチジョブを実行
3. 通知メールが送信されることを確認

---

## トラブルシューティング

### 問題: データベース接続エラー

**症状**
```
java.sql.SQLException: Connection refused
```

**解決方法**
1. データベースサーバーが起動しているか確認
2. 接続情報（ホスト、ポート、ユーザー、パスワード）が正しいか確認
3. ファイアウォール設定を確認
4. データベースの接続数上限を確認

### 問題: Javaクラスが見つからない

**症状**
```
java.lang.ClassNotFoundException: jp.co.company.sqas.service.AuditScoreCalculationService
```

**解決方法**
1. JARファイルが正しくデプロイされているか確認
2. クラスパスが正しく設定されているか確認
3. intra-martを再起動

### 問題: ワークフローが起動しない

**症状**
ワークフロー起動ボタンをクリックしてもエラーになる

**解決方法**
1. ワークフロー定義が正しくインポートされているか確認
2. ロールマッピングが正しく設定されているか確認
3. ユーザーに必要な権限が付与されているか確認
4. ワークフローログを確認

### 問題: バッチジョブが実行されない

**症状**
スケジュール時刻になってもバッチジョブが実行されない

**解決方法**
1. ジョブスケジューラが有効になっているか確認
2. ジョブが正しく登録されているか確認
3. ジョブの実行ログを確認
4. システム時刻とタイムゾーンを確認

### 問題: スコア計算が正しくない

**症状**
総合点の計算結果が期待値と異なる

**解決方法**
1. 計算式を確認: `(評価点合計 × 100) / (4 × 総項目数 - 4 × 未調査項目数)`
2. 未調査項目が正しくカウントされているか確認
3. ブラウザのコンソールでJavaScriptエラーを確認
4. AuditScoreCalculationService.java のロジックを確認

---

## チェックリスト

### デプロイ前チェックリスト

- [ ] intra-mart Accel Platform がインストールされている
- [ ] 必要なモジュール（IM-QuickWebSystem, IM-Workflow, IM-Authz）がインストールされている
- [ ] データベースサーバーが稼働している
- [ ] データベースユーザーが作成されている
- [ ] 環境変数が設定されている
- [ ] バックアップが取得されている

### データベースセットアップチェックリスト

- [ ] テーブルが正常に作成された
- [ ] インデックスが正常に作成された
- [ ] 外部キー制約が正常に作成された
- [ ] データベース接続テストが成功した
- [ ] サンプルデータが投入された（テスト環境のみ）

### アプリケーションデプロイチェックリスト

- [ ] Javaクラスがコンパイルされた
- [ ] JARファイルが作成された
- [ ] JARファイルがデプロイされた
- [ ] 画面定義がインポートされた
- [ ] JavaScriptファイルがデプロイされた
- [ ] ワークフロー定義がインポートされた
- [ ] 権限設定が完了した
- [ ] バッチジョブが登録された

### 設定チェックリスト

- [ ] データベース接続設定が完了した
- [ ] メール送信設定が完了した
- [ ] ログ設定が完了した
- [ ] システムパラメータが設定された
- [ ] ロールマッピングが完了した

### 動作確認チェックリスト

- [ ] データベース接続が確認できた
- [ ] 購買先マスター一覧画面が表示できた
- [ ] 審査採点フォームが表示できた
- [ ] スコア計算が正常に動作した
- [ ] ワークフローが正常に起動した
- [ ] バッチジョブが正常に実行された
- [ ] 通知メールが送信された

### 本番環境移行チェックリスト

- [ ] 本番環境のバックアップを取得した
- [ ] テスト環境で十分な動作確認を実施した
- [ ] 本番データの移行計画を作成した
- [ ] ロールバック手順を準備した
- [ ] 関係者に移行スケジュールを通知した
- [ ] 移行作業の承認を取得した

---

## サポート情報

### ログファイルの場所

- **アプリケーションログ**: `$IM_HOME/logs/sqas.log`
- **intra-martシステムログ**: `$IM_HOME/logs/platform.log`
- **ワークフローログ**: `$IM_HOME/logs/workflow.log`
- **バッチジョブログ**: `$IM_HOME/logs/job-scheduler.log`

### お問い合わせ先

- **開発チーム**: sqas-dev@company.example.com
- **品質保証部**: qa-dept@company.example.com
- **システム管理者**: system-admin@company.example.com

---

**購買先品質審査管理システム開発チーム**  
最終更新日: 2025年11月20日
