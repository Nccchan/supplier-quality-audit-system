
CREATE TABLE SupplierMaster (
    SupplierID VARCHAR(20) NOT NULL,
    CompanyName VARCHAR(200) NOT NULL,
    CompanyNameKana VARCHAR(200),
    PostalCode VARCHAR(10),
    Address VARCHAR(500),
    PhoneNumber VARCHAR(20),
    FaxNumber VARCHAR(20),
    RepresentativeName VARCHAR(100),
    ContactPersonName VARCHAR(100),
    ContactEmail VARCHAR(200),
    ISO9001Certified CHAR(1) NOT NULL DEFAULT '0',
    ISO9001CertNumber VARCHAR(50),
    ISO9001ExpiryDate DATE,
    InitialRegistrationDate DATE NOT NULL,
    CurrentRating INTEGER,
    NextReviewDate DATE,
    SupplierStatus VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    CreatedAt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CreatedBy VARCHAR(50) NOT NULL,
    UpdatedAt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UpdatedBy VARCHAR(50) NOT NULL,
    
    CONSTRAINT PK_SupplierMaster PRIMARY KEY (SupplierID),
    
    CONSTRAINT UQ_SupplierMaster_CompanyName UNIQUE (CompanyName),
    
    CONSTRAINT CHK_SupplierMaster_Rating CHECK (CurrentRating IN (1, 2, 3)),
    CONSTRAINT CHK_SupplierMaster_Status CHECK (SupplierStatus IN ('ACTIVE', 'SUSPENDED', 'INACTIVE')),
    CONSTRAINT CHK_SupplierMaster_ISO9001 CHECK (ISO9001Certified IN ('0', '1'))
);

CREATE INDEX IDX_SupplierMaster_NextReviewDate ON SupplierMaster(NextReviewDate);
CREATE INDEX IDX_SupplierMaster_CurrentRating ON SupplierMaster(CurrentRating);
CREATE INDEX IDX_SupplierMaster_Status ON SupplierMaster(SupplierStatus);
CREATE INDEX IDX_SupplierMaster_ISO9001 ON SupplierMaster(ISO9001Certified);

COMMENT ON TABLE SupplierMaster IS '購買先マスター - 購買先の基本情報と現行ステータスを管理';
COMMENT ON COLUMN SupplierMaster.SupplierID IS '購買先ID (形式: SUP-YYYYMMDD-XXX)';
COMMENT ON COLUMN SupplierMaster.CompanyName IS '会社名';
COMMENT ON COLUMN SupplierMaster.ISO9001Certified IS 'ISO9001認証有無 (1:有, 0:無)';
COMMENT ON COLUMN SupplierMaster.InitialRegistrationDate IS '初回登録日 - 再評価基準日として使用';
COMMENT ON COLUMN SupplierMaster.CurrentRating IS '現行等級 (1:優良, 2:良好, 3:標準)';
COMMENT ON COLUMN SupplierMaster.NextReviewDate IS '次回審査予定日 - 通知トリガーとして使用';
COMMENT ON COLUMN SupplierMaster.SupplierStatus IS '購買先ステータス (ACTIVE:有効, SUSPENDED:停止, INACTIVE:無効)';

CREATE TABLE AuditHistory (
    AuditID VARCHAR(20) NOT NULL,
    SupplierID VARCHAR(20) NOT NULL,
    AuditType VARCHAR(20) NOT NULL,
    AuditDate DATE NOT NULL,
    AuditorName VARCHAR(100) NOT NULL,
    AuditorDepartment VARCHAR(100) NOT NULL,
    TotalScore DECIMAL(5,2),
    Rating VARCHAR(10),
    FinalDecision VARCHAR(20),
    AuditReportFile VARCHAR(500),
    DocumentAuditScore DECIMAL(5,2),
    OnSiteAuditScore DECIMAL(5,2),
    Comments TEXT,
    ApprovedBy VARCHAR(50),
    ApprovedDate TIMESTAMP,
    CreatedAt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CreatedBy VARCHAR(50) NOT NULL,
    UpdatedAt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UpdatedBy VARCHAR(50) NOT NULL,
    
    CONSTRAINT PK_AuditHistory PRIMARY KEY (AuditID),
    
    CONSTRAINT FK_AuditHistory_Supplier FOREIGN KEY (SupplierID) 
        REFERENCES SupplierMaster(SupplierID) ON DELETE CASCADE,
    
    CONSTRAINT CHK_AuditHistory_Type CHECK (AuditType IN ('NEW', 'PERIODIC', 'SPECIAL')),
    CONSTRAINT CHK_AuditHistory_Rating CHECK (Rating IN ('優', '良', '可', '不可')),
    CONSTRAINT CHK_AuditHistory_Decision CHECK (FinalDecision IN ('PASS', 'FAIL', 'CONDITIONAL')),
    CONSTRAINT CHK_AuditHistory_TotalScore CHECK (TotalScore BETWEEN 0 AND 100),
    CONSTRAINT CHK_AuditHistory_DocScore CHECK (DocumentAuditScore BETWEEN 0 AND 100),
    CONSTRAINT CHK_AuditHistory_OnSiteScore CHECK (OnSiteAuditScore BETWEEN 0 AND 100)
);

