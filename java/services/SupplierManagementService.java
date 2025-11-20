package jp.co.company.sqas.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 購買先管理サービス
 * Supplier Management Service
 * 
 * 購買先マスターの管理、等級更新、ステータス管理を実装
 * 
 * @author Supplier Quality Audit System Development Team
 * @version 1.0
 * @since 2025-11-20
 */
public class SupplierManagementService {

    private AuditScoreCalculationService scoreService;

    public SupplierManagementService() {
        this.scoreService = new AuditScoreCalculationService();
    }

    /**
     * 購買先情報クラス
     */
    public static class SupplierInfo {
        private String supplierId;
        private String companyName;
        private boolean iso9001Certified;
        private LocalDate initialRegistrationDate;
        private Integer currentRating;
        private LocalDate nextReviewDate;
        private String supplierStatus;

        public SupplierInfo(String supplierId, String companyName, boolean iso9001Certified,
                          LocalDate initialRegistrationDate, Integer currentRating,
                          LocalDate nextReviewDate, String supplierStatus) {
            this.supplierId = supplierId;
            this.companyName = companyName;
            this.iso9001Certified = iso9001Certified;
            this.initialRegistrationDate = initialRegistrationDate;
            this.currentRating = currentRating;
            this.nextReviewDate = nextReviewDate;
            this.supplierStatus = supplierStatus;
        }

        public String getSupplierId() { return supplierId; }
        public void setSupplierId(String supplierId) { this.supplierId = supplierId; }
        
        public String getCompanyName() { return companyName; }
        public void setCompanyName(String companyName) { this.companyName = companyName; }
        
        public boolean isIso9001Certified() { return iso9001Certified; }
        public void setIso9001Certified(boolean iso9001Certified) { this.iso9001Certified = iso9001Certified; }
        
        public LocalDate getInitialRegistrationDate() { return initialRegistrationDate; }
        public void setInitialRegistrationDate(LocalDate initialRegistrationDate) { this.initialRegistrationDate = initialRegistrationDate; }
        
        public Integer getCurrentRating() { return currentRating; }
        public void setCurrentRating(Integer currentRating) { this.currentRating = currentRating; }
        
        public LocalDate getNextReviewDate() { return nextReviewDate; }
        public void setNextReviewDate(LocalDate nextReviewDate) { this.nextReviewDate = nextReviewDate; }
        
        public String getSupplierStatus() { return supplierStatus; }
        public void setSupplierStatus(String supplierStatus) { this.supplierStatus = supplierStatus; }

        /**
         * 再評価期限が近いかチェック（1ヶ月以内）
         */
        public boolean isReviewDueSoon() {
            if (nextReviewDate == null) return false;
            LocalDate oneMonthFromNow = LocalDate.now().plusMonths(1);
            return nextReviewDate.isBefore(oneMonthFromNow) || nextReviewDate.isEqual(oneMonthFromNow);
        }

        /**
         * 再評価期限が超過しているかチェック
         */
        public boolean isReviewOverdue() {
            if (nextReviewDate == null) return false;
            return nextReviewDate.isBefore(LocalDate.now());
        }

        /**
         * アラートレベルを取得（UI表示用）
         */
        public String getAlertLevel() {
            if (isReviewOverdue()) {
                return "CRITICAL"; // 赤色表示
            } else if (isReviewDueSoon()) {
                return "WARNING"; // 黄色表示
            } else {
                return "NORMAL"; // 通常表示
            }
        }
    }

    /**
     * 審査承認後に購買先マスターを更新する
     * 
     * @param supplierId 購買先ID
     * @param totalScore 総合点
     * @param auditDate 審査実施日
     * @return 更新後の等級
     */
    public Integer updateSupplierRatingAfterAudit(String supplierId, BigDecimal totalScore, LocalDate auditDate) {
        Integer newRating = scoreService.determineGrade(totalScore);
        
        if (newRating == null) {
            throw new IllegalStateException("Score is below 60. Supplier cannot be registered.");
        }

        LocalDate nextReviewDate = auditDate.plusYears(2);


        System.out.println(String.format(
            "Updated Supplier %s: Rating=%d, NextReviewDate=%s",
            supplierId, newRating, nextReviewDate
        ));

        return newRating;
    }

    /**
     * 購買先ステータスを変更する
     * 
     * @param supplierId 購買先ID
     * @param newStatus 新しいステータス（ACTIVE, SUSPENDED, INACTIVE）
     * @param reason 変更理由
     */
    public void updateSupplierStatus(String supplierId, String newStatus, String reason) {
        if (!newStatus.equals("ACTIVE") && !newStatus.equals("SUSPENDED") && !newStatus.equals("INACTIVE")) {
            throw new IllegalArgumentException("Invalid status. Must be ACTIVE, SUSPENDED, or INACTIVE");
        }


        System.out.println(String.format(
            "Updated Supplier %s status to %s. Reason: %s",
            supplierId, newStatus, reason
        ));
    }

