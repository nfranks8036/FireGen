package net.noahf.firegen.discord.utilities.ansi;

import lombok.AllArgsConstructor;
import net.noahf.firegen.discord.utilities.ImmutablePair;

import java.util.*;

public class AnsiTableBuilder {

    public static final int NO_MAXIMUM = -1;

    @AllArgsConstructor
    private static class Row {
        String[] cols;
        AnsiColor[] colors;
        long sortValue;
    }

    private List<Row> rows = new ArrayList<>();
    private final Set<Integer> noDuplicateColumns = new HashSet<>();
    private String[] header;
    private int maximumRows = NO_MAXIMUM;

    public AnsiTableBuilder header(String... cols) {
        this.header = cols;
        return this;
    }

    public AnsiTableBuilder row(String... cols) {
        return row(null, cols);
    }

    public AnsiTableBuilder row(AnsiColor color, String... cols) {
        return row(Long.MIN_VALUE, color, cols);
    }

    public AnsiTableBuilder row(long sortedValue, AnsiColor color, String... cols) {
        return this.row(sortedValue, new AnsiColor[] {color}, cols);
    }

    public AnsiTableBuilder row(long sortedValue, AnsiColor[] color, String... cols) {
        rows.add(new Row(cols, color, (sortedValue != Long.MIN_VALUE ? sortedValue : rows.size())));
        return this;
    }

    public AnsiTableBuilder disallowDuplicateOnColumn(int index) {
        if (index >= header.length || index < 0) {
            throw new ArrayIndexOutOfBoundsException("Expected header index to be within bounds '" + header.length + "', got '" + index + "'");
        }

        noDuplicateColumns.add(index);
        return this;
    }

    public AnsiTableBuilder maximumRows(int maximumRows) {
        if (maximumRows < -1) {
            throw new IllegalArgumentException("Maximum rows cannot be below -1, got '" + maximumRows + "'");
        }
        this.maximumRows = maximumRows;
        return this;
    }

    public ImmutablePair<String, Integer> build() {
        return this.build(Integer.MAX_VALUE);
    }

    public ImmutablePair<String, Integer> build(int limit) {
        if (header == null) {
            throw new IllegalStateException("Header must be set");
        }

        int colCount = header.length;
        int[] widths = new int[colCount];

        rows.sort(Comparator.comparingLong((Row a) -> a.sortValue).reversed());

        if (!noDuplicateColumns.isEmpty()) {
            Map<String, Row> unique = new LinkedHashMap<>();

            for (Row r : rows) {
                String key = buildKey(r.cols);
                unique.putIfAbsent(key, r); // keeps newest because sorted
            }

            rows = new ArrayList<>(unique.values());
        }

        updateWidths(widths, header);

        for (Row r : rows) {
            updateWidths(widths, r.cols);
        }

        StringBuilder sb = new StringBuilder();

        sb.append(formatRow(header, widths)).append("\n");

        sb.repeat("-", totalWidth(widths)).append("\n");

        int index = 0;
        for (Row r : rows) {
            if (index > limit) {
                break;
            }

            if (index > maximumRows) {
                break;
            }

            sb.append(formatRow(r.cols, widths, r.colors)).append("\n");
            index++;
        }

        return ImmutablePair.of(sb.toString(), index);
    }

    private String buildKey(String[] cols) {
        if (noDuplicateColumns.isEmpty()) return null;

        StringBuilder sb = new StringBuilder();
        for (int i : noDuplicateColumns) {
            sb.append(cols[i]).append('|');
        }
        return sb.toString();
    }

    private String formatRow(String[] row, int[] widths, AnsiColor... colors) {
        StringBuilder content = new StringBuilder();

        for (int i = 0; i < widths.length; i++) {
            String value = i < row.length && row[i] != null ? row[i] : "";
            content.append(padRight(value, widths[i])).append(" | ");
        }

        String line = content.toString();

        if (colors.length > 0) {
            for (AnsiColor c : colors) {
                line = c.wrap(line);
            }
            return line;
        }

        return line;
    }

    private void updateWidths(int[] widths, String[] row) {
        for (int i = 0; i < row.length; i++) {
            if (row[i] != null) {
                widths[i] = Math.max(widths[i], stripAnsi(row[i]).length());
            }
        }
    }

    private String padRight(String s, int width) {
        s = s == null ? "" : s;
        int len = stripAnsi(s).length();
        return s + " ".repeat(Math.max(0, width - len));
    }

    private int totalWidth(int[] widths) {
        int sum = 0;
        for (int w : widths) sum += w + 3; // " | "
        return sum + 1;
    }

    private String stripAnsi(String s) {
        return s.replaceAll("\u001B\\[[;\\d]*m", "");
    }

}