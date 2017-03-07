package com.poratu.idea.plugins.tomcat.conf;

import com.intellij.facet.FacetManager;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.ComboboxWithBrowseButton;
import com.intellij.ui.DocumentAdapter;
import com.poratu.idea.plugins.tomcat.setting.*;
import org.jdesktop.swingx.JXButton;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.NumberFormat;

/**
 * Author : zengkid
 * Date   : 2/16/2017
 * Time   : 3:15 PM
 */
public class TomcatSettingsEditor extends SettingsEditor<TomcatRunConfiguration> {
    private final Project project;
    private final TomcatRunConfiguration tomcatRunConfiguration;
    private RunnerSetting runnerSetting;

    public TomcatSettingsEditor(TomcatRunConfiguration tomcatRunConfiguration, Project project) {
        runnerSetting = new RunnerSetting(project);
        this.tomcatRunConfiguration = tomcatRunConfiguration;
        this.project = project;
        super.resetFrom(tomcatRunConfiguration);
    }

    @Override
    protected void resetEditorFrom(TomcatRunConfiguration tomcatRunConfiguration) {
//        String tomcatInstallation = tomcatRunConfiguration.getTomcatInstallation();
        TomcatInfo tomcatInfo = tomcatRunConfiguration.getTomcatInfo();
        if (tomcatInfo != null) {
            runnerSetting.getTomcatField().getComboBox().setSelectedItem(tomcatInfo);
        }
//        if (tomcatInstallation == null || tomcatInstallation.trim().equals("")) {
//            tomcatInstallation = tomcatInfo.getPath();
//        }
//        if (tomcatInstallation != null && !"".equals(tomcatInstallation.trim())) {
//            runnerSetting.getTomcatField().getComboBox().setSelectedItem(tomcatInstallation);
//        }
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

    }

    @Override
    protected void applyEditorTo(TomcatRunConfiguration tomcatRunConfiguration) throws ConfigurationException {
        TomcatInfo selectedItem = (TomcatInfo) runnerSetting.getTomcatField().getComboBox().getSelectedItem();
        if (selectedItem != null) {
            TomcatInfoConfigs.getInstance(project).setCurrent(selectedItem);
            tomcatRunConfiguration.setTomcatInfo(selectedItem);
        }
        tomcatRunConfiguration.setDocBase(runnerSetting.getDocBaseField().getText());
        tomcatRunConfiguration.setContextPath(runnerSetting.getContextPathField().getText());
        tomcatRunConfiguration.setPort(runnerSetting.getPortField().getText());
    }

    @NotNull
    @Override
    protected JComponent createEditor() {

        ComboboxWithBrowseButton tomcatField = runnerSetting.getTomcatField();
        TextFieldWithBrowseButton docBaseField = runnerSetting.getDocBaseField();
        JTextField contextPathField = runnerSetting.getContextPathField();
        JFormattedTextField portField = runnerSetting.getPortField();
        JXButton configrationButton = runnerSetting.getConfigrationButton();
        configrationButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {


                ShowSettingsUtil.getInstance().showSettingsDialog(project, TomcatSettingConfigurable.class);

            }
        });


        docBaseField.addBrowseFolderListener("webapp", "Choose Web Folder", project, FileChooserDescriptorFactory.createSingleFolderDescriptor().withRoots(project.getBaseDir()));
        docBaseField.getTextField().getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(DocumentEvent documentEvent) {

                if (!documentEvent.getType().equals(DocumentEvent.EventType.REMOVE)) {
                    String text = docBaseField.getText();
                    if (text != null && !text.trim().equals("")) {
                        VirtualFile fileByIoFile = LocalFileSystem.getInstance().findFileByIoFile(new File(text));
                        Module module = ModuleUtilCore.findModuleForFile(fileByIoFile, project);
                        contextPathField.setText("/" + module.getName());
                    }
                }

            }
        });




        portField.setValue(8080);
        DefaultFormatterFactory tf = new DefaultFormatterFactory();
        NumberFormat format = NumberFormat.getInstance();
        format.setGroupingUsed(false);
        NumberFormatter formatter = new NumberFormatter(format);
        formatter.setValueClass(Integer.class);
        formatter.setMinimum(0);
        formatter.setMaximum(65535);
        tf.setDefaultFormatter(formatter);
        portField.setFormatterFactory(tf);

        return runnerSetting.getMainPanel();
    }

}
