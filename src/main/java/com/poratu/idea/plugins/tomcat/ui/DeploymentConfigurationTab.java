package com.poratu.idea.plugins.tomcat.ui;

import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ui.JBUI;
import com.poratu.idea.plugins.tomcat.conf.EnhancedTomcatRunConfiguration;
import com.poratu.idea.plugins.tomcat.conf.TomcatRunConfiguration;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

/**
 * Phase 2: Deployment configuration tab - Ultimate-style deployment settings
 * Author: Gezahegn Lemma (Gezu)
 * Project: DevTomcat Plugin
 * Created: 6/9/25
 */
public class DeploymentConfigurationTab extends JPanel {

    private final Project project;

    // Deployment Configuration
    private JTextField contextPathField;
    private TextFieldWithBrowseButton docBaseField;
    private TextFieldWithBrowseButton catalinaBaseField;

    // Hot Deployment Settings
    private JCheckBox enableHotDeploymentCheckBox;
    private JCheckBox updateClassesAndResourcesCheckBox;
    private JCheckBox updateTriggerFilesCheckBox;

    // Deployment Options
    private JSpinner deploymentTimeoutSpinner;
    private JCheckBox enableAccessLogCheckBox;
    private JTextField accessLogPatternField;

    public DeploymentConfigurationTab(@NotNull Project project) {
        this.project = project;
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setBorder(JBUI.Borders.empty(10));

        // Create main panel with sections
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        // Add deployment configuration section
        mainPanel.add(createDeploymentConfigPanel());
        mainPanel.add(Box.createVerticalStrut(10));

        // Add hot deployment section
        mainPanel.add(createHotDeploymentPanel());
        mainPanel.add(Box.createVerticalStrut(10));

        // Add deployment options section
        mainPanel.add(createDeploymentOptionsPanel());

        // Add spacer
        mainPanel.add(Box.createVerticalGlue());

        add(mainPanel, BorderLayout.NORTH);
    }

    private JPanel createDeploymentConfigPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Deployment Configuration"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = JBUI.insets(5);
        gbc.anchor = GridBagConstraints.WEST;

        // Context Path
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Context Path:"), gbc);

        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        contextPathField = new JTextField();
        contextPathField.setToolTipText("Application context path (e.g., /myapp)");
        panel.add(contextPathField, gbc);

        // Document Base
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0;
        panel.add(new JLabel("Document Base:"), gbc);

        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        docBaseField = new TextFieldWithBrowseButton();
        docBaseField.setToolTipText("Path to web application directory");
        docBaseField.addActionListener(e -> chooseDocumentBase());
        panel.add(docBaseField, gbc);

        // Catalina Base
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0;
        panel.add(new JLabel("Catalina Base:"), gbc);

        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        catalinaBaseField = new TextFieldWithBrowseButton();
        catalinaBaseField.setToolTipText("Tomcat base directory (optional)");
        catalinaBaseField.addActionListener(e -> chooseCatalinaBase());
        panel.add(catalinaBaseField, gbc);

