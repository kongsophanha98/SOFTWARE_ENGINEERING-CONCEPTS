package edu.itc.salesreport.model;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record BranchSummary(
        String branch,
        BigDecimal revenue,
        BigDecimal discounts,
        int receipts,
        BigDecimal averageBasket,
        Map<String, BigDecimal> revenueByCategory,
        Map<PaymentMethod, BigDecimal> revenueByPayment,
        List<ProductTotal> topProducts
) {}