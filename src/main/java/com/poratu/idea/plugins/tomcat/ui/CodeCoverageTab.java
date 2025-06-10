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
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

/**
 * Phase 2: Code Coverage configuration tab - Coverage integration
 * Works with EnhancedTomcatRunConfiguration for full Ultimate-like experience
 */
public class CodeCoverageTab extends JPanel {

    private final Project project;

    // Coverage settings
    private JCheckBox enableCoverageCheckBox;
    private JCheckBox trackPerTestCheckBox;
    private JCheckBox samplingCoverageCheckBox;
    private JCheckBox tracingCoverageCheckBox;

    // Pattern settings
    private JTextField coverageIncludePatternsField;
    private JTextField coverageExcludePatternsField;
    private JTextArea coverageClassFiltersArea;

    // Coverage options
    private JCheckBox enableBranchCoverageCheckBox;
    private JCheckBox enableLineCoverageCheckBox;
    private JCheckBox mergeWithPreviousResultsCheckBox;

    // Coverage agent settings
    private JComboBox<String> coverageRunnerComboBox;
    private JTextField coverageAgentOptionsField;

    // Instructions
    private JTextArea coverageInstructionsArea;

    public CodeCoverageTab(@NotNull Project project) {
        this.project = project;
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setBorder(JBUI.Borders.empty(10));

        // Create main panel with scroll support
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        // Add coverage settings sections
        mainPanel.add(createBasicCoverageSettingsPanel());
        mainPanel.add(Box.createVerticalStrut(10));

        mainPanel.add(createCoverageOptionsPanel());
        mainPanel.add(Box.createVerticalStrut(10));

        mainPanel.add(createPatternSettingsPanel());
        mainPanel.add(Box.createVerticalStrut(10));

        mainPanel.add(createCoverageAgentPanel());
        mainPanel.add(Box.createVerticalStrut(10));

        mainPanel.add(createInstructionsPanel());

        // Add to scroll pane for long content
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * Create basic coverage settings panel
     */
    private JPanel createBasicCoverageSettingsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("Code Coverage Settings"));

        enableCoverageCheckBox = new JCheckBox("Enable code coverage", false);
        enableCoverageCheckBox.setToolTipText("Enable code coverage analysis for this Tomcat configuration");
        enableCoverageCheckBox.addActionListener(e -> updateCoverageFieldsState());

        trackPerTestCheckBox = new JCheckBox("Track coverage per test", false);
        trackPerTestCheckBox.setToolTipText("Track coverage information per individual test method");

        samplingCoverageCheckBox = new JCheckBox("Use sampling coverage", false);
        samplingCoverageCheckBox.setToolTipText("Use sampling mode for better performance (less accurate)");

        tracingCoverageCheckBox = new JCheckBox("Use tracing coverage", true);
        tracingCoverageCheckBox.setToolTipText("Use tracing mode for precise coverage (may impact performance)");

        panel.add(enableCoverageCheckBox);
        panel.add(trackPerTestCheckBox);
        panel.add(samplingCoverageCheckBox);
        panel.add(tracingCoverageCheckBox);

