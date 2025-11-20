package jp.co.company.sqas.demo.model;

import lombok.Data;
import java.util.List;

@Data
public class ScoreCalculationRequest {
    private List<ItemScore> scores;
    
    @Data
    public static class ItemScore {
        private String itemId;
        private Integer score; // 4, 2, 0, or null (未調査)
    }
}
