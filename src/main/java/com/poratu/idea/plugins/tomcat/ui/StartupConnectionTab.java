/**
 * Author: Gezahegn Lemma (Gezu)
 * Project: Dev Tomcat Plugin
 * Created: 6/9/25
 * Phase 2: Startup/Connection configuration tab - Matches REAL Ultimate interface
 */

package com.poratu.idea.plugins.tomcat.ui;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.JBUI;
import com.poratu.idea.plugins.tomcat.conf.EnhancedTomcatRunConfiguration;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Map;

/**
 * Real Ultimate Startup/Connection Tab - matches actual Ultimate interface exactly
 * Shows startup script, shutdown script, environment variables, and other connection settings
 */
public class StartupConnectionTab extends JPanel {

    // Startup script section (UI only - values not used in Phase 2)
    private TextFieldWithBrowseButton startupScriptField;
    private JCheckBox useDefaultStartupCheckBox;

    // Shutdown script section (UI only - values not used in Phase 2)
    private TextFieldWithBrowseButton shutdownScriptField;
    private JCheckBox useDefaultShutdownCheckBox;

    // Environment variables section (actively used)
    private JBTable environmentTable;
    private DefaultTableModel envTableModel;
    private JButton addEnvButton;
    private JButton removeEnvButton;
    private JCheckBox passParentEnvsCheckBox;

