package us.grahn.timelinr;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TimelineDisplayer {

	private static final int ESTIMATED_COL = 6;
	private static final int DATE_COL 	   = 3;
	private static final int TEXT_COL      = 0;

	public static void main(final String[] args) throws Exception {

		final File file = new File(args[0]);
		final CRFData data = new CRFData(file);

		int start = -1;
		for (int row = 0; row < data.getRowCount(); row++) {
			if ("BGN".equals(data.get(row, ESTIMATED_COL))) start = row;
			if ("OUT".equals(data.get(row, ESTIMATED_COL))  && start != -1) {

				final StringBuilder b = new StringBuilder();
				final List<Date> dates = new ArrayList<Date>();

				for (int j = start; j < row; j++) {
				    b.append(data.get(j, TEXT_COL));
				    b.append(" ");
				    final String date = data.get(j, DATE_COL);

				    if (date.matches("\\d{4}-\\d{2}-\\d{2}")) {
				        dates.add(new SimpleDateFormat("yyyy-mm-dd").parse(date));
				    } else if (date.matches("\\{4}-\\d{2}")) {
				        dates.add(new SimpleDateFormat("yyyy-mm").parse(date));
				    } else if (date.matches("\\{4}")) {
				        dates.add(new SimpleDateFormat("yyyy").parse(date));
				    }
				}

				start = -1;


			    if (!dates.isEmpty()) {
			        System.out.print(new SimpleDateFormat("yyyy-mm-dd").format(dates.get(0)));
			    } else {
			        System.out.print("          ");
			    }
			    System.out.print("\t");
				System.out.println(b.toString());
			}
		}

	}
}
