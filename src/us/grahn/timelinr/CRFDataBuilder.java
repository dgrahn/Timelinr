package us.grahn.timelinr;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter.DefaultHighlightPainter;


/**
 * A simple CRF data visualizer and builder.
 *
 * @author Dan Grahn
 */
public class CRFDataBuilder extends JFrame {

    /**
     * An {@code Action} which handles all the builder's actions.
     *
     * @author Dan Grahn
     */
    private class BuilderAction extends AbstractAction {

        public BuilderAction(final String name, final ImageIcon icon) {
            putValue(NAME, name);
            putValue(SMALL_ICON, icon);
        }

        public BuilderAction(final String name, final ImageIcon icon, final KeyStroke key) {
            this(name, icon);
            putValue(ACCELERATOR_KEY, key);
        }

        @Override
        public void actionPerformed(final ActionEvent e) {

            final String name = (String) getValue(NAME);
            if (LOAD_TEXT.equals(name)) load();
            else if (SAVE_TEXT.equals(name)) save();
            else if (REFRESH_TEXT.equals(name)) refreshHighlights();
            else if (ADD_COL_TEXT.equals(name)) addColumn();
            else if (UPDATE_TEXT.equals(name)) updateData();
            else if (REMOVE_TEXT.equals(name)) removeData();
        }

    }

    private static final ImageIcon ADD_COL_ICON = getIcon("add.png");
    private static final String    ADD_COL_TEXT = "Add Column";

    private static final String DEFAULT_HIGHLIGHTS =
              "C DATE #F38630\n"
            + "E BGN #69D2E7\n"
            + "E IN #A7DBD8";