    public StartupConnectionTab(@NotNull Project project, EnhancedTomcatRunConfiguration configuration) {
        // Note: configuration can be null during initial creation
        // project and configuration parameters are kept for API compatibility
        // but are not stored as fields since they're not used in this tab
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setBorder(JBUI.Borders.empty(10));

        // Create main panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        // Add startup script section
        mainPanel.add(createStartupScriptSection());
        mainPanel.add(Box.createVerticalStrut(15));

        // Add shutdown script section
        mainPanel.add(createShutdownScriptSection());
        mainPanel.add(Box.createVerticalStrut(15));

        // Add environment variables section
        mainPanel.add(createEnvironmentVariablesSection());

        // Add to scroll pane
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * Create startup script section - matches Ultimate's startup script configuration
     */
    private JPanel createStartupScriptSection() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Startup script"));

        JPanel contentPanel = new JPanel(new BorderLayout());

        // Use default checkbox
        useDefaultStartupCheckBox = new JCheckBox("Use default", true);
        useDefaultStartupCheckBox.addActionListener(e -> updateStartupScriptState());
        contentPanel.add(useDefaultStartupCheckBox, BorderLayout.NORTH);

        // Script path field
        startupScriptField = new TextFieldWithBrowseButton();
        startupScriptField.setToolTipText("Path to startup script");
        startupScriptField.setText("C:\\apache-tomcat-10.1.15\\bin\\catalina.bat run");
        startupScriptField.setEnabled(false); // Initially disabled when "Use default" is checked

        contentPanel.add(startupScriptField, BorderLayout.CENTER);
        panel.add(contentPanel, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Create shutdown script section - matches Ultimate's shutdown script configuration
     */
    private JPanel createShutdownScriptSection() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Shutdown script"));

        JPanel contentPanel = new JPanel(new BorderLayout());

        // Use default checkbox
        useDefaultShutdownCheckBox = new JCheckBox("Use default", true);
        useDefaultShutdownCheckBox.addActionListener(e -> updateShutdownScriptState());
        contentPanel.add(useDefaultShutdownCheckBox, BorderLayout.NORTH);

        // Script path field
        shutdownScriptField = new TextFieldWithBrowseButton();
        shutdownScriptField.setToolTipText("Path to shutdown script");
        shutdownScriptField.setText("C:\\apache-tomcat-10.1.15\\bin\\catalina.bat stop");
        shutdownScriptField.setEnabled(false); // Initially disabled when "Use default" is checked

        contentPanel.add(shutdownScriptField, BorderLayout.CENTER);
        panel.add(contentPanel, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Create environment variables section - matches Ultimate's environment variables
     */
    private JPanel createEnvironmentVariablesSection() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Environment Variables"));

        // Pass parent environments checkbox
        passParentEnvsCheckBox = new JCheckBox("Pass environment variables", true);
        panel.add(passParentEnvsCheckBox, BorderLayout.NORTH);

        // Environment variables table
        JPanel tablePanel = new JPanel(new BorderLayout());

        // Create table model
        String[] columnNames = {"Name", "Value"};
        envTableModel = new DefaultTableModel(columnNames, 0);

        environmentTable = new JBTable(envTableModel);
        environmentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        environmentTable.getColumnModel().getColumn(0).setPreferredWidth(150);
        environmentTable.getColumnModel().getColumn(1).setPreferredWidth(300);

        JScrollPane envScrollPane = new JScrollPane(environmentTable);
        envScrollPane.setPreferredSize(new Dimension(500, 120));
        tablePanel.add(envScrollPane, BorderLayout.CENTER);

        // Environment variables buttons
        JPanel envButtonsPanel = new JPanel();
        envButtonsPanel.setLayout(new BoxLayout(envButtonsPanel, BoxLayout.Y_AXIS));

        addEnvButton = new JButton("+");
        removeEnvButton = new JButton("-");

        addEnvButton.setPreferredSize(new Dimension(30, 25));
        removeEnvButton.setPreferredSize(new Dimension(30, 25));

        addEnvButton.addActionListener(e -> addEnvironmentVariable());
        removeEnvButton.addActionListener(e -> removeEnvironmentVariable());

        envButtonsPanel.add(addEnvButton);
        envButtonsPanel.add(Box.createVerticalStrut(2));
        envButtonsPanel.add(removeEnvButton);
        envButtonsPanel.add(Box.createVerticalGlue());

        tablePanel.add(envButtonsPanel, BorderLayout.EAST);
        panel.add(tablePanel, BorderLayout.CENTER);

        // Initialize empty environment variables like Ultimate
        initializeEnvironmentVariables();

        // Update button states
        updateEnvButtonStates();
        environmentTable.getSelectionModel().addListSelectionListener(e -> updateEnvButtonStates());

        return panel;
    }

    /**
     * Initialize environment variables - start empty like Ultimate shows
     */
    private void initializeEnvironmentVariables() {
        // Start with empty environment variables like Ultimate shows "No variables"
        // Don't add any default variables here - let the configuration populate them
    }

    /**
     * Add new environment variable
     */
    private void addEnvironmentVariable() {
        // Show dialog to add new environment variable
        JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
        JTextField nameField = new JTextField();
        JTextField valueField = new JTextField();

        panel.add(new JLabel("Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Value:"));
        panel.add(valueField);

        int result = JOptionPane.showConfirmDialog(getParent(), panel, "Add Environment Variable",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            String value = valueField.getText().trim();

            if (!name.isEmpty()) {
                envTableModel.addRow(new Object[]{name, value});
                updateEnvButtonStates();
            }
        }
    }

    /**
     * Remove selected environment variable
     */
    private void removeEnvironmentVariable() {
        int selectedRow = environmentTable.getSelectedRow();
        if (selectedRow >= 0) {
            envTableModel.removeRow(selectedRow);
            updateEnvButtonStates();
        }
    }

    /**
     * Update startup script field state
     */
    private void updateStartupScriptState() {
        boolean useDefault = useDefaultStartupCheckBox.isSelected();
        startupScriptField.setEnabled(!useDefault);

        if (useDefault) {
            startupScriptField.setText("C:\\apache-tomcat-10.1.15\\bin\\catalina.bat run");
        }
    }

    /**
     * Update shutdown script field state
     */
    private void updateShutdownScriptState() {
        boolean useDefault = useDefaultShutdownCheckBox.isSelected();
        shutdownScriptField.setEnabled(!useDefault);

        if (useDefault) {
            shutdownScriptField.setText("C:\\apache-tomcat-10.1.15\\bin\\catalina.bat stop");
        }
    }

    /**
     * Update environment variables button states
     */
    private void updateEnvButtonStates() {
        int selectedRow = environmentTable.getSelectedRow();
        removeEnvButton.setEnabled(selectedRow >= 0);
    }

    /**
     * Reset from configuration
     */
    public void resetFrom(@NotNull EnhancedTomcatRunConfiguration configuration) {
        try {
            // Reset startup/shutdown scripts
            useDefaultStartupCheckBox.setSelected(true);
            useDefaultShutdownCheckBox.setSelected(true);
            updateStartupScriptState();
            updateShutdownScriptState();

            // Reset environment variables - start empty like Ultimate
            envTableModel.setRowCount(0);

            // Load environment variables from configuration (if not null)
            if (configuration != null) {
                Map<String, String> envVars = configuration.getEnvironmentVariables();
                if (envVars != null) {
                    for (Map.Entry<String, String> entry : envVars.entrySet()) {
                        envTableModel.addRow(new Object[]{entry.getKey(), entry.getValue()});
                    }
                }
            }

            passParentEnvsCheckBox.setSelected(true);
            updateEnvButtonStates();

            System.out.println("DevTomcat: Reset startup/connection configuration from Enhanced config - " +
                    (configuration != null ? configuration.getEnvironmentVariables().size() : 0) + " environment variables loaded");
        } catch (Exception e) {
            System.err.println("DevTomcat: Error resetting startup/connection configuration: " + e.getMessage());
        }
    }

    /**
     * Apply to configuration
     */
    public void applyTo(@NotNull EnhancedTomcatRunConfiguration configuration) throws ConfigurationException {
        try {
            // Apply environment variables (the only values actually used)
            Map<String, String> envVars = new java.util.HashMap<>();
            for (int i = 0; i < envTableModel.getRowCount(); i++) {
                String name = (String) envTableModel.getValueAt(i, 0);
                String value = (String) envTableModel.getValueAt(i, 1);
                if (name != null && !name.trim().isEmpty()) {
                    envVars.put(name.trim(), value != null ? value.trim() : "");
                }
            }
            configuration.setEnvironmentVariables(envVars);

            // Note: Startup/shutdown script values are not applied because:
            // 1. The EnhancedTomcatRunConfiguration doesn't have setters for these yet
            // 2. The functionality is not implemented in Phase 2
            // 3. The UI fields exist only to match Ultimate's interface

            System.out.println("DevTomcat: Applied startup/connection configuration with " +
                    envVars.size() + " environment variables");
        } catch (Exception e) {
            throw new ConfigurationException("Failed to apply startup/connection configuration: " + e.getMessage());
        }
    }

    /**
     * Get environment variable count
     */
    public int getEnvironmentVariableCount() {
        return envTableModel.getRowCount();
    }

    /**
     * Check if custom startup script is configured
     */
    public boolean hasCustomStartupScript() {
        return !useDefaultStartupCheckBox.isSelected();
    }

    /**
     * Check if custom shutdown script is configured
     */
    public boolean hasCustomShutdownScript() {
        return !useDefaultShutdownCheckBox.isSelected();
    }

    /**
     * Get configured startup script path (UI value only - not used in Phase 2)
     */
    public String getStartupScriptPath() {
        return useDefaultStartupCheckBox.isSelected() ? null : startupScriptField.getText();
    }

    /**
     * Get configured shutdown script path (UI value only - not used in Phase 2)
     */
    public String getShutdownScriptPath() {
        return useDefaultShutdownCheckBox.isSelected() ? null : shutdownScriptField.getText();
    }

    /**
     * Check if pass parent environment variables is enabled
     */
    public boolean isPassParentEnvironmentVariables() {
        return passParentEnvsCheckBox.isSelected();
    }
}