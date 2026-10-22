package edu.itc.salesreport.ui;

import java.time.YearMonth;
import java.util.List;

public sealed interface SalesEvent {
    record MonthLoaded(YearMonth month, int rows, List<String> rejected) implements SalesEvent {}
    record BranchSelected(String branch) implements SalesEvent {}
}