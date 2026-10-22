package edu.itc.salesreport.ingest;

import edu.itc.salesreport.model.SaleTransaction;
import java.io.IOException;
import java.nio.file.*;
import java.time.YearMonth;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public final class CsvMonthReader {
    private static final Pattern FILE_PATTERN = Pattern.compile("(PNH|REP|BTB)-\\d{4}-\\d{2}-\\d{2}\\.csv");
    private final TransactionParser parser;

    public CsvMonthReader(TransactionParser parser) {
        this.parser = parser;
    }

    public MonthData read(Path dataDir, YearMonth month) throws IOException {
        Path monthDir = dataDir.resolve(month.toString());
        if (!Files.isDirectory(monthDir)) {
            throw new NoSuchFileException(monthDir.toString());
        }

        List<SaleTransaction> rows = new ArrayList<>();
        List<String> rejected = new ArrayList<>();

        try (Stream<Path> paths = Files.list(monthDir)) {
            List<Path> files = paths
                    .filter(Files::isRegularFile)
                    .filter(p -> FILE_PATTERN.matcher(p.getFileName().toString()).matches())
                    .sorted(Comparator.comparing(p -> p.getFileName().toString()))
                    .toList();

            for (Path file : files) {
                List<String> lines = Files.readAllLines(file);
                if (lines.isEmpty()) continue;
                for (int i = 1; i < lines.size(); i++) {
                    String line = lines.get(i);
                    int lineNo = i + 1;
                    try {
                        rows.add(parser.parse(line, lineNo));
                    } catch (InvalidRowException e) {
                        rejected.add("%s:%d %s".formatted(file.getFileName(), lineNo, e.getMessage()));
                    }
                }
            }
        }
        return new MonthData(month, rows, rejected);
    }
}