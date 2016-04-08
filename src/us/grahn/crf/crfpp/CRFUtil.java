package us.grahn.crf.crfpp;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;

class CRFUtil {

    public static final File LEARN = new File("crfpp/crf_learn.exe");

    public static final File TEST  = new File("crfpp/crf_test.exe");

    public static void run(final String command, final PrintStream stream) throws IOException, InterruptedException {

        final Process p = Runtime.getRuntime().exec(command);

        try (final BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            String line;
            while ((line = r.readLine()) != null) {
                stream.println(line);
            }
        }

        p.waitFor();
    }

}
