package com.poratu.idea.plugins.tomcat.setting;

import com.intellij.execution.ui.CommonJavaParametersPanel;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextComponentAccessor;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.CollectionComboBoxModel;
import com.intellij.ui.ComboboxWithBrowseButton;
import org.jdesktop.swingx.JXButton;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * Author : zengkid
 * Date   : 2017-02-23
 * Time   : 00:13
 */
public class RunnerSetting {
    private JPanel mainPanel;
    private TextFieldWithBrowseButton tomcatField;
    private TextFieldWithBrowseButton docBaseField;
    private JTextField contextPathField;
    private JFormattedTextField portField;
    private JXButton configrationButton;
    private CommonJavaParametersPanel javaParametersPanel;
    private ComboboxWithBrowseButton tomcatCombobox;
    private Project project;

    public RunnerSetting(Project project) {
        this.project = project;
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    public TextFieldWithBrowseButton getTomcatField() {
        return tomcatField;
    }

    public TextFieldWithBrowseButton getDocBaseField() {
        return docBaseField;
    }

    public JTextField getContextPathField() {
        return contextPathField;
    }

    public JFormattedTextField getPortField() {
        return portField;
    }

    public ComboboxWithBrowseButton getTomcatCombobox() {
        return tomcatCombobox;
    }


    public JXButton getConfigrationButton() {
        return configrationButton;
    }

    private void createUIComponents() {
        FileChooserDescriptor fileChooserDescriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor();



        tomcatCombobox = new ComboboxWithBrowseButton();
        JComboBox<String> comboBox = tomcatCombobox.getComboBox();
        CollectionComboBoxModel<String> aModel = new CollectionComboBoxModel<>();
        comboBox.setModel(aModel);

        tomcatCombobox.addBrowseFolderListener("title", "description", project, fileChooserDescriptor, new TextComponentAccessor<JComboBox>() {
            public String getText(JComboBox comboBox) {
                Object item = comboBox.getEditor().getItem();
                return item.toString();
            }

            public void setText(JComboBox comboBox, @NotNull String text) {
                comboBox.getEditor().setItem(text);
                CollectionComboBoxModel model = (CollectionComboBoxModel) comboBox.getModel();
                model.add(model.getSize(), text);
                model.setSelectedItem(text);
            }
        });
    }


}
