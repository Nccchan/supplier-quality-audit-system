package jp.co.company.sqas.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    
    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("title", "購買先品質審査管理システム - デモ環境");
        return "index";
    }
    
    @GetMapping("/suppliers")
    public String suppliers(Model model) {
        model.addAttribute("title", "購買先マスター一覧");
        return "suppliers";
    }
    
    @GetMapping("/audit-scoring")
    public String auditScoring(Model model) {
        model.addAttribute("title", "審査採点フォーム");
        return "audit-scoring";
    }
    
    @GetMapping("/workflow-simulator")
    public String workflowSimulator(Model model) {
        model.addAttribute("title", "ワークフローシミュレーター");
        return "workflow-simulator";
    }
}
