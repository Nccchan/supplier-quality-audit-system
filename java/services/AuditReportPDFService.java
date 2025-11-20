package jp.co.company.sqas.service;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 審査報告書PDF生成サービス
 * Audit Report PDF Generation Service
 * 
 * 様式-8（審査結果報告書）および様式-11（実施監査結果）のPDF生成を実装
 * 
 * 実際の実装では、Apache PDFBox、iText、JasperReportsなどのライブラリを使用
 * 
 * @author Supplier Quality Audit System Development Team
 * @version 1.0
 * @since 2025-11-20
 */
public class AuditReportPDFService {

    private static final String COMPANY_NAME = "株式会社○○製作所";
    private static final String COMPANY_LOGO_PATH = "/path/to/company_logo.png";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy年MM月dd日");

    /**
     * 審査結果報告書データクラス（様式-8）
     */
    public static class AuditReportData {
        private String auditId;
        private String supplierName;
        private String supplierAddress;
        private LocalDate auditDate;
        private String auditorName;
        private String auditorDepartment;
        private String auditType;
        private BigDecimal documentAuditScore;
        private BigDecimal onSiteAuditScore;
        private BigDecimal totalScore;
        private String rating;
        private String decision;
        private Integer grade;
        private String comments;
        private List<ScoreDetail> scoreDetails;
        private String approverName;
        private LocalDate approvalDate;

        public AuditReportData(String auditId, String supplierName, String supplierAddress,
                             LocalDate auditDate, String auditorName, String auditorDepartment,
                             String auditType, BigDecimal documentAuditScore, BigDecimal onSiteAuditScore,
                             BigDecimal totalScore, String rating, String decision, Integer grade,
                             String comments, List<ScoreDetail> scoreDetails,
                             String approverName, LocalDate approvalDate) {
            this.auditId = auditId;
            this.supplierName = supplierName;
            this.supplierAddress = supplierAddress;
            this.auditDate = auditDate;
            this.auditorName = auditorName;
            this.auditorDepartment = auditorDepartment;
            this.auditType = auditType;
            this.documentAuditScore = documentAuditScore;
            this.onSiteAuditScore = onSiteAuditScore;
            this.totalScore = totalScore;
            this.rating = rating;
            this.decision = decision;
            this.grade = grade;
            this.comments = comments;
            this.scoreDetails = scoreDetails;
            this.approverName = approverName;
            this.approvalDate = approvalDate;
        }

        public String getAuditId() { return auditId; }
        public String getSupplierName() { return supplierName; }
        public String getSupplierAddress() { return supplierAddress; }
        public LocalDate getAuditDate() { return auditDate; }
        public String getAuditorName() { return auditorName; }
        public String getAuditorDepartment() { return auditorDepartment; }
        public String getAuditType() { return auditType; }
        public BigDecimal getDocumentAuditScore() { return documentAuditScore; }
        public BigDecimal getOnSiteAuditScore() { return onSiteAuditScore; }
        public BigDecimal getTotalScore() { return totalScore; }
        public String getRating() { return rating; }
        public String getDecision() { return decision; }
        public Integer getGrade() { return grade; }
        public String getComments() { return comments; }
        public List<ScoreDetail> getScoreDetails() { return scoreDetails; }
        public String getApproverName() { return approverName; }
        public LocalDate getApprovalDate() { return approvalDate; }
    }

    /**
     * 採点詳細クラス
     */
    public static class ScoreDetail {
        private int questionNumber;
        private String questionText;
        private Integer score;
        private boolean isNotApplicable;
        private String remarks;

        public ScoreDetail(int questionNumber, String questionText, Integer score, 
                         boolean isNotApplicable, String remarks) {
            this.questionNumber = questionNumber;
            this.questionText = questionText;
            this.score = score;
            this.isNotApplicable = isNotApplicable;
            this.remarks = remarks;
        }

        public int getQuestionNumber() { return questionNumber; }
        public String getQuestionText() { return questionText; }
        public Integer getScore() { return score; }
        public boolean isNotApplicable() { return isNotApplicable; }
        public String getRemarks() { return remarks; }
    }

