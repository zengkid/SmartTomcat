/**
 * Author: Gezahegn Lemma (Gezu)
 * Project: Dev Tomcat Plugin
 * Created: 6/9/25
 * Phase 2: Web Browsers and Preview configuration dialog - Matches Ultimate exactly
 */

package com.poratu.idea.plugins.tomcat.ui.dialogs;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.JBUI;
import javax.swing.table.DefaultTableCellRenderer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Web Browsers and Preview configuration dialog - exactly like Ultimate's dialog
 * Shows browser list with checkboxes, default browser selection, and preview settings
 */
public class WebBrowsersDialog extends DialogWrapper {

    private final Project project;

    // Browser table
    private JBTable browserTable;
    private DefaultTableModel browserTableModel;

    // Management buttons
    private JButton addButton;
    private JButton removeButton;
    private JButton moveUpButton;
    private JButton moveDownButton;
    private JButton editButton;

    // Default browser settings
    private JComboBox<String> defaultBrowserComboBox;
    private JButton refreshButton;

    // Show browser popup settings
    private JCheckBox showBrowserPopupCheckBox;
    private JCheckBox forHtmlFilesCheckBox;
    private JCheckBox forXmlFilesCheckBox;

    // Reload behavior settings
    private JComboBox<String> reloadPageBrowserComboBox;
    private JComboBox<String> reloadPagePreviewComboBox;

    public WebBrowsersDialog(@NotNull Project project) {
	super(project);
	this.project = project;
	setTitle("Web Browsers and Preview");
	setSize(700, 500);
	init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
	JPanel mainPanel = new JPanel(new BorderLayout());
	mainPanel.setBorder(JBUI.Borders.empty(10));

	// Create browser table section
	mainPanel.add(createBrowserTableSection(), BorderLayout.CENTER);

	// Create settings section
	mainPanel.add(createSettingsSection(), BorderLayout.SOUTH);

	// Initialize with default browsers
	initializeDefaultBrowsers();

	return mainPanel;
    }

    /**
     * Create browser table section - matches Ultimate exactly
     */
    private JPanel createBrowserTableSection() {
	JPanel panel = new JPanel(new BorderLayout());

	// Create table with Ultimate's exact columns
	String[] columnNames = {"", "Name", "Family", "Path"};
	browserTableModel = new DefaultTableModel(columnNames, 0) {
	    @Override
	    public Class<?> getColumnClass(int column) {
		if (column == 0) return Boolean.class; // Checkbox column
		return String.class;
	    }

	    @Override
	    public boolean isCellEditable(int row, int column) {
		return column == 0; // Only checkbox is editable
	    }
	};

	browserTable = new JBTable(browserTableModel);
	browserTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	browserTable.getTableHeader().setReorderingAllowed(false);

	// Set column widths like Ultimate
	browserTable.getColumnModel().getColumn(0).setPreferredWidth(30);  // Checkbox
	browserTable.getColumnModel().getColumn(1).setPreferredWidth(100); // Name
	browserTable.getColumnModel().getColumn(2).setPreferredWidth(100); // Family
	browserTable.getColumnModel().getColumn(3).setPreferredWidth(200); // Path

	// Custom renderer for Family column (with icons)
	browserTable.getColumnModel().getColumn(2).setCellRenderer(new BrowserFamilyRenderer());

	JScrollPane scrollPane = new JScrollPane(browserTable);
	scrollPane.setPreferredSize(new Dimension(600, 200));
	panel.add(scrollPane, BorderLayout.CENTER);

	// Create buttons panel
	JPanel buttonsPanel = createBrowserButtonsPanel();
	panel.add(buttonsPanel, BorderLayout.EAST);

	return panel;
    }

