package us.grahn.timelinr;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import edu.stanford.nlp.ling.CoreAnnotations.DocDateAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import us.grahn.crf.CRFData;
import us.grahn.crf.crfpp.CRFTest;
import us.grahn.wiki.Wikipedia;

public class TlrApi {

    public static void main(final String[] args) {

        final TlrApi tlr = new TlrApi();

        final File input  = new File(args[0]);
        final File output = new File(args[1]);

        try (BufferedReader reader = new BufferedReader(new FileReader(input))) {

            String title;
            while ((title = reader.readLine()) != null) {

                final File outFile = new File(output, title + ".crf");

                System.out.print(String.format("%1$-40s", title));
                if (outFile.exists()) {
                    System.out.println("Already Completed");
                    continue;
                }

                try {
                    final CRFData data = tlr.process(title);
                    data.write(outFile);
                    System.out.println("Done");
                } catch (final Exception e) {
                    System.err.println("Failed");
                }
            }

        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    private final StanfordCoreNLP pipeline;

    public TlrApi() {
        // Setup the pipeline
        final Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, sutime");
        props.setProperty("customAnnotatorClass.sutime", "edu.stanford.nlp.time.TimeAnnotator");
        this.pipeline = new StanfordCoreNLP(props);
    }

    private boolean useEstimate = false;

    public void setUseEstimate(final boolean useEstimate) {
        this.useEstimate = useEstimate;
    }

    public boolean useEstimate() {
        return useEstimate;
    }

    private File model = null;

    public void setModel(final File model) {
        this.model = model;
    }

    private Annotation annotate(final String text) {

        final Annotation document = new Annotation(text);
        document.set(DocDateAnnotation.class, new SimpleDateFormat("yyyy-mm-dd").format(new Date()));

        pipeline.annotate(document);

        return document;
    }

    public CRFData process(final String title) {

        if (model == null && !useEstimate()) {
            throw new RuntimeException("Either model must be set or estimated value must be used.");
        }

        // Here is how the data is process
        // (1) Retrieve the Article
        final String text = Wikipedia.getExtract(title);
        if (text == null) return null;

        // (2) Annotate using Stanford NLP
        final Annotation document = annotate(text);

        // (3) Build the CRF file
        final CRFData data = TlrUtil.buildCrf(document, useEstimate());

        // (3a) If we are estimated, short-circuit
        if (useEstimate()) return data;

        // (4) Write the data to a temporary file
        final File temp = TlrUtil.getTempFile();
        data.write(temp);

        // (5) Process using CRF++
        final CRFTest test = new CRFTest();
        test.setModel(model);
        test.setInput(temp);

        // (6) Read in the new data
        try {
            final CRFData newData = new CRFData(temp);
            return newData;
        } catch (final IOException e) {
            return null;
        }
    }

}