CREATE INDEX IDX_AuditHistory_SupplierID ON AuditHistory(SupplierID);
CREATE INDEX IDX_AuditHistory_AuditDate ON AuditHistory(AuditDate);
CREATE INDEX IDX_AuditHistory_AuditType ON AuditHistory(AuditType);
CREATE INDEX IDX_AuditHistory_FinalDecision ON AuditHistory(FinalDecision);
CREATE INDEX IDX_AuditHistory_ApprovedDate ON AuditHistory(ApprovedDate);

COMMENT ON TABLE AuditHistory IS '審査/監査履歴 - 個別の審査実施記録と結果を管理';
COMMENT ON COLUMN AuditHistory.AuditID IS '審査ID (形式: AUD-YYYYMMDD-XXX)';
COMMENT ON COLUMN AuditHistory.AuditType IS '審査種別 (NEW:新規, PERIODIC:定期再評価, SPECIAL:特別審査)';
COMMENT ON COLUMN AuditHistory.TotalScore IS '総合点 (0-100点)';
COMMENT ON COLUMN AuditHistory.Rating IS '評価 (優:80点以上, 良:70-79点, 可:60-69点, 不可:60点未満)';
COMMENT ON COLUMN AuditHistory.FinalDecision IS '最終判定 (PASS:合格, FAIL:不合格, CONDITIONAL:是正指示)';
COMMENT ON COLUMN AuditHistory.AuditReportFile IS '審査報告書ファイルパス (様式-8, 様式-11)';
COMMENT ON COLUMN AuditHistory.DocumentAuditScore IS '書類審査点数 (様式-2)';
COMMENT ON COLUMN AuditHistory.OnSiteAuditScore IS '実地審査点数 (様式-5)';

CREATE TABLE CorrectiveActionHistory (
    CorrectiveActionID VARCHAR(20) NOT NULL,
    AuditID VARCHAR(20) NOT NULL,
    NonConformityDetails TEXT NOT NULL,
    ActionRequiredDetails TEXT NOT NULL,
    CorrectiveActionPlan TEXT,
    CorrectiveActionDeadline DATE NOT NULL,
    ActualCompletionDate DATE,
    IsApproved CHAR(1) NOT NULL DEFAULT '0',
    ApprovedBy VARCHAR(50),
    ApprovedDate TIMESTAMP,
    VerificationResult TEXT,
    VerificationDate DATE,
    Status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    CreatedAt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CreatedBy VARCHAR(50) NOT NULL,
    UpdatedAt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UpdatedBy VARCHAR(50) NOT NULL,
    
    CONSTRAINT PK_CorrectiveAction PRIMARY KEY (CorrectiveActionID),
    
    CONSTRAINT FK_CorrectiveAction_Audit FOREIGN KEY (AuditID) 
        REFERENCES AuditHistory(AuditID) ON DELETE CASCADE,
    
    CONSTRAINT CHK_CorrectiveAction_Approved CHECK (IsApproved IN ('0', '1')),
    CONSTRAINT CHK_CorrectiveAction_Status CHECK (Status IN ('OPEN', 'IN_PROGRESS', 'COMPLETED', 'OVERDUE'))
);

CREATE INDEX IDX_CorrectiveAction_AuditID ON CorrectiveActionHistory(AuditID);
CREATE INDEX IDX_CorrectiveAction_Deadline ON CorrectiveActionHistory(CorrectiveActionDeadline);
CREATE INDEX IDX_CorrectiveAction_Status ON CorrectiveActionHistory(Status);
CREATE INDEX IDX_CorrectiveAction_IsApproved ON CorrectiveActionHistory(IsApproved);