        return panel;
    }

    private JPanel createHotDeploymentPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("Hot Deployment"));

        enableHotDeploymentCheckBox = new JCheckBox("Enable hot deployment", false);
        enableHotDeploymentCheckBox.setToolTipText("Enable automatic redeployment when files change");
        enableHotDeploymentCheckBox.addActionListener(e -> updateHotDeploymentFieldsState());

        updateClassesAndResourcesCheckBox = new JCheckBox("Update classes and resources", true);
        updateClassesAndResourcesCheckBox.setToolTipText("Update classes and resources without server restart");

        updateTriggerFilesCheckBox = new JCheckBox("Update trigger files", false);
        updateTriggerFilesCheckBox.setToolTipText("Update files that trigger hot deployment");

        panel.add(enableHotDeploymentCheckBox);
        panel.add(updateClassesAndResourcesCheckBox);
        panel.add(updateTriggerFilesCheckBox);

        // Initially disable dependent checkboxes
        updateHotDeploymentFieldsState();

        return panel;
    }

    private JPanel createDeploymentOptionsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Deployment Options"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = JBUI.insets(5);
        gbc.anchor = GridBagConstraints.WEST;

        // Deployment Timeout
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Deployment Timeout (seconds):"), gbc);

        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        deploymentTimeoutSpinner = new JSpinner(new SpinnerNumberModel(30, 5, 300, 5));
        deploymentTimeoutSpinner.setToolTipText("Maximum time to wait for deployment completion");
        panel.add(deploymentTimeoutSpinner, gbc);

        // Enable Access Log
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.NONE;
        enableAccessLogCheckBox = new JCheckBox("Enable access log", true);
        enableAccessLogCheckBox.setToolTipText("Enable HTTP access logging");
        enableAccessLogCheckBox.addActionListener(e -> updateAccessLogFieldsState());
        panel.add(enableAccessLogCheckBox, gbc);

        // Access Log Pattern
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1;
        panel.add(new JLabel("Access Log Pattern:"), gbc);

        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        accessLogPatternField = new JTextField("combined");
        accessLogPatternField.setToolTipText("Access log format pattern (common, combined, or custom)");
        panel.add(accessLogPatternField, gbc);

        // Initially update access log fields state
        updateAccessLogFieldsState();

        return panel;
    }

    private void chooseDocumentBase() {
        FileChooserDescriptor descriptor = new FileChooserDescriptor(false, true, false, false, false, false);
        descriptor.setTitle("Select Document Base Directory");
        descriptor.setDescription("Choose the web application directory");

        VirtualFile file = FileChooser.chooseFile(descriptor, project, null);
        if (file != null) {
            docBaseField.setText(file.getPath());
        }
    }

    private void chooseCatalinaBase() {
        FileChooserDescriptor descriptor = new FileChooserDescriptor(false, true, false, false, false, false);
        descriptor.setTitle("Select Catalina Base Directory");
        descriptor.setDescription("Choose the Tomcat base directory");

        VirtualFile file = FileChooser.chooseFile(descriptor, project, null);
        if (file != null) {
            catalinaBaseField.setText(file.getPath());
        }
    }

    private void updateHotDeploymentFieldsState() {
        boolean enabled = enableHotDeploymentCheckBox.isSelected();
        updateClassesAndResourcesCheckBox.setEnabled(enabled);
        updateTriggerFilesCheckBox.setEnabled(enabled);
    }

    private void updateAccessLogFieldsState() {
        boolean enabled = enableAccessLogCheckBox.isSelected();
        accessLogPatternField.setEnabled(enabled);
    }

    public void resetFrom(@NotNull TomcatRunConfiguration configuration) {
        // Reset basic configuration
        contextPathField.setText(configuration.getContextPath() != null ? configuration.getContextPath() : "");
        docBaseField.setText(configuration.getDocBase() != null ? configuration.getDocBase() : "");
        catalinaBaseField.setText(configuration.getCatalinaBase() != null ? configuration.getCatalinaBase() : "");

        // Reset enhanced configuration if available
        if (configuration instanceof EnhancedTomcatRunConfiguration) {
            EnhancedTomcatRunConfiguration enhancedConfig = (EnhancedTomcatRunConfiguration) configuration;

            enableHotDeploymentCheckBox.setSelected(enhancedConfig.isHotDeploymentEnabled());
            updateClassesAndResourcesCheckBox.setSelected(enhancedConfig.isUpdateClassesAndResources());
            updateTriggerFilesCheckBox.setSelected(enhancedConfig.isUpdateTriggerFiles());

            deploymentTimeoutSpinner.setValue(enhancedConfig.getDeploymentTimeout());
            enableAccessLogCheckBox.setSelected(enhancedConfig.isEnableAccessLog());
            accessLogPatternField.setText(enhancedConfig.getAccessLogPattern());

            updateHotDeploymentFieldsState();
            updateAccessLogFieldsState();
        } else {
            // Set default values for regular TomcatRunConfiguration
            enableHotDeploymentCheckBox.setSelected(false);
            updateClassesAndResourcesCheckBox.setSelected(true);
            updateTriggerFilesCheckBox.setSelected(false);
            deploymentTimeoutSpinner.setValue(30);
            enableAccessLogCheckBox.setSelected(true);
            accessLogPatternField.setText("combined");

            updateHotDeploymentFieldsState();
            updateAccessLogFieldsState();
        }
    }

    public void applyTo(@NotNull TomcatRunConfiguration configuration) throws ConfigurationException {
        // Validate context path
        String contextPath = contextPathField.getText().trim();
        if (contextPath.isEmpty()) {
            throw new ConfigurationException("Context path cannot be empty");
        }

        // Validate document base
        String docBase = docBaseField.getText().trim();
        if (docBase.isEmpty()) {
            throw new ConfigurationException("Document base cannot be empty");
        }

        try {
            // Apply basic configuration to TomcatRunConfiguration
            configuration.setContextPath(contextPath);
            configuration.setDocBase(docBase);

            // Set Catalina base if provided
            String catalinaBase = catalinaBaseField.getText().trim();
            if (!catalinaBase.isEmpty()) {
                configuration.setCatalinaBase(catalinaBase);
            }

            // Apply enhanced configuration if this is an EnhancedTomcatRunConfiguration
            if (configuration instanceof EnhancedTomcatRunConfiguration) {
                EnhancedTomcatRunConfiguration enhancedConfig = (EnhancedTomcatRunConfiguration) configuration;

                // Apply hot deployment settings
                enhancedConfig.setHotDeploymentEnabled(enableHotDeploymentCheckBox.isSelected());
                enhancedConfig.setUpdateClassesAndResources(updateClassesAndResourcesCheckBox.isSelected());
                enhancedConfig.setUpdateTriggerFiles(updateTriggerFilesCheckBox.isSelected());

                // Apply deployment options
                enhancedConfig.setDeploymentTimeout((Integer) deploymentTimeoutSpinner.getValue());
                enhancedConfig.setEnableAccessLog(enableAccessLogCheckBox.isSelected());
                enhancedConfig.setAccessLogPattern(accessLogPatternField.getText().trim());

                System.out.println("DevTomcat: Applied enhanced deployment configuration");
            } else {
                System.out.println("DevTomcat: Applied basic deployment configuration (enhanced features not available)");
            }

        } catch (Exception e) {
            System.err.println("DevTomcat: Error applying deployment configuration: " + e.getMessage());
            throw new ConfigurationException("Failed to apply deployment configuration: " + e.getMessage());
        }
    }
}