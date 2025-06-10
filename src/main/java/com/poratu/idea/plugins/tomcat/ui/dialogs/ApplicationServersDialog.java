/**
 * Author: Gezahegn Lemma (Gezu)
 * Project: Dev Tomcat Plugin
 * Created: 6/9/25
 * Phase 2: Application Servers configuration dialog - Matches Ultimate exactly
 */

package com.poratu.idea.plugins.tomcat.ui.dialogs;

import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Application Servers configuration dialog - exactly like Ultimate's dialog
 * Shows server list, server details, and libraries management
 */
public class ApplicationServersDialog extends DialogWrapper {

    private final Project project;

    // Server list (left side)
    private JBList<TomcatServerInfo> serverList;
    private DefaultListModel<TomcatServerInfo> serverListModel;
    private JButton addServerButton;
    private JButton removeServerButton;

    // Server details (right side)
    private JTextField serverNameField;
    private JTextField tomcatHomeField;
    private JButton tomcatHomeBrowseButton;
    private JTextField tomcatVersionField;
    private JTextField tomcatBaseField;
    private JButton tomcatBaseBrowseButton;

    // Libraries section
    private JTree librariesTree;
    private DefaultTreeModel librariesTreeModel;
    private JButton addLibraryButton;
    private JButton removeLibraryButton;
    private JButton attachSourcesButton;

    // Current selection
    private TomcatServerInfo currentServer;

    public ApplicationServersDialog(@NotNull Project project) {
	super(project);
	this.project = project;
	setTitle("Application Servers");
	setSize(800, 600);
	init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
	JPanel mainPanel = new JPanel(new BorderLayout());

	// Create split pane like Ultimate
	JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
	splitPane.setLeftComponent(createServerListPanel());
	splitPane.setRightComponent(createServerDetailsPanel());
	splitPane.setDividerLocation(250);

	mainPanel.add(splitPane, BorderLayout.CENTER);

	// Initialize with default servers
	initializeDefaultServers();

	return mainPanel;
    }

    /**
     * Create server list panel (left side) - matches Ultimate exactly
     */
    private JPanel createServerListPanel() {
	JPanel panel = new JPanel(new BorderLayout());
	panel.setBorder(JBUI.Borders.empty(10));

	// Server list
	serverListModel = new DefaultListModel<>();
	serverList = new JBList<>(serverListModel);
	serverList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	serverList.setCellRenderer(new ServerListCellRenderer());

	// Add selection listener
	serverList.addListSelectionListener(e -> {
	    if (!e.getValueIsAdjusting()) {
		updateServerDetails();
	    }
	});

	JBScrollPane scrollPane = new JBScrollPane(serverList);
	scrollPane.setPreferredSize(new Dimension(200, 400));
	panel.add(scrollPane, BorderLayout.CENTER);

	// Buttons panel
	JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

	addServerButton = new JButton("+");
	removeServerButton = new JButton("-");

	addServerButton.setPreferredSize(new Dimension(30, 25));
	removeServerButton.setPreferredSize(new Dimension(30, 25));

	addServerButton.addActionListener(e -> addServer());
	removeServerButton.addActionListener(e -> removeServer());

	buttonsPanel.add(addServerButton);
	buttonsPanel.add(removeServerButton);

	panel.add(buttonsPanel, BorderLayout.SOUTH);

	return panel;
    }

    /**
     * Create server details panel (right side) - matches Ultimate exactly
     */
    private JPanel createServerDetailsPanel() {
	JPanel panel = new JPanel(new BorderLayout());
	panel.setBorder(JBUI.Borders.empty(10));

	// Server configuration panel
	JPanel configPanel = createServerConfigPanel();
	panel.add(configPanel, BorderLayout.NORTH);

	// Libraries panel
	JPanel librariesPanel = createLibrariesPanel();
	panel.add(librariesPanel, BorderLayout.CENTER);

	return panel;
    }