    /**
     * 再評価が必要な購買先リストを取得
     * 
     * @param daysBeforeDue 期限前の日数（例: 30日前）
     * @return 再評価が必要な購買先リスト
     */
    public List<SupplierInfo> getSuppliersRequiringReview(int daysBeforeDue) {
        LocalDate thresholdDate = LocalDate.now().plusDays(daysBeforeDue);
        

        List<SupplierInfo> suppliers = new ArrayList<>();
        
        suppliers.add(new SupplierInfo(
            "SUP-20231201-003",
            "関西精密株式会社",
            true,
            LocalDate.of(2023, 12, 1),
            3,
            LocalDate.of(2025, 12, 1),
            "ACTIVE"
        ));

        return suppliers;
    }

    /**
     * 購買先IDを生成する
     * 形式: SUP-YYYYMMDD-XXX
     * 
     * @return 新しい購買先ID
     */
    public String generateSupplierId() {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        
        int sequence = 1; // サンプル
        
        return String.format("SUP-%s-%03d", dateStr, sequence);
    }

    /**
     * 購買先の等級に応じた説明を取得
     * 
     * @param rating 等級（1, 2, 3）
     * @return 等級説明
     */
    public String getRatingDescription(Integer rating) {
        if (rating == null) {
            return "未評価";
        }
        
        switch (rating) {
            case 1:
                return "優良（80点以上）";
            case 2:
                return "良好（70-79点）";
            case 3:
                return "標準（60-69点）";
            default:
                return "不明";
        }
    }

    /**
     * ISO9001認証の有効期限チェック
     * 
     * @param expiryDate 有効期限
     * @return 有効期限が近い（3ヶ月以内）または超過している場合 true
     */
    public boolean isIso9001ExpiryDueSoon(LocalDate expiryDate) {
        if (expiryDate == null) return false;
        
        LocalDate threeMonthsFromNow = LocalDate.now().plusMonths(3);
        return expiryDate.isBefore(threeMonthsFromNow) || expiryDate.isEqual(threeMonthsFromNow);
    }

    /**
     * 購買先の総合評価レポートを生成
     * 
     * @param supplierId 購買先ID
     * @return 評価レポート文字列
     */
    public String generateSupplierEvaluationReport(String supplierId) {
        
        StringBuilder report = new StringBuilder();
        report.append("=== 購買先総合評価レポート ===\n");
        report.append("購買先ID: ").append(supplierId).append("\n");
        report.append("会社名: [DBから取得]\n");
        report.append("現行等級: [DBから取得]\n");
        report.append("ISO9001認証: [DBから取得]\n");
        report.append("初回登録日: [DBから取得]\n");
        report.append("次回審査予定日: [DBから取得]\n");
        report.append("\n=== 審査履歴 ===\n");
        report.append("[過去の審査履歴をDBから取得して表示]\n");
        report.append("\n=== 是正処置履歴 ===\n");
        report.append("[是正処置履歴をDBから取得して表示]\n");
        
        return report.toString();
    }

    /**
     * サンプル使用例（テスト用）
     */
    public static void main(String[] args) {
        SupplierManagementService service = new SupplierManagementService();

        String newSupplierId = service.generateSupplierId();
        System.out.println("Generated Supplier ID: " + newSupplierId);

        try {
            Integer newRating = service.updateSupplierRatingAfterAudit(
                "SUP-20231115-001",
                BigDecimal.valueOf(85.50),
                LocalDate.of(2023, 11, 15)
            );
            System.out.println("New Rating: " + newRating + " - " + service.getRatingDescription(newRating));
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }

        List<SupplierInfo> suppliersRequiringReview = service.getSuppliersRequiringReview(30);
        System.out.println("\n=== Suppliers Requiring Review (within 30 days) ===");
        for (SupplierInfo supplier : suppliersRequiringReview) {
            System.out.println(String.format(
                "%s - %s (Next Review: %s, Alert: %s)",
                supplier.getSupplierId(),
                supplier.getCompanyName(),
                supplier.getNextReviewDate(),
                supplier.getAlertLevel()
            ));
        }

        LocalDate expiryDate = LocalDate.of(2026, 2, 1);
        boolean isDueSoon = service.isIso9001ExpiryDueSoon(expiryDate);
        System.out.println("\nISO9001 Expiry Due Soon: " + isDueSoon);
    }
}
