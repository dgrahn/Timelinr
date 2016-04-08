package us.grahn.timelinr.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.text.SimpleDateFormat;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;

import us.grahn.crf.CRFData;
import us.grahn.timelinr.TlrData;
import us.grahn.timelinr.TlrData.TlrItem;

public class Timelinr extends JFrame {

    private static final Color WHITE = Color.WHITE;
    private static final Color BLUE  = new Color(0,   175, 240);

    private static class DatePanel extends JPanel {

        //private final JLabel labelYear  = new JLabel();
        private final JLabel labelMonth = new JLabel();
        private final JLabel labelDay   = new JLabel();

        public DatePanel(final TlrItem item) {

            labelMonth.setText(new SimpleDateFormat("MMM").format(item.getDate()).toUpperCase());
            labelMonth.setFont(labelMonth.getFont().deriveFont(15f).deriveFont(Font.BOLD));
            labelMonth.setForeground(WHITE);

            labelDay.setText(new SimpleDateFormat("dd").format(item.getDate()));
            labelDay.setForeground(WHITE);
            labelDay.setHorizontalAlignment(JLabel.CENTER);

            setBackground(BLUE);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(WHITE, 5),
                    BorderFactory.createEmptyBorder(5, 10, 5, 10)));
            setBorder(BorderFactory.createMatteBorder(10, 10, 10, 10, Color.WHITE));
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            add(labelMonth);
            add(labelDay);
        }

    }

    public static void main(final String[] args) throws Exception {

        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

        final CRFData crf  = new CRFData(new File("data/Barack Obama.tlr"));
        final TlrData data = new TlrData(crf);

        final Timelinr tlr = new Timelinr();
        tlr.setData(data);
        tlr.setLocationRelativeTo(null);
        tlr.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        tlr.setVisible(true);
    }

    public static final String TITLE = "Timelinr";

    private final JPanel panelTimeline = new JPanel();
    private final JScrollPane scrollTimeline = new JScrollPane(panelTimeline);
    private TlrData data = null;

    public void setData(final TlrData data) {
        this.data = data;

        for (final TlrItem it : data.getItems()) {
            System.out.println(it);
            panelTimeline.add(new DatePanel(it));
        }
    }

    public Timelinr() {

        // Scroll Pane
        panelTimeline.setLayout(new BoxLayout(panelTimeline, BoxLayout.Y_AXIS));
        scrollTimeline.setBorder(BorderFactory.createEmptyBorder());

        setLayout(new BorderLayout());
        add(scrollTimeline, BorderLayout.CENTER);

        setTitle(TITLE);
        setLocationRelativeTo(null);
        setSize(1360, 768);
    }

}