    /**
     * 様式-8（審査結果報告書）のPDFを生成
     * 
     * @param reportData 審査結果データ
     * @param outputPath 出力ファイルパス
     * @return 生成されたPDFファイルのパス
     */
    public String generateForm8PDF(AuditReportData reportData, String outputPath) throws IOException {
        System.out.println("=== Generating Form-8 (Audit Result Report) PDF ===");
        
        
        StringBuilder pdfContent = new StringBuilder();
        pdfContent.append("==============================================\n");
        pdfContent.append("         審査結果報告書（様式-8）           \n");
        pdfContent.append("==============================================\n\n");
        
        pdfContent.append("会社名: ").append(COMPANY_NAME).append("\n");
        pdfContent.append("報告日: ").append(LocalDate.now().format(DATE_FORMATTER)).append("\n\n");
        
        pdfContent.append("【基本情報】\n");
        pdfContent.append("審査ID: ").append(reportData.getAuditId()).append("\n");
        pdfContent.append("購買先名: ").append(reportData.getSupplierName()).append("\n");
        pdfContent.append("購買先住所: ").append(reportData.getSupplierAddress()).append("\n");
        pdfContent.append("審査実施日: ").append(reportData.getAuditDate().format(DATE_FORMATTER)).append("\n");
        pdfContent.append("審査種別: ").append(getAuditTypeText(reportData.getAuditType())).append("\n\n");
        
        pdfContent.append("【審査員情報】\n");
        pdfContent.append("審査員名: ").append(reportData.getAuditorName()).append("\n");
        pdfContent.append("所属部門: ").append(reportData.getAuditorDepartment()).append("\n\n");
        
        pdfContent.append("【審査結果】\n");
        if (reportData.getDocumentAuditScore() != null) {
            pdfContent.append("書類審査点数: ").append(reportData.getDocumentAuditScore()).append("点\n");
        }
        if (reportData.getOnSiteAuditScore() != null) {
            pdfContent.append("実地審査点数: ").append(reportData.getOnSiteAuditScore()).append("点\n");
        }
        pdfContent.append("総合点: ").append(reportData.getTotalScore()).append("点\n");
        pdfContent.append("評価: ").append(reportData.getRating()).append("\n");
        pdfContent.append("判定: ").append(getDecisionText(reportData.getDecision())).append("\n");
        if (reportData.getGrade() != null) {
            pdfContent.append("等級: ").append(reportData.getGrade()).append(" (").append(getGradeText(reportData.getGrade())).append(")\n");
        }
        pdfContent.append("\n");
        
        pdfContent.append("【採点詳細】\n");
        pdfContent.append("No. | 審査項目 | 評価点 | 備考\n");
        pdfContent.append("----+----------+--------+------\n");
        for (ScoreDetail detail : reportData.getScoreDetails()) {
            pdfContent.append(String.format("%3d | %-30s | %6s | %s\n",
                detail.getQuestionNumber(),
                detail.getQuestionText(),
                detail.isNotApplicable() ? "未調査" : detail.getScore() + "点",
                detail.getRemarks() != null ? detail.getRemarks() : ""
            ));
        }
        pdfContent.append("\n");
        
        pdfContent.append("【総合所見】\n");
        pdfContent.append(reportData.getComments() != null ? reportData.getComments() : "特記事項なし");
        pdfContent.append("\n\n");
        
        pdfContent.append("【承認情報】\n");
        pdfContent.append("承認者: ").append(reportData.getApproverName()).append("\n");
        pdfContent.append("承認日: ").append(reportData.getApprovalDate().format(DATE_FORMATTER)).append("\n\n");
        
        pdfContent.append("==============================================\n");
        pdfContent.append("※ このレポートはGCP0602に準拠して作成されました\n");
        pdfContent.append("==============================================\n");
        
        try (FileOutputStream fos = new FileOutputStream(outputPath)) {
            fos.write(pdfContent.toString().getBytes("UTF-8"));
        }
        
        System.out.println("PDF generated successfully: " + outputPath);
        return outputPath;
    }

