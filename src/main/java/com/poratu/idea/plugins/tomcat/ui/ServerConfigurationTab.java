/**
 * Author: Gezahegn Lemma (Gezu)
 * Project: Dev Tomcat Plugin
 * Created: 6/9/25
 * Phase 2: Server configuration tab - Matches REAL Ultimate interface exactly
 */

package com.poratu.idea.plugins.tomcat.ui;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.util.ui.JBUI;
import com.poratu.idea.plugins.tomcat.conf.EnhancedTomcatRunConfiguration;
import com.poratu.idea.plugins.tomcat.ui.dialogs.ApplicationServersDialog;
import com.poratu.idea.plugins.tomcat.ui.dialogs.WebBrowsersDialog;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.List;

import javax.swing.*;
import java.awt.*;

/**
 * Real Ultimate Server Tab - matches actual Ultimate interface exactly from screenshot
 * Shows: Application server dropdown, Open browser section, VM options, Update actions,
 * JRE selection, Tomcat Server Settings with ports, Before launch, and bottom checkboxes
 */
public class ServerConfigurationTab extends JPanel {

    private final Project project;

    // Application Server Section (matches Ultimate exactly)
    private JComboBox<String> applicationServerComboBox;
    private JButton configureButton;

    // Open Browser Section (matches Ultimate exactly)
    private JCheckBox afterLaunchCheckBox;
    private JComboBox<String> browserComboBox;
    private JTextField urlField;
    private JCheckBox withJavaScriptDebuggerCheckBox;

    // VM Options Section (matches Ultimate exactly)
    private JTextArea vmOptionsArea;

    // Update Actions Section (matches Ultimate exactly)
    private JComboBox<String> updateActionComboBox;
    private JCheckBox showDialogCheckBox;

    // JRE Section (matches Ultimate exactly)
    private JComboBox<String> jreComboBox;
    private JButton jreConfigureButton;

    // Tomcat Server Settings Section (KEY Ultimate section from screenshot!)
    private JTextField httpPortField;
    private JTextField httpsPortField;
    private JTextField jmxPortField;
    private JTextField ajpPortField;
    private JCheckBox deployApplicationsCheckBox;
    private JCheckBox preserveSessionsCheckBox;

    // Before Launch Section (matches Ultimate exactly)
    private JList<String> beforeLaunchList;
    private JButton addBeforeLaunchButton;
    private JButton removeBeforeLaunchButton;
    private JButton moveUpBeforeLaunchButton;
    private JButton moveDownBeforeLaunchButton;

    // Bottom Options (matches Ultimate exactly)
    private JCheckBox showThisPageCheckBox;
    private JCheckBox activateToolWindowCheckBox;
    private JCheckBox focusToolWindowCheckBox;