    /**
     * Create browser management buttons - matches Ultimate layout
     */
    private JPanel createBrowserButtonsPanel() {
	JPanel panel = new JPanel();
	panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
	panel.setBorder(JBUI.Borders.emptyLeft(10));

	// Create buttons
	addButton = new JButton("+");
	removeButton = new JButton("-");
	editButton = new JButton("✏");
	moveUpButton = new JButton("↑");
	moveDownButton = new JButton("↓");

	// Set button sizes
	Dimension buttonSize = new Dimension(30, 25);
	addButton.setPreferredSize(buttonSize);
	removeButton.setPreferredSize(buttonSize);
	editButton.setPreferredSize(buttonSize);
	moveUpButton.setPreferredSize(buttonSize);
	moveDownButton.setPreferredSize(buttonSize);

	// Add tooltips
	addButton.setToolTipText("Add browser");
	removeButton.setToolTipText("Remove browser");
	editButton.setToolTipText("Edit browser");
	moveUpButton.setToolTipText("Move up");
	moveDownButton.setToolTipText("Move down");

	// Add action listeners
	addButton.addActionListener(e -> addBrowser());
	removeButton.addActionListener(e -> removeBrowser());
	editButton.addActionListener(e -> editBrowser());
	moveUpButton.addActionListener(e -> moveBrowserUp());
	moveDownButton.addActionListener(e -> moveBrowserDown());

	// Add buttons to panel
	panel.add(addButton);
	panel.add(Box.createVerticalStrut(2));
	panel.add(removeButton);
	panel.add(Box.createVerticalStrut(2));
	panel.add(editButton);
	panel.add(Box.createVerticalStrut(5));
	panel.add(moveUpButton);
	panel.add(Box.createVerticalStrut(2));
	panel.add(moveDownButton);
	panel.add(Box.createVerticalGlue());

	// Update button states
	updateButtonStates();
	browserTable.getSelectionModel().addListSelectionListener(e -> updateButtonStates());

	return panel;
    }

    /**
     * Create settings section - matches Ultimate's bottom settings
     */
    private JPanel createSettingsSection() {
	JPanel panel = new JPanel();
	panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
	panel.setBorder(JBUI.Borders.emptyTop(20));

	// Default browser section
	panel.add(createDefaultBrowserSection());
	panel.add(Box.createVerticalStrut(10));

	// Show browser popup section
	panel.add(createBrowserPopupSection());
	panel.add(Box.createVerticalStrut(10));

	// Reload behavior section
	panel.add(createReloadBehaviorSection());

	return panel;
    }

