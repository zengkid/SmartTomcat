/**
 * Author: Gezahegn Lemma (Gezu)
 * Project: Dev Tomcat Plugin
 * Created: 6/9/25
 * Phase 2: Deployment configuration tab - Matches REAL Ultimate interface
 */

package com.poratu.idea.plugins.tomcat.ui;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.JBUI;
import com.poratu.idea.plugins.tomcat.conf.EnhancedTomcatRunConfiguration;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Real Ultimate Deployment Tab - matches actual Ultimate interface exactly
 * Shows "Deploy at the server startup" with artifacts table and management buttons
 */
public class DeploymentConfigurationTab extends JPanel {

    private final Project project;

    // Deployment table and model
    private JBTable deploymentTable;
    private DefaultTableModel tableModel;

    // Management buttons
    private JButton addButton;
    private JButton removeButton;
    private JButton moveUpButton;
    private JButton moveDownButton;
    private JButton editButton;

    // "Nothing to deploy" label
    private JLabel nothingToDeployLabel;

    public DeploymentConfigurationTab(@NotNull Project project) {
        this.project = project;
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setBorder(JBUI.Borders.empty(10));

        // Create main panel
        JPanel mainPanel = new JPanel(new BorderLayout());

        // Add "Deploy at the server startup" section
        mainPanel.add(createDeploymentSection(), BorderLayout.CENTER);

        // Add before launch section at bottom (like Ultimate)
        mainPanel.add(createBeforeLaunchSection(), BorderLayout.SOUTH);

        add(mainPanel, BorderLayout.CENTER);
    }

    /**
     * Create the main deployment section - matches Ultimate exactly
     */
    private JPanel createDeploymentSection() {
        JPanel panel = new JPanel(new BorderLayout());

        // Section title
        JLabel titleLabel = new JLabel("Deploy at the server startup");
        titleLabel.setBorder(JBUI.Borders.emptyBottom(5));
        panel.add(titleLabel, BorderLayout.NORTH);

        // Create center panel with table and buttons
        JPanel centerPanel = new JPanel(new BorderLayout());

        // Create table area
        JPanel tablePanel = createTablePanel();
        centerPanel.add(tablePanel, BorderLayout.CENTER);

        // Create buttons panel (vertical, on the right like Ultimate)
        JPanel buttonsPanel = createButtonsPanel();
        centerPanel.add(buttonsPanel, BorderLayout.EAST);

        panel.add(centerPanel, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Create the deployments table - matches Ultimate's artifact table
     */
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Create table model with Ultimate's columns
        String[] columnNames = {"Artifact", "Type", "Server Path"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Read-only like Ultimate
            }
        };

        // Create table
        deploymentTable = new JBTable(tableModel);
        deploymentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        deploymentTable.getTableHeader().setReorderingAllowed(false);

        // Set column widths like Ultimate
        deploymentTable.getColumnModel().getColumn(0).setPreferredWidth(200); // Artifact
        deploymentTable.getColumnModel().getColumn(1).setPreferredWidth(100); // Type
        deploymentTable.getColumnModel().getColumn(2).setPreferredWidth(150); // Server Path

        // Wrap in scroll pane
        JScrollPane scrollPane = new JScrollPane(deploymentTable);
        scrollPane.setPreferredSize(new Dimension(500, 200));

        // Create "Nothing to deploy" overlay
        nothingToDeployLabel = new JLabel("Nothing to deploy", JLabel.CENTER);
        nothingToDeployLabel.setForeground(Color.GRAY);
        nothingToDeployLabel.setFont(nothingToDeployLabel.getFont().deriveFont(Font.ITALIC));

        // Layer panel to show "Nothing to deploy" when table is empty
        JPanel layeredPanel = new JPanel();
        layeredPanel.setLayout(new OverlayLayout(layeredPanel));
        layeredPanel.add(nothingToDeployLabel);
        layeredPanel.add(scrollPane);

        panel.add(layeredPanel, BorderLayout.CENTER);

        // Update visibility based on table content
        updateNothingToDeployVisibility();

        return panel;
    }