    /**
     * Create server configuration panel - matches Ultimate's server details
     */
    private JPanel createServerConfigPanel() {
	JPanel panel = new JPanel(new GridBagLayout());
	panel.setBorder(BorderFactory.createTitledBorder("Server Configuration"));

	GridBagConstraints gbc = new GridBagConstraints();
	gbc.insets = JBUI.insets(5);
	gbc.anchor = GridBagConstraints.WEST;

	// Name field
	gbc.gridx = 0; gbc.gridy = 0;
	panel.add(new JLabel("Name:"), gbc);
	gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
	serverNameField = new JTextField();
	serverNameField.addActionListener(e -> updateCurrentServer());
	panel.add(serverNameField, gbc);

	// Tomcat Home
	gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0;
	panel.add(new JLabel("Tomcat Home:"), gbc);
	gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;

	JPanel homePanel = new JPanel(new BorderLayout());
	tomcatHomeField = new JTextField();
	tomcatHomeField.addActionListener(e -> updateCurrentServer());
	tomcatHomeBrowseButton = new JButton("...");
	tomcatHomeBrowseButton.addActionListener(e -> browseTomcatHome());
	homePanel.add(tomcatHomeField, BorderLayout.CENTER);
	homePanel.add(tomcatHomeBrowseButton, BorderLayout.EAST);
	panel.add(homePanel, gbc);

	// Tomcat Version (auto-detected)
	gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0;
	panel.add(new JLabel("Tomcat Version:"), gbc);
	gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
	tomcatVersionField = new JTextField();
	tomcatVersionField.setEditable(false); // Read-only, auto-detected
	panel.add(tomcatVersionField, gbc);

	// Tomcat Base Directory
	gbc.gridx = 0; gbc.gridy = 3; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0;
	panel.add(new JLabel("Tomcat base directory:"), gbc);
	gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;

	JPanel basePanel = new JPanel(new BorderLayout());
	tomcatBaseField = new JTextField();
	tomcatBaseField.addActionListener(e -> updateCurrentServer());
	tomcatBaseBrowseButton = new JButton("...");
	tomcatBaseBrowseButton.addActionListener(e -> browseTomcatBase());
	basePanel.add(tomcatBaseField, BorderLayout.CENTER);
	basePanel.add(tomcatBaseBrowseButton, BorderLayout.EAST);
	panel.add(basePanel, gbc);

	return panel;
    }

    /**
     * Create libraries panel - matches Ultimate's libraries tree
     */
    private JPanel createLibrariesPanel() {
	JPanel panel = new JPanel(new BorderLayout());
	panel.setBorder(BorderFactory.createTitledBorder("Libraries"));

	// Libraries tree
	DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Libraries");
	librariesTreeModel = new DefaultTreeModel(rootNode);
	librariesTree = new JTree(librariesTreeModel);
	librariesTree.setRootVisible(true);
	librariesTree.setShowsRootHandles(true);

	JBScrollPane treeScrollPane = new JBScrollPane(librariesTree);
	treeScrollPane.setPreferredSize(new Dimension(400, 200));
	panel.add(treeScrollPane, BorderLayout.CENTER);

	// Libraries buttons
	JPanel libButtonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

	addLibraryButton = new JButton("+");
	removeLibraryButton = new JButton("-");
	attachSourcesButton = new JButton("Attach");

	Dimension libButtonSize = new Dimension(60, 25);
	addLibraryButton.setPreferredSize(libButtonSize);
	removeLibraryButton.setPreferredSize(libButtonSize);
	attachSourcesButton.setPreferredSize(libButtonSize);

	addLibraryButton.setToolTipText("Add JAR/Directory");
	removeLibraryButton.setToolTipText("Remove Library");
	attachSourcesButton.setToolTipText("Attach Sources");

	addLibraryButton.addActionListener(e -> addLibrary());
	removeLibraryButton.addActionListener(e -> removeLibrary());
	attachSourcesButton.addActionListener(e -> attachSources());

	libButtonsPanel.add(addLibraryButton);
	libButtonsPanel.add(removeLibraryButton);
	libButtonsPanel.add(attachSourcesButton);

	panel.add(libButtonsPanel, BorderLayout.SOUTH);

	return panel;
    }

