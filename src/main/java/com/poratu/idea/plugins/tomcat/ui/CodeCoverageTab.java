/**
 * Author: Gezahegn Lemma (Gezu)
 * Project: Dev Tomcat Plugin
 * Created: 6/9/25
 */


package com.poratu.idea.plugins.tomcat.ui;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.util.ui.JBUI;
import com.poratu.idea.plugins.tomcat.conf.EnhancedTomcatRunConfiguration;
import com.poratu.idea.plugins.tomcat.conf.TomcatRunConfiguration;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

/**
 * Phase 2: Code Coverage configuration tab - Coverage integration
 * Author: Gezahegn Lemma (Gezu)
 * Project: Dev Tomcat Plugin
 * Created: 6/9/25
 */
public class CodeCoverageTab extends JPanel {

    private final Project project;

    private JCheckBox enableCoverageCheckBox;
    private JCheckBox trackPerTestCheckBox;
    private JTextField coverageIncludePatternsField;
    private JTextField coverageExcludePatternsField;
    private JTextArea coverageInstructionsArea;

    public CodeCoverageTab(@NotNull Project project) {
        this.project = project;
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setBorder(JBUI.Borders.empty(10));

        // Create main panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        // Add coverage settings
        mainPanel.add(createCoverageSettingsPanel());
        mainPanel.add(Box.createVerticalStrut(10));

        // Add pattern settings
        mainPanel.add(createPatternSettingsPanel());
        mainPanel.add(Box.createVerticalStrut(10));

        // Add instructions
        mainPanel.add(createInstructionsPanel());

        add(mainPanel, BorderLayout.NORTH);
    }

    private JPanel createCoverageSettingsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("Code Coverage Settings"));

        enableCoverageCheckBox = new JCheckBox("Enable code coverage", false);
        enableCoverageCheckBox.setToolTipText("Enable code coverage analysis for this configuration");
        enableCoverageCheckBox.addActionListener(e -> updateCoverageFieldsState());

        trackPerTestCheckBox = new JCheckBox("Track coverage per test", false);
        trackPerTestCheckBox.setToolTipText("Track coverage information per individual test");

        panel.add(enableCoverageCheckBox);
        panel.add(trackPerTestCheckBox);

        return panel;
    }

    private JPanel createPatternSettingsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Coverage Patterns"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = JBUI.insets(5);
        gbc.anchor = GridBagConstraints.WEST;

        // Include patterns
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Include patterns:"), gbc);

        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        coverageIncludePatternsField = new JTextField("com.yourcompany.*");
        coverageIncludePatternsField.setToolTipText("Patterns for classes to include in coverage (comma-separated)");
        panel.add(coverageIncludePatternsField, gbc);

        // Exclude patterns
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0;
        panel.add(new JLabel("Exclude patterns:"), gbc);

        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        coverageExcludePatternsField = new JTextField("*.test.*, *Test, *Tests");
        coverageExcludePatternsField.setToolTipText("Patterns for classes to exclude from coverage (comma-separated)");
        panel.add(coverageExcludePatternsField, gbc);

        return panel;
    }

    private JPanel createInstructionsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Instructions"));

        coverageInstructionsArea = new JTextArea(8, 50);
        coverageInstructionsArea.setEditable(false);
        coverageInstructionsArea.setOpaque(false);
        coverageInstructionsArea.setFont(coverageInstructionsArea.getFont().deriveFont(Font.ITALIC));
        coverageInstructionsArea.setText(
                "Code Coverage Integration:\n\n" +
                        "• Enable code coverage to analyze which parts of your code are executed during testing\n" +
                        "• Include/exclude patterns use standard wildcard syntax (* and ?)\n" +
                        "• Coverage data will be collected during Tomcat execution\n" +
                        "• Results can be viewed in the Coverage tool window\n\n" +
                        "Note: Code coverage may impact performance. Enable only when needed for analysis.\n\n" +
                        "Supported Coverage Tools:\n" +
                        "• IntelliJ IDEA built-in coverage\n" +
                        "• JaCoCo (if configured in your build)\n" +
                        "• Other coverage agents through VM options"
        );
        coverageInstructionsArea.setLineWrap(true);
        coverageInstructionsArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(coverageInstructionsArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void updateCoverageFieldsState() {
        boolean enabled = enableCoverageCheckBox.isSelected();
        trackPerTestCheckBox.setEnabled(enabled);
        coverageIncludePatternsField.setEnabled(enabled);
        coverageExcludePatternsField.setEnabled(enabled);
    }

    public void resetFrom(@NotNull TomcatRunConfiguration configuration) {
        if (configuration instanceof EnhancedTomcatRunConfiguration) {
            EnhancedTomcatRunConfiguration enhanced = (EnhancedTomcatRunConfiguration) configuration;
            enableCoverageCheckBox.setSelected(enhanced.isCoverageEnabled());
            trackPerTestCheckBox.setSelected(enhanced.isTrackPerTest());
        } else {
            enableCoverageCheckBox.setSelected(false);
            trackPerTestCheckBox.setSelected(false);
        }

        updateCoverageFieldsState();
    }

    public void applyTo(@NotNull TomcatRunConfiguration configuration) throws ConfigurationException {
        if (configuration instanceof EnhancedTomcatRunConfiguration) {
            EnhancedTomcatRunConfiguration enhanced = (EnhancedTomcatRunConfiguration) configuration;
            enhanced.setCoverageEnabled(enableCoverageCheckBox.isSelected());
            enhanced.setTrackPerTest(trackPerTestCheckBox.isSelected());
        }
    }
}