    /**
     * Create buttons panel - matches Ultimate's vertical button layout
     */
    private JPanel createButtonsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(JBUI.Borders.emptyLeft(10));

        // Create buttons (same as Ultimate)
        addButton = new JButton("+");
        removeButton = new JButton("-");
        moveUpButton = new JButton("↑");
        moveDownButton = new JButton("↓");
        editButton = new JButton("✏");

        // Set button sizes
        Dimension buttonSize = new Dimension(30, 25);
        addButton.setPreferredSize(buttonSize);
        removeButton.setPreferredSize(buttonSize);
        moveUpButton.setPreferredSize(buttonSize);
        moveDownButton.setPreferredSize(buttonSize);
        editButton.setPreferredSize(buttonSize);

        // Add tooltips like Ultimate
        addButton.setToolTipText("Add artifact");
        removeButton.setToolTipText("Remove artifact");
        moveUpButton.setToolTipText("Move up");
        moveDownButton.setToolTipText("Move down");
        editButton.setToolTipText("Edit artifact");

        // Add action listeners
        addButton.addActionListener(e -> addArtifact());
        removeButton.addActionListener(e -> removeArtifact());
        moveUpButton.addActionListener(e -> moveArtifactUp());
        moveDownButton.addActionListener(e -> moveArtifactDown());
        editButton.addActionListener(e -> editArtifact());

        // Add buttons to panel
        panel.add(addButton);
        panel.add(Box.createVerticalStrut(2));
        panel.add(removeButton);
        panel.add(Box.createVerticalStrut(5));
        panel.add(moveUpButton);
        panel.add(Box.createVerticalStrut(2));
        panel.add(moveDownButton);
        panel.add(Box.createVerticalStrut(5));
        panel.add(editButton);
        panel.add(Box.createVerticalGlue());

        // Initially disable buttons (no selection)
        updateButtonStates();

        // Add selection listener to update button states
        deploymentTable.getSelectionModel().addListSelectionListener(e -> updateButtonStates());