COMMENT ON TABLE CorrectiveActionHistory IS '是正処置履歴 - 不適合発生時の是正計画と実施状況を管理';
COMMENT ON COLUMN CorrectiveActionHistory.CorrectiveActionID IS '是正処置ID (形式: CA-YYYYMMDD-XXX)';
COMMENT ON COLUMN CorrectiveActionHistory.NonConformityDetails IS '不適合内容';
COMMENT ON COLUMN CorrectiveActionHistory.ActionRequiredDetails IS '是正要求事項';
COMMENT ON COLUMN CorrectiveActionHistory.CorrectiveActionDeadline IS '是正期限 (原則30日以内)';
COMMENT ON COLUMN CorrectiveActionHistory.IsApproved IS '承認フラグ (1:承認, 0:未承認)';
COMMENT ON COLUMN CorrectiveActionHistory.Status IS 'ステータス (OPEN:未着手, IN_PROGRESS:対応中, COMPLETED:完了, OVERDUE:期限超過)';

CREATE TABLE AuditScoreDetails (
    ScoreDetailID VARCHAR(20) NOT NULL,
    AuditID VARCHAR(20) NOT NULL,
    FormType VARCHAR(10) NOT NULL,
    QuestionNumber VARCHAR(10) NOT NULL,
    QuestionText TEXT NOT NULL,
    Score INTEGER NOT NULL,
    IsNotApplicable CHAR(1) NOT NULL DEFAULT '0',
    Remarks TEXT,
    CreatedAt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CreatedBy VARCHAR(50) NOT NULL,
    
    CONSTRAINT PK_AuditScoreDetails PRIMARY KEY (ScoreDetailID),
    
    CONSTRAINT FK_AuditScoreDetails_Audit FOREIGN KEY (AuditID) 
        REFERENCES AuditHistory(AuditID) ON DELETE CASCADE,
    
    CONSTRAINT CHK_AuditScoreDetails_FormType CHECK (FormType IN ('FORM-2', 'FORM-5')),
    CONSTRAINT CHK_AuditScoreDetails_Score CHECK (Score IN (0, 2, 4)),
    CONSTRAINT CHK_AuditScoreDetails_NA CHECK (IsNotApplicable IN ('0', '1'))
);

CREATE INDEX IDX_AuditScoreDetails_AuditID ON AuditScoreDetails(AuditID);
CREATE INDEX IDX_AuditScoreDetails_FormType ON AuditScoreDetails(FormType);

COMMENT ON TABLE AuditScoreDetails IS '審査採点詳細 - 様式-2, 様式-5の個別設問ごとの評価を記録';
COMMENT ON COLUMN AuditScoreDetails.FormType IS '様式種別 (FORM-2:書類審査, FORM-5:実地審査)';
COMMENT ON COLUMN AuditScoreDetails.Score IS '評価点 (4:適合, 2:一部不適合, 0:不適合)';
COMMENT ON COLUMN AuditScoreDetails.IsNotApplicable IS '未調査フラグ (1:未調査, 0:調査済)';

CREATE TABLE NotificationHistory (
    NotificationID VARCHAR(20) NOT NULL,
    NotificationType VARCHAR(30) NOT NULL,
    TargetID VARCHAR(20),
    RecipientUserID VARCHAR(50) NOT NULL,
    RecipientEmail VARCHAR(200),
    Subject VARCHAR(200) NOT NULL,
    MessageBody TEXT NOT NULL,
    SentDate TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    Status VARCHAR(20) NOT NULL DEFAULT 'SENT',
    
    CONSTRAINT PK_NotificationHistory PRIMARY KEY (NotificationID),
    
    CONSTRAINT CHK_NotificationHistory_Type CHECK (NotificationType IN 
        ('REVIEW_REMINDER', 'CORRECTIVE_ACTION_REMINDER', 'CORRECTIVE_ACTION_OVERDUE', 'AUDIT_APPROVAL_REQUEST')),
    CONSTRAINT CHK_NotificationHistory_Status CHECK (Status IN ('SENT', 'FAILED', 'PENDING'))
);

CREATE INDEX IDX_NotificationHistory_Type ON NotificationHistory(NotificationType);
CREATE INDEX IDX_NotificationHistory_SentDate ON NotificationHistory(SentDate);
CREATE INDEX IDX_NotificationHistory_Recipient ON NotificationHistory(RecipientUserID);

COMMENT ON TABLE NotificationHistory IS '通知履歴 - 送信した通知の履歴を管理';
COMMENT ON COLUMN NotificationHistory.NotificationType IS '通知種別 (REVIEW_REMINDER:再評価通知, CORRECTIVE_ACTION_REMINDER:是正期限通知, etc.)';
