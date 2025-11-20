package jp.co.company.sqas.demo.controller;

import jp.co.company.sqas.demo.service.AuditScoreService;
import jp.co.company.sqas.demo.service.SupplierService;
import jp.co.company.sqas.demo.service.BatchJobService;
import jp.co.company.sqas.demo.model.ScoreCalculationRequest;
import jp.co.company.sqas.demo.model.ScoreCalculationResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiController {
    
    @Autowired
    private AuditScoreService auditScoreService;
    
    @Autowired
    private SupplierService supplierService;
    
    @Autowired
    private BatchJobService batchJobService;
    
    @GetMapping("/suppliers")
    public ResponseEntity<?> getSuppliers() {
        return ResponseEntity.ok(supplierService.getAllSuppliers());
    }
    
    @PostMapping("/calculate-score")
    public ResponseEntity<ScoreCalculationResult> calculateScore(
            @RequestBody ScoreCalculationRequest request) {
        ScoreCalculationResult result = auditScoreService.calculateScore(request);
        return ResponseEntity.ok(result);
    }
    
    @PostMapping("/batch/review-reminder")
    public ResponseEntity<?> runReviewReminderBatch() {
        int count = batchJobService.runReviewReminderBatch();
        return ResponseEntity.ok(Map.of(
            "status", "success",
            "message", "再評価リマインダーバッチを実行しました",
            "notificationsSent", count
        ));
    }
    
    @PostMapping("/batch/corrective-action-reminder")
    public ResponseEntity<?> runCorrectiveActionReminderBatch() {
        Map<String, Integer> result = batchJobService.runCorrectiveActionReminderBatch();
        return ResponseEntity.ok(Map.of(
            "status", "success",
            "message", "是正処置リマインダーバッチを実行しました",
            "warningsSent", result.get("warnings"),
            "overdueNotificationsSent", result.get("overdue")
        ));
    }
    
    @PostMapping("/generate-pdf")
    public ResponseEntity<?> generatePdf(@RequestBody Map<String, Object> request) {
        String formType = (String) request.get("formType");
        String auditId = (String) request.get("auditId");
        
        String pdfPath = auditScoreService.generatePdf(formType, auditId);
        
        return ResponseEntity.ok(Map.of(
            "status", "success",
            "message", "PDFを生成しました",
            "pdfPath", pdfPath
        ));
    }
}
