package com.poratu.idea.plugins.tomcat.ui;

import com.intellij.openapi.project.Project;
import com.intellij.ui.AnActionButton;
import com.intellij.ui.AnActionButtonRunnable;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.JBUI;
import com.poratu.idea.plugins.tomcat.conf.TomcatRunConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Phase 2: Startup/Connection configuration tab - Ultimate-style JMX and environment settings
 * Provides JMX configuration, environment variables, and connection settings
 */
public class StartupConnectionTab extends JPanel {

    private final Project project;
    @Nullable
    private final TomcatRunConfiguration configuration;

    // JMX Configuration
    private JCheckBox enableJmxCheckBox;
    private JTextField jmxPortField;
    private JTextField jmxHostField;
    private JCheckBox jmxSslCheckBox;
    private JCheckBox jmxAuthCheckBox;
    private JTextField jmxUsernameField;
    private JPasswordField jmxPasswordField;

    // Environment Variables
    private JBTable envVarsTable;
    private EnvironmentVariablesTableModel envTableModel;
    private Map<String, String> environmentVariables;

    // Connection Settings
    private JTextField connectionTimeoutField;
    private JTextField readTimeoutField;
    private JCheckBox enableRemoteDebuggingCheckBox;
    private JTextField debugPortField;

    public StartupConnectionTab(@NotNull Project project, @Nullable TomcatRunConfiguration configuration) {
        this.project = project;
        this.configuration = configuration;
        this.environmentVariables = new HashMap<>();

        setLayout(new BorderLayout());
        initializeUI();
        loadDefaultConfiguration();
    }

    /**
     * Initialize the user interface
     */
    private void initializeUI() {
        setBorder(JBUI.Borders.empty(10));

        // Create tabbed pane for different sections
        JTabbedPane tabbedPane = new JTabbedPane();

        // JMX Configuration tab
        tabbedPane.addTab("JMX", createJmxPanel());

        // Environment Variables tab
        tabbedPane.addTab("Environment", createEnvironmentPanel());

        // Connection Settings tab
        tabbedPane.addTab("Connection", createConnectionPanel());

        add(tabbedPane, BorderLayout.CENTER);
    }

    /**
     * Create JMX configuration panel
     */
    private JPanel createJmxPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(JBUI.Borders.empty(15));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = JBUI.insets(5);
        gbc.anchor = GridBagConstraints.WEST;

        // Enable JMX checkbox
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        enableJmxCheckBox = new JCheckBox("Enable JMX remote management", false);
        enableJmxCheckBox.setToolTipText("Enable JMX for remote server management and monitoring");
        enableJmxCheckBox.addActionListener(e -> updateJmxFieldsState());
        panel.add(enableJmxCheckBox, gbc);

        // JMX Host
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        panel.add(new JLabel("JMX Host:"), gbc);

        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        jmxHostField = new JTextField("localhost");
        jmxHostField.setToolTipText("JMX server host (default: localhost)");
        panel.add(jmxHostField, gbc);

        // JMX Port
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0;
        panel.add(new JLabel("JMX Port:"), gbc);

        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        jmxPortField = new JTextField("1099");
        jmxPortField.setToolTipText("JMX server port (default: 1099)");
        panel.add(jmxPortField, gbc);

        // JMX SSL
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.NONE;
        jmxSslCheckBox = new JCheckBox("Enable SSL", false);
        jmxSslCheckBox.setToolTipText("Use SSL for JMX connections");
        panel.add(jmxSslCheckBox, gbc);

        // JMX Authentication
        gbc.gridx = 0; gbc.gridy = 4;
        jmxAuthCheckBox = new JCheckBox("Enable Authentication", false);
        jmxAuthCheckBox.setToolTipText("Require authentication for JMX connections");
        jmxAuthCheckBox.addActionListener(e -> updateJmxAuthFieldsState());
        panel.add(jmxAuthCheckBox, gbc);

