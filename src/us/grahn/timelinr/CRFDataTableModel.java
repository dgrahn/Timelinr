package us.grahn.timelinr;

import javax.swing.table.AbstractTableModel;

public class CRFDataTableModel extends AbstractTableModel {

    private final CRFData data;

    public CRFDataTableModel(final CRFData data) {
        this.data = data;
    }

    @Override
    public int getColumnCount() {
        return data.getColumnCount();
    }

    @Override
    public int getRowCount() {
        return data.getRowCount();
    }

    @Override
    public String getValueAt(final int row, final int col) {
        return data.get(row, col);
    }

}
