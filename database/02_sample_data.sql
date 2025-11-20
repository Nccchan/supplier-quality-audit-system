

INSERT INTO SupplierMaster (
    SupplierID, CompanyName, CompanyNameKana, PostalCode, Address,
    PhoneNumber, FaxNumber, RepresentativeName, ContactPersonName, ContactEmail,
    ISO9001Certified, ISO9001CertNumber, ISO9001ExpiryDate,
    InitialRegistrationDate, CurrentRating, NextReviewDate, SupplierStatus,
    CreatedBy, UpdatedBy
) VALUES (
    'SUP-20231115-001',
    '株式会社優良部品製作所',
    'カブシキガイシャユウリョウブヒンセイサクショ',
    '100-0001',
    '東京都千代田区千代田1-1-1',
    '03-1234-5678',
    '03-1234-5679',
    '山田太郎',
    '佐藤花子',
    'buhin-contact@example.com',
    '1',
    'ISO9001-2023-001',
    '2026-11-15',
    '2023-11-15',
    1,
    '2025-11-15',
    'ACTIVE',
    'system',
    'system'
);

INSERT INTO SupplierMaster (
    SupplierID, CompanyName, CompanyNameKana, PostalCode, Address,
    PhoneNumber, FaxNumber, RepresentativeName, ContactPersonName, ContactEmail,
    ISO9001Certified, ISO9001CertNumber, ISO9001ExpiryDate,
    InitialRegistrationDate, CurrentRating, NextReviewDate, SupplierStatus,
    CreatedBy, UpdatedBy
) VALUES (
    'SUP-20240301-002',
    '中部機械工業株式会社',
    'チュウブキカイコウギョウカブシキガイシャ',
    '460-0001',
    '愛知県名古屋市中区三の丸1-1-1',
    '052-1234-5678',
    '052-1234-5679',
    '鈴木一郎',
    '田中次郎',
    'tanaka@chubu-kikai.example.com',
    '0',
    NULL,
    NULL,
    '2024-03-01',
    2,
    '2026-03-01',
    'ACTIVE',
    'system',
    'system'
);

INSERT INTO SupplierMaster (
    SupplierID, CompanyName, CompanyNameKana, PostalCode, Address,
    PhoneNumber, FaxNumber, RepresentativeName, ContactPersonName, ContactEmail,
    ISO9001Certified, ISO9001CertNumber, ISO9001ExpiryDate,
    InitialRegistrationDate, CurrentRating, NextReviewDate, SupplierStatus,
    CreatedBy, UpdatedBy
) VALUES (
    'SUP-20231201-003',
    '関西精密株式会社',
    'カンサイセイミツカブシキガイシャ',
    '530-0001',
    '大阪府大阪市北区梅田1-1-1',
    '06-1234-5678',
    '06-1234-5679',
    '高橋三郎',
    '伊藤美咲',
    'ito@kansai-seimitsu.example.com',
    '1',
    'ISO9001-2023-003',
    '2026-12-01',
    '2023-12-01',
    3,
    '2025-12-01',
    'ACTIVE',
    'system',
    'system'
);

INSERT INTO SupplierMaster (
    SupplierID, CompanyName, CompanyNameKana, PostalCode, Address,
    PhoneNumber, FaxNumber, RepresentativeName, ContactPersonName, ContactEmail,
    ISO9001Certified, ISO9001CertNumber, ISO9001ExpiryDate,
    InitialRegistrationDate, CurrentRating, NextReviewDate, SupplierStatus,
    CreatedBy, UpdatedBy
) VALUES (
    'SUP-20251110-004',
    '九州電子部品株式会社',
    'キュウシュウデンシブヒンカブシキガイシャ',
    '810-0001',
    '福岡県福岡市中央区天神1-1-1',
    '092-1234-5678',
    '092-1234-5679',
    '渡辺五郎',
    '小林六子',
    'kobayashi@kyushu-denshi.example.com',
    '1',
    'ISO9001-2025-004',
    '2028-11-10',
    '2025-11-10',
    NULL,
    NULL,
    'ACTIVE',
    'system',
    'system'
);

