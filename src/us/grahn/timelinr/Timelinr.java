package us.grahn.timelinr;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.regex.Pattern;

import edu.stanford.nlp.ling.CoreAnnotations.DocDateAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.time.TimeAnnotations.TimexAnnotation;
import edu.stanford.nlp.time.Timex;
import edu.stanford.nlp.util.CoreMap;

public class Timelinr {

    public static void main(final String[] args) {

        final Timelinr tlr = new Timelinr();

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

    public Timelinr() {
        // Setup the pipeline
        final Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, sutime");
        props.setProperty("customAnnotatorClass.sutime", "edu.stanford.nlp.time.TimeAnnotator");
        this.pipeline = new StanfordCoreNLP(props);
    }

    private static final String getDate(final CoreLabel cl) {

        final Timex timex = cl.get(TimexAnnotation.class);
        if (timex == null) return "NONE";

        try {
            final Date date = timex.getDate().getTime();
            return new SimpleDateFormat("yyyy-MM-dd").format(date);
        } catch (final Exception e) { }

        if (timex.value() != null && Pattern.matches("\\d{4}(-\\d{2})?(-\\d{2})?", timex.value())) {
            return timex.value();
        }

        return "NONE";
    }

    private static final CRFData buildCRF(final Annotation document) {

        final CRFData data = new CRFData();

        for (final CoreMap cm : document.get(SentencesAnnotation.class)) {

            boolean hasDate = false;
            for (final CoreLabel cl : cm.get(TokensAnnotation.class)) {
                if (!"NONE".equals(getDate(cl))) {
                    hasDate = true;
                    break;
                }
            }

            String sentence = "BGN";
            String label = "BGN";
            for (final CoreLabel cl : cm.get(TokensAnnotation.class)) {
                final String word = cl.get(TextAnnotation.class);
                final String pos  = cl.get(PartOfSpeechAnnotation.class);
                final String type = cl.get(NamedEntityTagAnnotation.class);
                final String date = getDate(cl);
                final String sntc = sentence;
                final String cntx = !hasDate ? "OUT" : label;
                sentence = label = "IN";
                data.addRow(word, pos, type, date, sntc, cntx);
            }
        }

        return data;
    }

    public CRFData process(final String title) {

        // Get the article
        final String text;
        try {
            text = Wikipedia.getExtract(title);
        } catch (final IOException e) {
            e.printStackTrace();
            return null;
        }

        // Create an annotation from the given text
        final Annotation document = new Annotation(text);
        document.set(DocDateAnnotation.class, "2016-03-11"); // FIXME Use current date

        pipeline.annotate(document);

        return buildCRF(document);
    }

}
