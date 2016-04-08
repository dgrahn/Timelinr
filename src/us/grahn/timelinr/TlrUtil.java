package us.grahn.timelinr;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import java.util.regex.Pattern;

import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.time.TimeAnnotations.TimexAnnotation;
import edu.stanford.nlp.time.Timex;
import edu.stanford.nlp.util.CoreMap;
import us.grahn.crf.CRFData;

class TlrUtil {

    public static final String getDate(final CoreLabel cl) {

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

    public static final Date getDate(final String str) {

        try {
            if (TlrConstants.NON_DATE.equals(str)) {
                return null;
            } else if (str.matches("\\d{4}-\\d{2}-\\d{2}")) {
                return new SimpleDateFormat("yyyy-mm-dd").parse(str);
            } else if (str.matches("\\{4}-\\d{2}")) {
                return new SimpleDateFormat("yyyy-mm").parse(str);
            } else if (str.matches("\\{4}")) {
                return new SimpleDateFormat("yyyy").parse(str);
            } else {
                return null;
            }
        } catch (final ParseException e) {
            System.out.println("Error Parsing = " + str);
            return null;
        }
    }

    public static final boolean hasData(final CoreMap cm) {

        for (final CoreLabel cl : cm.get(TokensAnnotation.class)) {
            if (!"NONE".equals(getDate(cl))) return true;
        }

        return false;
    }

    public static final CRFData buildCrf(final Annotation document, final boolean includeEstimate) {

        final CRFData data = new CRFData();

        for (final CoreMap cm : document.get(SentencesAnnotation.class)) {

            final boolean hasDate = includeEstimate ? TlrUtil.hasData(cm) : false;

            String sentence = "BGN";
            String label = "BGN";
            for (final CoreLabel cl : cm.get(TokensAnnotation.class)) {
                final String word = cl.get(TextAnnotation.class);
                final String pos  = cl.get(PartOfSpeechAnnotation.class);
                final String type = cl.get(NamedEntityTagAnnotation.class);
                final String date = TlrUtil.getDate(cl);
                final String sntc = sentence;
                sentence = label = "IN";

                if (includeEstimate) {
                    final String cntx = !hasDate ? "OUT" : label;
                    data.addRow(word, pos, type, date, sntc, cntx);
                } else {
                    data.addRow(word, pos, type, date, sntc);
                }
            }
        }

        return data;

    }

    public static final File getTempFile() {
        final File temp = new File(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString() + ".tmp");
        temp.deleteOnExit();
        return temp;
    }

}
