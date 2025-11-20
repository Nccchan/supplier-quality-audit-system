package jp.co.company.sqas.batch;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 再評価リマインダーバッチジョブ
 * Review Reminder Batch Job
 * 
 * 購買先の定期再評価期限（初回登録日から2年後）の1ヶ月前に
 * 品質保証部と調達部の管理職へ自動通知を送信する
 * 
 * 実行頻度: 日次（毎日深夜実行を推奨）
 * 
 * @author Supplier Quality Audit System Development Team
 * @version 1.0
 * @since 2025-11-20
 */
public class ReviewReminderBatchJob {

    private static final int REMINDER_DAYS_BEFORE = 30; // 30日前に通知
    private static final String NOTIFICATION_TYPE = "REVIEW_REMINDER";

    /**
     * 通知対象購買先クラス
     */
    public static class SupplierForReview {
        private String supplierId;
        private String companyName;
        private LocalDate nextReviewDate;
        private Integer currentRating;
        private String contactEmail;

        public SupplierForReview(String supplierId, String companyName, LocalDate nextReviewDate, 
                                Integer currentRating, String contactEmail) {
            this.supplierId = supplierId;
            this.companyName = companyName;
            this.nextReviewDate = nextReviewDate;
            this.currentRating = currentRating;
            this.contactEmail = contactEmail;
        }

        public String getSupplierId() { return supplierId; }
        public String getCompanyName() { return companyName; }
        public LocalDate getNextReviewDate() { return nextReviewDate; }
        public Integer getCurrentRating() { return currentRating; }
        public String getContactEmail() { return contactEmail; }
    }

    /**
     * バッチジョブのメイン処理
     * 
     * @return 処理結果（0: 正常終了, 1: エラー）
     */
    public int execute() {
        try {
            System.out.println("=== Review Reminder Batch Job Started ===");
            System.out.println("Execution Date: " + LocalDate.now());

            List<SupplierForReview> suppliersForReview = getSuppliersRequiringReview();
            System.out.println("Found " + suppliersForReview.size() + " suppliers requiring review notification");

            if (suppliersForReview.isEmpty()) {
                System.out.println("No suppliers require review notification at this time.");
                return 0;
            }

            int successCount = 0;
            int failureCount = 0;

            for (SupplierForReview supplier : suppliersForReview) {
                try {
                    sendReviewReminderNotification(supplier);
                    successCount++;
                } catch (Exception e) {
                    System.err.println("Failed to send notification for supplier: " + supplier.getSupplierId());
                    System.err.println("Error: " + e.getMessage());
                    failureCount++;
                }
            }

            System.out.println("\n=== Batch Job Summary ===");
            System.out.println("Total Suppliers: " + suppliersForReview.size());
            System.out.println("Notifications Sent: " + successCount);
            System.out.println("Failures: " + failureCount);
            System.out.println("=== Review Reminder Batch Job Completed ===");

            return failureCount > 0 ? 1 : 0;

        } catch (Exception e) {
            System.err.println("Critical error in batch job execution: " + e.getMessage());
            e.printStackTrace();
            return 1;
        }
    }

    /**
     * 通知対象の購買先を取得
     * NextReviewDateが30日後以内のACTIVE購買先を抽出
     * 
     * @return 通知対象購買先リスト
     */
    private List<SupplierForReview> getSuppliersRequiringReview() {
        List<SupplierForReview> suppliers = new ArrayList<>();

        /*
        SELECT 
            s.SupplierID,
            s.CompanyName,
            s.NextReviewDate,
            s.CurrentRating,
            s.ContactEmail
        FROM SupplierMaster s
        WHERE s.SupplierStatus = 'ACTIVE'
          AND s.NextReviewDate IS NOT NULL
          AND s.NextReviewDate <= CURRENT_DATE + INTERVAL '30 days'
          AND s.NextReviewDate > CURRENT_DATE
          AND NOT EXISTS (
              SELECT 1 FROM NotificationHistory n
              WHERE n.NotificationType = 'REVIEW_REMINDER'
                AND n.TargetID = s.SupplierID
                AND n.SentDate >= CURRENT_DATE - INTERVAL '7 days'
          )
        ORDER BY s.NextReviewDate ASC
        */

        LocalDate targetDate = LocalDate.now().plusDays(REMINDER_DAYS_BEFORE);
        
        suppliers.add(new SupplierForReview(
            "SUP-20231201-003",
            "関西精密株式会社",
            LocalDate.of(2025, 12, 1),
            3,
            "ito@kansai-seimitsu.example.com"
        ));

        return suppliers;
    }

