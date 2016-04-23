package us.grahn.timelinr.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.io.File;
import java.text.SimpleDateFormat;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.Scrollable;
import javax.swing.UIManager;

import us.grahn.crf.CRFData;
import us.grahn.timelinr.TlrApi;
import us.grahn.timelinr.TlrData;
import us.grahn.timelinr.TlrData.TlrItem;

public class Timelinr extends JFrame {

    private static final Color WHITE = Color.WHITE;
    private static final Color BLUE  = new Color(0,   175, 240);

    private static class DatePanel extends JPanel {

        private final JLabel labelYear  = new JLabel();
        private final JLabel labelMonth = new JLabel();
        private final JLabel labelDay   = new JLabel();

        public DatePanel(final TlrItem item) {

            labelYear.setText(new SimpleDateFormat("yyyy").format(item.getDate()));
            labelYear.setForeground(Color.WHITE);
            labelYear.setHorizontalAlignment(JLabel.CENTER);
            labelYear.setFont(labelYear.getFont().deriveFont(Font.BOLD));

            labelMonth.setText(new SimpleDateFormat("MMM").format(item.getDate()).toUpperCase());
            labelMonth.setFont(labelMonth.getFont().deriveFont(14f).deriveFont(Font.BOLD));
            labelMonth.setForeground(WHITE);
            labelMonth.setHorizontalAlignment(JLabel.CENTER);

            labelDay.setText(new SimpleDateFormat("dd").format(item.getDate()));
            labelDay.setFont(labelDay.getFont().deriveFont(28f));
            labelDay.setForeground(WHITE);
            labelDay.setHorizontalAlignment(JLabel.CENTER);

            setBackground(BLUE);
            setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            setLayout(new GridBagLayout());

            final GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1;
            c.gridx = 0;

            add(labelMonth, c);
            add(labelDay, c);
            add(labelYear, c);
        }

    }

    private static class ScrollablePanel extends JPanel implements Scrollable {

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(final Rectangle visibleRect, final int orientation,
                final int direction) {
            return 50;
        }

        @Override
        public int getScrollableBlockIncrement(final Rectangle visibleRect, final int orientation,
                final int direction) {
            return 80;
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }


    }

    private static class EventPanel extends JPanel {

        private final JTextArea labelText = new JTextArea();

        public EventPanel(final TlrItem item) {

            labelText.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
            labelText.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
            labelText.setForeground(Color.DARK_GRAY);
            labelText.setBackground(Color.WHITE);
            labelText.setText(item.getContext());
            labelText.setWrapStyleWord(true);
            labelText.setWrapStyleWord(true);
            labelText.setLineWrap(true);
            labelText.setEditable(false);
            labelText.setFocusable(false);

            setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            setLayout(new BorderLayout());
            add(new DatePanel(item), BorderLayout.WEST);
            add(labelText, BorderLayout.CENTER);
        }
    }

    public static void main(final String[] args) throws Exception {

        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

        //final CRFData crf  = new CRFData(new File("data/Barack Obama.tlr"));
        //final TlrData data = new TlrData(crf);

        final Timelinr tlr = new Timelinr();
        //tlr.setData(data);
        tlr.setLocationRelativeTo(null);
        tlr.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        tlr.setVisible(true);
    }

    public static final String TITLE = "Timelinr";

    private final JPanel panelTimeline = new ScrollablePanel();

    private final JScrollPane scrollTimeline = new JScrollPane(panelTimeline,
            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

    private TlrData data = null;

    private final JMenuBar menubar = new JMenuBar();

    private final AbstractAction actionImport = new AbstractAction("Import") {

        @Override
        public void actionPerformed(final ActionEvent arg0) {
            importArticle();
        }

    };

    public void setData(final TlrData data) {

        panelTimeline.removeAll();

        this.data = data;

        for (final TlrItem it : data.getItems()) {
            //if (it.getContext().length() > 600) continue;
            panelTimeline.add(new EventPanel(it));
        }

        panelTimeline.revalidate();
    }

    public void importArticle() {

        final String article = JOptionPane.showInputDialog("Article:");

        final TlrApi api = new TlrApi();
        api.setModel(new File("data/model.mod"));

        final CRFData crfData = api.process(article);
        final TlrData tlrData = new TlrData(crfData);
        setData(tlrData);
        setTitle("Timelinr: " + article);
    }

    public Timelinr() {

        // File Menu
        final JMenu menuFile = new JMenu("File");
        menubar.add(menuFile);
        menuFile.add(actionImport);

        // Scroll Pane
        panelTimeline.setLayout(new BoxLayout(panelTimeline, BoxLayout.Y_AXIS));
        scrollTimeline.setBorder(BorderFactory.createEmptyBorder());

        setJMenuBar(menubar);
        setTitle(TITLE);
        setLocationRelativeTo(null);
        setSize(1360, 768);

        setLayout(new BorderLayout());
        add(scrollTimeline, BorderLayout.CENTER);
    }

}
