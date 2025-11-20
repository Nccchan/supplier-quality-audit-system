package jp.co.company.sqas.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * 審査採点計算サービス
 * Audit Score Calculation Service
 * 
 * GCP0602準拠の採点計算ロジックを実装
 * 総合点 = (評価点合計 × 100) / (4 × 総項目数 - 4 × 未調査項目数)
 * 
 * @author Supplier Quality Audit System Development Team
 * @version 1.0
 * @since 2025-11-20
 */
public class AuditScoreCalculationService {

    /**
     * 審査採点項目クラス
     */
    public static class AuditScoreItem {
        private String questionNumber;
        private String questionText;
        private int score; // 4: 適合, 2: 一部不適合, 0: 不適合
        private boolean isNotApplicable; // true: 未調査
        private String remarks;

        public AuditScoreItem(String questionNumber, String questionText, int score, boolean isNotApplicable, String remarks) {
            this.questionNumber = questionNumber;
            this.questionText = questionText;
            this.score = score;
            this.isNotApplicable = isNotApplicable;
            this.remarks = remarks;
        }

        public String getQuestionNumber() { return questionNumber; }
        public void setQuestionNumber(String questionNumber) { this.questionNumber = questionNumber; }
        
        public String getQuestionText() { return questionText; }
        public void setQuestionText(String questionText) { this.questionText = questionText; }
        
        public int getScore() { return score; }
        public void setScore(int score) { 
            if (score != 0 && score != 2 && score != 4) {
                throw new IllegalArgumentException("Score must be 0, 2, or 4");
            }
            this.score = score; 
        }
        
        public boolean isNotApplicable() { return isNotApplicable; }
        public void setNotApplicable(boolean notApplicable) { isNotApplicable = notApplicable; }
        
        public String getRemarks() { return remarks; }
        public void setRemarks(String remarks) { this.remarks = remarks; }
    }

    /**
     * 審査採点結果クラス
     */
    public static class AuditScoreResult {
        private BigDecimal totalScore;
        private int totalItems;
        private int notApplicableItems;
        private int scoreSum;
        private String rating; // 優, 良, 可, 不可
        private String decision; // PASS, FAIL, CONDITIONAL

        public AuditScoreResult(BigDecimal totalScore, int totalItems, int notApplicableItems, int scoreSum, String rating, String decision) {
            this.totalScore = totalScore;
            this.totalItems = totalItems;
            this.notApplicableItems = notApplicableItems;
            this.scoreSum = scoreSum;
            this.rating = rating;
            this.decision = decision;
        }

        public BigDecimal getTotalScore() { return totalScore; }
        public int getTotalItems() { return totalItems; }
        public int getNotApplicableItems() { return notApplicableItems; }
        public int getScoreSum() { return scoreSum; }
        public String getRating() { return rating; }
        public String getDecision() { return decision; }

        @Override
        public String toString() {
            return String.format(
                "AuditScoreResult{totalScore=%.2f, totalItems=%d, notApplicableItems=%d, scoreSum=%d, rating='%s', decision='%s'}",
                totalScore, totalItems, notApplicableItems, scoreSum, rating, decision
            );
        }
    }

    /**
     * 総合点を計算する
     * 
     * 計算式: 総合点 = (評価点合計 × 100) / (4 × 総項目数 - 4 × 未調査項目数)
     * 
     * @param scoreItems 審査採点項目リスト
     * @return 審査採点結果
     * @throws IllegalArgumentException 項目リストが空の場合、または全項目が未調査の場合
     */
    public AuditScoreResult calculateTotalScore(List<AuditScoreItem> scoreItems) {
        if (scoreItems == null || scoreItems.isEmpty()) {
            throw new IllegalArgumentException("Score items list cannot be null or empty");
        }

        int totalItems = scoreItems.size();
        
        int notApplicableItems = (int) scoreItems.stream()
            .filter(AuditScoreItem::isNotApplicable)
            .count();
        
        int scoreSum = scoreItems.stream()
            .filter(item -> !item.isNotApplicable())
            .mapToInt(AuditScoreItem::getScore)
            .sum();

        int denominator = (4 * totalItems) - (4 * notApplicableItems);
        
        if (denominator == 0) {
            throw new IllegalArgumentException("All items are marked as not applicable. Cannot calculate score.");
        }

        BigDecimal totalScore = BigDecimal.valueOf(scoreSum)
            .multiply(BigDecimal.valueOf(100))
            .divide(BigDecimal.valueOf(denominator), 2, RoundingMode.HALF_UP);

        String rating = determineRating(totalScore);
        String decision = determineDecision(totalScore);

        return new AuditScoreResult(totalScore, totalItems, notApplicableItems, scoreSum, rating, decision);
    }

    /**
     * 総合点に基づいて評価を決定する
     * 
     * 評価基準:
     * - 優: 80点以上
     * - 良: 70点以上80点未満
     * - 可: 60点以上70点未満
     * - 不可: 60点未満
     * 
     * @param totalScore 総合点
     * @return 評価（優、良、可、不可）
     */
    public String determineRating(BigDecimal totalScore) {
        if (totalScore.compareTo(BigDecimal.valueOf(80)) >= 0) {
            return "優";
        } else if (totalScore.compareTo(BigDecimal.valueOf(70)) >= 0) {
            return "良";
        } else if (totalScore.compareTo(BigDecimal.valueOf(60)) >= 0) {
            return "可";
        } else {
            return "不可";
        }
    }

