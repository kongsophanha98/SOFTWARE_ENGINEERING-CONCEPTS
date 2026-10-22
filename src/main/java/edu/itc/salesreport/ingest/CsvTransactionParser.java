package edu.itc.salesreport.ingest;

import edu.itc.salesreport.model.PaymentMethod;
import edu.itc.salesreport.model.SaleTransaction;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Set;

public final class CsvTransactionParser implements TransactionParser {
    private static final Set<String> BRANCHES = Set.of("PNH", "REP", "BTB");
    private static final Set<String> CATEGORIES = Set.of("Grocery", "Beverages", "Household", "Personal Care", "Electronics", "Stationery");

    @Override
    public SaleTransaction parse(String line, int lineNo) throws InvalidRowException {
        if (line == null || line.isBlank()) {
            throw new InvalidRowException("Empty line");
        }
        String[] p = line.split(",", -1);
        if (p.length != 10) {
            throw new InvalidRowException("Expected 10 fields, got " + p.length);
        }
        String branch = p[0].strip();
        if (!BRANCHES.contains(branch)) {
            throw new InvalidRowException("Unknown branch: " + branch);
        }
        LocalDate date;
        try {
            date = LocalDate.parse(p[1].strip());
        } catch (DateTimeParseException e) {
            throw new InvalidRowException("Invalid date: " + p[1]);
        }
        String receiptNo = p[2].strip();
        String sku = p[3].strip();
        String productName = p[4].strip();
        String category = p[5].strip();
        if (!CATEGORIES.contains(category)) {
            throw new InvalidRowException("Unknown category: " + category);
        }
        int qty;
        try {
            qty = Integer.parseInt(p[6].strip());
        } catch (NumberFormatException e) {
            throw new InvalidRowException("Invalid quantity: " + p[6]);
        }
        if (qty <= 0) {
            throw new InvalidRowException("quantity must be positive: " + qty);
        }
        BigDecimal unitPrice;
        try {
            unitPrice = new BigDecimal(p[7].strip()).setScale(2, RoundingMode.HALF_UP);
        } catch (Exception e) {
            throw new InvalidRowException("Invalid unit price: " + p[7]);
        }
        if (unitPrice.signum() < 0) {
            throw new InvalidRowException("Amount cannot be negative");
        }
        BigDecimal discount;
        try {
            discount = new BigDecimal(p[8].strip()).setScale(2, RoundingMode.HALF_UP);
        } catch (Exception e) {
            throw new InvalidRowException("Invalid discount: " + p[8]);
        }
        if (discount.signum() < 0) {
            throw new InvalidRowException("Amount cannot be negative");
        }
        BigDecimal gross = unitPrice.multiply(BigDecimal.valueOf(qty));
        if (discount.compareTo(gross) > 0) {
            throw new InvalidRowException("Discount exceeds line amount");
        }
        PaymentMethod paymentMethod;
        try {
            paymentMethod = PaymentMethod.valueOf(p[9].strip());
        } catch (IllegalArgumentException e) {
            throw new InvalidRowException("Unknown payment method: " + p[9]);
        }
        return new SaleTransaction(branch, date, receiptNo, sku, productName, category, qty, unitPrice, discount, paymentMethod);
    }
}