INSERT INTO SupplierMaster (
    SupplierID, CompanyName, CompanyNameKana, PostalCode, Address,
    PhoneNumber, FaxNumber, RepresentativeName, ContactPersonName, ContactEmail,
    ISO9001Certified, ISO9001CertNumber, ISO9001ExpiryDate,
    InitialRegistrationDate, CurrentRating, NextReviewDate, SupplierStatus,
    CreatedBy, UpdatedBy
) VALUES (
    'SUP-20240615-005',
    '東北製造株式会社',
    'トウホクセイゾウカブシキガイシャ',
    '980-0001',
    '宮城県仙台市青葉区一番町1-1-1',
    '022-1234-5678',
    '022-1234-5679',
    '木村七郎',
    '林八子',
    'hayashi@tohoku-seizo.example.com',
    '0',
    NULL,
    NULL,
    '2024-06-15',
    3,
    '2026-06-15',
    'SUSPENDED',
    'system',
    'system'
);


INSERT INTO AuditHistory (
    AuditID, SupplierID, AuditType, AuditDate,
    AuditorName, AuditorDepartment,
    TotalScore, Rating, FinalDecision,
    AuditReportFile, DocumentAuditScore, OnSiteAuditScore,
    Comments, ApprovedBy, ApprovedDate,
    CreatedBy, UpdatedBy
) VALUES (
    'AUD-20231115-001',
    'SUP-20231115-001',
    'NEW',
    '2023-11-15',
    '品質太郎',
    '品質保証部',
    85.50,
    '優',
    'PASS',
    '/reports/audit/AUD-20231115-001_report.pdf',
    87.00,
    84.00,
    'ISO9001認証取得済み。品質管理体制が優れている。',
    'qa_manager',
    '2023-11-20 10:30:00',
    'qa_auditor',
    'qa_auditor'
);

INSERT INTO AuditHistory (
    AuditID, SupplierID, AuditType, AuditDate,
    AuditorName, AuditorDepartment,
    TotalScore, Rating, FinalDecision,
    AuditReportFile, DocumentAuditScore, OnSiteAuditScore,
    Comments, ApprovedBy, ApprovedDate,
    CreatedBy, UpdatedBy
) VALUES (
    'AUD-20240301-002',
    'SUP-20240301-002',
    'NEW',
    '2024-03-01',
    '品質次郎',
    '品質保証部',
    72.00,
    '良',
    'CONDITIONAL',
    '/reports/audit/AUD-20240301-002_report.pdf',
    75.00,
    69.00,
    '品質記録の一部に不備あり。是正後に承認。',
    'qa_manager',
    '2024-03-15 14:00:00',
    'qa_auditor',
    'qa_auditor'
);

INSERT INTO AuditHistory (
    AuditID, SupplierID, AuditType, AuditDate,
    AuditorName, AuditorDepartment,
    TotalScore, Rating, FinalDecision,
    AuditReportFile, DocumentAuditScore, OnSiteAuditScore,
    Comments, ApprovedBy, ApprovedDate,
    CreatedBy, UpdatedBy
) VALUES (
    'AUD-20251101-003',
    'SUP-20231201-003',
    'PERIODIC',
    '2025-11-01',
    '品質三郎',
    '品質保証部',
    65.00,
    '可',
    'PASS',
    '/reports/audit/AUD-20251101-003_report.pdf',
    68.00,
    62.00,
    '定期再評価実施。前回より若干スコア低下。継続監視が必要。',
    'qa_manager',
    '2025-11-10 16:00:00',
    'qa_auditor',
    'qa_auditor'
);

INSERT INTO AuditHistory (
    AuditID, SupplierID, AuditType, AuditDate,
    AuditorName, AuditorDepartment,
    TotalScore, Rating, FinalDecision,
    AuditReportFile, DocumentAuditScore, OnSiteAuditScore,
    Comments, ApprovedBy, ApprovedDate,
    CreatedBy, UpdatedBy
) VALUES (
    'AUD-20251110-004',
    'SUP-20251110-004',
    'NEW',
    '2025-11-10',
    '品質四郎',
    '品質保証部',
    NULL,
    NULL,
    NULL,
    NULL,
    NULL,
    NULL,
    '書類審査実施中。実地審査は来週予定。',
    NULL,
    NULL,
    'qa_auditor',
    'qa_auditor'
);

