package jp.co.company.sqas.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BatchJobService {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    private JavaMailSender mailSender;
    
    /**
     * 再評価リマインダーバッチ
     * NextReviewDateが30日以内の購買先に通知
     */
    public int runReviewReminderBatch() {
        LocalDate today = LocalDate.now();
        LocalDate reminderDate = today.plusDays(30);
        
        String sql = "SELECT SupplierID, CompanyName, NextReviewDate " +
                "FROM SupplierMaster " +
                "WHERE NextReviewDate <= ? AND NextReviewDate >= ? " +
                "AND SupplierStatus = 'ACTIVE'";
        
        List<Map<String, Object>> suppliers = jdbcTemplate.queryForList(
                sql, reminderDate, today);
        
        for (Map<String, Object> supplier : suppliers) {
            sendReviewReminderEmail(supplier);
        }
        
        System.out.println("再評価リマインダー送信: " + suppliers.size() + "件");
        return suppliers.size();
    }
    
    /**
     * 是正処置リマインダーバッチ
     * CorrectiveActionDeadlineが3日以内または超過の是正処置に通知
     */
    public Map<String, Integer> runCorrectiveActionReminderBatch() {
        LocalDate today = LocalDate.now();
        LocalDate warningDate = today.plusDays(3);
        
        String warningSql = "SELECT c.CorrectiveActionID, c.ActionRequiredDetails, " +
                "c.CorrectiveActionDeadline, s.CompanyName " +
                "FROM CorrectiveActionHistory c " +
                "JOIN AuditHistory a ON c.AuditID = a.AuditID " +
                "JOIN SupplierMaster s ON a.SupplierID = s.SupplierID " +
                "WHERE c.CorrectiveActionDeadline <= ? " +
                "AND c.CorrectiveActionDeadline >= ? " +
                "AND c.CorrectiveActionStatus = 'PENDING'";
        
        List<Map<String, Object>> warnings = jdbcTemplate.queryForList(
                warningSql, warningDate, today);
        
        for (Map<String, Object> action : warnings) {
            sendCorrectiveActionWarningEmail(action);
        }
        
        String overdueSql = "SELECT c.CorrectiveActionID, c.ActionRequiredDetails, " +
                "c.CorrectiveActionDeadline, s.CompanyName " +
                "FROM CorrectiveActionHistory c " +
                "JOIN AuditHistory a ON c.AuditID = a.AuditID " +
                "JOIN SupplierMaster s ON a.SupplierID = s.SupplierID " +
                "WHERE c.CorrectiveActionDeadline < ? " +
                "AND c.CorrectiveActionStatus = 'PENDING'";
        
        List<Map<String, Object>> overdue = jdbcTemplate.queryForList(
                overdueSql, today);
        
        for (Map<String, Object> action : overdue) {
            sendCorrectiveActionOverdueEmail(action);
            
            jdbcTemplate.update(
                    "UPDATE CorrectiveActionHistory SET CorrectiveActionStatus = 'OVERDUE' " +
                    "WHERE CorrectiveActionID = ?",
                    action.get("correctiveactionid"));
        }
        
        System.out.println("是正処置警告送信: " + warnings.size() + "件");
        System.out.println("是正処置期限超過通知送信: " + overdue.size() + "件");
        
        Map<String, Integer> result = new HashMap<>();
        result.put("warnings", warnings.size());
        result.put("overdue", overdue.size());
        return result;
    }
    
    private void sendReviewReminderEmail(Map<String, Object> supplier) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("sqas-system@demo.example.com");
        message.setTo("qa-manager@demo.example.com", "procurement-manager@demo.example.com");
        message.setSubject("[SQAS] 再評価リマインダー: " + supplier.get("companyname"));
        message.setText(String.format(
                "購買先の再評価期限が近づいています。\n\n" +
                "購買先ID: %s\n" +
                "会社名: %s\n" +
                "再評価予定日: %s\n\n" +
                "再評価プロセスを開始してください。",
                supplier.get("supplierid"),
                supplier.get("companyname"),
                supplier.get("nextreviewdate")
        ));
        
        mailSender.send(message);
    }
    
    private void sendCorrectiveActionWarningEmail(Map<String, Object> action) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("sqas-system@demo.example.com");
        message.setTo("qa-manager@demo.example.com");
        message.setSubject("[SQAS] 是正処置期限警告: " + action.get("companyname"));
        message.setText(String.format(
                "是正処置の期限が近づいています。\n\n" +
                "会社名: %s\n" +
                "是正処置ID: %s\n" +
                "期限: %s\n" +
                "内容: %s\n\n" +
                "期限内に是正処置を完了してください。",
                action.get("companyname"),
                action.get("correctiveactionid"),
                action.get("correctiveactiondeadline"),
                action.get("actionrequireddetails")
        ));
        
        mailSender.send(message);
    }
    
    private void sendCorrectiveActionOverdueEmail(Map<String, Object> action) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("sqas-system@demo.example.com");
        message.setTo("qa-manager@demo.example.com");
        message.setSubject("[SQAS] 【緊急】是正処置期限超過: " + action.get("companyname"));
        message.setText(String.format(
                "是正処置の期限が超過しています。\n\n" +
                "会社名: %s\n" +
                "是正処置ID: %s\n" +
                "期限: %s\n" +
                "内容: %s\n\n" +
                "至急対応してください。",
                action.get("companyname"),
                action.get("correctiveactionid"),
                action.get("correctiveactiondeadline"),
                action.get("actionrequireddetails")
        ));
        
        mailSender.send(message);
    }
}
