package edu.itc.salesreport.ingest;

import edu.itc.salesreport.model.SaleTransaction;

public interface TransactionParser {
    SaleTransaction parse(String line, int lineNo) throws InvalidRowException;
}