    private static final ImageIcon LOAD_ICON    = getIcon("open.png");
    private static final String    LOAD_TEXT    = "Load...";
    private static final ImageIcon REFRESH_ICON = getIcon("refresh.png");
    private static final String    REFRESH_TEXT = "Refresh";
    private static final ImageIcon SAVE_ICON    = getIcon("save.png");
    private static final String    SAVE_TEXT    = "Save...";
    private static final KeyStroke SAVE_KEY     = KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_MASK);
    private static final ImageIcon UPDATE_ICON  = getIcon("star.png");
    private static final KeyStroke UPDATE_KEY   = KeyStroke.getKeyStroke(KeyEvent.VK_U, 0);
    private static final String    UPDATE_TEXT  = "Update Selection";
    private static final KeyStroke REMOVE_KEY   = KeyStroke.getKeyStroke(KeyEvent.VK_R, 0);
    private static final ImageIcon REMOVE_ICON  = getIcon("remove.png");
    private static final String    REMOVE_TEXT  = "Remove Selection";

    private static ImageIcon getIcon(final String path) {
        try {
            return new ImageIcon(ImageIO.read(new File("data/icons/" + path)));
        } catch (final IOException e) {
            return null;
        }
    }

    public static void main(final String[] args) throws Exception {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        final CRFDataBuilder builder = new CRFDataBuilder();
        builder.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        builder.setVisible(true);
    }

    private final Action          actionAddCol  = new BuilderAction(ADD_COL_TEXT, ADD_COL_ICON);
    private final Action          actionLoad    = new BuilderAction(LOAD_TEXT, LOAD_ICON);
    private final Action          actionRefresh = new BuilderAction(REFRESH_TEXT, REFRESH_ICON);
    private final Action          actionSave    = new BuilderAction(SAVE_TEXT, SAVE_ICON, SAVE_KEY);
    private final Action          actionUpdate  = new BuilderAction(UPDATE_TEXT, UPDATE_ICON, UPDATE_KEY);
    private final Action          actionRemove  = new BuilderAction(REMOVE_TEXT, REMOVE_ICON, REMOVE_KEY);
    private       CRFData         data          = null;
    private final JTextField      defaultText   = new JTextField("OUT");
    private final JMenu           editMenu      = new JMenu("Edit");
    private final JMenu           fileMenu      = new JMenu("File");
    private final List<Highlight> highlights    = new ArrayList<>();
    private final JTextArea       highlightText = new JTextArea();
    private final JSplitPane      mainSplitPane = new JSplitPane();
    private final JMenuBar        menubar       = new JMenuBar();
    private final JTextField      middleText    = new JTextField("IN");
    private final JSplitPane      sideSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    private final JTextField      startText     = new JTextField("BGN");
    private final JTextField      columnText    = new JTextField("LAST");
    private final JTable          table         = new JTable();
    private final JTextArea       textarea      = new JTextArea();
    private final JToolBar        toolbar       = new JToolBar();
    private       File            loadFile      = null;
    private       File            saveFile      = null;

    public CRFDataBuilder() {
        setTitle("Test Data Builder");
        setJMenuBar(menubar);
        setLayout(new BorderLayout());

        add(toolbar, BorderLayout.NORTH);
        add(mainSplitPane, BorderLayout.CENTER);

        // Setup the main split pane
        mainSplitPane.setRightComponent(new JScrollPane(textarea));
        mainSplitPane.setLeftComponent(sideSplitPane);

        // Setup the side split pane
        sideSplitPane.setTopComponent(new JScrollPane(highlightText));
        sideSplitPane.setBottomComponent(new JScrollPane(table));

        // Customize the highlight pane
        highlightText.setText(DEFAULT_HIGHLIGHTS);

        // Customize the text fields
        defaultText.setMaximumSize(new Dimension(1000, defaultText.getPreferredSize().height));
        startText.setMaximumSize(new Dimension(1000, startText.getPreferredSize().height));
        middleText.setMaximumSize(new Dimension(1000, middleText.getPreferredSize().height));
        columnText.setMaximumSize(new Dimension(1000, columnText.getPreferredSize().height));

        // Customize the text pane
        textarea.setEditable(false);
        textarea.setLineWrap(true);
        textarea.setWrapStyleWord(true);

        // File Menu
        menubar.add(fileMenu);
        fileMenu.add(actionLoad);
        fileMenu.add(actionSave);

        // Edit Menu
        menubar.add(editMenu);
        editMenu.add(actionRefresh);
        editMenu.addSeparator();
        editMenu.add(actionAddCol);
        editMenu.add(actionUpdate);
        editMenu.add(actionRemove);

        // Toolbar
        toolbar.add(actionLoad);
        toolbar.add(actionSave);
        toolbar.addSeparator();
        toolbar.add(actionRefresh);
        toolbar.addSeparator();
        toolbar.add(actionAddCol);
        toolbar.add(actionUpdate);
        toolbar.add(actionRemove);
        toolbar.add(Box.createHorizontalGlue());
        toolbar.add(new JLabel("Default: "));
        toolbar.add(defaultText);
        toolbar.add(new JLabel("  Column: "));
        toolbar.add(columnText);
        toolbar.add(new JLabel("  Start: "));
        toolbar.add(startText);
        toolbar.add(new JLabel("  Middle: "));
        toolbar.add(middleText);


        pack();
        setSize(1360, 768);
        setLocationRelativeTo(null);
        mainSplitPane.setDividerLocation(0.6);
        sideSplitPane.setDividerLocation(0.4);

        textarea.addCaretListener(new CaretListener() {

            @Override
            public void caretUpdate(final CaretEvent arg0) {
                final int startRow = data.offsetToRow(textarea.getSelectionStart());
                final int endRow   = data.offsetToRow(textarea.getSelectionEnd());

                if (startRow == -1 || endRow == -1) return;
                table.setRowSelectionInterval(startRow, endRow);
                table.scrollRectToVisible(table.getCellRect(startRow, 0, true));
            }

        });
    }

    protected void addColumn() {
        data.addColumn(defaultText.getText());
        getModel().fireTableStructureChanged();
    }

    private CRFDataTableModel getModel() {
        return (CRFDataTableModel) table.getModel();
    }

    protected void load() {

        final JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("CRF Data", "crf"));
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setMultiSelectionEnabled(false);

        final int returnVal = chooser.showOpenDialog(this);

        if (returnVal != JFileChooser.APPROVE_OPTION) return;

        load(chooser.getSelectedFile());
    }

    protected void load(final File file) {

        this.loadFile = file;

        new SwingWorker<Void, Void>() {

            @Override
            protected Void doInBackground() throws Exception {
                System.out.print("Reading Data... ");
                data = new CRFData(file);
                System.out.println("Done");
                return null;
            }

            @Override
            public void done() {
                refreshDisplay();
            }

        }.run();
    }

    protected void refreshDisplay() {

        // Update the table
        System.out.print("Updating GUI... ");

        refreshHighlights();

        table.setModel(new CRFDataTableModel(data));

        // Update the textarea
        textarea.setText(data.getText());
        textarea.setCaretPosition(0);
        refreshHighlights();

        System.out.println("Done");
    }

    protected void refreshHighlights() {

        // Parse the highlights.
        highlights.clear();

        for (final String line : highlightText.getText().split("\n")) {
            final String[] parsed = line.split("\\s+");

            try {
                final Highlight h = new Highlight();
                h.setCol(table.getColumn(parsed[0]).getModelIndex());
                h.setType(parsed[1]);
                h.setColor(parsed[2]);
                highlights.add(h);
            } catch (final IllegalArgumentException e) {

            }
        }

        // Remove all the existing highlights
        textarea.getHighlighter().removeAllHighlights();

        // Add the highlights back in
        int start = 0;
        for (int row = 0; row < data.getRowCount(); row++) {
            final int end = start + data.getWord(row).length() + 1;
            for (final Highlight h : highlights) {
                if (h.getType().equals(data.get(row, h.getCol()))) {
                    try {
                        textarea.getHighlighter()
                                .addHighlight(start, end,
                                        new DefaultHighlightPainter(h.getColor()));
                    } catch (final BadLocationException e) {
                        e.printStackTrace();
                    }
                }
            }
            start = end;
        }
    }

    protected void save() {

        final JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("CRF Data", "crf"));
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setSelectedFile(loadFile);
        chooser.setMultiSelectionEnabled(false);

        final int returnVal = chooser.showSaveDialog(this);
        if (returnVal != JFileChooser.APPROVE_OPTION) return;

        saveFile = chooser.getSelectedFile();

        System.out.print("Saving... ");
        data.write(saveFile);
        System.out.println("Done.");
    }

    protected void updateData() {
        final int startRow = data.offsetToRow(textarea.getSelectionStart());
        final int endRow   = data.offsetToRow(textarea.getSelectionEnd());
        final int col            = "LAST".equals(columnText.getText())
                                ? data.getColumnCount() - 1
                                : table.getColumn(columnText.getText()).getModelIndex();

        data.set(startRow, data.getColumnCount() - 1, startText.getText());
        getModel().fireTableCellUpdated(startRow, col);

        for (int row = startRow + 1; row <= endRow; row++) {
            data.set(row, col, middleText.getText());
            getModel().fireTableCellUpdated(row, col);
        }

        refreshHighlights();
    }

    protected void removeData() {

        final int startRow = data.offsetToRow(textarea.getSelectionStart());
        final int endRow   = data.offsetToRow(textarea.getSelectionEnd());
        final int col            = "LAST".equals(columnText.getText())
                                ? data.getColumnCount() - 1
                                : table.getColumn(columnText.getText()).getModelIndex();

        for (int row = startRow; row <= endRow; row++) {
            data.set(row, col, defaultText.getText());
            getModel().fireTableCellUpdated(row, col);
        }

        refreshHighlights();
    }


}
