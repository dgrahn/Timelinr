package us.grahn.crf.crfpp;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;
import java.util.UUID;

public class CRFLearn {

    public static void main(final String[] args) {

        final CRFLearn learn = new CRFLearn();
        learn.setTemplate(new File("data/templates/connl2000-only34.txt"));
        learn.setDirectory(new File("data/train"));
        learn.setModel(new File("data/model.mod"));
        learn.learn();

        final CRFTest test = new CRFTest();
        test.setInput(new File("data/test/combined.crf"));
        test.setOutput(new File("data/combined.tlr"));
        test.setModel(new File("data/model.mod"));
        test.test();

        System.out.println("Result = " + test.evaluate());
    }

    private File directory = null;
    private File model = null;
    private File temp = null;
    private File template = null;

    public void learn() {

        if (!CRFUtil.LEARN.exists())  throw new RuntimeException("crf_learn.exe does not exist.");
        if (!directory.exists())      throw new RuntimeException("Directory does not exist.");
        if (!directory.isDirectory()) throw new RuntimeException("Directory is not a directory.");
        if (!template.exists())       throw new RuntimeException("Template does not exist.");
        if (model.exists())           throw new RuntimeException("Model already exists.");

        try {
            // Initialize the temp file.
            this.temp = new File(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString() + ".crf");
            temp.deleteOnExit();
            temp.createNewFile();

            // Consolidate all the files in the directory into a single file
            try (final PrintWriter writer = new PrintWriter(temp)) {
                for (final File file : directory.listFiles()) {
                    try (final Scanner scanner = new Scanner(file)) {
                        while (scanner.hasNextLine()) {
                            writer.println(scanner.nextLine());
                        }
                    }
                }
            }

            // Run the learning process
            final String cmd = String.format("\"%s\" \"%s\" \"%s\" \"%s\"",
                    CRFUtil.LEARN.getAbsolutePath(),
                    template.getAbsolutePath(),
                    temp.getAbsolutePath(),
                    model.getAbsolutePath());

            CRFUtil.run(cmd, System.out);

            temp.delete();
        } catch (final IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void setDirectory(final File directory) {
        this.directory = directory;
    }

    public void setModel(final File model) {
        this.model = model;
    }

    public void setTemplate(final File template) {
        this.template = template;
    }

}
