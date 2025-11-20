# データベーススキーマ設計書
# Database Schema Design Document

## 概要 (Overview)

購買先品質審査管理システム (GCP0602準拠) のデータベーススキーマ設計。
intra-mart Accel Platform環境での運用を前提とする。

## ER図 (Entity Relationship Diagram)

```
┌─────────────────────────┐
│   SupplierMaster        │
│   (購買先マスター)        │
├─────────────────────────┤
│ SupplierID (PK)         │
│ CompanyName             │
│ CompanyNameKana         │
│ PostalCode              │
│ Address                 │
│ PhoneNumber             │
│ FaxNumber               │
│ RepresentativeName      │
│ ContactPersonName       │
│ ContactEmail            │
│ ISO9001Certified        │
│ ISO9001CertNumber       │
│ ISO9001ExpiryDate       │
│ InitialRegistrationDate │
│ CurrentRating           │
│ NextReviewDate          │
│ SupplierStatus          │
│ CreatedAt               │
│ CreatedBy               │
│ UpdatedAt               │
│ UpdatedBy               │
└─────────────────────────┘
           │
           │ 1:N
           ▼
┌─────────────────────────┐
│   AuditHistory          │
│   (審査/監査履歴)         │
├─────────────────────────┤
│ AuditID (PK)            │
│ SupplierID (FK)         │
│ AuditType               │
│ AuditDate               │
│ AuditorName             │
│ AuditorDepartment       │
│ TotalScore              │
│ Rating                  │
│ FinalDecision           │
│ AuditReportFile         │
│ DocumentAuditScore      │
│ OnSiteAuditScore        │
│ Comments                │
│ ApprovedBy              │
│ ApprovedDate            │
│ CreatedAt               │
│ CreatedBy               │
│ UpdatedAt               │
│ UpdatedBy               │
└─────────────────────────┘
           │
           │ 1:N
           ▼
┌─────────────────────────┐
│ CorrectiveActionHistory │
│ (是正処置履歴)            │
├─────────────────────────┤
│ CorrectiveActionID (PK) │
│ AuditID (FK)            │
│ NonConformityDetails    │
│ ActionRequiredDetails   │
│ CorrectiveActionPlan    │
│ CorrectiveActionDeadline│
│ ActualCompletionDate    │
│ IsApproved              │
│ ApprovedBy              │
│ ApprovedDate            │
│ VerificationResult      │
│ VerificationDate        │
│ Status                  │
│ CreatedAt               │
│ CreatedBy               │
│ UpdatedAt               │
│ UpdatedBy               │
└─────────────────────────┘
```

## テーブル定義 (Table Definitions)

### 1. SupplierMaster (購買先マスター)

購買先の基本情報と現行ステータスを管理する。

| カラム名 | データ型 | NULL | 説明 | 備考 |
|---------|---------|------|------|------|
| SupplierID | VARCHAR(20) | NOT NULL | 購買先ID | PK, 形式: SUP-YYYYMMDD-XXX |
| CompanyName | VARCHAR(200) | NOT NULL | 会社名 | |
| CompanyNameKana | VARCHAR(200) | NULL | 会社名カナ | |
| PostalCode | VARCHAR(10) | NULL | 郵便番号 | |
| Address | VARCHAR(500) | NULL | 住所 | |
| PhoneNumber | VARCHAR(20) | NULL | 電話番号 | |
| FaxNumber | VARCHAR(20) | NULL | FAX番号 | |
| RepresentativeName | VARCHAR(100) | NULL | 代表者名 | |
| ContactPersonName | VARCHAR(100) | NULL | 担当者名 | |
| ContactEmail | VARCHAR(200) | NULL | 担当者メールアドレス | |
| ISO9001Certified | CHAR(1) | NOT NULL | ISO9001認証有無 | '1':有, '0':無 |
| ISO9001CertNumber | VARCHAR(50) | NULL | ISO9001認証番号 | |
| ISO9001ExpiryDate | DATE | NULL | ISO9001有効期限 | |
| InitialRegistrationDate | DATE | NOT NULL | 初回登録日 | 再評価基準日 |
| CurrentRating | INTEGER | NULL | 現行等級 | 1:優良, 2:良好, 3:標準 |
| NextReviewDate | DATE | NULL | 次回審査予定日 | 通知トリガー |
| SupplierStatus | VARCHAR(20) | NOT NULL | 購買先ステータス | ACTIVE, SUSPENDED, INACTIVE |
| CreatedAt | TIMESTAMP | NOT NULL | 作成日時 | |
| CreatedBy | VARCHAR(50) | NOT NULL | 作成者 | |
| UpdatedAt | TIMESTAMP | NOT NULL | 更新日時 | |
| UpdatedBy | VARCHAR(50) | NOT NULL | 更新者 | |