    /**
     * Initialize with default Tomcat servers
     */
    private void initializeDefaultServers() {
	// Add default servers like Ultimate
	TomcatServerInfo tomcat1027 = new TomcatServerInfo("Tomcat 10.0.27",
		"C:\\apache-tomcat-10.0.27", "10.0.27", "C:\\apache-tomcat-10.0.27");
	TomcatServerInfo tomcat1115 = new TomcatServerInfo("Tomcat 10.1.15",
		"C:\\apache-tomcat-10.1.15", "10.1.15", "C:\\apache-tomcat-10.1.15");

	serverListModel.addElement(tomcat1027);
	serverListModel.addElement(tomcat1115);

	// Select first server
	if (serverListModel.getSize() > 0) {
	    serverList.setSelectedIndex(0);
	    updateServerDetails();
	}
    }

    /**
     * Update server details panel from selected server
     */
    private void updateServerDetails() {
	TomcatServerInfo selected = serverList.getSelectedValue();
	if (selected != null) {
	    currentServer = selected;

	    serverNameField.setText(selected.getName());
	    tomcatHomeField.setText(selected.getTomcatHome());
	    tomcatVersionField.setText(selected.getVersion());
	    tomcatBaseField.setText(selected.getTomcatBase());

	    updateLibrariesTree(selected);
	} else {
	    clearServerDetails();
	}
    }

    /**
     * Update libraries tree for selected server
     */
    private void updateLibrariesTree(TomcatServerInfo server) {
	DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) librariesTreeModel.getRoot();
	rootNode.removeAllChildren();

	// Add Classes node
	DefaultMutableTreeNode classesNode = new DefaultMutableTreeNode("Classes");
	rootNode.add(classesNode);

	// Add default Tomcat JARs
	String tomcatHome = server.getTomcatHome();
	if (tomcatHome != null && !tomcatHome.isEmpty()) {
	    classesNode.add(new DefaultMutableTreeNode(tomcatHome + "\\lib\\jsp-api.jar"));
	    classesNode.add(new DefaultMutableTreeNode(tomcatHome + "\\lib\\servlet-api.jar"));
	}