        return panel;
    }

    /**
     * Create coverage options panel
     */
    private JPanel createCoverageOptionsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("Coverage Options"));

        enableLineCoverageCheckBox = new JCheckBox("Enable line coverage", true);
        enableLineCoverageCheckBox.setToolTipText("Measure coverage at the line level");

        enableBranchCoverageCheckBox = new JCheckBox("Enable branch coverage", true);
        enableBranchCoverageCheckBox.setToolTipText("Measure coverage of conditional branches");

        mergeWithPreviousResultsCheckBox = new JCheckBox("Merge with previous results", false);
        mergeWithPreviousResultsCheckBox.setToolTipText("Merge coverage data with previous test runs");

        panel.add(enableLineCoverageCheckBox);
        panel.add(enableBranchCoverageCheckBox);
        panel.add(mergeWithPreviousResultsCheckBox);

        return panel;
    }

    /**
     * Create pattern settings panel
     */
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

        // Class filters
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0;
        panel.add(new JLabel("Class filters:"), gbc);

        gbc.gridx = 1; gbc.fill = GridBagConstraints.BOTH; gbc.weightx = 1.0; gbc.weighty = 1.0;
        coverageClassFiltersArea = new JTextArea(3, 30);
        coverageClassFiltersArea.setToolTipText("Advanced class filtering rules (one per line)");
        coverageClassFiltersArea.setLineWrap(true);
        coverageClassFiltersArea.setWrapStyleWord(true);
        JScrollPane filtersScrollPane = new JScrollPane(coverageClassFiltersArea);
        panel.add(filtersScrollPane, gbc);

        return panel;
    }

    /**
     * Create coverage agent panel
     */
    private JPanel createCoverageAgentPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Coverage Agent"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = JBUI.insets(5);
        gbc.anchor = GridBagConstraints.WEST;

        // Coverage runner
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Coverage runner:"), gbc);

        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        coverageRunnerComboBox = new JComboBox<>(new String[]{
                "IntelliJ IDEA", "JaCoCo", "Emma", "Cobertura", "Custom"
        });
        coverageRunnerComboBox.setToolTipText("Select the coverage engine to use");
        panel.add(coverageRunnerComboBox, gbc);

        // Agent options
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0;
        panel.add(new JLabel("Agent options:"), gbc);

        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        coverageAgentOptionsField = new JTextField();
        coverageAgentOptionsField.setToolTipText("Additional options for the coverage agent");
        panel.add(coverageAgentOptionsField, gbc);

        return panel;
    }

    /**
     * Create instructions panel
     */
    private JPanel createInstructionsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Coverage Instructions"));

        coverageInstructionsArea = new JTextArea(10, 50);
        coverageInstructionsArea.setEditable(false);
        coverageInstructionsArea.setOpaque(false);
        coverageInstructionsArea.setFont(coverageInstructionsArea.getFont().deriveFont(Font.ITALIC));
        coverageInstructionsArea.setText(
                "DevTomcat Code Coverage Integration:\n\n" +
                        "COVERAGE BASICS:\n" +
                        "• Enable code coverage to analyze which parts of your code are executed during Tomcat testing\n" +
                        "• Coverage data is collected during web application execution and testing\n" +
                        "• Results appear in the Coverage tool window after running your configuration\n\n" +
                        "PATTERN CONFIGURATION:\n" +
                        "• Include patterns: Specify which packages/classes to include (e.g., com.mycompany.*)\n" +
                        "• Exclude patterns: Specify what to exclude (e.g., *.test.*, *Test, *Tests)\n" +
                        "• Use standard wildcard syntax: * (any characters), ? (single character)\n\n" +
                        "COVERAGE MODES:\n" +
                        "• Tracing: More accurate but slower (recommended for detailed analysis)\n" +
                        "• Sampling: Faster but less precise (good for performance-sensitive scenarios)\n" +
                        "• Line coverage: Measures which lines of code are executed\n" +
                        "• Branch coverage: Measures which conditional branches are taken\n\n" +
                        "PERFORMANCE IMPACT:\n" +
                        "• Code coverage adds overhead to your Tomcat application\n" +
                        "• Enable only when you need coverage analysis\n" +
                        "• Consider using sampling mode for large applications\n\n" +
                        "SUPPORTED TOOLS:\n" +
                        "• IntelliJ IDEA built-in coverage (recommended)\n" +
                        "• JaCoCo agent (popular open-source option)\n" +
                        "• Emma/Cobertura (legacy tools)\n" +
                        "• Custom agents via VM options\n\n" +
                        "TOMCAT-SPECIFIC NOTES:\n" +
                        "• Coverage works with JSP compilation and servlet execution\n" +
                        "• Web requests and form submissions are tracked\n" +
                        "• AJAX calls and REST endpoints are included in coverage\n" +
                        "• Hot deployment may reset coverage data"
        );
        coverageInstructionsArea.setLineWrap(true);
        coverageInstructionsArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(coverageInstructionsArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setPreferredSize(new Dimension(500, 200));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Update coverage fields enabled state
     */
    private void updateCoverageFieldsState() {
        boolean enabled = enableCoverageCheckBox.isSelected();

        // Basic options
        trackPerTestCheckBox.setEnabled(enabled);
        samplingCoverageCheckBox.setEnabled(enabled);
        tracingCoverageCheckBox.setEnabled(enabled);

        // Coverage options
        enableLineCoverageCheckBox.setEnabled(enabled);
        enableBranchCoverageCheckBox.setEnabled(enabled);
        mergeWithPreviousResultsCheckBox.setEnabled(enabled);

        // Patterns
        coverageIncludePatternsField.setEnabled(enabled);
        coverageExcludePatternsField.setEnabled(enabled);
        coverageClassFiltersArea.setEnabled(enabled);

        // Agent settings
        coverageRunnerComboBox.setEnabled(enabled);
        coverageAgentOptionsField.setEnabled(enabled);
    }

    /**
     * Reset form from configuration
     */
    public void resetFrom(@NotNull EnhancedTomcatRunConfiguration configuration) {
        // Try to read coverage settings from VM options (temporary approach)
        String vmOptions = configuration.getVmOptions();
        boolean coverageEnabled = false;
        boolean trackPerTest = false;
        String coverageRunner = "IntelliJ IDEA";

        if (vmOptions != null) {
            coverageEnabled = vmOptions.contains("-Dcoverage.enabled=true");
            trackPerTest = vmOptions.contains("-Dcoverage.per.test=true");

            // Extract coverage runner if specified
            if (vmOptions.contains("-Dcoverage.runner=")) {
                int runnerStart = vmOptions.indexOf("-Dcoverage.runner=") + "-Dcoverage.runner=".length();
                int runnerEnd = vmOptions.indexOf(" ", runnerStart);
                if (runnerEnd == -1) runnerEnd = vmOptions.length();
                coverageRunner = vmOptions.substring(runnerStart, runnerEnd);
            }
        }

        // Reset coverage settings
        enableCoverageCheckBox.setSelected(coverageEnabled);
        trackPerTestCheckBox.setSelected(trackPerTest);

        // Reset coverage mode (defaults for now)
        samplingCoverageCheckBox.setSelected(false);
        tracingCoverageCheckBox.setSelected(true);

        // Reset coverage options (defaults for now)
        enableLineCoverageCheckBox.setSelected(true);
        enableBranchCoverageCheckBox.setSelected(true);
        mergeWithPreviousResultsCheckBox.setSelected(false);

        // Reset patterns (defaults)
        coverageIncludePatternsField.setText("com.yourcompany.*");
        coverageExcludePatternsField.setText("*.test.*, *Test, *Tests");
        coverageClassFiltersArea.setText("");

        // Reset agent settings
        coverageRunnerComboBox.setSelectedItem(coverageRunner);
        coverageAgentOptionsField.setText("");

        updateCoverageFieldsState();

        System.out.println("DevTomcat: Reset code coverage configuration - Coverage enabled: " + coverageEnabled);
    }

    /**
     * Apply form to configuration
     */
    public void applyTo(@NotNull EnhancedTomcatRunConfiguration configuration) throws ConfigurationException {
        // Check if the methods exist in the configuration
        try {
            // Try to apply coverage settings using reflection or direct method calls
            // For now, we'll use placeholder approach until methods are added

            boolean coverageEnabled = enableCoverageCheckBox.isSelected();
            boolean trackPerTest = trackPerTestCheckBox.isSelected();

            // TODO: Add these methods to EnhancedTomcatRunConfiguration:
            // configuration.setCoverageEnabled(coverageEnabled);
            // configuration.setTrackPerTest(trackPerTest);

            // Temporary workaround - store in VM options for now
            if (coverageEnabled) {
                String vmOptions = configuration.getVmOptions() != null ? configuration.getVmOptions() : "";

                // Add coverage-related VM options
                StringBuilder coverageVmOptions = new StringBuilder(vmOptions);

                if (!vmOptions.contains("-javaagent")) {
                    if (coverageVmOptions.length() > 0) {
                        coverageVmOptions.append(" ");
                    }
                    coverageVmOptions.append("-Dcoverage.enabled=true");

                    if (trackPerTest) {
                        coverageVmOptions.append(" -Dcoverage.per.test=true");
                    }

                    String runner = (String) coverageRunnerComboBox.getSelectedItem();
                    coverageVmOptions.append(" -Dcoverage.runner=").append(runner);
                }

                configuration.setVmOptions(coverageVmOptions.toString());

                System.out.println("DevTomcat: Applied code coverage configuration via VM options");
                System.out.println("DevTomcat: Coverage enabled: " + coverageEnabled);
                System.out.println("DevTomcat: Track per test: " + trackPerTest);
                System.out.println("DevTomcat: Coverage runner: " + coverageRunnerComboBox.getSelectedItem());
            } else {
                // Remove coverage VM options if coverage is disabled
                String vmOptions = configuration.getVmOptions();
                if (vmOptions != null) {
                    vmOptions = vmOptions.replaceAll("-Dcoverage\\.\\w+=[^\\s]*", "").trim();
                    configuration.setVmOptions(vmOptions);
                }
                System.out.println("DevTomcat: Applied code coverage configuration - Coverage disabled");
            }

        } catch (Exception e) {
            System.err.println("DevTomcat: Error applying coverage configuration: " + e.getMessage());
            throw new ConfigurationException("Failed to apply coverage settings: " + e.getMessage());
        }
    }

    /**
     * Check if coverage is configured
     */
    public boolean isCoverageEnabled() {
        return enableCoverageCheckBox.isSelected();
    }

    /**
     * Get selected coverage runner
     */
    public String getCoverageRunner() {
        return (String) coverageRunnerComboBox.getSelectedItem();
    }
}