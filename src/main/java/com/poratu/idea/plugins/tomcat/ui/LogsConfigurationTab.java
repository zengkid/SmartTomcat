package com.poratu.idea.plugins.tomcat.ui;

import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.AnActionButton;
import com.intellij.ui.AnActionButtonRunnable;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.JBUI;
import com.poratu.idea.plugins.tomcat.conf.TomcatRunConfiguration;
import com.poratu.idea.plugins.tomcat.logging.LogFileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Phase 2: Logs configuration tab - Ultimate-style log file management
 * Allows users to configure multiple log files to be displayed in separate tabs
 */
public class LogsConfigurationTab extends JPanel {

    private final Project project;
    @Nullable
    private final TomcatRunConfiguration configuration;

    // UI Components
    private JBTable logFilesTable;
    private LogFilesTableModel tableModel;
    private JCheckBox enableLoggingCheckBox;
    private JCheckBox skipContentCheckBox;
    private JCheckBox showAllMessagesCheckBox;

    // Log file configurations
    private List<LogFileConfiguration> logFileConfigurations;

    public LogsConfigurationTab(@NotNull Project project, @Nullable TomcatRunConfiguration configuration) {
        this.project = project;
        this.configuration = configuration;
        this.logFileConfigurations = new ArrayList<>();

        // Initialize with default Tomcat log files
        initializeDefaultLogFiles();

        setLayout(new BorderLayout());
        initializeUI();
    }

    /**
     * Initialize default Tomcat log files
     */
    // In your LogsConfigurationTab.java, replace the initializeDefaultLogFiles() method:

    private void initializeDefaultLogFiles() {
        logFileConfigurations.add(new LogFileConfiguration(
                "Catalina",
                "$CATALINA_BASE/logs/catalina.out",
                true,
                "Main Tomcat server log",
                false,  // skipContent
                true    // showAllMessages
        ));

        logFileConfigurations.add(new LogFileConfiguration(
                "Localhost",
                "$CATALINA_BASE/logs/localhost.$DATE.log",
                true,
                "Localhost application log",
                false,  // skipContent
                true    // showAllMessages
        ));

        logFileConfigurations.add(new LogFileConfiguration(
                "Manager",
                "$CATALINA_BASE/logs/manager.$DATE.log",
                false,
                "Tomcat Manager application log",
                false,  // skipContent
                true    // showAllMessages
        ));

        logFileConfigurations.add(new LogFileConfiguration(
                "Host-Manager",
                "$CATALINA_BASE/logs/host-manager.$DATE.log",
                false,
                "Tomcat Host Manager log",
                false,  // skipContent
                true    // showAllMessages
        ));
    }

    /**
     * Initialize the user interface
     */
    private void initializeUI() {
        // Create main panel with border
        setBorder(JBUI.Borders.empty(10));

        // Create options panel
        JPanel optionsPanel = createOptionsPanel();
        add(optionsPanel, BorderLayout.NORTH);

        // Create log files table panel
        JPanel tablePanel = createLogFilesTablePanel();
        add(tablePanel, BorderLayout.CENTER);

        // Create help panel
        JPanel helpPanel = createHelpPanel();
        add(helpPanel, BorderLayout.SOUTH);
    }