**制約:**
- PRIMARY KEY: SupplierID
- UNIQUE: CompanyName
- CHECK: CurrentRating IN (1, 2, 3)
- CHECK: SupplierStatus IN ('ACTIVE', 'SUSPENDED', 'INACTIVE')
- CHECK: ISO9001Certified IN ('0', '1')

**インデックス:**
- IDX_SupplierMaster_NextReviewDate ON NextReviewDate
- IDX_SupplierMaster_CurrentRating ON CurrentRating
- IDX_SupplierMaster_Status ON SupplierStatus

---

### 2. AuditHistory (審査/監査履歴)

個別の審査実施記録と結果を管理する。

| カラム名 | データ型 | NULL | 説明 | 備考 |
|---------|---------|------|------|------|
| AuditID | VARCHAR(20) | NOT NULL | 審査ID | PK, 形式: AUD-YYYYMMDD-XXX |
| SupplierID | VARCHAR(20) | NOT NULL | 購買先ID | FK → SupplierMaster |
| AuditType | VARCHAR(20) | NOT NULL | 審査種別 | NEW, PERIODIC, SPECIAL |
| AuditDate | DATE | NOT NULL | 審査実施日 | |
| AuditorName | VARCHAR(100) | NOT NULL | 審査員名 | |
| AuditorDepartment | VARCHAR(100) | NOT NULL | 審査員所属部門 | |
| TotalScore | DECIMAL(5,2) | NULL | 総合点 | 0-100点 |
| Rating | VARCHAR(10) | NULL | 評価 | 優, 良, 可, 不可 |
| FinalDecision | VARCHAR(20) | NULL | 最終判定 | PASS, FAIL, CONDITIONAL |
| AuditReportFile | VARCHAR(500) | NULL | 審査報告書ファイルパス | 様式-8, 様式-11 |
| DocumentAuditScore | DECIMAL(5,2) | NULL | 書類審査点数 | 様式-2 |
| OnSiteAuditScore | DECIMAL(5,2) | NULL | 実地審査点数 | 様式-5 |
| Comments | TEXT | NULL | コメント・所見 | |
| ApprovedBy | VARCHAR(50) | NULL | 承認者 | 品質保証部長 |
| ApprovedDate | TIMESTAMP | NULL | 承認日時 | |
| CreatedAt | TIMESTAMP | NOT NULL | 作成日時 | |
| CreatedBy | VARCHAR(50) | NOT NULL | 作成者 | |
| UpdatedAt | TIMESTAMP | NOT NULL | 更新日時 | |
| UpdatedBy | VARCHAR(50) | NOT NULL | 更新者 | |

**制約:**
- PRIMARY KEY: AuditID
- FOREIGN KEY: SupplierID REFERENCES SupplierMaster(SupplierID)
- CHECK: AuditType IN ('NEW', 'PERIODIC', 'SPECIAL')
- CHECK: Rating IN ('優', '良', '可', '不可')
- CHECK: FinalDecision IN ('PASS', 'FAIL', 'CONDITIONAL')
- CHECK: TotalScore BETWEEN 0 AND 100
- CHECK: DocumentAuditScore BETWEEN 0 AND 100
- CHECK: OnSiteAuditScore BETWEEN 0 AND 100

**インデックス:**
- IDX_AuditHistory_SupplierID ON SupplierID
- IDX_AuditHistory_AuditDate ON AuditDate
- IDX_AuditHistory_AuditType ON AuditType
- IDX_AuditHistory_FinalDecision ON FinalDecision

---

### 3. CorrectiveActionHistory (是正処置履歴)

