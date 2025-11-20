package jp.co.company.sqas.demo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScoreCalculationResult {
    private BigDecimal totalScore;
    private String rating;        // 優/良/可/不可
    private String decision;      // 合格/是正指示/不合格
    private Integer grade;        // 1/2/3
    private int totalItems;
    private int notApplicableItems;
    private int scoreSum;
    private String formula;
}