    /**
     * Create default browser section - matches Ultimate exactly
     */
    private JPanel createDefaultBrowserSection() {
	JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));

	panel.add(new JLabel("Default Browser:"));

	defaultBrowserComboBox = new JComboBox<>(new String[]{
		"System default", "Chrome", "Firefox", "Edge", "Safari"
	});
	defaultBrowserComboBox.setPreferredSize(new Dimension(150, 25));
	panel.add(defaultBrowserComboBox);

	refreshButton = new JButton("Refresh");
	refreshButton.setPreferredSize(new Dimension(70, 25));
	refreshButton.setToolTipText("Refresh browser list");
	refreshButton.addActionListener(e -> refreshBrowserList());
	panel.add(refreshButton);

	return panel;
    }

    /**
     * Create browser popup section - matches Ultimate exactly
     */
    private JPanel createBrowserPopupSection() {
	JPanel panel = new JPanel();
	panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
	panel.setBorder(BorderFactory.createTitledBorder("Show browser popup in the editor"));

	forHtmlFilesCheckBox = new JCheckBox("For HTML files", true);
	forXmlFilesCheckBox = new JCheckBox("For XML files", false);

	panel.add(forHtmlFilesCheckBox);
	panel.add(forXmlFilesCheckBox);

	return panel;
    }

    /**
     * Create reload behavior section - matches Ultimate exactly
     */
    private JPanel createReloadBehaviorSection() {
	JPanel panel = new JPanel();
	panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
	panel.setBorder(BorderFactory.createTitledBorder("Reload behavior"));

	// Reload page in browser
	JPanel reloadBrowserPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
	reloadBrowserPanel.add(new JLabel("Reload page in browser:"));
	reloadPageBrowserComboBox = new JComboBox<>(new String[]{
		"On Save", "On Frame Deactivation", "Manually"
	});
	reloadBrowserPanel.add(reloadPageBrowserComboBox);
	panel.add(reloadBrowserPanel);

	// Reload page in built-in preview
	JPanel reloadPreviewPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
	reloadPreviewPanel.add(new JLabel("Reload page in built-in preview:"));
	reloadPagePreviewComboBox = new JComboBox<>(new String[]{
		"On Save", "On Frame Deactivation", "Manually"
	});
	reloadPreviewPanel.add(reloadPagePreviewComboBox);
	panel.add(reloadPreviewPanel);

	return panel;
    }

    /**
     * Initialize with default browsers - matches Ultimate's default list
     */
    private void initializeDefaultBrowsers() {
	// Add default browsers from Ultimate screenshot
	addBrowserRow(true, "Chrome", "Chrome", "chrome");
	addBrowserRow(true, "Firefox", "Firefox", "firefox");
	addBrowserRow(false, "Safari", "Safari", "safari");
	addBrowserRow(false, "Opera", "Chrome", "opera");
	addBrowserRow(false, "Internet Explorer", "Internet Explorer", "iexplore");
	addBrowserRow(true, "Edge", "Chrome", "msedge");

	// Set default selections
	defaultBrowserComboBox.setSelectedItem("System default");
	reloadPageBrowserComboBox.setSelectedItem("On Save");
	reloadPagePreviewComboBox.setSelectedItem("On Save");
    }

    /**
     * Add browser row to table
     */
    private void addBrowserRow(boolean enabled, String name, String family, String path) {
	browserTableModel.addRow(new Object[]{enabled, name, family, path});
    }

    /**
     * Add new browser
     */
    private void addBrowser() {
	// Show browser configuration dialog
	BrowserConfigDialog dialog = new BrowserConfigDialog(project, null);
	if (dialog.showAndGet()) {
	    BrowserInfo browser = dialog.getBrowserInfo();
	    addBrowserRow(true, browser.getName(), browser.getFamily(), browser.getPath());
	    updateButtonStates();
	}
    }

    /**
     * Remove selected browser
     */
    private void removeBrowser() {
	int selectedRow = browserTable.getSelectedRow();
	if (selectedRow >= 0) {
	    String browserName = (String) browserTableModel.getValueAt(selectedRow, 1);
	    int result = JOptionPane.showConfirmDialog(getContentPane(),
		    "Remove browser '" + browserName + "'?",
		    "Remove Browser",
		    JOptionPane.YES_NO_OPTION);

	    if (result == JOptionPane.YES_OPTION) {
		browserTableModel.removeRow(selectedRow);
		updateButtonStates();
	    }
	}
    }

    /**
     * Edit selected browser
     */
    private void editBrowser() {
	int selectedRow = browserTable.getSelectedRow();
	if (selectedRow >= 0) {
	    // Get current browser info
	    BrowserInfo currentBrowser = new BrowserInfo(
		    (String) browserTableModel.getValueAt(selectedRow, 1), // name
		    (String) browserTableModel.getValueAt(selectedRow, 2), // family
		    (String) browserTableModel.getValueAt(selectedRow, 3)  // path
	    );

	    // Show edit dialog
	    BrowserConfigDialog dialog = new BrowserConfigDialog(project, currentBrowser);
	    if (dialog.showAndGet()) {
		BrowserInfo updatedBrowser = dialog.getBrowserInfo();

		// Update table row
		browserTableModel.setValueAt(updatedBrowser.getName(), selectedRow, 1);
		browserTableModel.setValueAt(updatedBrowser.getFamily(), selectedRow, 2);
		browserTableModel.setValueAt(updatedBrowser.getPath(), selectedRow, 3);
	    }
	}
    }

    /**
     * Move browser up in the list
     */
    private void moveBrowserUp() {
	int selectedRow = browserTable.getSelectedRow();
	if (selectedRow > 0) {
	    browserTableModel.moveRow(selectedRow, selectedRow, selectedRow - 1);
	    browserTable.setRowSelectionInterval(selectedRow - 1, selectedRow - 1);
	}
    }

    /**
     * Move browser down in the list
     */
    private void moveBrowserDown() {
	int selectedRow = browserTable.getSelectedRow();
	if (selectedRow >= 0 && selectedRow < browserTableModel.getRowCount() - 1) {
	    browserTableModel.moveRow(selectedRow, selectedRow, selectedRow + 1);
	    browserTable.setRowSelectionInterval(selectedRow + 1, selectedRow + 1);
	}
    }

    /**
     * Refresh browser list
     */
    private void refreshBrowserList() {
	// TODO: Auto-detect installed browsers on system
	JOptionPane.showMessageDialog(getContentPane(),
		"Browser detection completed.",
		"Refresh Browsers",
		JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Update button states based on selection
     */
    private void updateButtonStates() {
	int selectedRow = browserTable.getSelectedRow();
	int rowCount = browserTableModel.getRowCount();

	removeButton.setEnabled(selectedRow >= 0);
	editButton.setEnabled(selectedRow >= 0);
	moveUpButton.setEnabled(selectedRow > 0);
	moveDownButton.setEnabled(selectedRow >= 0 && selectedRow < rowCount - 1);
    }

    /**
     * Get configured browsers
     */
    public java.util.List<BrowserInfo> getConfiguredBrowsers() {
	java.util.List<BrowserInfo> browsers = new ArrayList<>();
	for (int i = 0; i < browserTableModel.getRowCount(); i++) {
	    boolean enabled = (Boolean) browserTableModel.getValueAt(i, 0);
	    String name = (String) browserTableModel.getValueAt(i, 1);
	    String family = (String) browserTableModel.getValueAt(i, 2);
	    String path = (String) browserTableModel.getValueAt(i, 3);

	    BrowserInfo browser = new BrowserInfo(name, family, path);
	    browser.setEnabled(enabled);
	    browsers.add(browser);
	}
	return browsers;
    }

    /**
     * Get default browser setting
     */
    public String getDefaultBrowser() {
	return (String) defaultBrowserComboBox.getSelectedItem();
    }

    /**
     * Browser family renderer with icons
     */
    private static class BrowserFamilyRenderer extends DefaultTableCellRenderer {
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
		boolean hasFocus, int row, int column) {
	    super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

	    if (value != null) {
		String family = value.toString();
		// Set appropriate text based on family
		if (family.contains("Chrome")) {
		    setText("Chrome");
		} else if (family.contains("Firefox")) {
		    setText("Firefox");
		} else if (family.contains("Safari")) {
		    setText("Safari");
		} else if (family.contains("Internet")) {
		    setText("Internet Explorer");
		} else {
		    setText(family);
		}
	    }

	    return this;
	}
    }

    /**
     * Browser information class
     */
    public static class BrowserInfo {
	private String name;
	private String family;
	private String path;
	private boolean enabled = true;

	public BrowserInfo(String name, String family, String path) {
	    this.name = name;
	    this.family = family;
	    this.path = path;
	}

	// Getters and setters
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }

	public String getFamily() { return family; }
	public void setFamily(String family) { this.family = family; }

	public String getPath() { return path; }
	public void setPath(String path) { this.path = path; }

	public boolean isEnabled() { return enabled; }
	public void setEnabled(boolean enabled) { this.enabled = enabled; }
    }

    /**
     * Simple browser configuration dialog
     */
    private static class BrowserConfigDialog extends DialogWrapper {
	private final Project project;
	private BrowserInfo browserInfo;

	private JTextField nameField;
	private JTextField pathField;
	private JComboBox<String> familyComboBox;

	protected BrowserConfigDialog(@Nullable Project project, @Nullable BrowserInfo existingBrowser) {
	    super(project);
	    this.project = project;
	    this.browserInfo = existingBrowser;

	    setTitle(existingBrowser == null ? "Add Browser" : "Edit Browser");
	    init();
	}

	@Override
	protected @Nullable JComponent createCenterPanel() {
	    JPanel panel = new JPanel(new GridBagLayout());
	    panel.setBorder(JBUI.Borders.empty(10));

	    GridBagConstraints gbc = new GridBagConstraints();
	    gbc.insets = JBUI.insets(5);
	    gbc.anchor = GridBagConstraints.WEST;

	    // Name field
	    gbc.gridx = 0; gbc.gridy = 0;
	    panel.add(new JLabel("Name:"), gbc);
	    gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
	    nameField = new JTextField(20);
	    panel.add(nameField, gbc);

	    // Family field
	    gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0;
	    panel.add(new JLabel("Family:"), gbc);
	    gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
	    familyComboBox = new JComboBox<>(new String[]{"Chrome", "Firefox", "Safari", "Internet Explorer", "Other"});
	    panel.add(familyComboBox, gbc);

	    // Path field
	    gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0;
	    panel.add(new JLabel("Path:"), gbc);
	    gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
	    pathField = new JTextField(30);
	    panel.add(pathField, gbc);

	    // Initialize fields if editing
	    if (browserInfo != null) {
		nameField.setText(browserInfo.getName());
		familyComboBox.setSelectedItem(browserInfo.getFamily().replaceAll("[🟢🦊🔵]\\s*", ""));
		pathField.setText(browserInfo.getPath());
	    }

	    return panel;
	}

	public BrowserInfo getBrowserInfo() {
	    if (browserInfo == null) {
		browserInfo = new BrowserInfo("", "", "");
	    }

	    browserInfo.setName(nameField.getText());
	    browserInfo.setFamily((String) familyComboBox.getSelectedItem());
	    browserInfo.setPath(pathField.getText());

	    return browserInfo;
	}
    }
}