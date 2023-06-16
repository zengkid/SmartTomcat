package com.poratu.idea.plugins.tomcat.conf;

import com.intellij.application.options.ModulesComboBox;
import com.intellij.execution.configuration.EnvironmentVariablesTextFieldWithBrowseButton;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.ui.TextComponentAccessor;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.CollectionComboBoxModel;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.RawCommandLineEditor;
import com.intellij.ui.UIBundle;
import com.intellij.ui.components.fields.ExtendableTextComponent;
import com.intellij.ui.components.fields.ExtendableTextField;
import com.intellij.util.Function;
import com.intellij.util.ui.FormBuilder;
import com.poratu.idea.plugins.tomcat.setting.TomcatInfo;
import com.poratu.idea.plugins.tomcat.setting.TomcatServerManagerState;
import com.poratu.idea.plugins.tomcat.utils.PluginUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.plaf.basic.BasicComboBoxEditor;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class TomcatRunnerSettingsForm implements Disposable {
    private static final Function<String, List<String>> PATH_SEPARATOR_LINE_PARSER = text -> {
        final List<String> result = new ArrayList<>();
        final StringTokenizer tokenizer = new StringTokenizer(text, File.pathSeparator, false);
        while (tokenizer.hasMoreTokens()) {
            result.add(tokenizer.nextToken());
        }
        return result;
    };
    private static final Function<List<String>, String> PATH_SEPARATOR_LINE_JOINER = strings -> StringUtil.join(strings, File.pathSeparator);

    private final Project project;
    private JPanel mainPanel;
    private final JPanel tomcatField = new JPanel(new BorderLayout());
    private final TomcatComboBox tomcatComboBox = new TomcatComboBox();
    private final TextFieldWithBrowseButton docBaseField = new TextFieldWithBrowseButton();
    private final JPanel modulesComboBoxPanel = new JPanel(new GridBagLayout());
    private final ModulesComboBox modulesComboBox = new ModulesComboBox();
    private final JTextField contextPathField = new JTextField();
    private final JPanel portFieldPanel = new JPanel(new GridBagLayout());
    private final JPanel adminPortFieldPanel = new JPanel(new GridBagLayout());
    private final JTextField portField = new JTextField();
    private final JTextField sslPortField = new JTextField();
    private final JTextField adminPort = new JTextField();
    private final RawCommandLineEditor vmOptions = new RawCommandLineEditor();
    private final EnvironmentVariablesTextFieldWithBrowseButton envOptions = new EnvironmentVariablesTextFieldWithBrowseButton();
    private final RawCommandLineEditor extraClassPath = new RawCommandLineEditor(PATH_SEPARATOR_LINE_PARSER, PATH_SEPARATOR_LINE_JOINER);


    TomcatRunnerSettingsForm(Project project) {
        this.project = project;

        createTomcatField();
        createClasspathField();
        createPortField();
        createAdminPortField();

        extraClassPath.getEditorField().getEmptyText().setText("Use '" + File.pathSeparator + "' to separate paths");

        initDeploymentDirectory();
        buildForm();
    }

    private void createTomcatField() {
        JButton configurationButton = new JButton("Configure...");
        configurationButton.addActionListener(e -> PluginUtils.openTomcatConfiguration());
        tomcatField.add(tomcatComboBox, BorderLayout.CENTER);
        tomcatField.add(configurationButton, BorderLayout.EAST);
    }

    private void createClasspathField() {
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.weighty = 1;
        modulesComboBoxPanel.add(modulesComboBox, c);
        modulesComboBox.setModules(PluginUtils.getModules(project));
    }

    private void createPortField() {
        JLabel sslPortLabel = new JLabel("SSL port:");
        sslPortLabel.setHorizontalAlignment(SwingConstants.CENTER);
        sslPortLabel.setLabelFor(sslPortField);

        GridBagConstraints c = new GridBagConstraints();

        // default constraints
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridy = 0;

        c.gridx = 0;
        c.weightx = 1;
        portFieldPanel.add(portField, c);

        c.gridx = 1;
        c.weightx = 0;
        c.ipadx = 10;
        portFieldPanel.add(sslPortLabel, c);

        c.gridx = 2;
        c.weightx = 1;
        portFieldPanel.add(sslPortField, c);
    }

    private void createAdminPortField() {
        GridBagConstraints c = new GridBagConstraints();

        // default constraints
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridy = 0;

        c.gridx = 0;
        c.weightx = 1;
        adminPortFieldPanel.add(adminPort, c);
    }

    private void initDeploymentDirectory() {
        FileChooserDescriptor descriptor = new IgnoreOutputFileChooserDescriptor(project);
        docBaseField.addBrowseFolderListener("Select Deployment Directory", "Please the directory to deploy",
                project, descriptor);
        docBaseField.getTextField().getDocument().addDocumentListener(new DocumentAdapter() {
            // Update module selection when docBase is changed
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                String docBase = docBaseField.getText();
                Module module = PluginUtils.findContaingModule(docBase, project);

                if (module != null) {
                    modulesComboBox.setSelectedModule(module);
                }
            }
        });
    }

    private void buildForm() {
        FormBuilder builder = FormBuilder.createFormBuilder()
                .addLabeledComponent("Tomcat server:", tomcatField)
                .addLabeledComponent("Deployment directory:", docBaseField)
                .addLabeledComponent("Use classpath of module:", modulesComboBoxPanel)
                .addLabeledComponent("Context path:", contextPathField)
                .addLabeledComponent("Server port:", portFieldPanel)
                .addLabeledComponent("Admin port:", adminPortFieldPanel)
                .addLabeledComponent("VM options:", vmOptions)
                .addLabeledComponent("Environment variables:", envOptions)
                .addLabeledComponent("Extra JVM classpath:", extraClassPath)
                .addComponentFillVertically(new JPanel(), 0);

        mainPanel = builder.getPanel();
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    public void resetFrom(TomcatRunConfiguration configuration) {
        tomcatComboBox.setSelectedItem(configuration.getTomcatInfo());
        docBaseField.setText(configuration.getDocBase());
        modulesComboBox.setSelectedModule(configuration.getModule());
        contextPathField.setText(configuration.getContextPath());
        portField.setText(String.valueOf(configuration.getPort()));
        sslPortField.setText(configuration.getSslPort() != null ? String.valueOf(configuration.getSslPort()) : "");
        adminPort.setText(String.valueOf(configuration.getAdminPort()));
        vmOptions.setText(configuration.getVmOptions());
        if (configuration.getEnvOptions() != null) {
            envOptions.setEnvs(configuration.getEnvOptions());
        }
        envOptions.setPassParentEnvs(configuration.isPassParentEnvs());
        extraClassPath.setText(configuration.getExtraClassPath());
    }

    public void applyTo(TomcatRunConfiguration configuration) throws ConfigurationException {
        try {
            configuration.setTomcatInfo((TomcatInfo) tomcatComboBox.getSelectedItem());
            configuration.setDocBase(docBaseField.getText());
            configuration.setModule(modulesComboBox.getSelectedModule());
            configuration.setContextPath(contextPathField.getText());
            configuration.setPort(PluginUtils.parsePort(portField.getText()));
            configuration.setSslPort(StringUtil.isNotEmpty(sslPortField.getText()) ? PluginUtils.parsePort(sslPortField.getText()) : null);
            configuration.setAdminPort(PluginUtils.parsePort(adminPort.getText()));
            configuration.setVmOptions(vmOptions.getText());
            configuration.setEnvOptions(envOptions.getEnvs());
            configuration.setPassParentEnvironmentVariables(envOptions.isPassParentEnvs());
            configuration.setExtraClassPath(extraClassPath.getText());
        } catch (Exception e) {
            throw new ConfigurationException(e.getMessage());
        }
    }

    @Override
    public void dispose() {
        mainPanel = null;
    }

    private static class TomcatComboBox extends JComboBox<TomcatInfo> {

        TomcatComboBox() {
            super();

            List<TomcatInfo> tomcatInfos = TomcatServerManagerState.getInstance().getTomcatInfos();
            ComboBoxModel<TomcatInfo> model = new CollectionComboBoxModel<>(tomcatInfos);
            setModel(model);

            initBrowsableEditor();
        }

        private void initBrowsableEditor() {
            ComboBoxEditor editor = new TomcatComboBoxEditor(this);
            setEditor(editor);
            setEditable(true);
        }

    }

    private static class TomcatComboBoxEditor extends BasicComboBoxEditor {
        private static final TomcatComboBoxTextComponentAccessor TEXT_COMPONENT_ACCESSOR = new TomcatComboBoxTextComponentAccessor();
        private final TomcatComboBox comboBox;
        private boolean fileDialogOpened;

        public TomcatComboBoxEditor(TomcatComboBox comboBox) {
            this.comboBox = comboBox;
        }

        @Override
        protected JTextField createEditorComponent() {
            ExtendableTextField editor = new ExtendableTextField();
            editor.addExtension(createBrowseExtension());
            editor.setBorder(null);
            editor.setEditable(false);
            editor.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getButton() == MouseEvent.BUTTON1 && !fileDialogOpened) {
                        if (comboBox.isPopupVisible()) {
                            comboBox.hidePopup();
                        } else {
                            comboBox.showPopup();
                        }
                    }
                }
            });
            return editor;
        }

        private ExtendableTextComponent.Extension createBrowseExtension() {
            String tooltip = UIBundle.message("component.with.browse.button.browse.button.tooltip.text");
            Runnable browseRunnable = () -> {
                fileDialogOpened = true;
                PluginUtils.chooseTomcat(tomcatInfo -> TEXT_COMPONENT_ACCESSOR.setText(comboBox, tomcatInfo.getPath()));
                SwingUtilities.invokeLater(() -> fileDialogOpened = false);
            };
            return ExtendableTextComponent.Extension.create(AllIcons.General.OpenDisk, AllIcons.General.OpenDiskHover,
                    tooltip, browseRunnable);
        }
    }

    private static class TomcatComboBoxTextComponentAccessor implements TextComponentAccessor<JComboBox<TomcatInfo>> {

        @Override
        public String getText(JComboBox<TomcatInfo> component) {
            return component.getEditor().getItem().toString();
        }

        @Override
        public void setText(JComboBox<TomcatInfo> comboBox, @NotNull String text) {
            TomcatServerManagerState.createTomcatInfo(text).ifPresent(tomcatInfo -> {
                CollectionComboBoxModel<TomcatInfo> model = (CollectionComboBoxModel<TomcatInfo>) comboBox.getModel();
                model.add(tomcatInfo);
                comboBox.setSelectedItem(tomcatInfo);
            });
        }

    }

    private static class IgnoreOutputFileChooserDescriptor extends FileChooserDescriptor {
        private static final FileChooserDescriptor singleFolderDescriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor();
        private final Project project;

        public IgnoreOutputFileChooserDescriptor(Project project) {
            super(singleFolderDescriptor);
            this.project = project;
        }

        @Override
        public boolean isFileVisible(VirtualFile file, boolean showHiddenFiles) {
            Module[] modules = ModuleManager.getInstance(project).getModules();

            for (Module module : modules) {
                VirtualFile[] excludeRoots = ModuleRootManager.getInstance(module).getExcludeRoots();
                for (VirtualFile excludeFile : excludeRoots) {
                    if (excludeFile.equals(file)) {
                        return false;
                    }
                }
            }

            return super.isFileVisible(file, showHiddenFiles);
        }
    }

}