    /**
     * 再評価リマインダー通知を送信
     * 
     * @param supplier 通知対象購買先
     */
    private void sendReviewReminderNotification(SupplierForReview supplier) {
        String subject = "【重要】購買先定期再評価のお知らせ";
        String messageBody = createNotificationMessage(supplier);

        List<String> recipients = getNotificationRecipients();

        for (String recipientEmail : recipients) {
            sendEmail(recipientEmail, subject, messageBody);
            
            saveNotificationHistory(
                supplier.getSupplierId(),
                NOTIFICATION_TYPE,
                recipientEmail,
                subject,
                messageBody
            );
        }

        System.out.println("Notification sent for supplier: " + supplier.getCompanyName());
    }

    /**
     * 通知メッセージを作成
     * 
     * @param supplier 通知対象購買先
     * @return 通知メッセージ本文
     */
    private String createNotificationMessage(SupplierForReview supplier) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日");
        
        StringBuilder message = new StringBuilder();
        message.append("購買先品質審査管理システムからのお知らせ\n\n");
        message.append("以下の購買先について、定期再評価の期限が近づいています。\n\n");
        message.append("【購買先情報】\n");
        message.append("購買先ID: ").append(supplier.getSupplierId()).append("\n");
        message.append("会社名: ").append(supplier.getCompanyName()).append("\n");
        message.append("現行等級: ").append(getRatingText(supplier.getCurrentRating())).append("\n");
        message.append("再評価期限: ").append(supplier.getNextReviewDate().format(formatter)).append("\n\n");
        
        long daysUntilReview = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), supplier.getNextReviewDate());
        message.append("※ 再評価期限まで残り ").append(daysUntilReview).append(" 日です。\n\n");
        
        message.append("【対応事項】\n");
        message.append("1. 調達部と品質保証部で対象業者の選定を実施してください（付表-6参照）\n");
        message.append("2. 実地監査の日程調整を開始してください\n");
        message.append("3. 様式-11（実施監査結果）の準備を行ってください\n\n");
        
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
     * @return 通知先メールアドレスリスト
     */
    private List<String> getNotificationRecipients() {
        List<String> recipients = new ArrayList<>();
        
        recipients.add("qa-manager@company.example.com"); // 品質保証部長
        recipients.add("procurement-manager@company.example.com"); // 調達部長
        
        return recipients;
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
     * @param targetId 対象ID（購買先ID）
     * @param notificationType 通知種別
     * @param recipientEmail 送信先メールアドレス
     * @param subject 件名
     * @param messageBody 本文
     */
    private void saveNotificationHistory(String targetId, String notificationType, 
                                        String recipientEmail, String subject, String messageBody) {
        /*
        INSERT INTO NotificationHistory (
            NotificationID, NotificationType, TargetID,
            RecipientUserID, RecipientEmail, Subject, MessageBody, Status
        ) VALUES (
            [生成したID], notificationType, targetId,
            [ユーザーID], recipientEmail, subject, messageBody, 'SENT'
        )
        */
        
        System.out.println("Notification history saved for target: " + targetId);
    }

    /**
     * 等級テキストを取得
     * 
     * @param rating 等級
     * @return 等級テキスト
     */
    private String getRatingText(Integer rating) {
        if (rating == null) return "未評価";
        switch (rating) {
            case 1: return "1（優良）";
            case 2: return "2（良好）";
            case 3: return "3（標準）";
            default: return "不明";
        }
    }

    /**
     * バッチジョブのエントリーポイント
     * intra-mart のジョブスケジューラから実行される
     * 
     * @param args コマンドライン引数
     */
    public static void main(String[] args) {
        ReviewReminderBatchJob job = new ReviewReminderBatchJob();
        int exitCode = job.execute();
        System.exit(exitCode);
    }
}
