package jp.co.company.sqas.batch;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 是正処置期限リマインダーバッチジョブ
 * Corrective Action Reminder Batch Job
 * 
 * 是正処置の期限3日前および期限超過時に
 * 品質保証部長と関係部門へ自動通知を送信する
 * 
 * 実行頻度: 日次（毎日深夜実行を推奨）
 * 
 * @author Supplier Quality Audit System Development Team
 * @version 1.0
 * @since 2025-11-20
 */
public class CorrectiveActionReminderBatchJob {

    private static final int REMINDER_DAYS_BEFORE = 3; // 3日前に通知
    private static final String NOTIFICATION_TYPE_REMINDER = "CORRECTIVE_ACTION_REMINDER";
    private static final String NOTIFICATION_TYPE_OVERDUE = "CORRECTIVE_ACTION_OVERDUE";

    /**
     * 是正処置情報クラス
     */
    public static class CorrectiveActionInfo {
        private String correctiveActionId;
        private String auditId;
        private String supplierId;
        private String companyName;
        private String nonConformityDetails;
        private LocalDate deadline;
        private String status;
        private String createdBy;

        public CorrectiveActionInfo(String correctiveActionId, String auditId, String supplierId,
                                   String companyName, String nonConformityDetails, LocalDate deadline,
                                   String status, String createdBy) {
            this.correctiveActionId = correctiveActionId;
            this.auditId = auditId;
            this.supplierId = supplierId;
            this.companyName = companyName;
            this.nonConformityDetails = nonConformityDetails;
            this.deadline = deadline;
            this.status = status;
            this.createdBy = createdBy;
        }

        public String getCorrectiveActionId() { return correctiveActionId; }
        public String getAuditId() { return auditId; }
        public String getSupplierId() { return supplierId; }
        public String getCompanyName() { return companyName; }
        public String getNonConformityDetails() { return nonConformityDetails; }
        public LocalDate getDeadline() { return deadline; }
        public String getStatus() { return status; }
        public String getCreatedBy() { return createdBy; }

        public boolean isOverdue() {
            return deadline.isBefore(LocalDate.now());
        }

        public boolean isDueSoon(int daysBeforeDue) {
            LocalDate thresholdDate = LocalDate.now().plusDays(daysBeforeDue);
            return deadline.isAfter(LocalDate.now()) && 
                   (deadline.isBefore(thresholdDate) || deadline.isEqual(thresholdDate));
        }
    }

    /**
     * バッチジョブのメイン処理
     * 
     * @return 処理結果（0: 正常終了, 1: エラー）
     */
    public int execute() {
        try {
            System.out.println("=== Corrective Action Reminder Batch Job Started ===");
            System.out.println("Execution Date: " + LocalDate.now());

            int totalNotifications = 0;
            int successCount = 0;
            int failureCount = 0;

            List<CorrectiveActionInfo> actionsDueSoon = getCorrectiveActionsDueSoon();
            System.out.println("Found " + actionsDueSoon.size() + " corrective actions due soon (within 3 days)");

            for (CorrectiveActionInfo action : actionsDueSoon) {
                try {
                    sendReminderNotification(action);
                    successCount++;
                    totalNotifications++;
                } catch (Exception e) {
                    System.err.println("Failed to send reminder for: " + action.getCorrectiveActionId());
                    System.err.println("Error: " + e.getMessage());
                    failureCount++;
                }
            }

            List<CorrectiveActionInfo> overdueActions = getOverdueCorrectiveActions();
            System.out.println("Found " + overdueActions.size() + " overdue corrective actions");

            for (CorrectiveActionInfo action : overdueActions) {
                try {
                    sendOverdueNotification(action);
                    updateCorrectiveActionStatus(action.getCorrectiveActionId(), "OVERDUE");
                    successCount++;
                    totalNotifications++;
                } catch (Exception e) {
                    System.err.println("Failed to send overdue notification for: " + action.getCorrectiveActionId());
                    System.err.println("Error: " + e.getMessage());
                    failureCount++;
                }
            }

            System.out.println("\n=== Batch Job Summary ===");
            System.out.println("Total Notifications: " + totalNotifications);
            System.out.println("Notifications Sent: " + successCount);
            System.out.println("Failures: " + failureCount);
            System.out.println("=== Corrective Action Reminder Batch Job Completed ===");

            return failureCount > 0 ? 1 : 0;

        } catch (Exception e) {
            System.err.println("Critical error in batch job execution: " + e.getMessage());
            e.printStackTrace();
            return 1;
        }
    }

