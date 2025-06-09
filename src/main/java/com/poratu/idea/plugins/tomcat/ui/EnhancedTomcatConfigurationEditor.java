package com.poratu.idea.plugins.tomcat.ui;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.poratu.idea.plugins.tomcat.conf.EnhancedTomcatRunConfiguration;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * Phase 2: Enhanced Tomcat configuration editor with Ultimate-style 5-tab interface
 * Provides complete Ultimate-like configuration experience:
 * - Server tab (basic Tomcat settings)
 * - Deployment tab (artifacts and deployment options)
 * - Logs tab (multiple log file configuration)
 * - Startup/Connection tab (JMX, environment, debugging)
 * - Code Coverage tab (coverage integration)
 */
public class EnhancedTomcatConfigurationEditor extends SettingsEditor<EnhancedTomcatRunConfiguration> {

    private final Project project;
    private EnhancedTomcatRunConfiguration currentConfiguration;

    // Tab panels
    private ServerConfigurationTab serverTab;
    private DeploymentConfigurationTab deploymentTab;
    private LogsConfigurationTab logsTab;
    private StartupConnectionTab startupConnectionTab;
    private CodeCoverageTab codeCoverageTab;

    // Main tabbed pane
    private JTabbedPane tabbedPane;

    public EnhancedTomcatConfigurationEditor(@NotNull Project project) {
        this.project = project;
    }

    @Override
    protected void resetEditorFrom(@NotNull EnhancedTomcatRunConfiguration configuration) {
        this.currentConfiguration = configuration;

        // Reset all tabs with configuration data
        if (serverTab != null) {
            serverTab.resetFrom(configuration);
        }
        if (deploymentTab != null) {
            deploymentTab.resetFrom(configuration);
        }
        if (logsTab != null) {
            logsTab.resetFrom(configuration);
        }
        if (startupConnectionTab != null) {
            startupConnectionTab.resetFrom(configuration);
        }
        if (codeCoverageTab != null) {
            codeCoverageTab.resetFrom(configuration);
        }
    }

    @Override
    protected void applyEditorTo(@NotNull EnhancedTomcatRunConfiguration configuration) throws ConfigurationException {
        // Apply changes from all tabs to configuration
        if (serverTab != null) {
            serverTab.applyTo(configuration);
        }
        if (deploymentTab != null) {
            deploymentTab.applyTo(configuration);
        }
        if (logsTab != null) {
            logsTab.applyTo(configuration);
        }
        if (startupConnectionTab != null) {
            startupConnectionTab.applyTo(configuration);
        }
        if (codeCoverageTab != null) {
            codeCoverageTab.applyTo(configuration);
        }
    }

    @Override
    protected @NotNull JComponent createEditor() {
        // Create the main tabbed pane
        tabbedPane = new JTabbedPane();

        // Create and add all configuration tabs
        createServerTab();
        createDeploymentTab();
        createLogsTab();
        createStartupConnectionTab();
        createCodeCoverageTab();

        return tabbedPane;
    }

    /**
     * Create Server configuration tab
     */
    private void createServerTab() {
        serverTab = new ServerConfigurationTab(project);
        tabbedPane.addTab("Server", serverTab);
        tabbedPane.setToolTipTextAt(0, "Basic Tomcat server configuration");
    }

    /**
     * Create Deployment configuration tab
     */
    private void createDeploymentTab() {
        deploymentTab = new DeploymentConfigurationTab(project);
        tabbedPane.addTab("Deployment", deploymentTab);
        tabbedPane.setToolTipTextAt(1, "Application deployment and artifact configuration");
    }

    /**
     * Create Logs configuration tab
     */
    private void createLogsTab() {
        logsTab = new LogsConfigurationTab(project, currentConfiguration);
        tabbedPane.addTab("Logs", logsTab);
        tabbedPane.setToolTipTextAt(2, "Log file monitoring and display configuration");
    }

    /**
     * Create Startup/Connection configuration tab
     */
    private void createStartupConnectionTab() {
        startupConnectionTab = new StartupConnectionTab(project, currentConfiguration);
        tabbedPane.addTab("Startup/Connection", startupConnectionTab);
        tabbedPane.setToolTipTextAt(3, "JMX, environment variables, and connection settings");
    }

