package us.grahn.crf.crfpp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import us.grahn.crf.CRFData;

public class CRFTest {

    private File model = null;
    private File input = null;
    private File output = null;

    public void setModel(final File model) {
        this.model = model;
    }

    public void setInput(final File input) {
        this.input = input;
    }

    public void setOutput(final File output) {
        this.output = output;
    }

    public void test() {

        try (PrintStream fos = new PrintStream(new FileOutputStream(output))) {

            final String cmd = String.format("\"%s\" -m \"%s\" \"%s\"",
                    CRFUtil.TEST.getAbsolutePath(),
                    model.getAbsolutePath(),
                    input.getAbsolutePath());

            CRFUtil.run(cmd, fos);

        } catch (final IOException | InterruptedException e) {
            e.printStackTrace();
        }

        try {
            // Make sure it's formatted well
            final CRFData data = new CRFData(output);
            data.write(output);
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    public double evaluate() {

        try {
            final CRFData data = new CRFData(output);
            final int estimatedCol = data.getColumnCount() - 1;
            final int actualCol    = data.getColumnCount() - 2;

            int errors = 0;

            for (int row = 0; row < data.getRowCount(); row++) {
                final String estimated = data.get(row, estimatedCol);
                final String actual    = data.get(row, actualCol);

                if (!estimated.equals(actual)) errors++;
            }

            return 1.0 - (1.0 * errors / data.getRowCount());

        } catch (final Exception e) {
            e.printStackTrace();
            return -1;

        }
    }

}
