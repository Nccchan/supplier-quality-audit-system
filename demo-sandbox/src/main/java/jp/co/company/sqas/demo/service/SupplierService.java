package jp.co.company.sqas.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class SupplierService {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    public List<Map<String, Object>> getAllSuppliers() {
        String sql = "SELECT " +
                "SupplierID, " +
                "CompanyName, " +
                "InitialRegistrationDate, " +
                "CurrentRating, " +
                "NextReviewDate, " +
                "ISO9001Certified, " +
                "ISO9001ExpiryDate, " +
                "SupplierStatus " +
                "FROM SupplierMaster " +
                "ORDER BY NextReviewDate ASC NULLS LAST";
        
        return jdbcTemplate.queryForList(sql);
    }
}
