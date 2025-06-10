package com.poratu.idea.plugins.tomcat.ui;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.poratu.idea.plugins.tomcat.conf.EnhancedTomcatRunConfiguration;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * Phase 2: Enhanced Tomcat configuration editor with Ultimate-style 5-tab interface
 * Uses the complete, fully-implemented Ultimate-style tabs
 */
public class EnhancedTomcatConfigurationEditor extends SettingsEditor<EnhancedTomcatRunConfiguration> {

    private final Project project;
    private EnhancedTomcatRunConfiguration currentConfiguration;

    // Full Ultimate-style tab panels
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

        System.out.println("DevTomcat: Reset all Ultimate-style tabs from Enhanced configuration");
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

        System.out.println("DevTomcat: Applied all Ultimate-style tabs to Enhanced configuration");
    }

    @Override
    protected @NotNull JComponent createEditor() {
        // Create the main tabbed pane
        tabbedPane = new JTabbedPane();

        // Create and add all Ultimate-style configuration tabs
        createServerTab();
        createDeploymentTab();
        createLogsTab();
        createStartupConnectionTab();
        createCodeCoverageTab();

        System.out.println("DevTomcat: Created Ultimate-style 5-tab interface");
        return tabbedPane;
    }

    /**
     * Create Server configuration tab - Uses the complete Ultimate-style ServerConfigurationTab
     */
    private void createServerTab() {
        serverTab = new ServerConfigurationTab(project);
        tabbedPane.addTab("Server", serverTab);
        tabbedPane.setToolTipTextAt(0, "Application server, ports, VM options, and Tomcat server settings");
        System.out.println("DevTomcat: Added complete Server tab");
    }

    /**
     * Create Deployment configuration tab - Uses the complete Ultimate-style DeploymentConfigurationTab
     */
    private void createDeploymentTab() {
        deploymentTab = new DeploymentConfigurationTab(project);
        tabbedPane.addTab("Deployment", deploymentTab);
        tabbedPane.setToolTipTextAt(1, "Application deployment artifacts and hot deployment configuration");
        System.out.println("DevTomcat: Added complete Deployment tab");
    }

    /**
     * Create Logs configuration tab - Uses the complete Ultimate-style LogsConfigurationTab
     */
    private void createLogsTab() {
        logsTab = new LogsConfigurationTab(project, currentConfiguration);
        tabbedPane.addTab("Logs", logsTab);
        tabbedPane.setToolTipTextAt(2, "Log file monitoring and display configuration");
        System.out.println("DevTomcat: Added complete Logs tab");
    }

    /**
     * Create Startup/Connection configuration tab - Uses the complete Ultimate-style StartupConnectionTab
     */
    private void createStartupConnectionTab() {
        startupConnectionTab = new StartupConnectionTab(project, currentConfiguration);
        tabbedPane.addTab("Startup/Connection", startupConnectionTab);
        tabbedPane.setToolTipTextAt(3, "JMX settings, environment variables, and connection configuration");
        System.out.println("DevTomcat: Added complete Startup/Connection tab");
    }

    /**
     * Create Code Coverage configuration tab - Uses the complete Ultimate-style CodeCoverageTab
     */
    private void createCodeCoverageTab() {
        codeCoverageTab = new CodeCoverageTab(project);
        tabbedPane.addTab("Code Coverage", codeCoverageTab);
        tabbedPane.setToolTipTextAt(4, "Code coverage integration and monitoring configuration");
        System.out.println("DevTomcat: Added complete Code Coverage tab");
    }

    /**
     * Get current configuration for tab initialization
     */
    public EnhancedTomcatRunConfiguration getCurrentConfiguration() {
        return currentConfiguration;
    }

    /**
     * Get the server tab for external access
     */
    public ServerConfigurationTab getServerTab() {
        return serverTab;
    }

    /**
     * Get the deployment tab for external access
     */
    public DeploymentConfigurationTab getDeploymentTab() {
        return deploymentTab;
    }

    /**
     * Get the logs tab for external access
     */
    public LogsConfigurationTab getLogsTab() {
        return logsTab;
    }

    /**
     * Get the startup/connection tab for external access
     */
    public StartupConnectionTab getStartupConnectionTab() {
        return startupConnectionTab;
    }

    /**
     * Get the code coverage tab for external access
     */
    public CodeCoverageTab getCodeCoverageTab() {
        return codeCoverageTab;
    }
}