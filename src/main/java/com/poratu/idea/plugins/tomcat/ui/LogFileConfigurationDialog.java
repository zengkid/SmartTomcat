/**
 * Author: Gezahegn Lemma (Gezu)
 * Project: Dev Tomcat Plugin
 * Created: 6/9/25
 */


package com.poratu.idea.plugins.tomcat.ui;

import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ui.JBUI;
import com.poratu.idea.plugins.tomcat.logging.LogFileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

/**
 * Phase 2: Dialog for adding/editing log file configurations
 * Provides Ultimate-style interface for configuring individual log files
 */
public class LogFileConfigurationDialog extends DialogWrapper {

    private final Project project;
    private LogFileConfiguration originalConfiguration;

    // UI Components
    private JTextField aliasField;
    private TextFieldWithBrowseButton filePathField;
    private JTextArea descriptionArea;
    private JCheckBox activeCheckBox;
    private JCheckBox skipContentCheckBox;
    private JCheckBox showAllMessagesCheckBox;

    /**
     * Constructor for adding new log file
     */
    public LogFileConfigurationDialog(@NotNull Project project) {
        this(project, null);
    }

    /**
     * Constructor for editing existing log file
     */
    public LogFileConfigurationDialog(@NotNull Project project, @Nullable LogFileConfiguration configuration) {
        super(project);
        this.project = project;
        this.originalConfiguration = configuration;

        setTitle(configuration == null ? "Add Log File" : "Edit Log File");
        setOKButtonText(configuration == null ? "Add" : "Update");

        init();

        if (configuration != null) {
            populateFields(configuration);
        }
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(JBUI.size(500, 350));

        // Create main form panel
        JPanel formPanel = createFormPanel();
        panel.add(formPanel, BorderLayout.CENTER);

        // Create help panel
        JPanel helpPanel = createHelpPanel();
        panel.add(helpPanel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Create the main form panel
     */
    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(JBUI.Borders.empty(10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = JBUI.insets(5);
        gbc.anchor = GridBagConstraints.WEST;

        // Alias field
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Alias:"), gbc);

        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        aliasField = new JTextField();
        aliasField.setToolTipText("Unique name for this log file (e.g., 'Catalina', 'Localhost')");
        panel.add(aliasField, gbc);

        // File path field
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0;
        panel.add(new JLabel("Log File Path:"), gbc);

        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        filePathField = new TextFieldWithBrowseButton();
        filePathField.setToolTipText("Path to the log file (supports variables: $CATALINA_BASE, $CATALINA_HOME, $DATE)");
        filePathField.addActionListener(e -> chooseLogFile());
        panel.add(filePathField, gbc);

        // Description field
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0;
        panel.add(new JLabel("Description:"), gbc);

        gbc.gridx = 1; gbc.fill = GridBagConstraints.BOTH; gbc.weightx = 1.0; gbc.weighty = 1.0;
        descriptionArea = new JTextArea(3, 30);
        descriptionArea.setToolTipText("Optional description for this log file");
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        JScrollPane descriptionScroll = new JScrollPane(descriptionArea);
        panel.add(descriptionScroll, gbc);

        // Options panel
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weighty = 0.0;
        JPanel optionsPanel = createOptionsPanel();
        panel.add(optionsPanel, gbc);

        return panel;
    }

    /**
     * Create options panel with checkboxes
     */
    private JPanel createOptionsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createTitledBorder("Options"));

        activeCheckBox = new JCheckBox("Active", true);
        activeCheckBox.setToolTipText("Enable monitoring of this log file");

        skipContentCheckBox = new JCheckBox("Skip content", false);
        skipContentCheckBox.setToolTipText("Skip file content when the file is too large");

        showAllMessagesCheckBox = new JCheckBox("Show all messages", true);
        showAllMessagesCheckBox.setToolTipText("Show all log messages including debug information");

        panel.add(activeCheckBox);
        panel.add(skipContentCheckBox);
        panel.add(showAllMessagesCheckBox);

        return panel;
    }

    /**
     * Create help panel with variable information
     */
    private JPanel createHelpPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Variables"));

        JTextArea helpText = new JTextArea();
        helpText.setEditable(false);
        helpText.setOpaque(false);
        helpText.setFont(helpText.getFont().deriveFont(Font.ITALIC));
        helpText.setText(
                "Available Variables:\n" +
                        "• $CATALINA_BASE - Tomcat base directory (e.g., /path/to/tomcat/base)\n" +
                        "• $CATALINA_HOME - Tomcat installation directory\n" +
                        "• $DATE - Current date in yyyy-MM-dd format\n\n" +
                        "Examples:\n" +
                        "• $CATALINA_BASE/logs/catalina.out\n" +
                        "• $CATALINA_BASE/logs/localhost.$DATE.log\n" +
                        "• $CATALINA_HOME/logs/manager.$DATE.log"
        );

        panel.add(helpText, BorderLayout.CENTER);
        return panel;
    }

    /**
     * Choose log file using file chooser
     */
    private void chooseLogFile() {
        FileChooserDescriptor descriptor = new FileChooserDescriptor(true, false, false, false, false, false);
        descriptor.setTitle("Select Log File");
        descriptor.setDescription("Choose the log file to monitor");

        VirtualFile file = FileChooser.chooseFile(descriptor, project, null);
        if (file != null) {
            filePathField.setText(file.getPath());
        }
    }

    /**
     * Populate fields with existing configuration
     */
    private void populateFields(@NotNull LogFileConfiguration configuration) {
        aliasField.setText(configuration.getAlias());
        filePathField.setText(configuration.getFilePath());
        descriptionArea.setText(configuration.getDescription());
        activeCheckBox.setSelected(configuration.isActive());
        skipContentCheckBox.setSelected(configuration.isSkipContent());
        showAllMessagesCheckBox.setSelected(configuration.isShowAllMessages());
    }

    /**
     * Create log file configuration from form data
     */
    public LogFileConfiguration getLogFileConfiguration() {
        return new LogFileConfiguration(
                aliasField.getText().trim(),
                filePathField.getText().trim(),
                activeCheckBox.isSelected(),
                descriptionArea.getText().trim(),
                skipContentCheckBox.isSelected(),
                showAllMessagesCheckBox.isSelected()
        );
    }

    @Override
    protected @Nullable ValidationInfo doValidate() {
        // Validate alias
        String alias = aliasField.getText().trim();
        if (alias.isEmpty()) {
            return new ValidationInfo("Alias cannot be empty", aliasField);
        }

        if (alias.contains(" ")) {
            return new ValidationInfo("Alias cannot contain spaces", aliasField);
        }

        // Validate file path
        String filePath = filePathField.getText().trim();
        if (filePath.isEmpty()) {
            return new ValidationInfo("Log file path cannot be empty", filePathField);
        }

        // Check for common path issues
        if (!filePath.contains("/") && !filePath.contains("\\") && !filePath.startsWith("$")) {
            return new ValidationInfo("Log file path should be an absolute path or use variables", filePathField);
        }

        return null;
    }

    @Override
    public @Nullable JComponent getPreferredFocusedComponent() {
        return aliasField;
    }
}
