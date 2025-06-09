/**
 * Author: Gezahegn Lemma (Gezu)
 * Project: Dev Tomcat Plugin
 * Created: 6/9/25
 */


package com.poratu.idea.plugins.tomcat.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

/**
 * Phase 2: Dialog for adding/editing environment variables
 * Provides interface for configuring Tomcat environment variables
 */
public class EnvironmentVariableDialog extends DialogWrapper {

    private final Project project;
    private final String originalName;

    private JTextField nameField;
    private JTextArea valueArea;

    /**
     * Constructor for adding new environment variable
     */
    public EnvironmentVariableDialog(@NotNull Project project) {
        this(project, null, null);
    }

    /**
     * Constructor for editing existing environment variable
     */
    public EnvironmentVariableDialog(@NotNull Project project, @Nullable String name, @Nullable String value) {
        super(project);
        this.project = project;
        this.originalName = name;

        setTitle(name == null ? "Add Environment Variable" : "Edit Environment Variable");
        setOKButtonText(name == null ? "Add" : "Update");

        init();

        if (name != null && value != null) {
            nameField.setText(name);
            valueArea.setText(value);
        }
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setPreferredSize(JBUI.size(400, 200));
        panel.setBorder(JBUI.Borders.empty(10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = JBUI.insets(5);
        gbc.anchor = GridBagConstraints.WEST;

        // Name field
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Name:"), gbc);

        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        nameField = new JTextField();
        nameField.setToolTipText("Environment variable name (e.g., JAVA_OPTS, CATALINA_OPTS)");
        panel.add(nameField, gbc);

        // Value field
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        panel.add(new JLabel("Value:"), gbc);

        gbc.gridx = 1; gbc.fill = GridBagConstraints.BOTH; gbc.weightx = 1.0; gbc.weighty = 1.0;
        valueArea = new JTextArea(5, 30);
        valueArea.setToolTipText("Environment variable value");
        valueArea.setLineWrap(true);
        valueArea.setWrapStyleWord(true);
        JScrollPane valueScroll = new JScrollPane(valueArea);
        panel.add(valueScroll, gbc);

        return panel;
    }

    /**
     * Get environment variable name
     */
    public String getVariableName() {
        return nameField.getText().trim();
    }

    /**
     * Get environment variable value
     */
    public String getVariableValue() {
        return valueArea.getText().trim();
    }

    @Override
    protected @Nullable ValidationInfo doValidate() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            return new ValidationInfo("Variable name cannot be empty", nameField);
        }

        if (name.contains(" ")) {
            return new ValidationInfo("Variable name cannot contain spaces", nameField);
        }

        if (name.contains("=")) {
            return new ValidationInfo("Variable name cannot contain '=' character", nameField);
        }

        return null;
    }

    @Override
    public @Nullable JComponent getPreferredFocusedComponent() {
        return nameField;
    }
}