        // JMX Username
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 1;
        panel.add(new JLabel("Username:"), gbc);

        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        jmxUsernameField = new JTextField();
        jmxUsernameField.setToolTipText("JMX authentication username");
        panel.add(jmxUsernameField, gbc);

        // JMX Password
        gbc.gridx = 0; gbc.gridy = 6; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0;
        panel.add(new JLabel("Password:"), gbc);

        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        jmxPasswordField = new JPasswordField();
        jmxPasswordField.setToolTipText("JMX authentication password");
        panel.add(jmxPasswordField, gbc);

        // Add spacer
        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 2; gbc.weighty = 1.0;
        panel.add(new JPanel(), gbc);

        // Initially disable JMX fields
        updateJmxFieldsState();

        return panel;
    }

    /**
     * Create environment variables panel
     */
    private JPanel createEnvironmentPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(JBUI.Borders.empty(15));

        // Create table for environment variables
        envTableModel = new EnvironmentVariablesTableModel();
        envVarsTable = new JBTable(envTableModel);
        envVarsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Configure table columns
        envVarsTable.getColumnModel().getColumn(0).setPreferredWidth(200); // Name
        envVarsTable.getColumnModel().getColumn(1).setPreferredWidth(300); // Value

        // Create toolbar
        ToolbarDecorator decorator = ToolbarDecorator.createDecorator(envVarsTable)
                .setAddAction(new AnActionButtonRunnable() {
                    @Override
                    public void run(AnActionButton button) {
                        addEnvironmentVariable();
                    }
                })
                .setRemoveAction(new AnActionButtonRunnable() {
                    @Override
                    public void run(AnActionButton button) {
                        removeSelectedEnvironmentVariable();
                    }
                })
                .setEditAction(new AnActionButtonRunnable() {
                    @Override
                    public void run(AnActionButton button) {
                        editSelectedEnvironmentVariable();
                    }
                });

        panel.add(decorator.createPanel(), BorderLayout.CENTER);

        // Add default environment variables
        addDefaultEnvironmentVariables();

        return panel;
    }

    /**
     * Create connection settings panel
     */
    private JPanel createConnectionPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(JBUI.Borders.empty(15));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = JBUI.insets(5);
        gbc.anchor = GridBagConstraints.WEST;

        // Connection Timeout
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Connection Timeout (ms):"), gbc);

        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        connectionTimeoutField = new JTextField("30000");
        connectionTimeoutField.setToolTipText("Connection timeout in milliseconds (default: 30000)");
        panel.add(connectionTimeoutField, gbc);

        // Read Timeout
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0;
        panel.add(new JLabel("Read Timeout (ms):"), gbc);

        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        readTimeoutField = new JTextField("60000");
        readTimeoutField.setToolTipText("Read timeout in milliseconds (default: 60000)");
        panel.add(readTimeoutField, gbc);

        // Remote Debugging
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.NONE;
        enableRemoteDebuggingCheckBox = new JCheckBox("Enable Remote Debugging", false);
        enableRemoteDebuggingCheckBox.setToolTipText("Enable remote debugging for the Tomcat server");
        enableRemoteDebuggingCheckBox.addActionListener(e -> updateDebugFieldsState());
        panel.add(enableRemoteDebuggingCheckBox, gbc);

        // Debug Port
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 1;
        panel.add(new JLabel("Debug Port:"), gbc);

        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        debugPortField = new JTextField("5005");
        debugPortField.setToolTipText("Remote debugging port (default: 5005)");
        panel.add(debugPortField, gbc);

        // Add spacer
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2; gbc.weighty = 1.0;
        panel.add(new JPanel(), gbc);

        // Initially disable debug fields
        updateDebugFieldsState();

        return panel;
    }

    /**
     * Load default configuration
     */
    private void loadDefaultConfiguration() {
        // Add common Tomcat environment variables
        addDefaultEnvironmentVariables();
    }

    /**
     * Add default environment variables
     */
    private void addDefaultEnvironmentVariables() {
        environmentVariables.put("JAVA_OPTS", "-Xmx512m -Xms256m");
        environmentVariables.put("CATALINA_OPTS", "-Dfile.encoding=UTF-8");
        envTableModel.fireTableDataChanged();
    }

    /**
     * Update JMX fields state based on enable checkbox
     */
    private void updateJmxFieldsState() {
        boolean enabled = enableJmxCheckBox.isSelected();
        jmxHostField.setEnabled(enabled);
        jmxPortField.setEnabled(enabled);
        jmxSslCheckBox.setEnabled(enabled);
        jmxAuthCheckBox.setEnabled(enabled);
        updateJmxAuthFieldsState();
    }

    /**
     * Update JMX authentication fields state
     */
    private void updateJmxAuthFieldsState() {
        boolean enabled = enableJmxCheckBox.isSelected() && jmxAuthCheckBox.isSelected();
        jmxUsernameField.setEnabled(enabled);
        jmxPasswordField.setEnabled(enabled);
    }

    /**
     * Update debug fields state based on enable checkbox
     */
    private void updateDebugFieldsState() {
        boolean enabled = enableRemoteDebuggingCheckBox.isSelected();
        debugPortField.setEnabled(enabled);
    }

    /**
     * Add new environment variable
     */
    private void addEnvironmentVariable() {
        EnvironmentVariableDialog dialog = new EnvironmentVariableDialog(project, null, null);
        if (dialog.showAndGet()) {
            environmentVariables.put(dialog.getVariableName(), dialog.getVariableValue());
            envTableModel.fireTableDataChanged();
        }
    }

    /**
     * Remove selected environment variable
     */
    private void removeSelectedEnvironmentVariable() {
        int selectedRow = envVarsTable.getSelectedRow();
        if (selectedRow >= 0) {
            List<String> keys = new ArrayList<>(environmentVariables.keySet());
            if (selectedRow < keys.size()) {
                environmentVariables.remove(keys.get(selectedRow));
                envTableModel.fireTableDataChanged();
            }
        }
    }

    /**
     * Edit selected environment variable
     */
    private void editSelectedEnvironmentVariable() {
        int selectedRow = envVarsTable.getSelectedRow();
        if (selectedRow >= 0) {
            List<String> keys = new ArrayList<>(environmentVariables.keySet());
            if (selectedRow < keys.size()) {
                String key = keys.get(selectedRow);
                String value = environmentVariables.get(key);

                EnvironmentVariableDialog dialog = new EnvironmentVariableDialog(project, key, value);
                if (dialog.showAndGet()) {
                    // Remove old key if name changed
                    if (!key.equals(dialog.getVariableName())) {
                        environmentVariables.remove(key);
                    }
                    environmentVariables.put(dialog.getVariableName(), dialog.getVariableValue());
                    envTableModel.fireTableDataChanged();
                }
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

            // Reset JMX configuration
            enableJmxCheckBox.setSelected(enhancedConfig.isJmxEnabled());
            jmxHostField.setText(enhancedConfig.getJmxHost());
            jmxPortField.setText(String.valueOf(enhancedConfig.getJmxPort()));
            jmxSslCheckBox.setSelected(enhancedConfig.isJmxSslEnabled());
            jmxAuthCheckBox.setSelected(enhancedConfig.isJmxAuthEnabled());
            jmxUsernameField.setText(enhancedConfig.getJmxUsername());
            jmxPasswordField.setText(enhancedConfig.getJmxPassword());

            // Reset environment variables
            environmentVariables.clear();
            environmentVariables.putAll(enhancedConfig.getEnvironmentVariables());

            // Reset connection settings
            connectionTimeoutField.setText(String.valueOf(enhancedConfig.getConnectionTimeout()));
            readTimeoutField.setText(String.valueOf(enhancedConfig.getReadTimeout()));
            enableRemoteDebuggingCheckBox.setSelected(enhancedConfig.isRemoteDebuggingEnabled());
            debugPortField.setText(String.valueOf(enhancedConfig.getDebugPort()));

            // Update field states
            updateJmxFieldsState();
            updateDebugFieldsState();

            // Refresh table
            envTableModel.fireTableDataChanged();
        }
    }

    /**
     * Apply configuration to EnhancedTomcatRunConfiguration
     */
    public void applyTo(@NotNull TomcatRunConfiguration configuration) throws com.intellij.openapi.options.ConfigurationException {
        if (configuration instanceof com.poratu.idea.plugins.tomcat.conf.EnhancedTomcatRunConfiguration) {
            com.poratu.idea.plugins.tomcat.conf.EnhancedTomcatRunConfiguration enhancedConfig =
                    (com.poratu.idea.plugins.tomcat.conf.EnhancedTomcatRunConfiguration) configuration;

            // Validate and apply JMX configuration
            try {
                int jmxPort = Integer.parseInt(jmxPortField.getText().trim());
                if (jmxPort < 1 || jmxPort > 65535) {
                    throw new com.intellij.openapi.options.ConfigurationException("JMX port must be between 1 and 65535");
                }
                enhancedConfig.setJmxPort(jmxPort);
            } catch (NumberFormatException e) {
                throw new com.intellij.openapi.options.ConfigurationException("Invalid JMX port number");
            }

            enhancedConfig.setJmxEnabled(enableJmxCheckBox.isSelected());
            enhancedConfig.setJmxHost(jmxHostField.getText().trim());
            enhancedConfig.setJmxSslEnabled(jmxSslCheckBox.isSelected());
            enhancedConfig.setJmxAuthEnabled(jmxAuthCheckBox.isSelected());
            enhancedConfig.setJmxUsername(jmxUsernameField.getText().trim());
            enhancedConfig.setJmxPassword(new String(jmxPasswordField.getPassword()));

            // Apply environment variables
            enhancedConfig.setEnvironmentVariables(environmentVariables);

            // Validate and apply connection settings
            try {
                int connectionTimeout = Integer.parseInt(connectionTimeoutField.getText().trim());
                int readTimeout = Integer.parseInt(readTimeoutField.getText().trim());
                int debugPort = Integer.parseInt(debugPortField.getText().trim());

                if (connectionTimeout < 1000) {
                    throw new com.intellij.openapi.options.ConfigurationException("Connection timeout must be at least 1000ms");
                }
                if (readTimeout < 1000) {
                    throw new com.intellij.openapi.options.ConfigurationException("Read timeout must be at least 1000ms");
                }
                if (debugPort < 1 || debugPort > 65535) {
                    throw new com.intellij.openapi.options.ConfigurationException("Debug port must be between 1 and 65535");
                }

                enhancedConfig.setConnectionTimeout(connectionTimeout);
                enhancedConfig.setReadTimeout(readTimeout);
                enhancedConfig.setDebugPort(debugPort);
            } catch (NumberFormatException e) {
                throw new com.intellij.openapi.options.ConfigurationException("Invalid number format in connection settings");
            }

            enhancedConfig.setRemoteDebuggingEnabled(enableRemoteDebuggingCheckBox.isSelected());
        }
    }

    /**
     * Table model for environment variables
     */
    private class EnvironmentVariablesTableModel extends AbstractTableModel {

        private final String[] columnNames = {"Name", "Value"};

        @Override
        public int getRowCount() {
            return environmentVariables.size();
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
        public Object getValueAt(int row, int column) {
            List<String> keys = new ArrayList<>(environmentVariables.keySet());
            if (row < 0 || row >= keys.size()) {
                return null;
            }

            String key = keys.get(row);
            switch (column) {
                case 0: return key;
                case 1: return environmentVariables.get(key);
                default: return null;
            }
        }
    }
}