/**
 * Author: Gezahegn Lemma (Gezu)
 * Project: Dev Tomcat Plugin
 * Created: 6/9/25
 * Phase 2: Logs configuration tab - Matches REAL Ultimate interface
 */

package com.poratu.idea.plugins.tomcat.ui;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.JBUI;
import com.poratu.idea.plugins.tomcat.conf.EnhancedTomcatRunConfiguration;
import com.poratu.idea.plugins.tomcat.logging.LogFileConfiguration;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

/**
 * Real Ultimate Logs Tab - matches actual Ultimate interface exactly
 * Shows "Log files to be shown in console" with checkboxes and Skip Content options
 */
public class LogsConfigurationTab extends JPanel {

    private final Project project;
    private final EnhancedTomcatRunConfiguration configuration;

    // Logs table and model
    private JBTable logsTable;
    private DefaultTableModel tableModel;

    // Management buttons
    private JButton addButton;
    private JButton removeButton;
    private JButton editButton;

    // Bottom options (like Ultimate)
    private JCheckBox saveConsoleOutputCheckBox;
    private JTextField saveToFileField;
    private JButton browseFileButton;
    private JCheckBox showConsoleStdOutCheckBox;
    private JCheckBox showConsoleStdErrCheckBox;

    public LogsConfigurationTab(@NotNull Project project, EnhancedTomcatRunConfiguration configuration) {
        this.project = project;
        this.configuration = configuration;
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setBorder(JBUI.Borders.empty(10));

        // Create main panel
        JPanel mainPanel = new JPanel(new BorderLayout());

        // Add main logs section
        mainPanel.add(createLogsSection(), BorderLayout.CENTER);

        // Add bottom options section (like Ultimate)
        mainPanel.add(createBottomOptionsSection(), BorderLayout.SOUTH);

        add(mainPanel, BorderLayout.CENTER);
    }