    /**
     * 様式-11（実施監査結果）のPDFを生成
     * 
     * @param reportData 監査結果データ
     * @param outputPath 出力ファイルパス
     * @return 生成されたPDFファイルのパス
     */
    public String generateForm11PDF(AuditReportData reportData, String outputPath) throws IOException {
        System.out.println("=== Generating Form-11 (On-site Audit Result) PDF ===");
        
        StringBuilder pdfContent = new StringBuilder();
        pdfContent.append("==============================================\n");
        pdfContent.append("        実施監査結果（様式-11）            \n");
        pdfContent.append("==============================================\n\n");
        
        pdfContent.append("会社名: ").append(COMPANY_NAME).append("\n");
        pdfContent.append("報告日: ").append(LocalDate.now().format(DATE_FORMATTER)).append("\n\n");
        
        pdfContent.append("【基本情報】\n");
        pdfContent.append("審査ID: ").append(reportData.getAuditId()).append("\n");
        pdfContent.append("購買先名: ").append(reportData.getSupplierName()).append("\n");
        pdfContent.append("購買先住所: ").append(reportData.getSupplierAddress()).append("\n");
        pdfContent.append("監査実施日: ").append(reportData.getAuditDate().format(DATE_FORMATTER)).append("\n");
        pdfContent.append("監査種別: 定期再評価\n\n");
        
        pdfContent.append("【監査員情報】\n");
        pdfContent.append("監査員名: ").append(reportData.getAuditorName()).append("\n");
        pdfContent.append("所属部門: ").append(reportData.getAuditorDepartment()).append("\n\n");
        
        pdfContent.append("【監査結果】\n");
        pdfContent.append("実地監査点数: ").append(reportData.getOnSiteAuditScore()).append("点\n");
        pdfContent.append("総合点: ").append(reportData.getTotalScore()).append("点\n");
        pdfContent.append("評価: ").append(reportData.getRating()).append("\n");
        pdfContent.append("判定: ").append(getDecisionText(reportData.getDecision())).append("\n");
        if (reportData.getGrade() != null) {
            pdfContent.append("更新後等級: ").append(reportData.getGrade()).append(" (").append(getGradeText(reportData.getGrade())).append(")\n");
        }
        pdfContent.append("\n");
        
        pdfContent.append("【監査項目評価】\n");
        pdfContent.append("No. | 監査項目 | 評価点 | 備考\n");
        pdfContent.append("----+----------+--------+------\n");
        for (ScoreDetail detail : reportData.getScoreDetails()) {
            pdfContent.append(String.format("%3d | %-30s | %6s | %s\n",
                detail.getQuestionNumber(),
                detail.getQuestionText(),
                detail.isNotApplicable() ? "未調査" : detail.getScore() + "点",
                detail.getRemarks() != null ? detail.getRemarks() : ""
            ));
        }
        pdfContent.append("\n");
        
        pdfContent.append("【監査所見】\n");
        pdfContent.append(reportData.getComments() != null ? reportData.getComments() : "特記事項なし");
        pdfContent.append("\n\n");
        
        pdfContent.append("【承認情報】\n");
        pdfContent.append("承認者: ").append(reportData.getApproverName()).append("\n");
        pdfContent.append("承認日: ").append(reportData.getApprovalDate().format(DATE_FORMATTER)).append("\n\n");
        
        pdfContent.append("==============================================\n");
        pdfContent.append("※ このレポートはGCP0602に準拠して作成されました\n");
        pdfContent.append("==============================================\n");
        
        try (FileOutputStream fos = new FileOutputStream(outputPath)) {
            fos.write(pdfContent.toString().getBytes("UTF-8"));
        }
        
        System.out.println("PDF generated successfully: " + outputPath);
        return outputPath;
    }

    /**
     * 審査種別テキストを取得
     */
    private String getAuditTypeText(String auditType) {
        switch (auditType) {
            case "NEW": return "新規審査";
            case "PERIODIC": return "定期再評価";
            case "SPECIAL": return "特別審査";
            default: return auditType;
        }
    }

    /**
     * 判定テキストを取得
     */
    private String getDecisionText(String decision) {
        switch (decision) {
            case "PASS": return "合格";
            case "CONDITIONAL": return "是正指示";
            case "FAIL": return "不合格";
            default: return decision;
        }
    }

    /**
     * 等級テキストを取得
     */
    private String getGradeText(Integer grade) {
        if (grade == null) return "未評価";
        switch (grade) {
            case 1: return "優良";
            case 2: return "良好";
            case 3: return "標準";
            default: return "不明";
        }
    }

    /**
     * サンプル使用例（テスト用）
     */
    public static void main(String[] args) {
        AuditReportPDFService service = new AuditReportPDFService();

        List<ScoreDetail> scoreDetails = List.of(
            new ScoreDetail(1, "品質マニュアルが整備されているか", 4, false, "適切に整備されている"),
            new ScoreDetail(2, "組織図が明確に定義されているか", 4, false, ""),
            new ScoreDetail(3, "品質記録が適切に保管されているか", 2, false, "一部記録に不備あり"),
            new ScoreDetail(4, "文書管理規定が整備されているか", 4, false, ""),
            new ScoreDetail(5, "内部監査が定期的に実施されているか", 4, false, "")
        );

        AuditReportData reportData = new AuditReportData(
            "AUD-20251120-001",
            "株式会社優良部品製作所",
            "東京都千代田区千代田1-1-1",
            LocalDate.of(2025, 11, 20),
            "品質太郎",
            "品質保証部",
            "NEW",
            BigDecimal.valueOf(85.50),
            BigDecimal.valueOf(88.00),
            BigDecimal.valueOf(87.00),
            "優",
            "PASS",
            1,
            "全体的に品質管理体制が整っており、優良な購買先として評価できる。",
            scoreDetails,
            "品質部長",
            LocalDate.of(2025, 11, 21)
        );

        try {
            String form8Path = "/tmp/audit_report_form8.txt";
            service.generateForm8PDF(reportData, form8Path);
            System.out.println("Form-8 generated: " + form8Path);

            String form11Path = "/tmp/audit_report_form11.txt";
            service.generateForm11PDF(reportData, form11Path);
            System.out.println("Form-11 generated: " + form11Path);

        } catch (IOException e) {
            System.err.println("Error generating PDF: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
