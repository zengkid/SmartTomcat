package com.poratu.idea.plugins.tomcat.conf;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.ui.ComboboxWithBrowseButton;
import com.intellij.ui.DocumentAdapter;
import com.poratu.idea.plugins.tomcat.setting.RunnerSetting;
import com.poratu.idea.plugins.tomcat.setting.TomcatInfo;
import com.poratu.idea.plugins.tomcat.setting.TomcatSettingConfigurable;
import org.jdesktop.swingx.JXButton;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;
import java.io.File;
import java.text.NumberFormat;
import java.util.Map;

/**
 * Author : zengkid
 * Date   : 2/16/2017
 * Time   : 3:15 PM
 */
public class TomcatSettingsEditor extends SettingsEditor<TomcatRunConfiguration> {
    private final Project project;
    private final RunnerSetting runnerSetting;

    public TomcatSettingsEditor(TomcatRunConfiguration tomcatRunConfiguration, Project project) {
        runnerSetting = new RunnerSetting(project);
        this.project = project;
        super.resetFrom(tomcatRunConfiguration);
    }

    @Override
    protected void resetEditorFrom(TomcatRunConfiguration tomcatRunConfiguration) {

        TomcatInfo tomcatInfo = tomcatRunConfiguration.getTomcatInfo();
        if (tomcatInfo != null) {
            runnerSetting.getTomcatField().getComboBox().setSelectedItem(tomcatInfo);
        }

        String docBase = tomcatRunConfiguration.getDocBase();
        if (docBase != null && !"".equals(docBase.trim())) {
            runnerSetting.getDocBaseField().setText(docBase);
            runnerSetting.getDocBaseField().getTextField().setText(docBase);
        }


        String contextPath = tomcatRunConfiguration.getContextPath();
        if (contextPath != null && !"".equals(contextPath.trim())) {
            runnerSetting.getContextPathField().setText(contextPath);
        }
        String port = tomcatRunConfiguration.getPort();
        if (port != null && !"".equals(port.trim())) {
            runnerSetting.getPortField().setText(port);
        }
        String adminPort = tomcatRunConfiguration.getAdminPort();
        if (adminPort != null && !"".equals(adminPort.trim())) {
            runnerSetting.getAdminPort().setText(adminPort);
        }

        String vmOptions = tomcatRunConfiguration.getVmOptions();
        if (vmOptions != null && !"".equals(vmOptions.trim())) {
            runnerSetting.getVmOptons().setText(vmOptions);
        }

        Map<String, String> envOptions = tomcatRunConfiguration.getEnvOptions();
        if (envOptions != null && !envOptions.isEmpty()) {
            runnerSetting.getEnvOptions().setEnvs(envOptions);
        }

        Boolean passParentEnvs = tomcatRunConfiguration.getPassParentEnvironmentVariables();
        if (passParentEnvs != null) {
            runnerSetting.getEnvOptions().setPassParentEnvs(passParentEnvs);
        }

    }

    @Override
    protected void applyEditorTo(TomcatRunConfiguration tomcatRunConfiguration) throws ConfigurationException {
        TomcatInfo selectedItem = (TomcatInfo) runnerSetting.getTomcatField().getComboBox().getSelectedItem();
        if (selectedItem != null) {
            tomcatRunConfiguration.setTomcatInfo(selectedItem);
        }
        tomcatRunConfiguration.setDocBase(runnerSetting.getDocBaseField().getText());
        tomcatRunConfiguration.setContextPath(runnerSetting.getContextPathField().getText());
        tomcatRunConfiguration.setPort(runnerSetting.getPortField().getText());
        tomcatRunConfiguration.setAdminPort(runnerSetting.getAdminPort().getText());
        tomcatRunConfiguration.setVmOptions(runnerSetting.getVmOptons().getText());
        tomcatRunConfiguration.setEnvOptions(runnerSetting.getEnvOptions().getEnvs());
        tomcatRunConfiguration.setPassParentEnvironmentVariables(runnerSetting.getEnvOptions().isPassParentEnvs());


    }

    @NotNull
    @Override
    protected JComponent createEditor() {

        ComboboxWithBrowseButton tomcatField = runnerSetting.getTomcatField();
        TextFieldWithBrowseButton docBaseField = runnerSetting.getDocBaseField();

        JTextField contextPathField = runnerSetting.getContextPathField();
        JFormattedTextField portField = runnerSetting.getPortField();
        JFormattedTextField adminPort = runnerSetting.getAdminPort();

        JXButton configrationButton = runnerSetting.getConfigrationButton();
        configrationButton.addActionListener(e -> ShowSettingsUtil.getInstance().showSettingsDialog(project, TomcatSettingConfigurable.class));


        VirtualFile baseDir = VirtualFileManager.getInstance().getFileSystem("file").findFileByPath(project.getBasePath());

        FileChooserDescriptor chooserDescriptor = new IgnoreOutputFileChooserDescriptor(project).withRoots(baseDir);
        docBaseField.addBrowseFolderListener("webapp", "Choose Web Folder", project, chooserDescriptor);
        docBaseField.getTextField().getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(DocumentEvent documentEvent) {

                if (!documentEvent.getType().equals(DocumentEvent.EventType.REMOVE)) {
                    String text = docBaseField.getText();
                    if (text != null && !text.trim().equals("")) {
                        VirtualFile fileByIoFile = LocalFileSystem.getInstance().findFileByIoFile(new File(text));

                        Module module = ModuleUtilCore.findModuleForFile(fileByIoFile, project);
                        String contextPath = module == null ? "/" : "/" + module.getName();
                        contextPathField.setText(contextPath);
                    }
                }

            }
        });

        portField.setValue(8080);
        adminPort.setValue(8005);
        DefaultFormatterFactory tf = new DefaultFormatterFactory();
        NumberFormat format = NumberFormat.getInstance();
        format.setGroupingUsed(false);
        NumberFormatter formatter = new NumberFormatter(format);
        formatter.setValueClass(Integer.class);
        formatter.setMinimum(0);
        formatter.setMaximum(65535);
        tf.setDefaultFormatter(formatter);
        portField.setFormatterFactory(tf);
        adminPort.setFormatterFactory(tf);

        return runnerSetting.getMainPanel();
    }

    class IgnoreOutputFileChooserDescriptor extends FileChooserDescriptor {
        private Project project;

        public IgnoreOutputFileChooserDescriptor(Project project) {
            super(false, true, false, false, false, false);
            this.project = project;
        }

        @Override
        public boolean isFileVisible(VirtualFile file, boolean showHiddenFiles) {

            ModuleManager moduleManager = ModuleManager.getInstance(project);
            Module[] modules = moduleManager.getModules();

            for (Module module : modules) {
                VirtualFile[] excludeRoots = ModuleRootManager.getInstance(module).getExcludeRoots();
                for (VirtualFile excludeFile : excludeRoots) {
                    if (excludeFile.getCanonicalPath().equals(file.getCanonicalPath())) {
                        return false;
                    }
                }

            }
            return super.isFileVisible(file, showHiddenFiles);
        }
    }

}
