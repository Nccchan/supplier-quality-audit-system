package jp.co.company.sqas.demo.service;

import jp.co.company.sqas.demo.model.ScoreCalculationRequest;
import jp.co.company.sqas.demo.model.ScoreCalculationResult;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class AuditScoreService {
    
    /**
     * GCP0602準拠のスコア計算
     * 計算式: (評価点合計 × 100) / (4 × 総項目数 - 4 × 未調査項目数)
     */
    public ScoreCalculationResult calculateScore(ScoreCalculationRequest request) {
        int totalItems = request.getScores().size();
        int notApplicableItems = 0;
        int scoreSum = 0;
        
        for (ScoreCalculationRequest.ItemScore item : request.getScores()) {
            if (item.getScore() == null) {
                notApplicableItems++;
            } else {
                scoreSum += item.getScore();
            }
        }
        
        int denominator = (4 * totalItems) - (4 * notApplicableItems);
        
        BigDecimal totalScore;
        if (denominator == 0) {
            totalScore = BigDecimal.ZERO;
        } else {
            totalScore = BigDecimal.valueOf(scoreSum)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(denominator), 2, RoundingMode.HALF_UP);
        }
        
        String rating = determineRating(totalScore);
        
        String decision = determineDecision(totalScore);
        
        Integer grade = determineGrade(totalScore);
        
        String formula = String.format("(%d × 100) / (4 × %d - 4 × %d) = %.2f",
                scoreSum, totalItems, notApplicableItems, totalScore);
        
        return new ScoreCalculationResult(
                totalScore, rating, decision, grade,
                totalItems, notApplicableItems, scoreSum, formula
        );
    }
    
    private String determineRating(BigDecimal totalScore) {
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
    
    private String determineDecision(BigDecimal totalScore) {
        if (totalScore.compareTo(BigDecimal.valueOf(80)) >= 0) {
            return "合格";
        } else if (totalScore.compareTo(BigDecimal.valueOf(60)) >= 0) {
            return "是正指示";
        } else {
            return "不合格";
        }
    }
    
    private Integer determineGrade(BigDecimal totalScore) {
        if (totalScore.compareTo(BigDecimal.valueOf(80)) >= 0) {
            return 1; // 優良
        } else if (totalScore.compareTo(BigDecimal.valueOf(70)) >= 0) {
            return 2; // 良好
        } else {
            return 3; // 標準
        }
    }
    
    public String generatePdf(String formType, String auditId) {
        String filename = String.format("demo-output/%s_%s.pdf", formType, auditId);
        System.out.println("PDF生成: " + filename);
        return filename;
    }
}
