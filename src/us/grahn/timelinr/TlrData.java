package us.grahn.timelinr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import us.grahn.crf.CRFData;

public class TlrData {

    public static class TlrItem implements Comparable<TlrItem> {

        private final Date date;
        private final String cntx;

        public TlrItem(final Date date, final String cntx) {
            this.date = date;
            this.cntx = cntx;
        }

        @Override
        public int compareTo(final TlrItem o) {
            return date.compareTo(o.date);
        }

        public Date getDate() {
            return date;
        }

        public String getContext() {
            return cntx;
        }

        @Override
        public String toString() {
            return String.format("%1$tY-%1$tm-%1$td\t%2$s", date, cntx);
        }

    }

    private final CRFData data;

    public List<TlrItem> getItems() {
        return items;
    }

    private final List<TlrItem> items = new ArrayList<TlrItem>();

    private void addDates(final Set<Date> dates, final String cntx) {

        for (final Date d : dates) {
            items.add(new TlrItem(d, cntx));
        }

    }

    public TlrData(final CRFData data) {
        this.data = data;

        final int startRow = -1;
        final int lastRow  = -1;

        final StringBuilder sb = new StringBuilder();
        final Set<Date> dates = new HashSet<Date>();

        for (int row = 0; row < data.getRowCount(); row++) {

            final Date   date = TlrUtil.getDate(data.get(row, TlrConstants.DATE));
            final String cntx = data.get(row, data.getColumnCount() - 1);
            final String word = data.get(row, TlrConstants.WORD);

            switch (cntx) {
                case TlrConstants.CONTEXT_START:
                    if (!sb.toString().trim().isEmpty() && !dates.isEmpty()) {
                        addDates(dates, sb.toString());
                        sb.setLength(0);
                        dates.clear();
                    }
                case TlrConstants.CONTEXT_IN:
                    sb.append(word);
                    sb.append(" ");
                    break;

                case TlrConstants.CONTEXT_OUT:
                    if (sb.toString().trim().isEmpty() || dates.isEmpty()) break;
                    addDates(dates, sb.toString());
                    sb.setLength(0);
                    dates.clear();
                    break;
                default:
            }

            if (date != null) dates.add(date);
        }

        Collections.sort(items);
    }

}
