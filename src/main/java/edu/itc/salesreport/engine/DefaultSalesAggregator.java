package edu.itc.salesreport.engine;

import edu.itc.salesreport.model.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public final class DefaultSalesAggregator implements SalesAggregator {
    @Override
    public BranchSummary summarize(String branchName, List<SaleTransaction> rows) {
        BigDecimal totalRevenue = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalDiscounts = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        Set<String> receipts = new HashSet<>();
        Map<String, BigDecimal> categoryRevenue = new TreeMap<>();
        Map<PaymentMethod, BigDecimal> paymentRevenue = new EnumMap<>(PaymentMethod.class);
        Map<String, ProductAccumulator> productMap = new HashMap<>();

        for (SaleTransaction t : rows) {
            BigDecimal rev = t.revenue();
            totalRevenue = totalRevenue.add(rev);
            totalDiscounts = totalDiscounts.add(t.discount());
            receipts.add(t.receiptNo());

            categoryRevenue.merge(t.category(), rev, BigDecimal::add);
            paymentRevenue.merge(t.paymentMethod(), rev, BigDecimal::add);

            productMap.computeIfAbsent(t.sku(), k -> new ProductAccumulator(t.sku(), t.productName()))
                    .add(rev);
        }

        int receiptCount = receipts.size();
        BigDecimal avgBasket = receiptCount == 0 ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                : totalRevenue.divide(BigDecimal.valueOf(receiptCount), 2, RoundingMode.HALF_UP);

        List<ProductTotal> topProducts = productMap.values().stream()
                .sorted(Comparator.comparing(ProductAccumulator::revenue).reversed()
                        .thenComparing(ProductAccumulator::sku))
                .limit(10)
                .map(p -> new ProductTotal(p.sku, p.productName, p.revenue))
                .toList();

        return new BranchSummary(
                branchName,
                totalRevenue,
                totalDiscounts,
                receiptCount,
                avgBasket,
                categoryRevenue,
                paymentRevenue,
                topProducts
        );
    }

    private static final class ProductAccumulator {
        final String sku;
        final String productName;
        BigDecimal revenue = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        ProductAccumulator(String sku, String productName) {
            this.sku = sku;
            this.productName = productName;
        }

        void add(BigDecimal amount) {
            revenue = revenue.add(amount);
        }

        BigDecimal revenue() { return revenue; }
        String sku() { return sku; }
    }
}