    /**
     * Create the main logs section - matches Ultimate exactly
     */
    private JPanel createLogsSection() {
        JPanel panel = new JPanel(new BorderLayout());

        // Section title (like Ultimate)
        JLabel titleLabel = new JLabel("Log files to be shown in console");
        titleLabel.setBorder(JBUI.Borders.emptyBottom(5));
        panel.add(titleLabel, BorderLayout.NORTH);

        // Create center panel with table and buttons
        JPanel centerPanel = new JPanel(new BorderLayout());

        // Create table area
        JPanel tablePanel = createLogsTablePanel();
        centerPanel.add(tablePanel, BorderLayout.CENTER);

        // Create buttons panel (vertical, on the right like Ultimate)
        JPanel buttonsPanel = createButtonsPanel();
        centerPanel.add(buttonsPanel, BorderLayout.EAST);

        panel.add(centerPanel, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Create the logs table - matches Ultimate's log files table with checkboxes
     */
    private JPanel createLogsTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Create table model with Ultimate's exact columns
        String[] columnNames = {"Is Active", "Log File Entry", "Skip Content"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public Class<?> getColumnClass(int column) {
                switch (column) {
                    case 0: // Is Active
                    case 2: // Skip Content
                        return Boolean.class;
                    case 1: // Log File Entry
                        return String.class;
                    default:
                        return Object.class;
                }
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0 || column == 2; // Only checkboxes are editable
            }
        };

        // Create table
        logsTable = new JBTable(tableModel);
        logsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        logsTable.getTableHeader().setReorderingAllowed(false);

        // Set column widths like Ultimate
        logsTable.getColumnModel().getColumn(0).setPreferredWidth(80);  // Is Active checkbox
        logsTable.getColumnModel().getColumn(1).setPreferredWidth(300); // Log File Entry
        logsTable.getColumnModel().getColumn(2).setPreferredWidth(100); // Skip Content checkbox

        // Add default Tomcat log files (like Ultimate)
        addDefaultLogFiles();

        // Wrap in scroll pane
        JScrollPane scrollPane = new JScrollPane(logsTable);
        scrollPane.setPreferredSize(new Dimension(500, 150));

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Add default Tomcat log files (matches Ultimate's default logs)
     */
    private void addDefaultLogFiles() {
        // Tomcat Localhost Log (active by default)
        tableModel.addRow(new Object[]{true, "Tomcat Localhost Log", false});

        // Tomcat Catalina Log (active by default)
        tableModel.addRow(new Object[]{true, "Tomcat Catalina Log", false});

        // Tomcat Manager Log (inactive by default)
        tableModel.addRow(new Object[]{false, "Tomcat Manager Log", false});

        // Tomcat Host Manager Log (inactive by default)
        tableModel.addRow(new Object[]{false, "Tomcat Host Manager Log", false});

        // Tomcat Localhost Access Log (inactive by default)
        tableModel.addRow(new Object[]{false, "Tomcat Localhost Access Log", false});
    }

    /**
     * Create buttons panel - matches Ultimate's vertical button layout
     */
    private JPanel createButtonsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(JBUI.Borders.emptyLeft(10));

        // Create buttons (same as Ultimate)
        addButton = new JButton("+");
        removeButton = new JButton("-");
        editButton = new JButton("✏");

        // Set button sizes
        Dimension buttonSize = new Dimension(30, 25);
        addButton.setPreferredSize(buttonSize);
        removeButton.setPreferredSize(buttonSize);
        editButton.setPreferredSize(buttonSize);

        // Add tooltips like Ultimate
        addButton.setToolTipText("Add log file");
        removeButton.setToolTipText("Remove log file");
        editButton.setToolTipText("Edit log file");

        // Add action listeners
        addButton.addActionListener(e -> addLogFile());
        removeButton.addActionListener(e -> removeLogFile());
        editButton.addActionListener(e -> editLogFile());

        // Add buttons to panel
        panel.add(addButton);
        panel.add(Box.createVerticalStrut(2));
        panel.add(removeButton);
        panel.add(Box.createVerticalStrut(5));
        panel.add(editButton);
        panel.add(Box.createVerticalGlue());

        // Update button states
        updateButtonStates();

        // Add selection listener to update button states
        logsTable.getSelectionModel().addListSelectionListener(e -> updateButtonStates());

        return panel;
    }

    /**
     * Create bottom options section - matches Ultimate's bottom section exactly
     */
    private JPanel createBottomOptionsSection() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(JBUI.Borders.emptyTop(20));

        // Save console output to file option
        JPanel saveToFilePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        saveConsoleOutputCheckBox = new JCheckBox("Save console output to file:");
        saveToFileField = new JTextField(30);
        saveToFileField.setEnabled(false);
        browseFileButton = new JButton("...");
        browseFileButton.setEnabled(false);

        saveConsoleOutputCheckBox.addActionListener(e -> {
            boolean enabled = saveConsoleOutputCheckBox.isSelected();
            saveToFileField.setEnabled(enabled);
            browseFileButton.setEnabled(enabled);
        });

        saveToFilePanel.add(saveConsoleOutputCheckBox);
        saveToFilePanel.add(saveToFileField);
        saveToFilePanel.add(browseFileButton);

        // Show console options (like Ultimate)
        showConsoleStdOutCheckBox = new JCheckBox("Show console when a message is printed to standard output stream");
        showConsoleStdErrCheckBox = new JCheckBox("Show console when a message is printed to standard error stream");

        panel.add(saveToFilePanel);
        panel.add(showConsoleStdOutCheckBox);
        panel.add(showConsoleStdErrCheckBox);

        return panel;
    }

    /**
     * Add new log file - shows dialog like Ultimate
     */
    private void addLogFile() {
        // TODO: Show Ultimate-style log file configuration dialog
        // For now, add a placeholder
        String logFileName = JOptionPane.showInputDialog(getParent(),
                "Enter log file name:",
                "Add Log File",
                JOptionPane.QUESTION_MESSAGE);

        if (logFileName != null && !logFileName.trim().isEmpty()) {
            tableModel.addRow(new Object[]{true, logFileName.trim(), false});
        }

        System.out.println("DevTomcat: Add log file dialog (Ultimate-style) - TODO");
    }

    /**
     * Remove selected log file
     */
    private void removeLogFile() {
        int selectedRow = logsTable.getSelectedRow();
        if (selectedRow >= 0) {
            // Don't allow removing default Tomcat logs
            String logName = (String) tableModel.getValueAt(selectedRow, 1);
            if (isDefaultTomcatLog(logName)) {
                JOptionPane.showMessageDialog(getParent(),
                        "Cannot remove default Tomcat log files",
                        "Remove Log File",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            tableModel.removeRow(selectedRow);
            updateButtonStates();
        }
    }

    /**
     * Edit selected log file
     */
    private void editLogFile() {
        int selectedRow = logsTable.getSelectedRow();
        if (selectedRow >= 0) {
            // TODO: Show Ultimate-style log file edit dialog
            System.out.println("DevTomcat: Edit log file dialog (Ultimate-style) - TODO");
        }
    }

    /**
     * Check if this is a default Tomcat log that shouldn't be removed
     */
    private boolean isDefaultTomcatLog(String logName) {
        return logName.startsWith("Tomcat ");
    }

    /**
     * Update button enabled states based on selection
     */
    private void updateButtonStates() {
        int selectedRow = logsTable.getSelectedRow();

        // Enable/disable buttons based on selection
        removeButton.setEnabled(selectedRow >= 0);
        editButton.setEnabled(selectedRow >= 0);

        // Disable remove for default Tomcat logs
        if (selectedRow >= 0) {
            String logName = (String) tableModel.getValueAt(selectedRow, 1);
            removeButton.setEnabled(!isDefaultTomcatLog(logName));
        }
    }

    /**
     * Reset from configuration - loads log file configurations
     */
    public void resetFrom(@NotNull EnhancedTomcatRunConfiguration configuration) {
        // Clear existing custom logs (keep default Tomcat logs)
        clearCustomLogFiles();

        // Load log file configurations from Enhanced config
        for (LogFileConfiguration logConfig : configuration.getLogFileConfigurations()) {
            boolean isActive = logConfig.isActive();
            String logName = logConfig.getDisplayName();
            boolean skipContent = logConfig.isSkipContent();

            // Check if this is already in the default logs
            boolean isDefault = false;
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                String existingName = (String) tableModel.getValueAt(i, 1);
                if (existingName.equals(logName)) {
                    // Update existing default log
                    tableModel.setValueAt(isActive, i, 0);
                    tableModel.setValueAt(skipContent, i, 2);
                    isDefault = true;
                    break;
                }
            }

            // Add as custom log if not default
            if (!isDefault) {
                tableModel.addRow(new Object[]{isActive, logName, skipContent});
            }
        }

        // Reset bottom options
        saveConsoleOutputCheckBox.setSelected(false);
        saveToFileField.setText("");
        saveToFileField.setEnabled(false);
        browseFileButton.setEnabled(false);
        showConsoleStdOutCheckBox.setSelected(false);
        showConsoleStdErrCheckBox.setSelected(false);

        updateButtonStates();

        System.out.println("DevTomcat: Reset logs configuration from Enhanced config");
    }

    /**
     * Apply to configuration - saves log configurations
     */
    public void applyTo(@NotNull EnhancedTomcatRunConfiguration configuration) throws ConfigurationException {
        // Create log file configurations from table
        java.util.List<LogFileConfiguration> logConfigs = new java.util.ArrayList<>();

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            boolean isActive = (Boolean) tableModel.getValueAt(i, 0);
            String logName = (String) tableModel.getValueAt(i, 1);
            boolean skipContent = (Boolean) tableModel.getValueAt(i, 2);

            // Create LogFileConfiguration
            LogFileConfiguration logConfig = new LogFileConfiguration();
            logConfig.setAlias(logName);
            logConfig.setActive(isActive);
            logConfig.setSkipContent(skipContent);
            logConfig.setShowAllMessages(!skipContent);

            // Set appropriate file path based on log name
            if (logName.contains("Localhost")) {
                logConfig.setFilePath("$CATALINA_BASE/logs/localhost.$DATE.log");
            } else if (logName.contains("Catalina")) {
                logConfig.setFilePath("$CATALINA_BASE/logs/catalina.out");
            } else if (logName.contains("Manager")) {
                logConfig.setFilePath("$CATALINA_BASE/logs/manager.$DATE.log");
            } else if (logName.contains("Host Manager")) {
                logConfig.setFilePath("$CATALINA_BASE/logs/host-manager.$DATE.log");
            } else if (logName.contains("Access")) {
                logConfig.setFilePath("$CATALINA_BASE/logs/localhost_access_log.$DATE.txt");
            } else {
                // Custom log file - use name as path for now
                logConfig.setFilePath(logName);
            }

            logConfigs.add(logConfig);
        }

        // Apply to configuration
        configuration.setLogFileConfigurations(logConfigs);

        System.out.println("DevTomcat: Applied " + logConfigs.size() + " log file configurations");
    }

    /**
     * Clear custom log files but keep default Tomcat logs
     */
    private void clearCustomLogFiles() {
        for (int i = tableModel.getRowCount() - 1; i >= 0; i--) {
            String logName = (String) tableModel.getValueAt(i, 1);
            if (!isDefaultTomcatLog(logName)) {
                tableModel.removeRow(i);
            }
        }
    }

    /**
     * Get count of active log files
     */
    public int getActiveLogFileCount() {
        int count = 0;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if ((Boolean) tableModel.getValueAt(i, 0)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Check if any log files are configured
     */
    public boolean hasLogFiles() {
        return tableModel.getRowCount() > 0;
    }
}