    public ServerConfigurationTab(@NotNull Project project) {
        this.project = project;
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setBorder(JBUI.Borders.empty(10));

        // Create main panel with scroll support
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        // Add all sections in Ultimate's exact order from screenshot
        mainPanel.add(createApplicationServerSection());
        mainPanel.add(Box.createVerticalStrut(10));

        mainPanel.add(createOpenBrowserSection());
        mainPanel.add(Box.createVerticalStrut(10));

        mainPanel.add(createVmOptionsSection());
        mainPanel.add(Box.createVerticalStrut(10));

        mainPanel.add(createUpdateActionsSection());
        mainPanel.add(Box.createVerticalStrut(10));

        mainPanel.add(createJreSection());
        mainPanel.add(Box.createVerticalStrut(10));

        // KEY SECTION - Tomcat Server Settings (exactly from screenshot!)
        mainPanel.add(createTomcatServerSettingsSection());
        mainPanel.add(Box.createVerticalStrut(10));

        mainPanel.add(createBeforeLaunchSection());
        mainPanel.add(Box.createVerticalStrut(10));

        mainPanel.add(createBottomOptionsSection());

        // Add to scroll pane
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * Application server section - exactly like Ultimate screenshot
     */
    private JPanel createApplicationServerSection() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));

        panel.add(new JLabel("Application server:"));
        panel.add(Box.createHorizontalStrut(10));

        // Dropdown showing "Tomcat 10.1.15" like in screenshot
        applicationServerComboBox = new JComboBox<>(new String[]{"Tomcat 10.1.15"});
        applicationServerComboBox.setPreferredSize(new Dimension(200, 25));
        panel.add(applicationServerComboBox);

        panel.add(Box.createHorizontalStrut(10));
        configureButton = new JButton("Configure...");

        // Add action listener to open Application Servers dialog
        configureButton.addActionListener(e -> openApplicationServersDialog());
        panel.add(configureButton);

        return panel;
    }

    /**
     * Open browser section - exactly like Ultimate screenshot
     */
    private JPanel createOpenBrowserSection() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("Open browser"));

        // First line: After launch checkbox and browser selection
        JPanel firstLine = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        afterLaunchCheckBox = new JCheckBox("After launch", true);
        firstLine.add(afterLaunchCheckBox);

        firstLine.add(Box.createHorizontalStrut(10));
        browserComboBox = new JComboBox<>(new String[]{"Default", "Chrome", "Firefox", "Safari", "Edge"});
        firstLine.add(browserComboBox);

        firstLine.add(Box.createHorizontalStrut(10));
        firstLine.add(new JLabel("..."));

        firstLine.add(Box.createHorizontalStrut(10));
        withJavaScriptDebuggerCheckBox = new JCheckBox("with JavaScript debugger");
        firstLine.add(withJavaScriptDebuggerCheckBox);

        panel.add(firstLine);
        panel.add(Box.createVerticalStrut(5));

        // URL field line
        JPanel urlLine = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        urlLine.add(new JLabel("URL:"));
        urlLine.add(Box.createHorizontalStrut(10));
        urlField = new JTextField("http://localhost:8080/", 30);
        urlLine.add(urlField);
        urlLine.add(Box.createHorizontalStrut(10));

        // Browse button that opens Web Browsers dialog
        JButton urlBrowseButton = new JButton("...");
        urlBrowseButton.addActionListener(e -> openWebBrowsersDialog());
        urlLine.add(urlBrowseButton);

        panel.add(urlLine);

        return panel;
    }

    /**
     * VM options section - exactly like Ultimate screenshot
     */
    private JPanel createVmOptionsSection() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("VM options"));

        vmOptionsArea = new JTextArea(3, 50);
        vmOptionsArea.setToolTipText("JVM arguments for Tomcat startup");
        vmOptionsArea.setLineWrap(true);
        vmOptionsArea.setWrapStyleWord(true);
        JScrollPane vmScrollPane = new JScrollPane(vmOptionsArea);
        panel.add(vmScrollPane, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Update actions section - exactly like Ultimate screenshot
     */
    private JPanel createUpdateActionsSection() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));

        panel.add(new JLabel("On 'Update' action:"));
        panel.add(Box.createHorizontalStrut(10));

        updateActionComboBox = new JComboBox<>(new String[]{
                "Restart server",
                "Redeploy",
                "Update classes and resources"
        });
        panel.add(updateActionComboBox);

        panel.add(Box.createHorizontalStrut(10));
        showDialogCheckBox = new JCheckBox("Show dialog", true);
        panel.add(showDialogCheckBox);

        return panel;
    }

    /**
     * JRE section - exactly like Ultimate screenshot
     */
    private JPanel createJreSection() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));

        panel.add(new JLabel("JRE:"));
        panel.add(Box.createHorizontalStrut(10));

        jreComboBox = new JComboBox<>(new String[]{"Project SDK", "java version \"11.0.2\""});
        jreComboBox.setPreferredSize(new Dimension(200, 25));
        panel.add(jreComboBox);

        panel.add(Box.createHorizontalStrut(10));
        jreConfigureButton = new JButton("...");
        jreConfigureButton.addActionListener(e -> configureJRE());
        panel.add(jreConfigureButton);

        return panel;
    }

    /**
     * Open Application Servers configuration dialog
     */
    private void openApplicationServersDialog() {
        ApplicationServersDialog dialog = new ApplicationServersDialog(project);
        if (dialog.showAndGet()) {
            // Update application server dropdown with configured servers
            java.util.List<ApplicationServersDialog.TomcatServerInfo> servers = dialog.getConfiguredServers();
            applicationServerComboBox.removeAllItems();

            for (ApplicationServersDialog.TomcatServerInfo server : servers) {
                applicationServerComboBox.addItem(server.getName());
            }

            if (servers.size() > 0) {
                applicationServerComboBox.setSelectedIndex(0);
            }

            System.out.println("DevTomcat: Application servers configured - " + servers.size() + " servers");
        }
    }

    /**
     * Open Web Browsers configuration dialog
     */
    private void openWebBrowsersDialog() {
        WebBrowsersDialog dialog = new WebBrowsersDialog(project);
        if (dialog.showAndGet()) {
            // Update browser dropdown with configured browsers
            java.util.List<WebBrowsersDialog.BrowserInfo> browsers = dialog.getConfiguredBrowsers();
            browserComboBox.removeAllItems();

            browserComboBox.addItem("Default");
            for (WebBrowsersDialog.BrowserInfo browser : browsers) {
                if (browser.isEnabled()) {
                    browserComboBox.addItem(browser.getName());
                }
            }

            // Set default browser
            String defaultBrowser = dialog.getDefaultBrowser();
            if (!"System default".equals(defaultBrowser)) {
                browserComboBox.setSelectedItem(defaultBrowser);
            }

            System.out.println("DevTomcat: Web browsers configured - " + browsers.size() + " browsers");
        }
    }

    /**
     * Configure JRE settings
     */
    private void configureJRE() {
        // TODO: Open JRE configuration dialog
        JOptionPane.showMessageDialog(getParent(),
                "JRE configuration dialog - TODO\nWould open SDK/JRE selection dialog",
                "Configure JRE",
                JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * KEY SECTION - Tomcat Server Settings exactly from Ultimate screenshot!
     * This is the most important section that makes it look like Ultimate
     */
    private JPanel createTomcatServerSettingsSection() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Tomcat Server Settings"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = JBUI.insets(5);
        gbc.anchor = GridBagConstraints.WEST;

        // HTTP port (left side, row 0)
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("HTTP port:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 0.3;
        httpPortField = new JTextField("8080", 8);
        panel.add(httpPortField, gbc);

        // Deploy applications checkbox (right side, row 0)
        gbc.gridx = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.7;
        deployApplicationsCheckBox = new JCheckBox("Deploy applications configured in Tomcat instance");
        panel.add(deployApplicationsCheckBox, gbc);

        // HTTPS port (left side, row 1)
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0;
        panel.add(new JLabel("HTTPS port:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 0.3;
        httpsPortField = new JTextField(8);
        panel.add(httpsPortField, gbc);

        // Preserve sessions checkbox (right side, row 1)
        gbc.gridx = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.7;
        preserveSessionsCheckBox = new JCheckBox("Preserve sessions across restarts and redeploys");
        panel.add(preserveSessionsCheckBox, gbc);

        // JMX port (left side, row 2) - KEY Ultimate feature from screenshot!
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.0;
        panel.add(new JLabel("JMX port:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 0.3;
        jmxPortField = new JTextField("1099", 8);  // Default 1099 like Ultimate
        panel.add(jmxPortField, gbc);

        // AJP port (left side, row 3)
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.0;
        panel.add(new JLabel("AJP port:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 0.3;
        ajpPortField = new JTextField(8);
        panel.add(ajpPortField, gbc);

        return panel;
    }

    /**
     * Before launch section - exactly like Ultimate screenshot
     */
    private JPanel createBeforeLaunchSection() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Before launch"));

        // Create list with default "Build" item like Ultimate
        DefaultListModel<String> listModel = new DefaultListModel<>();
        listModel.addElement("Build");
        beforeLaunchList = new JList<>(listModel);
        beforeLaunchList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane listScrollPane = new JScrollPane(beforeLaunchList);
        listScrollPane.setPreferredSize(new Dimension(400, 60));
        panel.add(listScrollPane, BorderLayout.CENTER);

        // Add buttons panel (vertical layout like Ultimate)
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS));
        buttonsPanel.setBorder(JBUI.Borders.emptyLeft(10));

        addBeforeLaunchButton = new JButton("+");
        removeBeforeLaunchButton = new JButton("-");
        moveUpBeforeLaunchButton = new JButton("↑");
        moveDownBeforeLaunchButton = new JButton("↓");

        Dimension buttonSize = new Dimension(30, 25);
        addBeforeLaunchButton.setPreferredSize(buttonSize);
        removeBeforeLaunchButton.setPreferredSize(buttonSize);
        moveUpBeforeLaunchButton.setPreferredSize(buttonSize);
        moveDownBeforeLaunchButton.setPreferredSize(buttonSize);

        buttonsPanel.add(addBeforeLaunchButton);
        buttonsPanel.add(Box.createVerticalStrut(2));
        buttonsPanel.add(removeBeforeLaunchButton);
        buttonsPanel.add(Box.createVerticalStrut(5));
        buttonsPanel.add(moveUpBeforeLaunchButton);
        buttonsPanel.add(Box.createVerticalStrut(2));
        buttonsPanel.add(moveDownBeforeLaunchButton);
        buttonsPanel.add(Box.createVerticalGlue());

        panel.add(buttonsPanel, BorderLayout.EAST);

        return panel;
    }

    /**
     * Bottom options section - exactly like Ultimate screenshot
     */
    private JPanel createBottomOptionsSection() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(JBUI.Borders.emptyTop(10));

        showThisPageCheckBox = new JCheckBox("Show this page");
        activateToolWindowCheckBox = new JCheckBox("Activate tool window", true);
        focusToolWindowCheckBox = new JCheckBox("Focus tool window");

        panel.add(showThisPageCheckBox);
        panel.add(activateToolWindowCheckBox);
        panel.add(focusToolWindowCheckBox);

        return panel;
    }

    /**
     * Phase 2: Reset form from EnhancedTomcatRunConfiguration (matches Ultimate data)
     */
    public void resetFrom(@NotNull EnhancedTomcatRunConfiguration configuration) {
        // Application server (show configured Tomcat)
        applicationServerComboBox.setSelectedItem("Tomcat 10.1.15");

        // Open browser settings
        afterLaunchCheckBox.setSelected(true);
        browserComboBox.setSelectedItem("Default");
        withJavaScriptDebuggerCheckBox.setSelected(false);

        // URL with context path
        String contextPath = configuration.getContextPath();
        int port = configuration.getPort() != null ? configuration.getPort() : 8080;
        if (contextPath != null && !contextPath.isEmpty()) {
            urlField.setText("http://localhost:" + port + contextPath);
        } else {
            urlField.setText("http://localhost:" + port + "/");
        }

        // VM options from configuration
        String vmOptions = configuration.getVmOptions();
        if (vmOptions != null) {
            vmOptionsArea.setText(vmOptions);
        } else {
            vmOptionsArea.setText("");
        }

        // Update actions
        if (configuration.isUpdateClassesAndResources()) {
            updateActionComboBox.setSelectedItem("Update classes and resources");
        } else {
            updateActionComboBox.setSelectedItem("Restart server");
        }
        showDialogCheckBox.setSelected(true);

        // JRE selection
        jreComboBox.setSelectedItem("Project SDK");

        // Tomcat Server Settings (KEY section from Ultimate screenshot!)
        httpPortField.setText(String.valueOf(port));

        // HTTPS port (empty by default like Ultimate)
        httpsPortField.setText("");

        // JMX port (KEY Ultimate feature!)
        if (configuration.isJmxEnabled()) {
            jmxPortField.setText(String.valueOf(configuration.getJmxPort()));
        } else {
            jmxPortField.setText("1099"); // Default like Ultimate
        }

        // AJP port (empty by default like Ultimate)
        ajpPortField.setText("");

        // Checkboxes
        deployApplicationsCheckBox.setSelected(false); // Default like Ultimate
        preserveSessionsCheckBox.setSelected(false);   // Default like Ultimate

        // Bottom options (like Ultimate screenshot)
        showThisPageCheckBox.setSelected(false);
        activateToolWindowCheckBox.setSelected(true);  // Default checked like Ultimate
        focusToolWindowCheckBox.setSelected(false);

        System.out.println("DevTomcat: Reset server configuration from Enhanced config");
    }

    /**
     * Phase 2: Apply form to EnhancedTomcatRunConfiguration
     */
    public void applyTo(@NotNull EnhancedTomcatRunConfiguration configuration) throws ConfigurationException {
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

        // Apply VM options
        String vmOptions = vmOptionsArea.getText().trim();
        configuration.setVmOptions(vmOptions);

        // Phase 2: Apply JMX configuration (KEY Ultimate feature!)
        try {
            String jmxPortText = jmxPortField.getText().trim();
            if (!jmxPortText.isEmpty()) {
                int jmxPort = Integer.parseInt(jmxPortText);
                if (jmxPort < 1 || jmxPort > 65535) {
                    throw new ConfigurationException("JMX port must be between 1 and 65535");
                }
                configuration.setJmxPort(jmxPort);
                configuration.setJmxEnabled(true); // Enable JMX if port is set
            } else {
                configuration.setJmxEnabled(false);
            }
        } catch (NumberFormatException e) {
            throw new ConfigurationException("Invalid JMX port number");
        }

        // Phase 2: Apply update actions
        String selectedAction = (String) updateActionComboBox.getSelectedItem();
        if ("Update classes and resources".equals(selectedAction)) {
            configuration.setUpdateClassesAndResources(true);
            configuration.setHotDeploymentEnabled(true);
        } else {
            configuration.setUpdateClassesAndResources(false);
            configuration.setHotDeploymentEnabled(false);
        }

        // TODO: Phase 2 - Apply additional Ultimate settings
        // These will be implemented as we add full Ultimate feature support:
        // - HTTPS port configuration
        // - AJP port configuration
        // - Deploy applications checkbox
        // - Preserve sessions checkbox
        // - Browser launch settings
        // - Before launch tasks

        System.out.println("DevTomcat: Applied server configuration to Enhanced config");
    }

    /**
     * Validate current configuration
     */
    public boolean isConfigurationValid() {
        try {
            // Validate HTTP port
            String httpPortText = httpPortField.getText().trim();
            if (!httpPortText.isEmpty()) {
                int httpPort = Integer.parseInt(httpPortText);
                if (httpPort <= 0 || httpPort > 65535) {
                    return false;
                }
            }

            // Validate JMX port
            String jmxPortText = jmxPortField.getText().trim();
            if (!jmxPortText.isEmpty()) {
                int jmxPort = Integer.parseInt(jmxPortText);
                if (jmxPort <= 0 || jmxPort > 65535) {
                    return false;
                }
            }

            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Get effective VM options including JMX if enabled
     */
    public String getEffectiveVmOptions() {
        StringBuilder vmOptions = new StringBuilder();

        // Add user VM options
        String userVmOptions = vmOptionsArea.getText().trim();
        if (!userVmOptions.isEmpty()) {
            vmOptions.append(userVmOptions);
        }

        // Add JMX options if JMX port is configured (Ultimate behavior)
        String jmxPortText = jmxPortField.getText().trim();
        if (!jmxPortText.isEmpty()) {
            if (vmOptions.length() > 0) {
                vmOptions.append(" ");
            }
            vmOptions.append("-Dcom.sun.management.jmxremote ");
            vmOptions.append("-Dcom.sun.management.jmxremote.port=").append(jmxPortText).append(" ");
            vmOptions.append("-Dcom.sun.management.jmxremote.ssl=false ");
            vmOptions.append("-Dcom.sun.management.jmxremote.authenticate=false ");
            vmOptions.append("-Dcom.sun.management.jmxremote.local.only=false");
        }

        return vmOptions.toString();
    }

    /**
     * Check if JMX is configured
     */
    public boolean isJmxConfigured() {
        String jmxPortText = jmxPortField.getText().trim();
        return !jmxPortText.isEmpty();
    }
}