INSERT INTO AuditHistory (
    AuditID, SupplierID, AuditType, AuditDate,
    AuditorName, AuditorDepartment,
    TotalScore, Rating, FinalDecision,
    AuditReportFile, DocumentAuditScore, OnSiteAuditScore,
    Comments, ApprovedBy, ApprovedDate,
    CreatedBy, UpdatedBy
) VALUES (
    'AUD-20251015-005',
    'SUP-20240615-005',
    'SPECIAL',
    '2025-10-15',
    '品質五郎',
    '品質保証部',
    45.00,
    '不可',
    'FAIL',
    '/reports/audit/AUD-20251015-005_report.pdf',
    50.00,
    40.00,
    '重大な品質問題が発覚。取引停止措置。是正完了後に再審査。',
    'qa_manager',
    '2025-10-20 09:00:00',
    'qa_auditor',
    'qa_auditor'
);


INSERT INTO CorrectiveActionHistory (
    CorrectiveActionID, AuditID,
    NonConformityDetails, ActionRequiredDetails,
    CorrectiveActionPlan, CorrectiveActionDeadline, ActualCompletionDate,
    IsApproved, ApprovedBy, ApprovedDate,
    VerificationResult, VerificationDate, Status,
    CreatedBy, UpdatedBy
) VALUES (
    'CA-20240301-001',
    'AUD-20240301-002',
    '品質記録の保管方法が不適切。一部記録が欠落している。',
    '品質記録管理規定の見直しと、記録保管システムの整備を実施すること。',
    '1. 品質記録管理規定を改訂（3/10完了予定）\n2. 記録保管用データベースシステム導入（3/25完了予定）\n3. 担当者への教育実施（3/30完了予定）',
    '2024-03-31',
    '2024-03-28',
    '1',
    'qa_manager',
    '2024-03-29 10:00:00',
    '是正処置計画通りに実施完了。記録管理システムが適切に運用されていることを確認。',
    '2024-03-29',
    'COMPLETED',
    'qa_auditor',
    'qa_auditor'
);

INSERT INTO CorrectiveActionHistory (
    CorrectiveActionID, AuditID,
    NonConformityDetails, ActionRequiredDetails,
    CorrectiveActionPlan, CorrectiveActionDeadline, ActualCompletionDate,
    IsApproved, ApprovedBy, ApprovedDate,
    VerificationResult, VerificationDate, Status,
    CreatedBy, UpdatedBy
) VALUES (
    'CA-20251015-002',
    'AUD-20251015-005',
    '製造工程における検査工程の欠落。不良品が出荷される可能性がある。',
    '検査工程の追加と、検査手順書の整備、検査員の教育を実施すること。',
    '1. 検査工程フローの見直し（11/1完了予定）\n2. 検査手順書作成（11/10完了予定）\n3. 検査員教育実施（11/20完了予定）\n4. 検査設備導入（11/30完了予定）',
    '2025-11-14',
    NULL,
    '1',
    'qa_manager',
    '2025-10-25 15:00:00',
    NULL,
    NULL,
    'IN_PROGRESS',
    'qa_auditor',
    'qa_auditor'
);

INSERT INTO CorrectiveActionHistory (
    CorrectiveActionID, AuditID,
    NonConformityDetails, ActionRequiredDetails,
    CorrectiveActionPlan, CorrectiveActionDeadline, ActualCompletionDate,
    IsApproved, ApprovedBy, ApprovedDate,
    VerificationResult, VerificationDate, Status,
    CreatedBy, UpdatedBy
) VALUES (
    'CA-20251101-003',
    'AUD-20251101-003',
    '校正記録の一部に記入漏れがある。',
    '校正記録フォーマットの見直しと、記入チェックリストの作成を実施すること。',
    NULL,
    '2025-12-01',
    NULL,
    '0',
    NULL,
    NULL,
    NULL,
    NULL,
    'OVERDUE',
    'qa_auditor',
    'qa_auditor'
);


INSERT INTO AuditScoreDetails (ScoreDetailID, AuditID, FormType, QuestionNumber, QuestionText, Score, IsNotApplicable, Remarks, CreatedBy)
VALUES ('SD-20231115-001', 'AUD-20231115-001', 'FORM-2', '1', '品質マニュアルが整備されているか', 4, '0', 'ISO9001準拠の品質マニュアルあり', 'qa_auditor');