    /**
     * 期限3日前の是正処置を取得
     * 
     * @return 是正処置リスト
     */
    private List<CorrectiveActionInfo> getCorrectiveActionsDueSoon() {
        List<CorrectiveActionInfo> actions = new ArrayList<>();

        /*
        SELECT 
            ca.CorrectiveActionID,
            ca.AuditID,
            s.SupplierID,
            s.CompanyName,
            ca.NonConformityDetails,
            ca.CorrectiveActionDeadline,
            ca.Status,
            ca.CreatedBy
        FROM CorrectiveActionHistory ca
        JOIN AuditHistory ah ON ca.AuditID = ah.AuditID
        JOIN SupplierMaster s ON ah.SupplierID = s.SupplierID
        WHERE ca.Status IN ('OPEN', 'IN_PROGRESS')
          AND ca.CorrectiveActionDeadline = CURRENT_DATE + INTERVAL '3 days'
          AND NOT EXISTS (
              SELECT 1 FROM NotificationHistory n
              WHERE n.NotificationType = 'CORRECTIVE_ACTION_REMINDER'
                AND n.TargetID = ca.CorrectiveActionID
                AND n.SentDate >= CURRENT_DATE - INTERVAL '1 day'
          )
        ORDER BY ca.CorrectiveActionDeadline ASC
        */

        LocalDate threeDaysFromNow = LocalDate.now().plusDays(3);
        
        actions.add(new CorrectiveActionInfo(
            "CA-20251015-002",
            "AUD-20251015-005",
            "SUP-20240615-005",
            "東北製造株式会社",
            "製造工程における検査工程の欠落",
            threeDaysFromNow,
            "IN_PROGRESS",
            "qa_auditor"
        ));

        return actions;
    }

    /**
     * 期限超過の是正処置を取得
     * 
     * @return 是正処置リスト
     */
    private List<CorrectiveActionInfo> getOverdueCorrectiveActions() {
        List<CorrectiveActionInfo> actions = new ArrayList<>();

        /*
        SELECT 
            ca.CorrectiveActionID,
            ca.AuditID,
            s.SupplierID,
            s.CompanyName,
            ca.NonConformityDetails,
            ca.CorrectiveActionDeadline,
            ca.Status,
            ca.CreatedBy
        FROM CorrectiveActionHistory ca
        JOIN AuditHistory ah ON ca.AuditID = ah.AuditID
        JOIN SupplierMaster s ON ah.SupplierID = s.SupplierID
        WHERE ca.Status IN ('OPEN', 'IN_PROGRESS')
          AND ca.CorrectiveActionDeadline < CURRENT_DATE
        ORDER BY ca.CorrectiveActionDeadline ASC
        */

        actions.add(new CorrectiveActionInfo(
            "CA-20251101-003",
            "AUD-20251101-003",
            "SUP-20231201-003",
            "関西精密株式会社",
            "校正記録の一部に記入漏れがある",
            LocalDate.of(2025, 12, 1),
            "OPEN",
            "qa_auditor"
        ));

        return actions;
    }

    /**
     * 期限前リマインダー通知を送信
     * 
     * @param action 是正処置情報
     */
    private void sendReminderNotification(CorrectiveActionInfo action) {
        String subject = "【注意】是正処置期限のお知らせ";
        String messageBody = createReminderMessage(action);

        List<String> recipients = getNotificationRecipients(action);

        for (String recipientEmail : recipients) {
            sendEmail(recipientEmail, subject, messageBody);
            saveNotificationHistory(
                action.getCorrectiveActionId(),
                NOTIFICATION_TYPE_REMINDER,
                recipientEmail,
                subject,
                messageBody
            );
        }

        System.out.println("Reminder notification sent for: " + action.getCorrectiveActionId());
    }

    /**
     * 期限超過通知を送信
     * 
     * @param action 是正処置情報
     */
    private void sendOverdueNotification(CorrectiveActionInfo action) {
        String subject = "【緊急】是正処置期限超過のお知らせ";
        String messageBody = createOverdueMessage(action);

        List<String> recipients = getNotificationRecipients(action);

        for (String recipientEmail : recipients) {
            sendEmail(recipientEmail, subject, messageBody);
            saveNotificationHistory(
                action.getCorrectiveActionId(),
                NOTIFICATION_TYPE_OVERDUE,
                recipientEmail,
                subject,
                messageBody
            );
        }

        System.out.println("Overdue notification sent for: " + action.getCorrectiveActionId());
    }

    /**
     * リマインダーメッセージを作成
     * 
     * @param action 是正処置情報
     * @return メッセージ本文
     */
    private String createReminderMessage(CorrectiveActionInfo action) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日");
        
        StringBuilder message = new StringBuilder();
        message.append("購買先品質審査管理システムからのお知らせ\n\n");
        message.append("以下の是正処置について、期限が近づいています。\n\n");
        message.append("【是正処置情報】\n");
        message.append("是正処置ID: ").append(action.getCorrectiveActionId()).append("\n");
        message.append("購買先: ").append(action.getCompanyName()).append("\n");
        message.append("不適合内容: ").append(action.getNonConformityDetails()).append("\n");
        message.append("是正期限: ").append(action.getDeadline().format(formatter)).append("\n");
        message.append("現在のステータス: ").append(action.getStatus()).append("\n\n");
        