    /**
     * Create options panel with checkboxes
     */
    private JPanel createOptionsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createTitledBorder("Log Display Options"));

        enableLoggingCheckBox = new JCheckBox("Save console output to file", true);
        enableLoggingCheckBox.setToolTipText("Save all console output to a log file");

        skipContentCheckBox = new JCheckBox("Skip content", false);
        skipContentCheckBox.setToolTipText("Skip file content when the file is too large");

        showAllMessagesCheckBox = new JCheckBox("Show all messages", true);
        showAllMessagesCheckBox.setToolTipText("Show all log messages including debug information");

        panel.add(enableLoggingCheckBox);
        panel.add(skipContentCheckBox);
        panel.add(showAllMessagesCheckBox);

        return panel;
    }

    /**
     * Create log files table panel
     */
    private JPanel createLogFilesTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Log Files"));

        // Create table model and table
        tableModel = new LogFilesTableModel();
        logFilesTable = new JBTable(tableModel);
        logFilesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Configure table columns
        logFilesTable.getColumnModel().getColumn(0).setPreferredWidth(80);   // Is Active
        logFilesTable.getColumnModel().getColumn(1).setPreferredWidth(100);  // Alias
        logFilesTable.getColumnModel().getColumn(2).setPreferredWidth(300);  // Log File Path
        logFilesTable.getColumnModel().getColumn(3).setPreferredWidth(200);  // Description

        // Create toolbar with add/remove buttons
        ToolbarDecorator decorator = ToolbarDecorator.createDecorator(logFilesTable)
                .setAddAction(new AnActionButtonRunnable() {
                    @Override
                    public void run(AnActionButton button) {
                        addLogFile();
                    }
                })
                .setRemoveAction(new AnActionButtonRunnable() {
                    @Override
                    public void run(AnActionButton button) {
                        removeSelectedLogFile();
                    }
                })
                .setEditAction(new AnActionButtonRunnable() {
                    @Override
                    public void run(AnActionButton button) {
                        editSelectedLogFile();
                    }
                });

        panel.add(decorator.createPanel(), BorderLayout.CENTER);

        return panel;
    }

    /**
     * Create help panel with information
     */
    private JPanel createHelpPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JTextArea helpText = new JTextArea();
        helpText.setEditable(false);
        helpText.setOpaque(false);
        helpText.setFont(helpText.getFont().deriveFont(Font.ITALIC));
        helpText.setText(
                "Variables: $CATALINA_BASE, $CATALINA_HOME, $DATE (current date in yyyy-MM-dd format)\n" +
                        "Example: $CATALINA_BASE/logs/catalina.out, $CATALINA_BASE/logs/localhost.$DATE.log\n" +
                        "Active log files will be displayed as separate tabs in the console."
        );

        panel.add(helpText, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Add new log file configuration
     */
    private void addLogFile() {
        LogFileConfigurationDialog dialog = new LogFileConfigurationDialog(project);
        if (dialog.showAndGet()) {
            LogFileConfiguration config = dialog.getLogFileConfiguration();
            logFileConfigurations.add(config);
            tableModel.fireTableDataChanged();
        }
    }

    /**
     * Remove selected log file configuration
     */
    private void removeSelectedLogFile() {
        int selectedRow = logFilesTable.getSelectedRow();
        if (selectedRow >= 0 && selectedRow < logFileConfigurations.size()) {
            logFileConfigurations.remove(selectedRow);
            tableModel.fireTableDataChanged();
        }
    }

    /**
     * Edit selected log file configuration
     */
    private void editSelectedLogFile() {
        int selectedRow = logFilesTable.getSelectedRow();
        if (selectedRow >= 0 && selectedRow < logFileConfigurations.size()) {
            LogFileConfiguration config = logFileConfigurations.get(selectedRow);
            LogFileConfigurationDialog dialog = new LogFileConfigurationDialog(project, config);
            if (dialog.showAndGet()) {
                LogFileConfiguration updatedConfig = dialog.getLogFileConfiguration();
                logFileConfigurations.set(selectedRow, updatedConfig);
                tableModel.fireTableDataChanged();
            }
        }
    }

    /**
     * Reset configuration from EnhancedTomcatRunConfiguration
     */
    public void resetFrom(@NotNull TomcatRunConfiguration configuration) {
        if (configuration instanceof com.poratu.idea.plugins.tomcat.conf.EnhancedTomcatRunConfiguration) {
            com.poratu.idea.plugins.tomcat.conf.EnhancedTomcatRunConfiguration enhancedConfig =
                    (com.poratu.idea.plugins.tomcat.conf.EnhancedTomcatRunConfiguration) configuration;

            // Reset log file configurations
            this.logFileConfigurations.clear();
            this.logFileConfigurations.addAll(enhancedConfig.getLogFileConfigurations());

            // Reset options
            enableLoggingCheckBox.setSelected(enhancedConfig.isLoggingEnabled());
            skipContentCheckBox.setSelected(enhancedConfig.isSkipContent());
            showAllMessagesCheckBox.setSelected(enhancedConfig.isShowAllMessages());

            // Refresh table
            tableModel.fireTableDataChanged();
        }
    }

    /**
     * Apply configuration to EnhancedTomcatRunConfiguration
     */
    public void applyTo(@NotNull TomcatRunConfiguration configuration) throws com.intellij.openapi.options.ConfigurationException {
        if (configuration instanceof com.poratu.idea.plugins.tomcat.conf.EnhancedTomcatRunConfiguration) {
            com.poratu.idea.plugins.tomcat.conf.EnhancedTomcatRunConfiguration enhancedConfig =
                    (com.poratu.idea.plugins.tomcat.conf.EnhancedTomcatRunConfiguration) configuration;

            // Validate log file configurations
            for (LogFileConfiguration config : logFileConfigurations) {
                if (!config.isValid()) {
                    throw new com.intellij.openapi.options.ConfigurationException(
                            "Invalid log file configuration: " + config.getAlias());
                }
            }

            // Apply log file configurations
            enhancedConfig.setLogFileConfigurations(logFileConfigurations);

            // Apply options
            enhancedConfig.setLoggingEnabled(enableLoggingCheckBox.isSelected());
            enhancedConfig.setSkipContent(skipContentCheckBox.isSelected());
            enhancedConfig.setShowAllMessages(showAllMessagesCheckBox.isSelected());
        }
    }

    /**
     * Get current log file configurations
     */
    public List<LogFileConfiguration> getLogFileConfigurations() {
        return new ArrayList<>(logFileConfigurations);
    }

    /**
     * Check if logging is enabled
     */
    public boolean isLoggingEnabled() {
        return enableLoggingCheckBox.isSelected();
    }

    /**
     * Check if content should be skipped
     */
    public boolean isSkipContent() {
        return skipContentCheckBox.isSelected();
    }

    /**
     * Check if all messages should be shown
     */
    public boolean isShowAllMessages() {
        return showAllMessagesCheckBox.isSelected();
    }

    /**
     * Table model for log files configuration
     */
    private class LogFilesTableModel extends AbstractTableModel {

        private final String[] columnNames = {"Active", "Alias", "Log File Path", "Description"};

        @Override
        public int getRowCount() {
            return logFileConfigurations.size();
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public String getColumnName(int column) {
            return columnNames[column];
        }

        @Override
        public Class<?> getColumnClass(int column) {
            if (column == 0) {
                return Boolean.class;
            }
            return String.class;
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return column == 0; // Only the Active column is editable
        }

        @Override
        public Object getValueAt(int row, int column) {
            if (row < 0 || row >= logFileConfigurations.size()) {
                return null;
            }

            LogFileConfiguration config = logFileConfigurations.get(row);
            return switch (column) {
                case 0 -> config.isActive();
                case 1 -> config.getAlias();
                case 2 -> config.getFilePath();
                case 3 -> config.getDescription();
                default -> null;
            };
        }

        @Override
        public void setValueAt(Object value, int row, int column) {
            if (row < 0 || row >= logFileConfigurations.size()) {
                return;
            }

            LogFileConfiguration config = logFileConfigurations.get(row);
            if (column == 0 && value instanceof Boolean) {
                config.setActive((Boolean) value);
                fireTableCellUpdated(row, column);
            }
        }
    }
}