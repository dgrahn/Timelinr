package us.grahn.crf;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class CRFData implements Iterable<List<String>> {

    private final List<String> columns = new ArrayList<>();
    private final List<List<String>> data = new ArrayList<>();

    public CRFData() {

    }

    public CRFData(final File file) throws FileNotFoundException, IOException {

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            for (String line; (line = br.readLine()) != null;) {
            	if (line.trim().isEmpty()) continue;
                data.add(Arrays.asList(line.split("\\s+")));
            }
        }

        resetColumns();
    }

    public CRFData(final List<List<String>> data) {
        this.data.addAll(data);
        resetColumns();
    }

    public void addRow(final String... row) {
        this.data.add(Arrays.asList(row));
    }

    public List<String> get(final int row) {
        return data.get(row);
    }

    public String get(final int row, final int col) {
        return data.get(row).get(col);
    }

    public int getColumnCount() {
        return data.get(0).size();
    }

    public String getColumnName(final int col) {
        return columns.get(col);
    }

    public int getMaxLength(final int row) {

        int max = 0;

        for (final String colData : get(row)) {
            max = Math.max(max, colData.length());
        }

        return max;
    }

    public int getRowCount() {
        return data.size();
    }

    public String getText() {

        final StringBuilder sb = new StringBuilder();

        for (int i = 0; i < getRowCount(); i++) {
            sb.append(getWord(i));
            sb.append(" ");
        }

        return sb.toString();
    }

    public String getWord(final int row) {
        return data.get(row).get(0);
    }

    @Override
    public Iterator<List<String>> iterator() {
        return data.iterator();
    }

    public int offsetToRow(final int offset) {
        int theOffset = 0;
        for (int row = 0; row < getRowCount(); row++) {
            theOffset += getWord(row).length();
            if (theOffset >= offset) return row;
            theOffset++;
        }

        return -1;
    }

    private void resetColumns() {

        columns.clear();

        for (int i = 0; i < data.get(0).size(); i++) {
            if (i == 0) columns.add("Word");
            else columns.add("Col " + i);
        }
    }

    public void set(final int row, final int col, final String val) {
        data.get(row).set(col, val);
    }

    public void write(final File file) {

        // Get the column sizes
        final int columnSizes[] = new int[data.get(0).size()];

        for (final List<String> row : data) {
            int i = 0;
            for (final String col : row) {
                columnSizes[i] = Math.max(columnSizes[i], col.length());
                i++;
            }
        }

        // Build the format string
        String format = "";
        for (int i = 0; i < columnSizes.length; i++) {
            format += "%" + (i + 1) + "$-" + (columnSizes[i] + 1) + "s";
        }

        format += "\n";

        // Print the table
        try (PrintWriter writer = new PrintWriter(file)) {
            for (final List<String> row : data) {
                writer.format(format, row.toArray());
            }
        } catch (final FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void addColumn(final String text) {

        for (final List<String> row : data) {
            row.add(text);
        }
    }

}