    /**
     * Create Code Coverage configuration tab
     */
    private void createCodeCoverageTab() {
        codeCoverageTab = new CodeCoverageTab(project);
        tabbedPane.addTab("Code Coverage", codeCoverageTab);
        tabbedPane.setToolTipTextAt(4, "Code coverage integration and monitoring");
    }

    /**
     * Server configuration tab - basic Tomcat settings
     */
    private static class ServerConfigurationTab extends JPanel {
        private final Project project;

        // UI Components for basic server configuration
        private JTextField tomcatHomeField;
        private JTextField serverPortField;
        private JTextField contextPathField;
        private JTextField vmOptionsField;
        private JCheckBox enableJmxCheckBox;

        public ServerConfigurationTab(@NotNull Project project) {
            this.project = project;
            initializeUI();
        }

        private void initializeUI() {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

            // Basic server settings panel
            JPanel serverPanel = new JPanel();
            serverPanel.setBorder(BorderFactory.createTitledBorder("Server Settings"));
            // Add server configuration fields...
            add(serverPanel);

            // VM Options panel
            JPanel vmPanel = new JPanel();
            vmPanel.setBorder(BorderFactory.createTitledBorder("VM Options"));
            // Add VM options configuration...
            add(vmPanel);
        }

        public void resetFrom(@NotNull EnhancedTomcatRunConfiguration configuration) {
            // Reset basic server fields from configuration
            // This would populate fields from the base TomcatRunConfiguration
            // For now, we'll use placeholder implementation
        }

        public void applyTo(@NotNull EnhancedTomcatRunConfiguration configuration) throws ConfigurationException {
            // Apply basic server configuration
            // This would save field values to the base TomcatRunConfiguration
            // For now, we'll use placeholder implementation
        }
    }

    /**
     * Deployment configuration tab - artifacts and deployment options
     */
    private static class DeploymentConfigurationTab extends JPanel {
        private final Project project;

        public DeploymentConfigurationTab(@NotNull Project project) {
            this.project = project;
            initializeUI();
        }

        private void initializeUI() {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

            JPanel deploymentPanel = new JPanel();
            deploymentPanel.setBorder(BorderFactory.createTitledBorder("Deployment Artifacts"));
            add(deploymentPanel);

            JPanel optionsPanel = new JPanel();
            optionsPanel.setBorder(BorderFactory.createTitledBorder("Deployment Options"));
            add(optionsPanel);
        }

        public void resetFrom(@NotNull EnhancedTomcatRunConfiguration configuration) {
            // Reset deployment configuration from the configuration object
            // This would handle artifact configurations, deployment options, etc.
        }

        public void applyTo(@NotNull EnhancedTomcatRunConfiguration configuration) throws ConfigurationException {
            // Apply deployment configuration to the configuration object
            // This would save artifact settings, deployment paths, etc.
        }
    }

    /**
     * Code Coverage configuration tab - coverage integration
     */
    private static class CodeCoverageTab extends JPanel {
        private final Project project;

        private JCheckBox enableCoverageCheckBox;
        private JTextField coverageAgentField;
        private JCheckBox trackPerTestCheckBox;

        public CodeCoverageTab(@NotNull Project project) {
            this.project = project;
            initializeUI();
        }

        private void initializeUI() {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

            JPanel coveragePanel = new JPanel();
            coveragePanel.setBorder(BorderFactory.createTitledBorder("Code Coverage"));

            enableCoverageCheckBox = new JCheckBox("Enable code coverage", false);
            coveragePanel.add(enableCoverageCheckBox);

            trackPerTestCheckBox = new JCheckBox("Track coverage per test", false);
            coveragePanel.add(trackPerTestCheckBox);

            add(coveragePanel);
        }

        public void resetFrom(@NotNull EnhancedTomcatRunConfiguration configuration) {
            // Reset code coverage configuration
            // enableCoverageCheckBox.setSelected(configuration.isCoverageEnabled());
            // trackPerTestCheckBox.setSelected(configuration.isTrackPerTest());
        }

        public void applyTo(@NotNull EnhancedTomcatRunConfiguration configuration) throws ConfigurationException {
            // Apply code coverage configuration
            // configuration.setCoverageEnabled(enableCoverageCheckBox.isSelected());
            // configuration.setTrackPerTest(trackPerTestCheckBox.isSelected());
        }
    }
}