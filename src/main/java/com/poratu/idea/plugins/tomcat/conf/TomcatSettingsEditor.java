package com.poratu.idea.plugins.tomcat.conf;

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.ComboboxWithBrowseButton;
import com.intellij.ui.DocumentAdapter;
import com.poratu.idea.plugins.tomcat.setting.RunnerSetting;
import com.poratu.idea.plugins.tomcat.setting.TomcatInfo;
import com.poratu.idea.plugins.tomcat.setting.TomcatInfoConfigs;
import com.poratu.idea.plugins.tomcat.setting.TomcatSettingConfigurable;
import org.jdesktop.swingx.JXButton;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
        String docModuleRoot = tomcatRunConfiguration.getDocModuleRoot();
        if (docModuleRoot != null && !"".equals(docModuleRoot.trim())) {
            runnerSetting.getDocModuleRoot().setText(docModuleRoot);
            runnerSetting.getDocModuleRoot().getTextField().setText(docModuleRoot);
        }
        String contextPath = tomcatRunConfiguration.getContextPath();
        if (contextPath != null && !"".equals(contextPath.trim())) {
            runnerSetting.getContextPathField().setText(contextPath);
        }
        String port = tomcatRunConfiguration.getPort();
        if (port != null && !"".equals(port.trim())) {
            runnerSetting.getPortField().setText(port);
        }
        String ajpPort = tomcatRunConfiguration.getAjpPort();
        if (ajpPort != null && !"".equals(ajpPort.trim())) {
            runnerSetting.getAjpPort().setText(ajpPort);
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

        String className = tomcatRunConfiguration.getClassName();
        if (className != null && !"".equals(className.trim())) {
            runnerSetting.getClassName().setText(className);
        }

        String debug = tomcatRunConfiguration.getDebug();
        if (debug != null && !"".equals(debug.trim())) {
            runnerSetting.getDebug().setText(debug);
        }

        String digest = tomcatRunConfiguration.getDigest();
        if (digest != null && !"".equals(digest.trim())) {
            runnerSetting.getDigest().setText(digest);
        }

        String roleNameCol = tomcatRunConfiguration.getRoleNameCol();
        if (roleNameCol != null && !"".equals(roleNameCol.trim())) {
            runnerSetting.getRoleNameCol().setText(roleNameCol);
        }

        String userCredCol = tomcatRunConfiguration.getUserCredCol();
        if (userCredCol != null && !"".equals(userCredCol.trim())) {
            runnerSetting.getUserCredCol().setText(userCredCol);
        }

        String userNameCol = tomcatRunConfiguration.getUserNameCol();
        if (userNameCol != null && !"".equals(userNameCol.trim())) {
            runnerSetting.getUserNameCol().setText(userNameCol);
        }

        String userRoleTable = tomcatRunConfiguration.getUserRoleTable();
        if (userRoleTable != null && !"".equals(userRoleTable.trim())) {
            runnerSetting.getUserRoleTable().setText(userRoleTable);
        }

        String userTable = tomcatRunConfiguration.getUserTable();
        if (userTable != null && !"".equals(userTable.trim())) {
            runnerSetting.getUserTable().setText(userTable);
        }

        String jndiGlobal = tomcatRunConfiguration.getJndiGlobal();
        if (jndiGlobal != null && !"".equals(jndiGlobal.trim())) {
            runnerSetting.getJndiGlobal().setText(jndiGlobal);
        }

        String jndiName = tomcatRunConfiguration.getJndiName();
        if (jndiName != null && !"".equals(jndiName.trim())) {
            runnerSetting.getJndiName().setText(jndiName);
        }

        String jndiType = tomcatRunConfiguration.getJndiType();
        if (jndiType != null && !"".equals(jndiType.trim())) {
            runnerSetting.getJndiType().setText(jndiType);
        }

        String dataSourceName = tomcatRunConfiguration.getDataSourceName();
        if (dataSourceName != null && !"".equals(dataSourceName.trim())) {
            runnerSetting.getDataSourceName().setText(dataSourceName);
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
        tomcatRunConfiguration.setDocModuleRoot(runnerSetting.getDocModuleRoot().getText());
        tomcatRunConfiguration.setContextPath(runnerSetting.getContextPathField().getText());
        tomcatRunConfiguration.setPort(runnerSetting.getPortField().getText());
        tomcatRunConfiguration.setAjpPort(runnerSetting.getAjpPort().getText());
        tomcatRunConfiguration.setAdminPort(runnerSetting.getAdminPort().getText());
        tomcatRunConfiguration.setVmOptions(runnerSetting.getVmOptons().getText());
        tomcatRunConfiguration.setEnvOptions(runnerSetting.getEnvOptions().getEnvs());
        tomcatRunConfiguration.setPassParentEnvironmentVariables(runnerSetting.getEnvOptions().isPassParentEnvs());
        tomcatRunConfiguration.setClassName(runnerSetting.getClassName().getText());
        tomcatRunConfiguration.setDebug(runnerSetting.getDebug().getText());
        tomcatRunConfiguration.setDigest(runnerSetting.getDigest().getText());
        tomcatRunConfiguration.setRoleNameCol(runnerSetting.getRoleNameCol().getText());
        tomcatRunConfiguration.setUserCredCol(runnerSetting.getUserCredCol().getText());
        tomcatRunConfiguration.setUserNameCol(runnerSetting.getUserNameCol().getText());
        tomcatRunConfiguration.setUserRoleTable(runnerSetting.getUserRoleTable().getText());
        tomcatRunConfiguration.setUserTable(runnerSetting.getUserTable().getText());
        tomcatRunConfiguration.setJndiGlobal(runnerSetting.getJndiGlobal().getText());
        tomcatRunConfiguration.setJndiName(runnerSetting.getJndiName().getText());
        tomcatRunConfiguration.setJndiType(runnerSetting.getJndiType().getText());
        tomcatRunConfiguration.setDataSourceName(runnerSetting.getDataSourceName().getText());

    }

    @NotNull
    @Override
    protected JComponent createEditor() {

        ComboboxWithBrowseButton tomcatField = runnerSetting.getTomcatField();
        TextFieldWithBrowseButton docBaseField = runnerSetting.getDocBaseField();
        TextFieldWithBrowseButton docModuleRoot = runnerSetting.getDocModuleRoot();
        JTextField contextPathField = runnerSetting.getContextPathField();
        JFormattedTextField portField = runnerSetting.getPortField();
        JFormattedTextField ajpPort = runnerSetting.getAjpPort();
        JFormattedTextField adminPort = runnerSetting.getAdminPort();
        JTextField className = runnerSetting.getClassName();
        JTextField debug = runnerSetting.getDebug();
        JTextField digest = runnerSetting.getDigest();
        JTextField roleNameCol = runnerSetting.getRoleNameCol();
        JTextField userCredCol = runnerSetting.getUserCredCol();
        JTextField userNameCol = runnerSetting.getUserNameCol();
        JTextField userRoleTable = runnerSetting.getUserRoleTable();
        JTextField userTable = runnerSetting.getUserTable();
        JTextField jndiGlobal = runnerSetting.getJndiGlobal();
        JTextField jndiName = runnerSetting.getJndiName();
        JTextField jndiType = runnerSetting.getJndiType();
        JTextField dataSourceName = runnerSetting.getDataSourceName();
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
                        String contextPath = module == null ? "/" : "/" + module.getName();
                        contextPathField.setText(contextPath);
                    }
                }

            }
        });

        docModuleRoot.addBrowseFolderListener("Module Root", "Choose Module Root Folder", project, FileChooserDescriptorFactory.createSingleFolderDescriptor().withRoots(project.getBaseDir()));
//        docModuleRoot.getTextField().getDocument().addDocumentListener(new DocumentAdapter() {
//            @Override
//            protected void textChanged(DocumentEvent documentEvent) {
//
//                if (!documentEvent.getType().equals(DocumentEvent.EventType.REMOVE)) {
//                    String text = docBaseField.getText();
//                    if (text != null && !text.trim().equals("")) {
//                        VirtualFile fileByIoFile = LocalFileSystem.getInstance().findFileByIoFile(new File(text));
//                        Module module = ModuleUtilCore.findModuleForFile(fileByIoFile, project);
//                        if(module == null) {
//                            throw new RuntimeException("The Module Root specified is not a module according to Intellij");
//                        }
//                    }
//                }
//
//            }
//        });


        portField.setValue(8080);
        ajpPort.setValue(8009);
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
        ajpPort.setFormatterFactory(tf);
        adminPort.setFormatterFactory(tf);

        return runnerSetting.getMainPanel();
    }

}
