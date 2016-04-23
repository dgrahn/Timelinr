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

    private void addDates(final Set<Date> dates, final StringBuilder sb) {

        if (!dates.isEmpty() && sb.length() > 0) {
            addDates(dates, sb.toString());
        }

        sb.setLength(0);
        dates.clear();
    }

    private void addWord(final StringBuilder sb, final String word) {
        if (word.matches("\\W") && sb.length() != 0) sb.setLength(sb.length() - 1);
        sb.append(word);
        sb.append(" ");
    }

    public TlrData(final CRFData data) {
        this.data = data;

        final StringBuilder sb = new StringBuilder();
        final Set<Date> dates = new HashSet<>();

        final int count = 0;
        boolean inSentence = false;

        for (int row = 0; row < data.getRowCount(); row++) {

            final String cntx = data.get(row, TlrConstants.CNTX);
            final String word = data.get(row, TlrConstants.WORD);

            final Date date = TlrUtil.getDate(data.get(row, TlrConstants.DATE));
            if (date != null) dates.add(date);

            if (inSentence) {

                switch (cntx) {
                    case TlrConstants.CONTEXT_IN:
                        addWord(sb, word);
                        break;

                    case TlrConstants.CONTEXT_OUT:
                        addDates(dates, sb);
                        inSentence = false;
                        break;

                    case TlrConstants.CONTEXT_START:
                        addDates(dates, sb);
                        addWord(sb, word);
                }
            } else {
                switch (cntx) {
                    case TlrConstants.CONTEXT_START:
                        inSentence = true;
                        addWord(sb, word);
                    default:
                        dates.clear();
                        sb.setLength(0);
                }
            }
        }

        Collections.sort(items);
    }

}