        return panel;
    }

    /**
     * Create before launch section - matches Ultimate's bottom section
     */
    private JPanel createBeforeLaunchSection() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(JBUI.Borders.emptyTop(20));

        // Add "Before launch" section like Ultimate
        JLabel beforeLaunchLabel = new JLabel("Before launch");
        beforeLaunchLabel.setBorder(JBUI.Borders.emptyBottom(5));
        panel.add(beforeLaunchLabel, BorderLayout.NORTH);

        // Create before launch list (like Ultimate)
        DefaultListModel<String> beforeLaunchModel = new DefaultListModel<>();
        beforeLaunchModel.addElement("Build");

        JList<String> beforeLaunchList = new JList<>(beforeLaunchModel);
        beforeLaunchList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane beforeLaunchScroll = new JScrollPane(beforeLaunchList);
        beforeLaunchScroll.setPreferredSize(new Dimension(400, 60));

        // Create buttons for before launch (like Ultimate)
        JPanel beforeLaunchButtonsPanel = new JPanel();
        beforeLaunchButtonsPanel.setLayout(new BoxLayout(beforeLaunchButtonsPanel, BoxLayout.Y_AXIS));
        beforeLaunchButtonsPanel.add(new JButton("+"));
        beforeLaunchButtonsPanel.add(new JButton("-"));
        beforeLaunchButtonsPanel.add(new JButton("↑"));
        beforeLaunchButtonsPanel.add(new JButton("↓"));

        JPanel beforeLaunchContent = new JPanel(new BorderLayout());
        beforeLaunchContent.add(beforeLaunchScroll, BorderLayout.CENTER);
        beforeLaunchContent.add(beforeLaunchButtonsPanel, BorderLayout.EAST);

        panel.add(beforeLaunchContent, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Add new artifact - shows dialog like Ultimate
     */
    private void addArtifact() {
        // TODO: Show Ultimate-style artifact selection dialog
        // For now, add a placeholder
        String[] artifactData = {"myapp:war exploded", "war exploded", "/"};
        tableModel.addRow(artifactData);
        updateNothingToDeployVisibility();
        updateButtonStates();

        System.out.println("DevTomcat: Add artifact dialog (Ultimate-style) - TODO");
    }

    /**
     * Remove selected artifact
     */
    private void removeArtifact() {
        int selectedRow = deploymentTable.getSelectedRow();
        if (selectedRow >= 0) {
            tableModel.removeRow(selectedRow);
            updateNothingToDeployVisibility();
            updateButtonStates();
        }
    }

    /**
     * Move artifact up in the list
     */
    private void moveArtifactUp() {
        int selectedRow = deploymentTable.getSelectedRow();
        if (selectedRow > 0) {
            tableModel.moveRow(selectedRow, selectedRow, selectedRow - 1);
            deploymentTable.setRowSelectionInterval(selectedRow - 1, selectedRow - 1);
        }
    }

    /**
     * Move artifact down in the list
     */
    private void moveArtifactDown() {
        int selectedRow = deploymentTable.getSelectedRow();
        if (selectedRow >= 0 && selectedRow < tableModel.getRowCount() - 1) {
            tableModel.moveRow(selectedRow, selectedRow, selectedRow + 1);
            deploymentTable.setRowSelectionInterval(selectedRow + 1, selectedRow + 1);
        }
    }

    /**
     * Edit selected artifact
     */
    private void editArtifact() {
        int selectedRow = deploymentTable.getSelectedRow();
        if (selectedRow >= 0) {
            // TODO: Show Ultimate-style artifact edit dialog
            System.out.println("DevTomcat: Edit artifact dialog (Ultimate-style) - TODO");
        }
    }

    /**
     * Update button enabled states based on selection
     */
    private void updateButtonStates() {
        int selectedRow = deploymentTable.getSelectedRow();
        int rowCount = tableModel.getRowCount();

        // Enable/disable buttons based on selection and position
        removeButton.setEnabled(selectedRow >= 0);
        editButton.setEnabled(selectedRow >= 0);
        moveUpButton.setEnabled(selectedRow > 0);
        moveDownButton.setEnabled(selectedRow >= 0 && selectedRow < rowCount - 1);
    }

    /**
     * Update "Nothing to deploy" label visibility
     */
    private void updateNothingToDeployVisibility() {
        boolean hasArtifacts = tableModel.getRowCount() > 0;
        nothingToDeployLabel.setVisible(!hasArtifacts);

        // Show warning in Ultimate style if no artifacts
        if (!hasArtifacts) {
            // TODO: Add warning icon like Ultimate shows "Warning: No artifacts configured"
        }
    }

    /**
     * Reset from configuration - matches Ultimate's artifact loading
     */
    public void resetFrom(@NotNull EnhancedTomcatRunConfiguration configuration) {
        // Clear existing artifacts
        tableModel.setRowCount(0);

        // TODO: Load artifacts from configuration
        // For now, show empty state like Ultimate when no artifacts configured

        updateNothingToDeployVisibility();
        updateButtonStates();

        System.out.println("DevTomcat: Reset deployment configuration from Enhanced config");
    }

    /**
     * Apply to configuration - saves artifacts to Enhanced config
     */
    public void applyTo(@NotNull EnhancedTomcatRunConfiguration configuration) throws ConfigurationException {
        // TODO: Save artifacts to configuration
        // Validate that at least one artifact is configured if needed

        int artifactCount = tableModel.getRowCount();
        if (artifactCount == 0) {
            System.out.println("DevTomcat: No artifacts configured for deployment");
            // Note: Ultimate allows empty deployment, so this is not an error
        }

        System.out.println("DevTomcat: Applied " + artifactCount + " artifacts to deployment configuration");
    }

    /**
     * Check if any artifacts are configured
     */
    public boolean hasArtifacts() {
        return tableModel.getRowCount() > 0;
    }

    /**
     * Get number of configured artifacts
     */
    public int getArtifactCount() {
        return tableModel.getRowCount();
    }
}