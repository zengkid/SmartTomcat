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
 * Phase 2: Server configuration tab - Basic Tomcat server settings
 * Author: Gezahegn Lemma (Gezu)
 * Project: Dev Tomcat Plugin
 * Created: 6/9/25
 */
public class ServerConfigurationTab extends JPanel {

    private final Project project;

    // Server Configuration
    private JTextField serverNameField;
    private JTextField httpPortField;
    private JTextField httpsPortField;
    private JTextField adminPortField;

    // VM Options
    private JTextArea vmOptionsArea;
    private JTextField extraClassPathField;

    // Environment
    private JCheckBox passParentEnvsCheckBox;

    public ServerConfigurationTab(@NotNull Project project) {
        this.project = project;
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setBorder(JBUI.Borders.empty(10));

        // Create main panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        // Add server settings section
        mainPanel.add(createServerSettingsPanel());
        mainPanel.add(Box.createVerticalStrut(10));

        // Add VM options section
        mainPanel.add(createVmOptionsPanel());
        mainPanel.add(Box.createVerticalStrut(10));

        // Add environment section
        mainPanel.add(createEnvironmentPanel());

        add(mainPanel, BorderLayout.NORTH);
    }

    private JPanel createServerSettingsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Server Settings"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = JBUI.insets(5);
        gbc.anchor = GridBagConstraints.WEST;

        // Server Name
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Name:"), gbc);

        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        serverNameField = new JTextField();
        serverNameField.setToolTipText("Configuration name");
        panel.add(serverNameField, gbc);

        // HTTP Port
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0;
        panel.add(new JLabel("HTTP Port:"), gbc);

        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        httpPortField = new JTextField();
        httpPortField.setToolTipText("HTTP connector port (default: 8080)");
        panel.add(httpPortField, gbc);

        // HTTPS Port
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0;
        panel.add(new JLabel("HTTPS Port:"), gbc);

        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        httpsPortField = new JTextField();
        httpsPortField.setToolTipText("HTTPS connector port (optional)");
        panel.add(httpsPortField, gbc);

        // Admin Port
        gbc.gridx = 0; gbc.gridy = 3; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0;
        panel.add(new JLabel("Admin Port:"), gbc);

        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        adminPortField = new JTextField();
        adminPortField.setToolTipText("Server shutdown port (default: 8005)");
        panel.add(adminPortField, gbc);

        return panel;
    }

    private JPanel createVmOptionsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("VM Options"));

        // VM Options text area
        vmOptionsArea = new JTextArea(4, 50);
        vmOptionsArea.setToolTipText("JVM arguments for Tomcat startup");
        vmOptionsArea.setLineWrap(true);
        vmOptionsArea.setWrapStyleWord(true);
        JScrollPane vmScrollPane = new JScrollPane(vmOptionsArea);
        panel.add(vmScrollPane, BorderLayout.CENTER);

        // Extra classpath
        JPanel classpathPanel = new JPanel(new BorderLayout());
        classpathPanel.add(new JLabel("Extra Classpath:"), BorderLayout.WEST);
        extraClassPathField = new JTextField();
        extraClassPathField.setToolTipText("Additional classpath entries");
        classpathPanel.add(extraClassPathField, BorderLayout.CENTER);

        panel.add(classpathPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createEnvironmentPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("Environment"));

        passParentEnvsCheckBox = new JCheckBox("Pass parent environment variables", true);
        passParentEnvsCheckBox.setToolTipText("Include system environment variables in Tomcat process");
        panel.add(passParentEnvsCheckBox);

        return panel;
    }

    public void resetFrom(@NotNull TomcatRunConfiguration configuration) {
        // Reset basic configuration
        serverNameField.setText(configuration.getName());

        if (configuration.getPort() != null) {
            httpPortField.setText(configuration.getPort().toString());
        }

        if (configuration.getSslPort() != null) {
            httpsPortField.setText(configuration.getSslPort().toString());
        }

        if (configuration.getAdminPort() != null) {
            adminPortField.setText(configuration.getAdminPort().toString());
        }

        vmOptionsArea.setText(configuration.getVmOptions());
        extraClassPathField.setText(configuration.getExtraClassPath());
        passParentEnvsCheckBox.setSelected(configuration.isPassParentEnvs());
    }

    public void applyTo(@NotNull TomcatRunConfiguration configuration) throws ConfigurationException {
        // Validate and apply server name
        String name = serverNameField.getText().trim();
        if (name.isEmpty()) {
            throw new ConfigurationException("Server name cannot be empty");
        }
        configuration.setName(name);

        // Validate and apply HTTP port
        try {
            String httpPortText = httpPortField.getText().trim();
            if (!httpPortText.isEmpty()) {
                int httpPort = Integer.parseInt(httpPortText);
                if (httpPort < 1 || httpPort > 65535) {
                    throw new ConfigurationException("HTTP port must be between 1 and 65535");
                }
                configuration.setPort(httpPort);
            }
        } catch (NumberFormatException e) {
            throw new ConfigurationException("Invalid HTTP port number");
        }

        // Validate and apply HTTPS port
        try {
            String httpsPortText = httpsPortField.getText().trim();
            if (!httpsPortText.isEmpty()) {
                int httpsPort = Integer.parseInt(httpsPortText);
                if (httpsPort < 1 || httpsPort > 65535) {
                    throw new ConfigurationException("HTTPS port must be between 1 and 65535");
                }
                configuration.setSslPort(httpsPort);
            }
        } catch (NumberFormatException e) {
            throw new ConfigurationException("Invalid HTTPS port number");
        }

        // Validate and apply admin port
        try {
            String adminPortText = adminPortField.getText().trim();
            if (!adminPortText.isEmpty()) {
                int adminPort = Integer.parseInt(adminPortText);
                if (adminPort < 1 || adminPort > 65535) {
                    throw new ConfigurationException("Admin port must be between 1 and 65535");
                }
                configuration.setAdminPort(adminPort);
            }
        } catch (NumberFormatException e) {
            throw new ConfigurationException("Invalid admin port number");
        }

        // Apply VM options and classpath
        configuration.setVmOptions(vmOptionsArea.getText().trim());
        configuration.setExtraClassPath(extraClassPathField.getText().trim());
        configuration.setPassParentEnvironmentVariables(passParentEnvsCheckBox.isSelected());
    }
}