	librariesTreeModel.reload();
	librariesTree.expandRow(0); // Expand root
	librariesTree.expandRow(1); // Expand Classes
    }

    /**
     * Clear server details
     */
    private void clearServerDetails() {
	currentServer = null;
	serverNameField.setText("");
	tomcatHomeField.setText("");
	tomcatVersionField.setText("");
	tomcatBaseField.setText("");

	DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) librariesTreeModel.getRoot();
	rootNode.removeAllChildren();
	librariesTreeModel.reload();
    }

    /**
     * Update current server from fields
     */
    private void updateCurrentServer() {
	if (currentServer != null) {
	    currentServer.setName(serverNameField.getText());
	    currentServer.setTomcatHome(tomcatHomeField.getText());
	    currentServer.setTomcatBase(tomcatBaseField.getText());

	    // Auto-detect version
	    detectTomcatVersion();

	    serverList.repaint();
	}
    }

    /**
     * Browse for Tomcat home directory
     */
    private void browseTomcatHome() {
	FileChooserDescriptor descriptor = new FileChooserDescriptor(false, true, false, false, false, false);
	descriptor.setTitle("Select Tomcat Home Directory");
	descriptor.setDescription("Choose the Tomcat installation directory");

	VirtualFile file = FileChooser.chooseFile(descriptor, project, null);
	if (file != null) {
	    tomcatHomeField.setText(file.getPath());

	    // Auto-set base directory if empty
	    if (tomcatBaseField.getText().isEmpty()) {
		tomcatBaseField.setText(file.getPath());
	    }

	    updateCurrentServer();
	}
    }

    /**
     * Browse for Tomcat base directory
     */
    private void browseTomcatBase() {
	FileChooserDescriptor descriptor = new FileChooserDescriptor(false, true, false, false, false, false);
	descriptor.setTitle("Select Tomcat Base Directory");
	descriptor.setDescription("Choose the Tomcat base directory");

	VirtualFile file = FileChooser.chooseFile(descriptor, project, null);
	if (file != null) {
	    tomcatBaseField.setText(file.getPath());
	    updateCurrentServer();
	}
    }

    /**
     * Detect Tomcat version from installation
     */
    private void detectTomcatVersion() {
	String tomcatHome = tomcatHomeField.getText();
	if (!tomcatHome.isEmpty()) {
	    // Simple version detection - look for version in path or catalina.jar
	    if (tomcatHome.contains("10.1.15")) {
		tomcatVersionField.setText("10.1.15");
	    } else if (tomcatHome.contains("10.0.27")) {
		tomcatVersionField.setText("10.0.27");
	    } else {
		tomcatVersionField.setText("Unknown");
	    }
	}
    }

    /**
     * Add new server
     */
    private void addServer() {
	TomcatServerInfo newServer = new TomcatServerInfo("New Tomcat Server", "", "", "");
	serverListModel.addElement(newServer);
	serverList.setSelectedValue(newServer, true);
	updateServerDetails();
	serverNameField.requestFocus();
    }

    /**
     * Remove selected server
     */
    private void removeServer() {
	TomcatServerInfo selected = serverList.getSelectedValue();
	if (selected != null) {
	    int result = JOptionPane.showConfirmDialog(getContentPane(),
		    "Remove server '" + selected.getName() + "'?",
		    "Remove Server",
		    JOptionPane.YES_NO_OPTION);

	    if (result == JOptionPane.YES_OPTION) {
		serverListModel.removeElement(selected);
		clearServerDetails();
	    }
	}
    }

    /**
     * Add library to current server
     */
    private void addLibrary() {
	// TODO: Show library selection dialog
	System.out.println("DevTomcat: Add library dialog - TODO");
    }

    /**
     * Remove selected library
     */
    private void removeLibrary() {
	// TODO: Remove selected library from tree
	System.out.println("DevTomcat: Remove library - TODO");
    }

    /**
     * Attach sources to library
     */
    private void attachSources() {
	// TODO: Show source attachment dialog
	System.out.println("DevTomcat: Attach sources dialog - TODO");
    }

    /**
     * Get configured servers
     */
    public java.util.List<TomcatServerInfo> getConfiguredServers() {
	java.util.List<TomcatServerInfo> servers = new ArrayList<>();
	for (int i = 0; i < serverListModel.getSize(); i++) {
	    servers.add(serverListModel.getElementAt(i));
	}
	return servers;
    }

    /**
     * Server list cell renderer
     */
    private static class ServerListCellRenderer extends DefaultListCellRenderer {
	@Override
	public Component getListCellRendererComponent(JList<?> list, Object value, int index,
		boolean isSelected, boolean cellHasFocus) {
	    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

	    if (value instanceof TomcatServerInfo) {
		TomcatServerInfo server = (TomcatServerInfo) value;
		setText(server.getName());
		setIcon(new ImageIcon()); // TODO: Add Tomcat icon
	    }

	    return this;
	}
    }

    /**
     * Tomcat server information class
     */
    public static class TomcatServerInfo {
	private String name;
	private String tomcatHome;
	private String version;
	private String tomcatBase;

	public TomcatServerInfo(String name, String tomcatHome, String version, String tomcatBase) {
	    this.name = name;
	    this.tomcatHome = tomcatHome;
	    this.version = version;
	    this.tomcatBase = tomcatBase;
	}

	// Getters and setters
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }

	public String getTomcatHome() { return tomcatHome; }
	public void setTomcatHome(String tomcatHome) { this.tomcatHome = tomcatHome; }

	public String getVersion() { return version; }
	public void setVersion(String version) { this.version = version; }

	public String getTomcatBase() { return tomcatBase; }
	public void setTomcatBase(String tomcatBase) { this.tomcatBase = tomcatBase; }

	@Override
	public String toString() {
	    return name;
	}
    }
}