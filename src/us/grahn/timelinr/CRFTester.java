package us.grahn.timelinr;

import java.io.File;

public class CRFTester {

	public static void main(final String[] args) throws Exception {

		final File file = new File(args[0]);
		final CRFData data = new CRFData(file);

		final int estimatedCol = data.getColumnCount() - 1;
		final int actualCol    = data.getColumnCount() - 2;

		int errors = 0;

		for (int row = 0; row < data.getRowCount(); row++) {
			final String estimated = data.get(row, estimatedCol);
			final String actual    = data.get(row, actualCol);

			if (!estimated.equals(actual)) errors++;
		}

		System.out.println("Rows = " + data.getRowCount());
		System.out.println("Errs = " + errors);
		System.out.println("   % = " + (1.0 * errors / data.getRowCount()));
		data.write(file);
	}

}