不適合発生時の是正計画と実施状況を管理する。

| カラム名 | データ型 | NULL | 説明 | 備考 |
|---------|---------|------|------|------|
| CorrectiveActionID | VARCHAR(20) | NOT NULL | 是正処置ID | PK, 形式: CA-YYYYMMDD-XXX |
| AuditID | VARCHAR(20) | NOT NULL | 審査ID | FK → AuditHistory |
| NonConformityDetails | TEXT | NOT NULL | 不適合内容 | |
| ActionRequiredDetails | TEXT | NOT NULL | 是正要求事項 | |
| CorrectiveActionPlan | TEXT | NULL | 是正処置計画 | 購買先提出 |
| CorrectiveActionDeadline | DATE | NOT NULL | 是正期限 | 原則30日以内 |
| ActualCompletionDate | DATE | NULL | 実際完了日 | |
| IsApproved | CHAR(1) | NOT NULL | 承認フラグ | '1':承認, '0':未承認 |
| ApprovedBy | VARCHAR(50) | NULL | 承認者 | 品質保証部長 |
| ApprovedDate | TIMESTAMP | NULL | 承認日時 | |
| VerificationResult | TEXT | NULL | 検証結果 | |
| VerificationDate | DATE | NULL | 検証実施日 | |
| Status | VARCHAR(20) | NOT NULL | ステータス | OPEN, IN_PROGRESS, COMPLETED, OVERDUE |
| CreatedAt | TIMESTAMP | NOT NULL | 作成日時 | |
| CreatedBy | VARCHAR(50) | NOT NULL | 作成者 | |
| UpdatedAt | TIMESTAMP | NOT NULL | 更新日時 | |
| UpdatedBy | VARCHAR(50) | NOT NULL | 更新者 | |

**制約:**
- PRIMARY KEY: CorrectiveActionID
- FOREIGN KEY: AuditID REFERENCES AuditHistory(AuditID)
- CHECK: IsApproved IN ('0', '1')
- CHECK: Status IN ('OPEN', 'IN_PROGRESS', 'COMPLETED', 'OVERDUE')

**インデックス:**
- IDX_CorrectiveAction_AuditID ON AuditID
- IDX_CorrectiveAction_Deadline ON CorrectiveActionDeadline
- IDX_CorrectiveAction_Status ON Status

---

## データ整合性ルール (Data Integrity Rules)

1. **カスケード削除**: 購買先マスター削除時は、関連する審査履歴・是正処置履歴も論理削除する（物理削除は行わない）
2. **再評価日自動計算**: InitialRegistrationDate + 2年 = NextReviewDate（トリガーまたはバッチ処理で更新）
3. **等級更新**: 審査承認時、SupplierMaster.CurrentRatingを自動更新
4. **是正期限超過**: CorrectiveActionDeadline < CURRENT_DATE かつ Status != 'COMPLETED' の場合、Statusを'OVERDUE'に自動更新

---

## セキュリティ考慮事項 (Security Considerations)

1. **アクセス制御**: intra-mart IM-Authzによるロールベースアクセス制御
2. **監査ログ**: 全テーブルにCreatedBy, UpdatedBy, CreatedAt, UpdatedAtを記録
3. **データ暗号化**: 個人情報（担当者名、メールアドレス等）は必要に応じて暗号化
4. **バックアップ**: 日次バックアップ必須（ISO9001監査証跡として保管）

---

## パフォーマンス最適化 (Performance Optimization)

1. **インデックス戦略**: 検索頻度の高いカラム（NextReviewDate, CurrentRating, Status等）にインデックス作成
2. **パーティショニング**: AuditHistory, CorrectiveActionHistoryは年度別パーティショニング検討
3. **アーカイブ**: 5年以上経過したデータは別テーブルへアーカイブ

---

## 今後の拡張性 (Future Extensibility)

1. **評価項目詳細テーブル**: 様式-2, 様式-5の個別設問ごとの評価を記録するテーブル追加検討
2. **添付ファイル管理テーブル**: ISO認証書、契約書等の文書管理を別テーブル化
3. **通知履歴テーブル**: 送信した通知の履歴管理

---

作成日: 2025-11-20
作成者: Supplier Quality Audit System Development Team