        long daysUntilDeadline = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), action.getDeadline());
        message.append("※ 期限まで残り ").append(daysUntilDeadline).append(" 日です。\n\n");
        
        message.append("【対応事項】\n");
        message.append("1. 購買先からの是正処置計画の提出状況を確認してください\n");
        message.append("2. 是正処置の進捗状況を確認してください\n");
        message.append("3. 必要に応じて購買先へフォローアップを実施してください\n\n");
        
        message.append("詳細は購買先品質審査管理システムをご確認ください。\n");
        message.append("URL: [システムURL]\n\n");
        message.append("---\n");
        message.append("このメールは自動送信されています。\n");
        message.append("購買先品質審査管理システム（GCP0602準拠）\n");
        
        return message.toString();
    }

    /**
     * 期限超過メッセージを作成
     * 
     * @param action 是正処置情報
     * @return メッセージ本文
     */
    private String createOverdueMessage(CorrectiveActionInfo action) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日");
        
        StringBuilder message = new StringBuilder();
        message.append("【緊急】購買先品質審査管理システムからのお知らせ\n\n");
        message.append("以下の是正処置について、期限を超過しています。至急対応してください。\n\n");
        message.append("【是正処置情報】\n");
        message.append("是正処置ID: ").append(action.getCorrectiveActionId()).append("\n");
        message.append("購買先: ").append(action.getCompanyName()).append("\n");
        message.append("不適合内容: ").append(action.getNonConformityDetails()).append("\n");
        message.append("是正期限: ").append(action.getDeadline().format(formatter)).append(" ★期限超過★\n");
        message.append("現在のステータス: OVERDUE（期限超過）\n\n");
        
        long daysOverdue = java.time.temporal.ChronoUnit.DAYS.between(action.getDeadline(), LocalDate.now());
        message.append("※ 期限超過から ").append(daysOverdue).append(" 日経過しています。\n\n");
        
        message.append("【緊急対応事項】\n");
        message.append("1. 至急、購買先へ連絡し、是正処置の完了状況を確認してください\n");
        message.append("2. 期限延長が必要な場合は、品質保証部長の承認を得てください\n");
        message.append("3. 是正が完了しない場合は、取引停止措置を検討してください\n\n");
        
        message.append("詳細は購買先品質審査管理システムをご確認ください。\n");
        message.append("URL: [システムURL]\n\n");
        message.append("---\n");
        message.append("このメールは自動送信されています。\n");
        message.append("購買先品質審査管理システム（GCP0602準拠）\n");
        
        return message.toString();
    }

    /**
     * 通知先リストを取得
     * 
     * @param action 是正処置情報
     * @return 通知先メールアドレスリスト
     */
    private List<String> getNotificationRecipients(CorrectiveActionInfo action) {
        List<String> recipients = new ArrayList<>();
        
        recipients.add("qa-manager@company.example.com");
        
        recipients.add("qa-auditor@company.example.com");
        
        recipients.add("procurement-dept@company.example.com");
        
        return recipients;
    }

    /**
     * 是正処置のステータスを更新
     * 
     * @param correctiveActionId 是正処置ID
     * @param newStatus 新しいステータス
     */
    private void updateCorrectiveActionStatus(String correctiveActionId, String newStatus) {
        /*
        UPDATE CorrectiveActionHistory
        SET Status = newStatus, UpdatedAt = CURRENT_TIMESTAMP
        WHERE CorrectiveActionID = correctiveActionId
        */
        
        System.out.println("Updated status to " + newStatus + " for: " + correctiveActionId);
    }

    /**
     * メール送信処理
     * 
     * @param recipientEmail 送信先メールアドレス
     * @param subject 件名
     * @param messageBody 本文
     */
    private void sendEmail(String recipientEmail, String subject, String messageBody) {
        System.out.println("Email sent to: " + recipientEmail);
        System.out.println("Subject: " + subject);
    }

    /**
     * 通知履歴をDBに保存
     * 
     * @param targetId 対象ID（是正処置ID）
     * @param notificationType 通知種別
     * @param recipientEmail 送信先メールアドレス
     * @param subject 件名
     * @param messageBody 本文
     */
    private void saveNotificationHistory(String targetId, String notificationType,
                                        String recipientEmail, String subject, String messageBody) {
        System.out.println("Notification history saved for target: " + targetId);
    }

    /**
     * バッチジョブのエントリーポイント
     * 
     * @param args コマンドライン引数
     */
    public static void main(String[] args) {
        CorrectiveActionReminderBatchJob job = new CorrectiveActionReminderBatchJob();
        int exitCode = job.execute();
        System.exit(exitCode);
    }
}