INSERT INTO AuditScoreDetails (ScoreDetailID, AuditID, FormType, QuestionNumber, QuestionText, Score, IsNotApplicable, Remarks, CreatedBy)
VALUES ('SD-20231115-002', 'AUD-20231115-001', 'FORM-2', '2', '組織図が明確に定義されているか', 4, '0', '組織図および職務分掌明確', 'qa_auditor');

INSERT INTO AuditScoreDetails (ScoreDetailID, AuditID, FormType, QuestionNumber, QuestionText, Score, IsNotApplicable, Remarks, CreatedBy)
VALUES ('SD-20231115-003', 'AUD-20231115-001', 'FORM-2', '3', '品質記録が適切に保管されているか', 4, '0', '電子記録システムで一元管理', 'qa_auditor');

INSERT INTO AuditScoreDetails (ScoreDetailID, AuditID, FormType, QuestionNumber, QuestionText, Score, IsNotApplicable, Remarks, CreatedBy)
VALUES ('SD-20231115-101', 'AUD-20231115-001', 'FORM-5', '1', '製造設備は適切に保全されているか', 4, '0', '定期保全計画に基づき実施', 'qa_auditor');

INSERT INTO AuditScoreDetails (ScoreDetailID, AuditID, FormType, QuestionNumber, QuestionText, Score, IsNotApplicable, Remarks, CreatedBy)
VALUES ('SD-20231115-102', 'AUD-20231115-001', 'FORM-5', '2', '作業環境は清潔に保たれているか', 4, '0', '5S活動が徹底されている', 'qa_auditor');

INSERT INTO AuditScoreDetails (ScoreDetailID, AuditID, FormType, QuestionNumber, QuestionText, Score, IsNotApplicable, Remarks, CreatedBy)
VALUES ('SD-20231115-103', 'AUD-20231115-001', 'FORM-5', '3', '検査設備の校正は適切に実施されているか', 2, '0', '一部設備の校正期限が近い', 'qa_auditor');


INSERT INTO NotificationHistory (
    NotificationID, NotificationType, TargetID,
    RecipientUserID, RecipientEmail,
    Subject, MessageBody, Status
) VALUES (
    'NTF-20251020-001',
    'REVIEW_REMINDER',
    'SUP-20231201-003',
    'qa_manager',
    'qa-manager@company.example.com',
    '【重要】購買先定期再評価のお知らせ',
    '購買先「関西精密株式会社」の定期再評価期限（2025-12-01）が1ヶ月後に迫っています。\n再評価の準備を開始してください。',
    'SENT'
);

INSERT INTO NotificationHistory (
    NotificationID, NotificationType, TargetID,
    RecipientUserID, RecipientEmail,
    Subject, MessageBody, Status
) VALUES (
    'NTF-20251111-002',
    'CORRECTIVE_ACTION_REMINDER',
    'CA-20251015-002',
    'qa_manager',
    'qa-manager@company.example.com',
    '【注意】是正処置期限のお知らせ',
    '是正処置ID「CA-20251015-002」の期限（2025-11-14）が3日後に迫っています。\n進捗状況を確認してください。',
    'SENT'
);

INSERT INTO NotificationHistory (
    NotificationID, NotificationType, TargetID,
    RecipientUserID, RecipientEmail,
    Subject, MessageBody, Status
) VALUES (
    'NTF-20251202-003',
    'CORRECTIVE_ACTION_OVERDUE',
    'CA-20251101-003',
    'qa_manager',
    'qa-manager@company.example.com',
    '【緊急】是正処置期限超過のお知らせ',
    '是正処置ID「CA-20251101-003」の期限（2025-12-01）を超過しています。\n至急対応してください。',
    'SENT'
);


SELECT 'SupplierMaster' AS TableName, COUNT(*) AS RecordCount FROM SupplierMaster
UNION ALL
SELECT 'AuditHistory', COUNT(*) FROM AuditHistory
UNION ALL
SELECT 'CorrectiveActionHistory', COUNT(*) FROM CorrectiveActionHistory
UNION ALL
SELECT 'AuditScoreDetails', COUNT(*) FROM AuditScoreDetails
UNION ALL
SELECT 'NotificationHistory', COUNT(*) FROM NotificationHistory;