    /**
     * 総合点に基づいて判定を決定する
     * 
     * 判定基準:
     * - PASS (合格): 80点以上
     * - CONDITIONAL (是正指示): 60点以上80点未満
     * - FAIL (不合格): 60点未満
     * 
     * @param totalScore 総合点
     * @return 判定（PASS, CONDITIONAL, FAIL）
     */
    public String determineDecision(BigDecimal totalScore) {
        if (totalScore.compareTo(BigDecimal.valueOf(80)) >= 0) {
            return "PASS";
        } else if (totalScore.compareTo(BigDecimal.valueOf(60)) >= 0) {
            return "CONDITIONAL";
        } else {
            return "FAIL";
        }
    }

    /**
     * 等級を決定する（審査承認後にSupplierMaster.CurrentRatingを更新する際に使用）
     * 
     * 等級基準:
     * - 1 (優良): 80点以上
     * - 2 (良好): 70点以上80点未満
     * - 3 (標準): 60点以上70点未満
     * - null: 60点未満（購買先リスト登録不可）
     * 
     * @param totalScore 総合点
     * @return 等級（1, 2, 3, null）
     */
    public Integer determineGrade(BigDecimal totalScore) {
        if (totalScore.compareTo(BigDecimal.valueOf(80)) >= 0) {
            return 1; // 優良
        } else if (totalScore.compareTo(BigDecimal.valueOf(70)) >= 0) {
            return 2; // 良好
        } else if (totalScore.compareTo(BigDecimal.valueOf(60)) >= 0) {
            return 3; // 標準
        } else {
            return null; // 登録不可
        }
    }

    /**
     * 書類審査と実地審査の総合点を統合する
     * 
     * @param documentScore 書類審査点数（様式-2）
     * @param onSiteScore 実地審査点数（様式-5）
     * @param documentWeight 書類審査の重み（デフォルト: 0.4）
     * @param onSiteWeight 実地審査の重み（デフォルト: 0.6）
     * @return 統合総合点
     */
    public BigDecimal calculateIntegratedScore(
            BigDecimal documentScore, 
            BigDecimal onSiteScore,
            BigDecimal documentWeight,
            BigDecimal onSiteWeight) {
        
        if (documentScore == null || onSiteScore == null) {
            throw new IllegalArgumentException("Document score and on-site score cannot be null");
        }

        if (documentWeight == null) {
            documentWeight = BigDecimal.valueOf(0.4);
        }
        if (onSiteWeight == null) {
            onSiteWeight = BigDecimal.valueOf(0.6);
        }

        BigDecimal weightSum = documentWeight.add(onSiteWeight);
        if (weightSum.compareTo(BigDecimal.ONE) != 0) {
            throw new IllegalArgumentException("Sum of weights must equal 1.0");
        }

        BigDecimal integratedScore = documentScore.multiply(documentWeight)
            .add(onSiteScore.multiply(onSiteWeight))
            .setScale(2, RoundingMode.HALF_UP);

        return integratedScore;
    }

    /**
     * 次回審査予定日を計算する（初回登録日から2年後）
     * 
     * @param initialRegistrationDate 初回登録日（YYYY-MM-DD形式）
     * @return 次回審査予定日（YYYY-MM-DD形式）
     */
    public String calculateNextReviewDate(String initialRegistrationDate) {
        if (initialRegistrationDate == null || initialRegistrationDate.isEmpty()) {
            throw new IllegalArgumentException("Initial registration date cannot be null or empty");
        }

        try {
            String[] parts = initialRegistrationDate.split("-");
            int year = Integer.parseInt(parts[0]) + 2;
            String month = parts[1];
            String day = parts[2];
            
            return String.format("%04d-%s-%s", year, month, day);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid date format. Expected YYYY-MM-DD", e);
        }
    }

    /**
     * 是正期限を計算する（審査日から30日後）
     * 
     * @param auditDate 審査実施日（YYYY-MM-DD形式）
     * @return 是正期限（YYYY-MM-DD形式）
     */
    public String calculateCorrectiveActionDeadline(String auditDate) {
        if (auditDate == null || auditDate.isEmpty()) {
            throw new IllegalArgumentException("Audit date cannot be null or empty");
        }

        return auditDate + " + 30 days"; // プレースホルダー
    }

    /**
     * サンプル使用例（テスト用）
     */
    public static void main(String[] args) {
        AuditScoreCalculationService service = new AuditScoreCalculationService();

        List<AuditScoreItem> items = List.of(
            new AuditScoreItem("1", "品質マニュアルが整備されているか", 4, false, "ISO9001準拠"),
            new AuditScoreItem("2", "組織図が明確に定義されているか", 4, false, "明確"),
            new AuditScoreItem("3", "品質記録が適切に保管されているか", 4, false, "電子記録システム"),
            new AuditScoreItem("4", "検査設備の校正は適切に実施されているか", 2, false, "一部期限近い"),
            new AuditScoreItem("5", "環境管理体制が整備されているか", 0, true, "未調査")
        );

        AuditScoreResult result = service.calculateTotalScore(items);
        System.out.println(result);

        BigDecimal documentScore = BigDecimal.valueOf(87.00);
        BigDecimal onSiteScore = BigDecimal.valueOf(84.00);
        BigDecimal integratedScore = service.calculateIntegratedScore(
            documentScore, onSiteScore, 
            BigDecimal.valueOf(0.4), BigDecimal.valueOf(0.6)
        );
        System.out.println("Integrated Score: " + integratedScore);

        String nextReviewDate = service.calculateNextReviewDate("2023-11-15");
        System.out.println("Next Review Date: " + nextReviewDate);
    }
}
